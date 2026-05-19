package com.sqlparse.optimizer;

import net.sf.jsqlparser.statement.Statement;

public interface OptimizationRule {

    String getName();

    String getDescription();

    boolean canApply(Statement statement, OptimizationContext context);

    Statement apply(Statement statement, OptimizationContext context);
}
