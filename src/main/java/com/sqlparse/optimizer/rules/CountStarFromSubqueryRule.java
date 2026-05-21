package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CountStarFromSubqueryRule implements OptimizationRule {

    @Override
    public String getName() {
        return "COUNT(*)子查询优化(CountStarFromSubquery)";
    }

    @Override
    public String getDescription() {
        return "优化 COUNT(*)/SUM(*)/AVG(*)/MIN(*)/MAX(*) FROM 子查询，移除不必要的子查询";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString().toUpperCase();
        return (sql.contains("COUNT(*)") || sql.contains("SUM(*)") ||
                sql.contains("AVG(*)") || sql.contains("MIN(*)") ||
                sql.contains("MAX(*)") || sql.contains("COUNT(1)")) &&
               sql.contains("FROM (SELECT");
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = optimizeCountStarFromSubquery(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String optimizeCountStarFromSubquery(String sql) {
        String result = sql;

        Pattern pattern = Pattern.compile(
            "(?i)(COUNT|SUM|AVG|MIN|MAX)\\s*\\(\\s*\\*\\s*\\)\\s*FROM\\s*\\(\\s*SELECT\\s+\\*\\s+FROM\\s+([\\w.]+)([^)]*)\\)(\\s+\\w+)?",
            Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(result);

        while (matcher.find()) {
            String aggregateFunc = matcher.group(1).toUpperCase();
            String tableName = matcher.group(2);
            String subqueryExtra = matcher.group(3) != null ? matcher.group(3) : "";
            String alias = matcher.group(4) != null ? matcher.group(4).trim() : "";

            String whereClause = "";
            String orderByClause = "";

            if (subqueryExtra.contains("WHERE")) {
                int whereIndex = subqueryExtra.indexOf("WHERE");
                int orderIndex = subqueryExtra.indexOf("ORDER BY");

                if (orderIndex > whereIndex && orderIndex > 0) {
                    whereClause = subqueryExtra.substring(whereIndex, orderIndex).trim();
                    orderByClause = subqueryExtra.substring(orderIndex).trim();
                } else if (orderIndex < 0) {
                    whereClause = subqueryExtra.substring(whereIndex).trim();
                }
            } else if (subqueryExtra.contains("ORDER BY")) {
                int orderIndex = subqueryExtra.indexOf("ORDER BY");
                orderByClause = subqueryExtra.substring(orderIndex).trim();
            }

            String optimized = aggregateFunc + "(*) FROM " + tableName;
            if (!whereClause.isEmpty()) {
                optimized += " " + whereClause;
            }

            result = matcher.replaceFirst(Matcher.quoteReplacement(optimized));
            matcher = pattern.matcher(result);
        }

        pattern = Pattern.compile(
            "(?i)(COUNT|SUM|AVG|MIN|MAX)\\s*\\(\\s*1\\s*\\)\\s*FROM\\s*\\(\\s*SELECT\\s+\\*\\s+FROM\\s+([\\w.]+)([^)]*)\\)(\\s+\\w+)?",
            Pattern.DOTALL
        );
        matcher = pattern.matcher(result);

        while (matcher.find()) {
            String aggregateFunc = matcher.group(1).toUpperCase();
            String tableName = matcher.group(2);
            String subqueryExtra = matcher.group(3) != null ? matcher.group(3) : "";
            String alias = matcher.group(4) != null ? matcher.group(4).trim() : "";

            String whereClause = "";
            String orderByClause = "";

            if (subqueryExtra.contains("WHERE")) {
                int whereIndex = subqueryExtra.indexOf("WHERE");
                int orderIndex = subqueryExtra.indexOf("ORDER BY");

                if (orderIndex > whereIndex && orderIndex > 0) {
                    whereClause = subqueryExtra.substring(whereIndex, orderIndex).trim();
                    orderByClause = subqueryExtra.substring(orderIndex).trim();
                } else if (orderIndex < 0) {
                    whereClause = subqueryExtra.substring(whereIndex).trim();
                }
            } else if (subqueryExtra.contains("ORDER BY")) {
                int orderIndex = subqueryExtra.indexOf("ORDER BY");
                orderByClause = subqueryExtra.substring(orderIndex).trim();
            }

            String optimized = aggregateFunc + "(*) FROM " + tableName;
            if (!whereClause.isEmpty()) {
                optimized += " " + whereClause;
            }

            result = matcher.replaceFirst(Matcher.quoteReplacement(optimized));
            matcher = pattern.matcher(result);
        }

        return result;
    }
}