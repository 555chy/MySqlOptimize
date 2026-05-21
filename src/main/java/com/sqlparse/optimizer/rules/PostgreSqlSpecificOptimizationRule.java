package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.DatabaseType;
import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostgreSqlSpecificOptimizationRule implements OptimizationRule {

    @Override
    public String getName() {
        return "PostgreSQL特定优化(PostgreSqlSpecificOptimization)";
    }

    @Override
    public String getDescription() {
        return "PostgreSQL数据库特定的优化规则";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        return context.getDatabaseType() == DatabaseType.POSTGRESQL;
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = optimizePostgreSql(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String optimizePostgreSql(String sql) {
        String result = sql;
        result = optimizePostgreSqlFunctions(result);
        result = optimizePostgreSqlTypes(result);
        return result;
    }

    private String optimizePostgreSqlFunctions(String sql) {
        String result = sql;
        result = result.replaceAll("(?i)\\bSYSDATE\\b", "CURRENT_DATE");
        result = result.replaceAll("(?i)\\bSYSTIMESTAMP\\b", "CURRENT_TIMESTAMP");
        result = result.replaceAll("(?i)\\bIFNULL\\s*\\(", "COALESCE(");
        return result;
    }

    private String optimizePostgreSqlTypes(String sql) {
        return sql;
    }
}
