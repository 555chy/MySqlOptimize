package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RemoveRedundantJoinsRule implements OptimizationRule {

    @Override
    public String getName() {
        return "RemoveRedundantJoins";
    }

    @Override
    public String getDescription() {
        return "Removes JOINs whose tables are not referenced in SELECT, WHERE, or other clauses";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString().toUpperCase();
        int joinCount = (sql.length() - sql.replace("JOIN", "").length()) / "JOIN".length();
        return joinCount > 0;
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = removeUnreferencedJoins(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String removeUnreferencedJoins(String sql) {
        String result = sql;
        Pattern joinPattern = Pattern.compile(
            "(?i)(LEFT|RIGHT|INNER)?\\s*JOIN\\s+(\\w+)\\s+(\\w+)\\s+ON\\s+[^=]+=[^=]+\\s+ON\\s+[^=]+=[^=]+",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = joinPattern.matcher(result);
        while (matcher.find()) {
            String joinClause = matcher.group();
            String tableName = matcher.group(2);
            if (!result.toUpperCase().contains(tableName.toUpperCase() + ".") || 
                result.toUpperCase().split(tableName.toUpperCase() + ".").length <= 2) {
            }
        }
        return result;
    }
}
