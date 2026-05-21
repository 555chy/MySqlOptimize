package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.DatabaseType;
import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqliteSpecificOptimizationRule implements OptimizationRule {

    @Override
    public String getName() {
        return "SQLite特定优化(SqliteSpecificOptimization)";
    }

    @Override
    public String getDescription() {
        return "SQLite数据库特定的优化规则";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        // 暂时禁用，避免不必要的转换
        return false;
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = optimizeSqliteSql(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String optimizeSqliteSql(String sql) {
        String result = sql;
        result = optimizeSqliteDateFunctions(result);
        result = optimizeSqliteNullHandling(result);
        result = optimizeSqliteFunctions(result);
        return result;
    }

    private String optimizeSqliteDateFunctions(String sql) {
        String result = sql;
        result = result.replaceAll("(?i)\\bNOW\\s*\\(\\s*\\)", "DATETIME('now')");
        result = result.replaceAll("(?i)\\bCURRENT_TIMESTAMP\\b", "DATETIME('now')");
        result = result.replaceAll("(?i)\\bCURRENT_DATE\\b", "DATE('now')");
        return result;
    }

    private String optimizeSqliteNullHandling(String sql) {
        String result = sql;
        Pattern pattern = Pattern.compile("(?i)IS\\s+NOT\\s+NULL", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "NOT NULL");
        }
        matcher.appendTail(sb);
        result = sb.toString();
        return result;
    }

    private String optimizeSqliteFunctions(String sql) {
        String result = sql;
        result = result.replaceAll("(?i)\\bIFNULL\\s*\\(", "IFNULL(");
        result = result.replaceAll("(?i)\\bNVL\\s*\\(", "IFNULL(");
        return result;
    }
}
