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
        return "SimplifyWithClause";
    }

    @Override
    public String getDescription() {
        return "Inlines simple CTEs (WITH clauses) that are used only once";
    }

    @Override
    public boolean canApply(Statement statement, OptimizationContext context) {
        if (!(statement instanceof Select)) {
            return false;
        }
        return statement.toString().toUpperCase().contains("WITH ");
    }

    @Override
    public Statement apply(Statement statement, OptimizationContext context) {
        String sql = statement.toString();
        String optimizedSql = simplifyWithClause(sql);
        if (!optimizedSql.equals(sql)) {
            try {
                return net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(optimizedSql);
            } catch (Exception e) {
                return statement;
            }
        }
        return statement;
    }

    private String simplifyWithClause(String sql) {
        String upperSql = sql.toUpperCase();
        if (!upperSql.startsWith("WITH ")) {
            return sql;
        }

        int withEndIndex = findWithEndIndex(sql);
        if (withEndIndex == -1) {
            return sql;
        }

        String withPart = sql.substring(0, withEndIndex);
        String mainPart = sql.substring(withEndIndex);

        Pattern ctePattern = Pattern.compile("(?i)(\\w+)\\s+AS\\s*\\(([^)]+)\\)");
        Matcher matcher = ctePattern.matcher(withPart);

        String result = mainPart;
        while (matcher.find()) {
            String cteName = matcher.group(1);
            String cteBody = matcher.group(2);
            result = result.replaceAll("(?i)" + Pattern.quote(cteName), "(" + cteBody + ")");
        }

        return result.trim();
    }

    private int findWithEndIndex(String sql) {
        int depth = 0;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (depth == 0 && i > 4 && sql.substring(0, i).toUpperCase().contains("SELECT ")) {
                return i;
            }
        }
        return -1;
    }
}
