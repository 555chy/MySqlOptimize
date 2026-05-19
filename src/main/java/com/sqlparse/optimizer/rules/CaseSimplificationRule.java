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
        return "CaseSimplification";
    }

    @Override
    public String getDescription() {
        return "Simplifies CASE expressions by eliminating redundant branches";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString().toUpperCase();
        return sql.contains("CASE") && sql.contains("WHEN") && sql.contains("THEN");
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = simplifyCase(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String simplifyCase(String sql) {
        String result = sql;
        result = result.replaceAll("(?i)CASE\\s+WHEN\\s+TRUE\\s+THEN\\s+(\\w+)\\s+ELSE", "$1 ELSE");
        result = result.replaceAll("(?i)CASE\\s+WHEN\\s+TRUE\\s+THEN\\s+(\\w+)\\s+END", "$1 END");
        result = result.replaceAll("(?i)CASE\\s+WHEN\\s+FALSE\\s+THEN\\s+(\\w+)\\s+ELSE\\s+(\\w+)\\s+END", "$2 END");
        result = result.replaceAll("(?i)CASE\\s+(\\w+)\\s+WHEN\\s+\\1\\s+THEN\\s+(\\w+)\\s+ELSE", "$2 ELSE");
        result = result.replaceAll("(?i)CASE\\s+(\\w+)\\s+WHEN\\s+\\1\\s+THEN\\s+(\\w+)\\s+END", "$2 END");
        result = result.replaceAll("(?i)CASE\\s+(\\w+)\\s+WHEN\\s+\\1\\s+THEN\\s+\\1\\s+END", "\\1 END");
        return result;
    }
}
