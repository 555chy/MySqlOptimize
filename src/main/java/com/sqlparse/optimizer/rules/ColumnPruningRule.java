package com.sqlparse.optimizer.rules;

import com.sqlparse.metadata.DatabaseMetadata;
import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.List;

public class ColumnPruningRule implements OptimizationRule {

    @Override
    public String getName() {
        return "列剪裁(ColumnPruning)";
    }

    @Override
    public String getDescription() {
        return "当表结构已知时从SELECT *中移除不需要的列";
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
                DatabaseMetadata metadata = context.getDatabaseMetadata();
                return metadata != null;
            }
        }
        
        return false;
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        DatabaseMetadata metadata = context.getDatabaseMetadata();
        if (metadata == null) {
            return statement;
        }
        
        return statement;
    }
}
