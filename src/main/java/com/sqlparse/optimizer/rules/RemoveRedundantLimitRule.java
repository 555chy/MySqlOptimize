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
        // 禁用此规则，因为表数据行数是动态变化的，无法安全地判断LIMIT是否冗余
        return false;
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
