package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotInToNotExistsRule implements OptimizationRule {

    @Override
    public String getName() {
        return "NotInToNotExists";
    }

    @Override
    public String getDescription() {
        return "Converts NOT IN subqueries to NOT EXISTS for better performance";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        return statement.toString().toUpperCase().contains("NOT IN (SELECT");
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = convertNotInToNotExists(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String convertNotInToNotExists(String sql) {
        String result = sql;
        Pattern pattern = Pattern.compile(
            "(?i)(\\w+)\\s+NOT\\s+IN\\s*\\((SELECT\\s+)(\\w+)\\s+FROM\\s+(\\w+)(.*?)\\)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(result);
        
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String leftColumn = matcher.group(1);
            String selectPrefix = matcher.group(2);
            String rightColumn = matcher.group(3);
            String tableName = matcher.group(4);
            String restOfQuery = matcher.group(5);
            
            String replacement = String.format(
                "NOT EXISTS (%s1 FROM %s%s WHERE %s.%s = %s)",
                selectPrefix, tableName, restOfQuery, tableName, rightColumn, leftColumn
            );
            
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }
}
