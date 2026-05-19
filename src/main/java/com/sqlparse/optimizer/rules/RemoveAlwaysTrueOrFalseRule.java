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
        return "RemoveAlwaysTrueOrFalse";
    }

    @Override
    public String getDescription() {
        return "Removes conditions that are always TRUE (like 1=1) or always FALSE (like 1=0)";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString().toUpperCase();
        return sql.contains("1=1") || sql.contains("1=0") || 
               sql.contains("TRUE") || sql.contains("FALSE");
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = removeTrivialConditions(sql);
        if (!optimizedSql.equals(sql)) {
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
        
        result = Pattern.compile("(?i)\\s+AND\\s+WHERE").matcher(result).replaceAll(" WHERE");
        result = Pattern.compile("(?i)WHERE\\s+AND").matcher(result).replaceAll("WHERE");
        result = Pattern.compile("(?i)\\s+OR\\s+WHERE").matcher(result).replaceAll(" WHERE");
        result = Pattern.compile("(?i)WHERE\\s+OR").matcher(result).replaceAll("WHERE");
        result = Pattern.compile("(?i)WHERE\\s*$").matcher(result).replaceAll("");
        
        return result.trim();
    }
}
