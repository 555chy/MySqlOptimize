package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RemoveAlwaysTrueOrFalseRule implements OptimizationRule {

    @Override
    public String getName() {
        return "移除恒等表达式(RemoveAlwaysTrueOrFalse)";
    }

    @Override
    public String getDescription() {
        return "移除始终为TRUE（如1=1）或始终为FALSE（如1=0）的条件";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString().toUpperCase();
        return sql.contains("1=1") || sql.contains("1=0") ||
               sql.contains("TRUE") || sql.contains("FALSE") ||
               containsNumericComparison(sql);
    }

    private boolean containsNumericComparison(String sql) {
        Pattern pattern = Pattern.compile("\\d+\\s*[><=]+\\s*\\d+");
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            String comparison = matcher.group();
            if (isAlwaysTrue(comparison)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAlwaysTrue(String comparison) {
        Pattern pattern = Pattern.compile("(\\d+)\\s*([><=!]+|<>)\\s*(\\d+)");
        Matcher matcher = pattern.matcher(comparison);
        if (matcher.matches()) {
            double left = Double.parseDouble(matcher.group(1));
            String op = matcher.group(2);
            double right = Double.parseDouble(matcher.group(3));

            switch (op) {
                case ">":
                    return left > right;
                case "<":
                    return left < right;
                case ">=":
                    return left >= right;
                case "<=":
                    return left <= right;
                case "=":
                case "==":
                    return left == right;
                case "<>":
                case "!=":
                    return left != right;
                default:
                    return false;
            }
        }
        return false;
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = removeTrivialConditions(sql);
        int maxIterations = 10;
        int iteration = 0;

        while (!optimizedSql.equals(sql) && iteration < maxIterations) {
            sql = optimizedSql;
            optimizedSql = removeTrivialConditions(sql);
            iteration++;
        }

        if (!optimizedSql.equals(statement.toString())) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String removeTrivialConditions(String sql) {
        String result = sql;
        result = result.replaceAll("(?i)\\bAND\\s+1\\s*=\\s*1\\b", "");
        result = result.replaceAll("(?i)\\b1\\s*=\\s*1\\s+AND\\b", "");
        result = result.replaceAll("(?i)\\bOR\\s+1\\s*=\\s*0\\b", "");
        result = result.replaceAll("(?i)\\b1\\s*=\\s*0\\s+OR\\b", "");
        result = result.replaceAll("(?i)AND\\s+\\(.*?\\)\\s*=\\s*TRUE", "");
        result = result.replaceAll("(?i)TRUE\\s*=\\s*\\(.*?\\)\\s+AND", "");
        result = result.replaceAll("(?i)\\(\\s*\\)\\s*=\\s*TRUE", "");
        result = result.replaceAll("(?i)TRUE\\s*=\\s*\\(\\s*\\)", "");

        result = result.replaceAll("(?i)\\bWHERE\\s+1\\s*=\\s*1\\b", "");
        result = result.replaceAll("(?i)\\bWHERE\\s+1\\s*<>\\s*1\\b", "WHERE 1=1");

        result = removeNumericAlwaysTrueConditions(result);

        result = Pattern.compile("(?i)\\s+AND\\s+WHERE").matcher(result).replaceAll(" WHERE");
        result = Pattern.compile("(?i)WHERE\\s+AND").matcher(result).replaceAll("WHERE");
        result = Pattern.compile("(?i)\\s+OR\\s+WHERE").matcher(result).replaceAll(" WHERE");
        result = Pattern.compile("(?i)WHERE\\s+OR").matcher(result).replaceAll("WHERE");
        result = Pattern.compile("(?i)WHERE\\s*$").matcher(result).replaceAll("");

        result = Pattern.compile("(?i)\\(\\s*\\)").matcher(result).replaceAll("");

        result = Pattern.compile("(?i)\\s+AND\\s+AND\\s+").matcher(result).replaceAll(" AND ");
        result = Pattern.compile("(?i)\\s+OR\\s+OR\\s+").matcher(result).replaceAll(" OR ");
        result = Pattern.compile("(?i)\\s+AND\\s+OR\\s+").matcher(result).replaceAll(" OR ");
        result = Pattern.compile("(?i)\\s+OR\\s+AND\\s+").matcher(result).replaceAll(" AND ");

        return result.trim();
    }

    private String removeNumericAlwaysTrueConditions(String sql) {
        String result = sql;

        // 只匹配独立的完整条件，避免匹配表达式内部的子部分
        // 格式: (|AND|OR|WHERE) 条件 (|AND|OR|)
        String[] operators = {"AND", "OR", "WHERE"};
        
        for (String op : operators) {
            Pattern pattern = Pattern.compile("(?i)(?<=\\b" + Pattern.quote(op) + "\\b)\\s*(\\d+\\s*[><=!]+\\s*\\d+)\\s*(?=\\b(AND|OR|$)\\b)");
            Matcher matcher = pattern.matcher(result);
            StringBuffer sb = new StringBuffer();
            
            while (matcher.find()) {
                String comparison = matcher.group(1);
                if (isAlwaysTrue(comparison)) {
                    matcher.appendReplacement(sb, "");
                } else {
                    matcher.appendReplacement(sb, matcher.group());
                }
            }
            
            matcher.appendTail(sb);
            result = sb.toString();
        }
        
        // 清理多余的空格
        result = result.replaceAll("\\s+", " ");
        result = result.replaceAll("\\s*WHERE\\s*AND\\s*", " WHERE ");
        result = result.replaceAll("\\s*WHERE\\s*OR\\s*", " WHERE ");
        result = result.replaceAll("\\s+WHERE\\s*$", "");

        return result.trim();
    }
}