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
        return "LIKE模式优化(LikePatternOptimize)";
    }

    @Override
    public String getDescription() {
        return "优化LIKE模式以获得更好的索引使用";
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

        Pattern exactMatchPattern = Pattern.compile(
            "(?i)(LIKE)\\s+'([^%_]+)'",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = exactMatchPattern.matcher(result);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String likeKeyword = matcher.group(1);
            String value = matcher.group(2);

            if (value != null && !value.contains("%") && !value.contains("_")) {
                String replacement = "= '" + value + "'";
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
            }
        }
        matcher.appendTail(sb);
        result = sb.toString();

        return result;
    }
}
