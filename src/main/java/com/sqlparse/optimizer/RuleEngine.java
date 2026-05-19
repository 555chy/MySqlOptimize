package com.sqlparse.optimizer;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

import java.util.ArrayList;
import java.util.List;

public class RuleEngine {
    private List<OptimizationRule> rules;
    private OptimizationContext context;
    private OptimizationLog log;

    public RuleEngine() {
        this.rules = new ArrayList<>();
        this.context = new OptimizationContext();
        this.log = OptimizationLog.getInstance();
    }

    public RuleEngine(OptimizationContext context) {
        this.rules = new ArrayList<>();
        this.context = context;
        this.log = OptimizationLog.getInstance();
    }

    public void registerRule(OptimizationRule rule) {
        rules.add(rule);
        log.log(rule.getName(), "RuleEngine", "Registered");
    }

    public void registerRules(List<OptimizationRule> rules) {
        for (OptimizationRule rule : rules) {
            registerRule(rule);
        }
    }

    public void unregisterRule(String ruleName) {
        rules.removeIf(rule -> rule.getName().equals(ruleName));
        log.log(ruleName, "RuleEngine", "Unregistered");
    }

    public List<OptimizationRule> getRules() {
        return new ArrayList<>(rules);
    }

    public OptimizationResult optimize(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        return optimize(statement, sql);
    }

    public OptimizationResult optimize(Statement statement, String originalSql) {
        OptimizationResult result = new OptimizationResult(statement, originalSql);
        log.clear();
        log.log("Starting optimization process");

        Statement current = statement;
        for (OptimizationRule rule : rules) {
            if (rule.canApply(current, context)) {
                log.log(rule.getName(), "Query", "Applying rule");
                Statement optimized = rule.apply(current, context);
                RuleApplication application = new RuleApplication(
                        rule.getName(),
                        rule.getDescription(),
                        "Query",
                        true
                );
                result.addAppliedRule(application);
                current = optimized;
                log.log(rule.getName(), "Query", "Rule applied successfully");
            } else {
                RuleApplication application = new RuleApplication(
                        rule.getName(),
                        rule.getDescription(),
                        "Query",
                        false
                );
                result.addAppliedRule(application);
            }
        }

        result.setOptimizedStatement(current);
        result.setOptimizedSql(current.toString());
        log.log("Optimization completed. " + result.getAppliedRuleCount() + " rules applied.");

        return result;
    }

    public OptimizationContext getContext() {
        return context;
    }

    public void setContext(OptimizationContext context) {
        this.context = context;
    }
}
