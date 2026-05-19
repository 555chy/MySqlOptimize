package com.sqlparse.optimizer;

import net.sf.jsqlparser.statement.Statement;

import java.util.ArrayList;
import java.util.List;

public class OptimizationResult {
    private Statement originalStatement;
    private Statement optimizedStatement;
    private List<RuleApplication> appliedRules;
    private String originalSql;
    private String optimizedSql;

    public OptimizationResult(Statement originalStatement, String originalSql) {
        this.originalStatement = originalStatement;
        this.originalSql = originalSql;
        this.optimizedStatement = originalStatement;
        this.optimizedSql = originalSql;
        this.appliedRules = new ArrayList<>();
    }

    public Statement getOriginalStatement() {
        return originalStatement;
    }

    public void setOriginalStatement(Statement originalStatement) {
        this.originalStatement = originalStatement;
    }

    public Statement getOptimizedStatement() {
        return optimizedStatement;
    }

    public void setOptimizedStatement(Statement optimizedStatement) {
        this.optimizedStatement = optimizedStatement;
    }

    public List<RuleApplication> getAppliedRules() {
        return appliedRules;
    }

    public void addAppliedRule(RuleApplication application) {
        this.appliedRules.add(application);
    }

    public String getOriginalSql() {
        return originalSql;
    }

    public void setOriginalSql(String originalSql) {
        this.originalSql = originalSql;
    }

    public String getOptimizedSql() {
        return optimizedSql;
    }

    public void setOptimizedSql(String optimizedSql) {
        this.optimizedSql = optimizedSql;
    }

    public boolean isOptimized() {
        return !appliedRules.isEmpty();
    }

    public int getAppliedRuleCount() {
        return (int) appliedRules.stream().filter(RuleApplication::isApplied).count();
    }
}
