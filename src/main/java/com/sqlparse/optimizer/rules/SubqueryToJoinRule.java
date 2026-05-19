package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubqueryToJoinRule implements OptimizationRule {

    @Override
    public String getName() {
        return "SubqueryToJoin";
    }

    @Override
    public String getDescription() {
        return "Converts eligible subqueries to JOINs for better performance";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString().toUpperCase();
        return sql.contains("FROM (SELECT") && sql.contains("WHERE EXISTS");
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = convertSubqueryToJoin(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String convertSubqueryToJoin(String sql) {
        String result = sql;
        Pattern pattern = Pattern.compile(
            "(?i)(SELECT\\s+[^*]+\\s+FROM\\s+\\w+\\s+WHERE\\s+EXISTS\\s*\\()\\s*SELECT\\s+1\\s+FROM\\s+(\\w+)\\s+WHERE\\s+(\\w+)\\.(\\w+)\\s*=\\s*(\\w+)\\.(\\w+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(result);
        if (matcher.find()) {
            result = result.replace(matcher.group(0), 
                "SELECT " + matcher.group(1).replaceFirst("(?i)WHERE EXISTS\\s*\\(", "") + 
                " INNER JOIN " + matcher.group(2) + 
                " ON " + matcher.group(3) + "." + matcher.group(4) + " = " + matcher.group(5) + "." + matcher.group(6));
        }
        return result;
    }
}
