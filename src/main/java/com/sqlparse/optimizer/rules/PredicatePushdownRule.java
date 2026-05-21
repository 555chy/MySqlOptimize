package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PredicatePushdownRule implements OptimizationRule {

    @Override
    public String getName() {
        return "谓词下推(PredicatePushdown)";
    }

    @Override
    public String getDescription() {
        return "将WHERE条件下推到嵌套查询的最深层级";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString().toUpperCase();
        return sql.contains("FROM (SELECT") && sql.contains("WHERE");
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = pushdownPredicates(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String pushdownPredicates(String sql) {
        String result = sql;
        result = pushdownSimplePredicates(result);
        return result;
    }

    private String pushdownSimplePredicates(String sql) {
        String result = sql;
        
        Pattern pattern = Pattern.compile(
            "(?i)(WHERE\\s+[^']+?)\\s+AND\\s+([\\w.]+)\\s*(=|>|<|>=|<=|<>)\\s*([\\w.'()]+)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(result);
        StringBuffer sb = new StringBuffer();
        boolean modified = false;
        
        while (matcher.find()) {
            String whereClause = matcher.group(1);
            String column = matcher.group(2);
            String operator = matcher.group(3);
            String value = matcher.group(4);
            
            if (!whereClause.toUpperCase().contains(column)) {
                modified = true;
            }
        }
        
        if (!modified) {
            return sql;
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }
}
