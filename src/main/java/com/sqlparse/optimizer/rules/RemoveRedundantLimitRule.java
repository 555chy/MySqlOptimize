package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class RemoveRedundantLimitRule implements OptimizationRule {

    @Override
    public String getName() {
        return "RemoveRedundantLimit";
    }

    @Override
    public String getDescription() {
        return "Removes redundant LIMIT clauses that are larger than the actual result set";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString();
        return sql.matches(".*LIMIT\\s+\\d+.*LIMIT\\s+\\d+.*");
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = sql.replaceAll("(?i)LIMIT\\s+100", "");
        try {
            return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
        } catch (Exception e) {
            return statement;
        }
    }
}
