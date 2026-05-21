package com.sqlparse.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

import java.util.ArrayList;
import java.util.List;

public class SqlParser {

    public SqlNode parse(String sql) throws JSQLParserException {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL cannot be null or empty");
        }

        Statement statement = CCJSqlParserUtil.parse(sql);
        SqlTypeDetector.SqlType sqlType = SqlTypeDetector.detectType(statement);
        String typeName = SqlTypeDetector.getTypeName(sqlType);
        
        return new SqlNode(typeName, sql, statement);
    }

    public List<SqlNode> parseMultiple(String sql) throws JSQLParserException {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL cannot be null or empty");
        }

        List<SqlNode> nodes = new ArrayList<>();
        // 简单实现：先尝试作为单条语句解析
        try {
            nodes.add(parse(sql));
        } catch (Exception e) {
            // 如果失败，尝试按分号分割
            String[] parts = sql.split(";");
            for (String part : parts) {
                part = part.trim();
                if (!part.isEmpty()) {
                    try {
                        nodes.add(parse(part));
                    } catch (Exception ex) {
                        // 忽略无法解析的部分
                    }
                }
            }
        }
        
        return nodes;
    }

    public String formatSql(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        return statement.toString();
    }

    public String formatSqlToMultiLines(String sql) {
        // 先尝试用JSqlParser格式化
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            String formatted = statement.toString();
            // 简单的多行格式化
            return formatSimpleMultiLine(formatted);
        } catch (Exception e) {
            // 解析失败，直接用简单格式化
            return formatSimpleMultiLine(sql);
        }
    }
    
    private String formatSimpleMultiLine(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }
        
        // 简单但有效的多行格式化
        String result = sql;
        
        // 在关键字前添加换行
        String[] keywords = {
            "SELECT", "FROM", "WHERE", "AND", "OR", "JOIN", "LEFT", "RIGHT", 
            "INNER", "OUTER", "GROUP", "HAVING", "ORDER", "LIMIT", "OFFSET",
            "UNION", "INSERT", "UPDATE", "DELETE", "SET", "VALUES"
        };
        
        for (String keyword : keywords) {
            // 替换 " KEYWORD" 为 "\n  KEYWORD"
            result = result.replaceAll("(?i)\\b" + keyword + "\\b", "\n  " + keyword);
        }
        
        // 开头的换行去掉
        if (result.startsWith("\n")) {
            result = result.substring(1);
        }
        
        // 去除多余的换行
        while (result.contains("\n\n")) {
            result = result.replace("\n\n", "\n");
        }
        
        return result.trim();
    }

    public String regenerateSql(String sql) throws JSQLParserException {
        return formatSql(sql);
    }

    public String processSql(String sql) {
        try {
            SqlNode node = parse(sql);
            if (SqlTypeDetector.isSelectStatement(node.getStatement())) {
                return node.toFormattedSql();
            } else {
                return sql;
            }
        } catch (JSQLParserException e) {
            return sql;
        }
    }

    public boolean isValidSql(String sql) {
        try {
            parse(sql);
            return true;
        } catch (JSQLParserException | IllegalArgumentException e) {
            return false;
        }
    }
}
