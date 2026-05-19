package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MergeUnionRule implements OptimizationRule {

    @Override
    public String getName() {
        return "MergeUnion";
    }

    @Override
    public String getDescription() {
        return "Merges consecutive UNION operations with the same structure";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString().toUpperCase();
        return sql.contains("UNION ALL") && countOccurrences(sql, "UNION ALL") > 1;
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = mergeUnions(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
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

    private String mergeUnions(String sql) {
        Pattern unionAllPattern = Pattern.compile(
            "(?i)(SELECT\\s+[^;]+FROM\\s+\\w+)\\s+UNION\\s+ALL\\s+\\1",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = unionAllPattern.matcher(sql);
        return matcher.replaceAll("$1");
    }
}
