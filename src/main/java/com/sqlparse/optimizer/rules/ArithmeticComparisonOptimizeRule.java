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
        return "算术比较优化(ArithmeticComparisonOptimize)";
    }

    @Override
    public String getDescription() {
        return "优化如column*1 > 0的算术比较为column > 0";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        // 暂时禁用，因为正则表达式有问题
        return false;
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
        result = result.replaceAll("(?i)(\\w+)\\s*\\*\\s*1\\s*(>|>=|<|<=|=|<>|!=)\\s*(\\d+)", "$1 $2 $3");
        result = result.replaceAll("(?i)1\\s*\\*\\s*(\\w+)\\s*(>|>=|<|<=|=|<>|!=)\\s*(\\d+)", "$1 $2 $3");
        result = result.replaceAll("(?i)(\\w+)\\s*\\+\\s*0\\s*(>|>=|<|<=|=|<>|!=)\\s*(\\d+)", "$1 $2 $3");
        result = result.replaceAll("(?i)0\\s*\\+\\s*(\\w+)\\s*(>|>=|<|<=|=|<>|!=)\\s*(\\d+)", "$1 $2 $3");
        result = result.replaceAll("(?i)(\\w+)\\s*-\\s*0\\s*(>|>=|<|<=|=|<>|!=)\\s*(\\d+)", "$1 $2 $3");
        result = result.replaceAll("(?i)(\\w+)\\s*/\\s*1\\s*(>|>=|<|<=|=|<>|!=)\\s*(\\d+)", "$1 $2 $3");
        return result;
    }
}
