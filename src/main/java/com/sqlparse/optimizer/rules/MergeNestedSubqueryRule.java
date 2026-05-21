package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MergeNestedSubqueryRule implements OptimizationRule {

    @Override
    public String getName() {
        return "合并嵌套子查询(MergeNestedSubquery)";
    }

    @Override
    public String getDescription() {
        return "将简单的嵌套子查询合并到主查询中";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString().toUpperCase();
        return sql.contains("FROM (SELECT");
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = mergeNestedSubqueries(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String mergeNestedSubqueries(String sql) {
        String result = sql;
        int maxIterations = 10;
        int iterations = 0;

        while (iterations < maxIterations) {
            String beforeMatch = result;
            result = mergeOneSubquery(result);
            if (result.equals(beforeMatch)) {
                break;
            }
            iterations++;
        }

        return result;
    }

    private String mergeOneSubquery(String sql) {
        Pattern pattern = Pattern.compile("(?i)FROM\\s*\\(\\s*SELECT\\s+");
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            int selectStart = matcher.start();
            int openParenPos = matcher.end() - 1;

            String matchedSubquery = extractMatchingParentheses(sql, openParenPos);
            if (matchedSubquery != null && matchedSubquery.length() > 2) {
                String innerContent = matchedSubquery.substring(1, matchedSubquery.length() - 1).trim();

                int aliasStart = openParenPos + matchedSubquery.length();
                String alias = "";
                if (aliasStart < sql.length()) {
                    String afterSubquery = sql.substring(aliasStart).trim();
                    Pattern aliasPattern = Pattern.compile("^(\\w+)");
                    Matcher aliasMatcher = aliasPattern.matcher(afterSubquery);
                    if (aliasMatcher.find()) {
                        alias = aliasMatcher.group(1);
                    }
                }

                String tableName = extractTableName(innerContent);
                if (tableName != null && !tableName.isEmpty()) {
                    String beforeFrom = sql.substring(0, selectStart);
                    String replacement = "FROM " + tableName;
                    if (!alias.isEmpty()) {
                        replacement += " " + alias;
                    }

                    String afterSubqueryFull = sql.substring(selectStart);
                    int endOfThisSubquery = selectStart + matchedSubquery.length() + alias.length();
                    if (endOfThisSubquery < sql.length() && alias.length() > 0) {
                        afterSubqueryFull = sql.substring(endOfThisSubquery);
                    } else if (endOfThisSubquery < sql.length()) {
                        afterSubqueryFull = sql.substring(endOfThisSubquery);
                    } else {
                        afterSubqueryFull = "";
                    }

                    return beforeFrom + replacement + afterSubqueryFull;
                }
            }
        }

        return sql;
    }

    private String extractMatchingParentheses(String sql, int startPos) {
        if (startPos < 0 || startPos >= sql.length() || sql.charAt(startPos) != '(') {
            return null;
        }

        int depth = 1;
        int i = startPos + 1;

        while (i < sql.length() && depth > 0) {
            char c = sql.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth == 0) {
                    return sql.substring(startPos, i + 1);
                }
            }
            i++;
        }

        return null;
    }

    private String extractTableName(String subqueryContent) {
        String upperContent = subqueryContent.toUpperCase();

        Pattern pattern = Pattern.compile("(?i)\\bFROM\\s+([\\w.]+)");
        Matcher matcher = pattern.matcher(subqueryContent);

        if (matcher.find()) {
            String tableName = matcher.group(1).trim();

            if (upperContent.contains("JOIN ") || upperContent.contains(" UNION ") ||
                upperContent.contains("GROUP BY") || upperContent.contains("HAVING")) {
                return null;
            }

            return tableName;
        }

        return null;
    }
}
