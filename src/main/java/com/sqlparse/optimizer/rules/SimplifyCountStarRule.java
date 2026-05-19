package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.List;

public class SimplifyCountStarRule implements OptimizationRule {

    @Override
    public String getName() {
        return "SimplifyCountStar";
    }

    @Override
    public String getDescription() {
        return "Simplifies COUNT expressions to COUNT(*) for better performance";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }

        Select select = (Select) statement;
        if (!(select.getSelectBody() instanceof PlainSelect)) {
            return false;
        }

        String sql = statement.toString();
        return sql.toUpperCase().contains("COUNT(1)") || sql.toUpperCase().contains("COUNT(ID)");
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = sql.replaceAll("(?i)COUNT\\(1\\)", "COUNT(*)");
        optimizedSql = optimizedSql.replaceAll("(?i)COUNT\\(id\\)", "COUNT(*)");
        try {
            return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
        } catch (Exception e) {
            return statement;
        }
    }
}
