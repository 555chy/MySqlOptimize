package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NullCheckEliminationRule implements OptimizationRule {

    @Override
    public String getName() {
        return "NullCheckElimination";
    }

    @Override
    public String getDescription() {
        return "Removes unnecessary NULL checks on non-nullable columns";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString().toUpperCase();
        return sql.contains("IS NULL") || sql.contains("IS NOT NULL");
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = eliminateNullChecks(sql, context);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String eliminateNullChecks(String sql, OptimizationContext context) {
        String result = sql;
        result = result.replaceAll("(?i)\\bCOALESCE\\([^,]+,\\s*[^)]+\\)\\s*(<>|<|>|<=|>=)\\s*NULL\\b", "TRUE");
        result = result.replaceAll("(?i)\\bNULL\\s*(<>|<|>|<=|>=)\\s*COALESCE\\([^,]+,\\s*[^)]+\\)\\b", "TRUE");
        result = result.replaceAll("(?i)\\bIFNULL\\([^,]+,\\s*[^)]+\\)\\s*IS\\s+NULL\\b", "FALSE");
        result = result.replaceAll("(?i)\\bIFNULL\\([^,]+,\\s*[^)]+\\)\\s*IS\\s+NOT\\s+NULL\\b", "TRUE");
        return result;
    }
}
