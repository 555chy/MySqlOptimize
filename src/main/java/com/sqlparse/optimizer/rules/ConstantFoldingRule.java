package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConstantFoldingRule implements OptimizationRule {

    @Override
    public String getName() {
        return "常量折叠(ConstantFolding)";
    }

    @Override
    public String getDescription() {
        return "在解析时计算常量表达式而非运行时";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString();
        Pattern pattern = Pattern.compile("\\d+\\s*[+\\-*/]\\s*\\d+");
        return pattern.matcher(sql).find();
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = foldConstants(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String foldConstants(String sql) {
        String result = sql;
        result = result.replaceAll("(?i)\\b0\\s*\\+\\s*(\\d+)", "$1");
        result = result.replaceAll("(?i)(\\d+)\\s*\\+\\s*0\\b", "$1");
        result = result.replaceAll("(?i)\\b1\\s*\\*\\s*(\\d+)", "$1");
        result = result.replaceAll("(?i)(\\d+)\\s*\\*\\s*1\\b", "$1");
        result = result.replaceAll("(?i)\\b(\\d+)\\s*\\/\\s*1\\b", "$1");
        result = result.replaceAll("(?i)\\b0\\s*\\*\\s*\\d+\\b", "0");
        result = result.replaceAll("(?i)\\b\\d+\\s*\\*\\s*0\\b", "0");
        result = result.replaceAll("(?i)\\b\\d+\\s*-\\s*0\\b", "$1");
        
        result = foldPattern(result, "\\b(\\d+)\\s*-\\s*(\\d+)\\b");
        result = foldPattern(result, "\\b(\\d+)\\s*\\+\\s*(\\d+)\\b");
        
        return result;
    }
    
    private String foldPattern(String sql, String patternStr) {
        Pattern pattern = Pattern.compile("(?i)" + patternStr);
        Matcher matcher = pattern.matcher(sql);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            try {
                int a = Integer.parseInt(matcher.group(1).trim());
                int b = Integer.parseInt(matcher.group(2).trim());
                int result = (patternStr.contains("-")) ? (a - b) : (a + b);
                matcher.appendReplacement(sb, String.valueOf(result));
            } catch (NumberFormatException e) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
