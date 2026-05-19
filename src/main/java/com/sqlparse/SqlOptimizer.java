package com.sqlparse;

import com.sqlparse.metadata.DatabaseMetadata;
import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationResult;
import com.sqlparse.optimizer.RuleEngine;
import com.sqlparse.optimizer.rules.*;
import net.sf.jsqlparser.JSQLParserException;

import java.util.Arrays;

public class SqlOptimizer {
    private RuleEngine ruleEngine;
    private OptimizationContext context;

    public SqlOptimizer() {
        this.context = new OptimizationContext();
        initializeRuleEngine();
    }

    public SqlOptimizer(DatabaseMetadata metadata) {
        this.context = new OptimizationContext(metadata);
        initializeRuleEngine();
    }

    private void initializeRuleEngine() {
        this.ruleEngine = new RuleEngine(context);
        
        ruleEngine.registerRules(Arrays.asList(
            new SimplifyCountStarRule(),
            new RemoveRedundantDistinctRule(),
            new RemoveUnnecessaryOrderByRule(),
            new RemoveRedundantLimitRule(),
            new SimplifyWithClauseRule(),
            new MergeNestedSubqueryRule(),
            new UseExistsInsteadOfInRule(),
            new OptimizeJoinOrderRule(),
            new PredicatePushdownRule(),
            new RemoveAlwaysTrueOrFalseRule(),
            new ConstantFoldingRule(),
            new MergeUnionRule(),
            new RemoveRedundantJoinsRule(),
            new ColumnPruningRule(),
            new SubqueryToJoinRule(),
            new OrConditionRewriteRule(),
            new CaseSimplificationRule(),
            new NotInToNotExistsRule(),
            new NullCheckEliminationRule(),
            new ArithmeticComparisonOptimizeRule(),
            new LikePatternOptimizeRule()
        ));
    }

    public OptimizationResult optimize(String sql) throws JSQLParserException {
        return ruleEngine.optimize(sql);
    }

    public void setMetadata(DatabaseMetadata metadata) {
        this.context.setDatabaseMetadata(metadata);
    }

    public RuleEngine getRuleEngine() {
        return ruleEngine;
    }
}
