package com.sqlparse.optimizer.rules;

import com.sqlparse.optimizer.OptimizationContext;
import com.sqlparse.optimizer.OptimizationRule;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimplifyWithClauseRule implements OptimizationRule {

    @Override
    public String getName() {
        return "简化WITH子句(SimplifyWithClause)";
    }

    @Override
    public String getDescription() {
        return "内联只使用一次的简单CTE（WITH子句）";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        String sql = statement.toString().toUpperCase();
        return sql.contains("WITH") && sql.contains("AS (");
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = inlineSimpleCTEs(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String inlineSimpleCTEs(String sql) {
        String result = sql;
        
        Pattern ctePattern = Pattern.compile(
            "(?i)WITH\\s+(\\w+)\\s+AS\\s*\\(\\s*(SELECT\\s+.+?)\\s*\\)\\s*(SELECT\\s+[^;]+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher matcher = ctePattern.matcher(result);
        
        if (matcher.find()) {
            String cteName = matcher.group(1);
            String cteSelect = matcher.group(2);
            String mainQuery = matcher.group(3);
            
            if (!cteSelect.toUpperCase().contains("UNION") && 
                !cteSelect.toUpperCase().contains("JOIN") &&
                cteSelect.toUpperCase().contains("FROM")) {
                String tableRef = "( " + cteSelect + " )";
                if (mainQuery.toUpperCase().contains(cteName.toUpperCase())) {
                    String occurrenceCount = countOccurrences(mainQuery.toUpperCase(), cteName.toUpperCase());
                    if ("1".equals(occurrenceCount)) {
                        result = cteSelect.replaceFirst("(?i)SELECT\\s+", "SELECT ") + 
                                mainQuery.replaceFirst("(?i)" + Pattern.quote(cteName), "(" + cteSelect + ")");
                    }
                }
            }
        }
        
        return result;
    }

    private String countOccurrences(String str, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return String.valueOf(count);
    }
}
