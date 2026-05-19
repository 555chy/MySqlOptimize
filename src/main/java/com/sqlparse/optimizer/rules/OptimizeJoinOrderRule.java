package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

public class OptimizeJoinOrderRule implements OptimizationRule {

    @Override
    public String getName() {
        return "OptimizeJoinOrder";
    }

    @Override
    public String getDescription() {
        return "Optimizes JOIN order based on table size information when available";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString().toUpperCase();
        int joinCount = (sql.length() - sql.replace("JOIN", "").length()) / "JOIN".length();
        return joinCount > 1;
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        return statement;
    }
}
