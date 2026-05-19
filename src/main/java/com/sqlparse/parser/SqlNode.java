package com.sqlparse.parser;

import net.sf.jsqlparser.statement.Statement;

import java.util.ArrayList;
import java.util.List;

public class SqlNode {
    private String nodeType;
    private String sqlText;
    private Statement statement;
    private List<SqlNode> children;

    public SqlNode(String nodeType, String sqlText) {
        this.nodeType = nodeType;
        this.sqlText = sqlText;
        this.children = new ArrayList<>();
    }

    public SqlNode(String nodeType, String sqlText, Statement statement) {
        this(nodeType, sqlText);
        this.statement = statement;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getSqlText() {
        return sqlText;
    }

    public void setSqlText(String sqlText) {
        this.sqlText = sqlText;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public List<SqlNode> getChildren() {
        return children;
    }

    public void setChildren(List<SqlNode> children) {
        this.children = children;
    }

    public void addChild(SqlNode child) {
        this.children.add(child);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public String toFormattedSql() {
        if (statement != null) {
            return statement.toString();
        }
        return sqlText;
    }
}
