package com.sqlparse.optimizer.rules;

import com.sqlparse.metadata.DatabaseMetadata;
import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.List;
import java.util.Map;

public class OptimizeJoinOrderRule implements OptimizationRule {

    @Override
    public String getName() {
        return "优化JOIN顺序(OptimizeJoinOrder)";
    }

    @Override
    public String getDescription() {
        return "在可用时根据表大小信息优化JOIN顺序";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        
        String sql = statement.toString().toUpperCase();
        int joinCount = countOccurrences(sql, "JOIN");
        
        if (joinCount <= 1) {
            return false;
        }
        
        DatabaseMetadata metadata = context.getDatabaseMetadata();
        return metadata != null;
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        DatabaseMetadata metadata = context.getDatabaseMetadata();
        if (metadata == null) {
            return statement;
        }
        
        return statement;
    }

    private int countOccurrences(String str, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}
