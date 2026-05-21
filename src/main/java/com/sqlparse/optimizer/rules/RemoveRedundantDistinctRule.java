package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RemoveRedundantDistinctRule implements OptimizationRule {

    @Override
    public String getName() {
        return "移除冗余DISTINCT(RemoveRedundantDistinct)";
    }

    @Override
    public String getDescription() {
        return "移除结果已经唯一时的冗余DISTINCT";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        
        String sql = statement.toString();
        if (!sql.toUpperCase().contains("DISTINCT")) {
            return false;
        }
        
        boolean hasGroupBy = sql.toUpperCase().contains("GROUP BY");
        boolean hasLeftJoin = sql.toUpperCase().contains("LEFT JOIN");
        boolean hasLimit1 = sql.toUpperCase().contains("LIMIT 1");
        
        if (hasLeftJoin) {
            return false;
        }
        
        if (hasGroupBy || hasLimit1) {
            return true;
        }
        
        return false;
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        
        if (sql.toUpperCase().contains("LEFT JOIN")) {
            return statement;
        }
        
        Pattern distinctPattern = Pattern.compile("(?i)(COUNT|SUM|AVG|MAX|MIN)\\s*\\(\\s*DISTINCT\\s+(\\w+)\\s*\\)\\s*AS\\s*(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = distinctPattern.matcher(sql);
        
        if (matcher.find()) {
            return statement;
        }
        
        Pattern selectDistinctPattern = Pattern.compile(
            "(?i)^\\s*SELECT\\s+DISTINCT\\s+",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
        );
        Pattern groupByPattern = Pattern.compile(
            "(?i)GROUP\\s+BY",
            Pattern.CASE_INSENSITIVE
        );
        
        if (selectDistinctPattern.matcher(sql).find() && groupByPattern.matcher(sql).find()) {
            String optimizedSql = sql.replaceFirst(
                "(?i)^(\\s*SELECT\\s+)DISTINCT(\\s+)",
                "$1$2"
            );
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        
        return statement;
    }
}
