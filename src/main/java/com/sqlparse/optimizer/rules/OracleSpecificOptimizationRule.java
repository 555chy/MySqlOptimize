package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.DatabaseType;
import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OracleSpecificOptimizationRule implements OptimizationRule {

    @Override
    public String getName() {
        return "Oracle特定优化(OracleSpecificOptimization)";
    }

    @Override
    public String getDescription() {
        return "Oracle数据库特定的优化规则";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        return context.getDatabaseType() == DatabaseType.ORACLE;
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = optimizeOracleSql(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String optimizeOracleSql(String sql) {
        String result = sql;
        result = optimizeOracleDateFunctions(result);
        result = optimizeOracleHints(result);
        result = optimizeOracleRowNum(result);
        return result;
    }

    private String optimizeOracleDateFunctions(String sql) {
        String result = sql;
        result = result.replaceAll("(?i)\\bNOW\\s*\\(\\s*\\)", "SYSDATE");
        result = result.replaceAll("(?i)\\bCURRENT_DATE\\b", "SYSDATE");
        result = result.replaceAll("(?i)\\bCURRENT_TIMESTAMP\\b", "SYSTIMESTAMP");
        return result;
    }

    private String optimizeOracleHints(String sql) {
        return sql;
    }

    private String optimizeOracleRowNum(String sql) {
        return sql;
    }
}
