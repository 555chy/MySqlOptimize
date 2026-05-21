package com.sqlparse.optimizer.rules;

import com.sqlparse.metadata.DatabaseMetadata;
import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RemoveRedundantLimitRule implements OptimizationRule {

    @Override
    public String getName() {
        return "移除冗余LIMIT(RemoveRedundantLimit)";
    }

    @Override
    public String getDescription() {
        return "移除大于实际结果集的冗余LIMIT子句";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        
        Select select = (Select) statement;
        if (!(select.getSelectBody() instanceof PlainSelect)) {
            return false;
        }
        
        String sql = statement.toString().toUpperCase();
        return sql.contains("LIMIT");
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = removeRedundantLimits(sql, context);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String removeRedundantLimits(String sql, OptimizationContext context) {
        return sql;
    }
}
