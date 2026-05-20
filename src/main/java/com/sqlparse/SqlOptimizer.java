package com.sqlparse;

import com.sqlparse.metadata.DatabaseMetadata;
import com.sqlparse.optimizer.DatabaseFunctionMapper;
import com.sqlparse.optimizer.DatabaseType;
import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationResult;
import com.sqlparse.optimizer.RuleEngine;
import com.sqlparse.optimizer.rules.*;
import net.sf.jsqlparser.JSQLParserException;

import java.util.Arrays;

public class SqlOptimizer {
    private RuleEngine ruleEngine;
    private OptimizationContext context;
    private DatabaseType databaseType;

    public SqlOptimizer() {
        this.databaseType = DatabaseType.MYSQL;
        this.context = new OptimizationContext(this.databaseType);
        initializeRuleEngine();
    }

    public SqlOptimizer(DatabaseMetadata metadata) {
        this.databaseType = DatabaseType.MYSQL;
        this.context = new OptimizationContext(metadata, this.databaseType);
        initializeRuleEngine();
    }

    public SqlOptimizer(DatabaseType databaseType) {
        this.databaseType = databaseType;
        this.context = new OptimizationContext(this.databaseType);
        initializeRuleEngine();
    }

    public SqlOptimizer(DatabaseMetadata metadata, DatabaseType databaseType) {
        this.databaseType = databaseType;
        this.context = new OptimizationContext(metadata, this.databaseType);
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
        String normalizedSql = DatabaseFunctionMapper.mapFunctions(sql, databaseType);
        OptimizationResult result = ruleEngine.optimize(normalizedSql);
        
        String finalSql = DatabaseFunctionMapper.mapFunctions(result.getOptimizedSql(), databaseType);
        result.setOptimizedSql(finalSql);
        
        return result;
    }

    public void setMetadata(DatabaseMetadata metadata) {
        this.context.setDatabaseMetadata(metadata);
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
        this.context.setDatabaseType(databaseType);
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public RuleEngine getRuleEngine() {
        return ruleEngine;
    }
}
