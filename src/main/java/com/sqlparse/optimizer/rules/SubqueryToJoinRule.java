package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubqueryToJoinRule implements OptimizationRule {

    @Override
    public String getName() {
        return "子查询转JOIN(SubqueryToJoin)";
    }

    @Override
    public String getDescription() {
        return "将符合条件的子查询转换为JOIN以提升性能";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString().toUpperCase();
        return sql.contains("FROM (SELECT") && sql.contains("WHERE EXISTS");
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = convertSubqueryToJoin(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String convertSubqueryToJoin(String sql) {
        String result = sql;
        
        Pattern pattern = Pattern.compile(
            "(?i)(SELECT\\s+[^\\*]+?\\s+FROM\\s+\\w+)\\s+WHERE\\s+EXISTS\\s*\\(\\s*SELECT\\s+\\d+\\s+FROM\\s+(\\w+)\\s+WHERE\\s+(\\w+)\\.(\\w+)\\s*=\\s*(\\w+)\\.(\\w+)(?:\\s+AND\\s+([^)]+))?\\s*\\)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(result);
        
        if (matcher.find()) {
            String selectPart = matcher.group(1);
            String subqueryTable = matcher.group(2);
            String leftTable = matcher.group(3);
            String leftColumn = matcher.group(4);
            String rightTable = matcher.group(5);
            String rightColumn = matcher.group(6);
            String extraCondition = matcher.group(7);
            
            String replacement = selectPart + " INNER JOIN " + subqueryTable + 
                                 " ON " + leftTable + "." + leftColumn + " = " + 
                                 rightTable + "." + rightColumn;
            
            if (extraCondition != null && !extraCondition.trim().isEmpty()) {
                replacement += " AND " + extraCondition;
            }
            
            result = matcher.replaceFirst(Matcher.quoteReplacement(replacement));
        }
        
        return result;
    }
}
