package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.DatabaseType;
import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MySqlSpecificOptimizationRule implements OptimizationRule {

    @Override
    public String getName() {
        return "MySQL特定优化(MySqlSpecificOptimization)";
    }

    @Override
    public String getDescription() {
        return "MySQL数据库特定的优化规则";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        return context.getDatabaseType() == DatabaseType.MYSQL;
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = optimizeMySql(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String optimizeMySql(String sql) {
        String result = sql;
        result = optimizeMySqlFunctions(result);
        result = optimizeMySqlIndexes(result);
        return result;
    }

    private String optimizeMySqlFunctions(String sql) {
        String result = sql;
        result = result.replaceAll("(?i)\\bSYSDATE\\b", "NOW()");
        result = result.replaceAll("(?i)\\bSYSTIMESTAMP\\b", "NOW()");
        result = result.replaceAll("(?i)\\bNVL\\s*\\(", "IFNULL(");
        return result;
    }

    private String optimizeMySqlIndexes(String sql) {
        return sql;
    }
}
