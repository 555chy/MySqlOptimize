package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;

import java.util.ArrayList;
import java.util.List;

public class ColumnPruningRule implements OptimizationRule {

    @Override
    public String getName() {
        return "ColumnPruning";
    }

    @Override
    public String getDescription() {
        return "Removes unneeded columns from SELECT * when table schema is known";
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
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        
        for (SelectItem item : selectItems) {
            if (item instanceof AllColumns) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        return statement;
    }
}
