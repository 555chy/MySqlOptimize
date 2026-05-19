package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArithmeticComparisonOptimizeRule implements OptimizationRule {

    @Override
    public String getName() {
        return "ArithmeticComparisonOptimize";
    }

    @Override
    public String getDescription() {
        return "Optimizes arithmetic comparisons like column*1 > 0 to column > 0";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString();
        Pattern pattern = Pattern.compile("\\w+\\s*\\*\\s*\\d+|\\d+\\s*\\*\\s*\\w+");
        return pattern.matcher(sql).find();
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = optimizeArithmeticComparisons(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String optimizeArithmeticComparisons(String sql) {
        String result = sql;
        result = result.replaceAll("(?i)(\\w+)\\s*\\*\\s*1(\\s*(>|<|>=|<=|=|<>))", "$1$2");
        result = result.replaceAll("(?i)1\\s*\\*\\s*(\\w+)(\\s*(>|<|>=|<=|=|<>))", "$1$2");
        result = result.replaceAll("(?i)(\\w+)\\s*\\+\\s*0(\\s*(>|<|>=|<=|=|<>))", "$1$2");
        result = result.replaceAll("(?i)0\\s*\\+\\s*(\\w+)(\\s*(>|<|>=|<=|=|<>))", "$1$2");
        result = result.replaceAll("(?i)(\\w+)\\s*-\\s*0(\\s*(>|<|>=|<=|=|<>))", "$1$2");
        result = result.replaceAll("(?i)(\\w+)\\s*/\\s*1(\\s*(>|<|>=|<=|=|<>))", "$1$2");
        return result;
    }
}
