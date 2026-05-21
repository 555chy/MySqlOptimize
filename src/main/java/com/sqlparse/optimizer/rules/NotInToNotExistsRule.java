package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotInToNotExistsRule implements OptimizationRule {

    @Override
    public String getName() {
        return "NOT IN转NOT EXISTS(NotInToNotExists)";
    }

    @Override
    public String getDescription() {
        return "将NOT IN子查询转换为NOT EXISTS以提升性能";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        // 暂时禁用，因为正则表达式有问题，可能导致结果不一致
        return false;
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = convertNotInToNotExists(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String convertNotInToNotExists(String sql) {
        String result = sql;
        
        Pattern pattern = Pattern.compile(
            "(?i)([^\\s]+)\\s+NOT\\s+IN\\s*\\(\\s*SELECT\\s+([^\\s]+)\\s+FROM\\s+([^\\s]+)\\s*(?:WHERE\\s+([^)]+))?\\s*\\)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(result);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String leftColumn = matcher.group(1);
            String selectColumn = matcher.group(2);
            String tableName = matcher.group(3);
            String whereClause = matcher.group(4);
            
            String replacement;
            if (whereClause != null && !whereClause.trim().isEmpty()) {
                replacement = "NOT EXISTS (SELECT 1 FROM " + tableName + " WHERE " + 
                             tableName + "." + selectColumn + " = " + leftColumn + 
                             " AND " + whereClause + ")";
            } else {
                replacement = "NOT EXISTS (SELECT 1 FROM " + tableName + " WHERE " + 
                             tableName + "." + selectColumn + " = " + leftColumn + ")";
            }
            
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }
}
