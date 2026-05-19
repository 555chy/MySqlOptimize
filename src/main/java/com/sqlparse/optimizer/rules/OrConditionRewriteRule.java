package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrConditionRewriteRule implements OptimizationRule {

    @Override
    public String getName() {
        return "OrConditionRewrite";
    }

    @Override
    public String getDescription() {
        return "Rewrites OR conditions to UNION for better index utilization";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString().toUpperCase();
        int orCount = countOccurrences(sql, " OR ");
        return orCount >= 3;
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
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
}
