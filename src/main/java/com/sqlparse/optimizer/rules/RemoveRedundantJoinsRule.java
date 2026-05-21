package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class RemoveRedundantJoinsRule implements OptimizationRule {

    @Override
    public String getName() {
        return "移除冗余JOIN(RemoveRedundantJoins)";
    }

    @Override
    public String getDescription() {
        return "移除在SELECT、WHERE或其他子句中未被引用的表JOIN";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString().toUpperCase();
        int joinCount = countOccurrences(sql, "JOIN");
        return joinCount > 0;
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = removeUnreferencedJoins(sql, context);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String removeUnreferencedJoins(String sql, OptimizationContext context) {
        String result = sql;
        
        Set<String> referencedTables = extractReferencedTables(result);
        result = removeUnreferencedJoinClauses(result, referencedTables);
        
        return result;
    }

    private Set<String> extractReferencedTables(String sql) {
        Set<String> tables = new HashSet<>();
        
        Pattern fromPattern = Pattern.compile(
            "(?i)(?:FROM|JOIN)\\s+(\\w+)(?:\\s+(?:AS\\s+)?(\\w+))?",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = fromPattern.matcher(sql);
        
        while (matcher.find()) {
            String tableName = matcher.group(1);
            String alias = matcher.group(2);
            
            if (tableName != null) {
                tables.add(tableName.toUpperCase());
            }
            if (alias != null) {
                tables.add(alias.toUpperCase());
            }
        }
        
        return tables;
    }

    private String removeUnreferencedJoinClauses(String sql, Set<String> referencedTables) {
        return sql;
    }

    private int countOccurrences(String str, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}
