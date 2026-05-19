package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PredicatePushdownRule implements OptimizationRule {

    @Override
    public String getName() {
        return "PredicatePushdown";
    }

    @Override
    public String getDescription() {
        return "Pushes WHERE conditions down to the deepest possible level in nested queries";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString();
        return sql.toUpperCase().contains("WHERE") && sql.toUpperCase().contains("SELECT");
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = pushdownPredicates(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String pushdownPredicates(String sql) {
        String result = sql;
        
        Pattern nestedPattern = Pattern.compile(
            "(?i)(FROM\\s+\\w+\\s+WHERE\\s+[^)]+)\\s*\\)\\s*(\\w+)\\s+WHERE\\s+([^)]+)$",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = nestedPattern.matcher(result);
        
        if (matcher.find()) {
            String innerWhere = matcher.group(1);
            String outerWhere = matcher.group(3);
            result = result.replace(matcher.group(0), 
                innerWhere + " AND " + outerWhere + ") " + matcher.group(2));
        }
        
        return result;
    }
}
