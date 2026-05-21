package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubqueryOrderByRemovalRule implements OptimizationRule {

    @Override
    public String getName() {
        return "移除子查询ORDER BY(SubqueryOrderByRemoval)";
    }

    @Override
    public String getDescription() {
        return "移除子查询中的不必要ORDER BY子句（当子查询被聚合函数使用时）";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString().toUpperCase();
        return (sql.contains("COUNT(") || sql.contains("SUM(") ||
                sql.contains("AVG(") || sql.contains("MIN(") ||
                sql.contains("MAX(")) &&
               sql.contains("FROM (SELECT");
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = removeSubqueryOrderBy(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String removeSubqueryOrderBy(String sql) {
        String result = sql;

        Pattern pattern = Pattern.compile(
            "(?i)(COUNT|SUM|AVG|MIN|MAX)\\s*\\(\\s*\\*\\s*\\)\\s*FROM\\s*\\(\\s*SELECT[^)]+ORDER\\s+BY[^)]+\\)",
            Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(result);

        while (matcher.find()) {
            String match = matcher.group();
            int selectEnd = match.indexOf("ORDER BY");
            if (selectEnd > 0) {
                String beforeOrderBy = match.substring(0, selectEnd).trim();
                if (beforeOrderBy.endsWith(")")) {
                    beforeOrderBy = beforeOrderBy.substring(0, beforeOrderBy.length() - 1);
                }
                String replacement = match.replace(match, beforeOrderBy + ")");
                result = result.replace(match, replacement);
            }
        }

        return result;
    }
}