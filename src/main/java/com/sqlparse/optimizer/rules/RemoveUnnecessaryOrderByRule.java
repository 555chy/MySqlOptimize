package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

import java.util.List;

public class RemoveUnnecessaryOrderByRule implements OptimizationRule {

    @Override
    public String getName() {
        return "移除不必要ORDER BY(RemoveUnnecessaryOrderBy)";
    }

    @Override
    public String getDescription() {
        return "从SELECT COUNT(*)查询中移除不必要的ORDER BY子句";
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

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        List<OrderByElement> orderByElements = plainSelect.getOrderByElements();

        if (orderByElements == null || orderByElements.isEmpty()) {
            return false;
        }

        List<SelectItem> selectItems = plainSelect.getSelectItems();
        if (selectItems.size() != 1) {
            return false;
        }

        SelectItem selectItem = selectItems.get(0);
        return isCountFunction(selectItem);
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        Select select = (Select) statement;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        plainSelect.setOrderByElements(null);
        return select;
    }

    private boolean isCountFunction(SelectItem selectItem) {
        if (selectItem instanceof SelectExpressionItem) {
            SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
            return selectExpressionItem.getExpression() instanceof Function;
        }
        String expr = selectItem.toString();
        return expr.toUpperCase().contains("COUNT(");
    }
}
