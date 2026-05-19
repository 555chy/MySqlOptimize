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
        return "MergeNestedSubquery";
    }

    @Override
    public String getDescription() {
        return "Merges simple nested subqueries into the main query";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        return statement.toString().toUpperCase().contains("FROM (SELECT");
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
        Pattern pattern = Pattern.compile("(?i)FROM\\s*\\((SELECT[^)]+)\\)\\s*(\\w+)");
        Matcher matcher = pattern.matcher(result);
        
        if (matcher.find()) {
            String subquery = matcher.group(1);
            String alias = matcher.group(2);
            result = matcher.replaceFirst("FROM " + extractFromClause(subquery) + " " + alias);
        }
        
        return result;
    }

    private String extractFromClause(String subquery) {
        Pattern fromPattern = Pattern.compile("(?i)FROM\\s+([^\\s;]+)");
        Matcher matcher = fromPattern.matcher(subquery);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return subquery;
    }
}
