package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LikePatternOptimizeRule implements OptimizationRule {

    @Override
    public String getName() {
        return "LikePatternOptimize";
    }

    @Override
    public String getDescription() {
        return "Optimizes LIKE patterns for better index usage";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString().toUpperCase();
        return sql.contains("LIKE");
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = optimizeLikePatterns(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String optimizeLikePatterns(String sql) {
        String result = sql;
        Pattern pattern = Pattern.compile("(?i)LIKE\\s+'([^']+)%'");
        Matcher matcher = pattern.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String content = matcher.group(1);
            matcher.appendReplacement(sb, "LIKE '" + content + "%'");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
