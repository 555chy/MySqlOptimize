package com.sqlparse.parser;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

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

    public String formatSqlToMultiLines(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }
        try {
            return SqlFormatter.format(sql);
        } catch (Exception e) {
            return sql;
        }
    }
}
