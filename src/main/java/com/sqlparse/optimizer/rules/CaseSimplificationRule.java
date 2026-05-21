package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaseSimplificationRule implements OptimizationRule {

    @Override
    public String getName() {
        return "CASE表达式简化(CaseSimplification)";
    }

    @Override
    public String getDescription() {
        return "通过消除冗余分支简化CASE表达式";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString().toUpperCase();
        return sql.contains("CASE") && sql.contains("WHEN");
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = simplifyCaseExpressions(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String simplifyCaseExpressions(String sql) {
        String result = sql;
        
        result = simplifyCaseWhenTrue(result);
        result = simplifyCaseWhenFalse(result);
        result = simplifyCaseWithSameConditions(result);
        
        return result;
    }

    private String simplifyCaseWhenTrue(String sql) {
        String result = sql;
        
        Pattern pattern = Pattern.compile(
            "(?i)CASE\\s+WHEN\\s+1\\s*=\\s*1\\s+THEN\\s+([^\\s]+)\\s+ELSE\\s+([^\\s]+)\\s+END",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(result);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String thenValue = matcher.group(1);
            String elseValue = matcher.group(2);
            if (thenValue.equals(elseValue)) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(thenValue));
            }
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String simplifyCaseWhenFalse(String sql) {
        String result = sql;
        
        Pattern pattern = Pattern.compile(
            "(?i)CASE\\s+WHEN\\s+1\\s*=\\s*0\\s+THEN\\s+([^\\s]+)\\s+ELSE\\s+([^\\s]+)\\s+END",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(result);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String thenValue = matcher.group(1);
            String elseValue = matcher.group(2);
            if (thenValue.equals(elseValue)) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(elseValue));
            }
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String simplifyCaseWithSameConditions(String sql) {
        return sql;
    }
}
