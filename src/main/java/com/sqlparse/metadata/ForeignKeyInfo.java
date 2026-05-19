package com.sqlparse.metadata;

public class ForeignKeyInfo {
    private String name;
    private String tableName;
    private String columnName;
    private String foreignTableName;
    private String foreignColumnName;
    private String updateRule;
    private String deleteRule;

    public ForeignKeyInfo() {
    }

    public ForeignKeyInfo(String name, String tableName, String columnName, String foreignTableName, String foreignColumnName) {
        this.name = name;
        this.tableName = tableName;
        this.columnName = columnName;
        this.foreignTableName = foreignTableName;
        this.foreignColumnName = foreignColumnName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getForeignTableName() {
        return foreignTableName;
    }

    public void setForeignTableName(String foreignTableName) {
        this.foreignTableName = foreignTableName;
    }

    public String getForeignColumnName() {
        return foreignColumnName;
    }

    public void setForeignColumnName(String foreignColumnName) {
        this.foreignColumnName = foreignColumnName;
    }

    public String getUpdateRule() {
        return updateRule;
    }

    public void setUpdateRule(String updateRule) {
        this.updateRule = updateRule;
    }

    public String getDeleteRule() {
        return deleteRule;
    }

    public void setDeleteRule(String deleteRule) {
        this.deleteRule = deleteRule;
    }

    @Override
    public String toString() {
        return "ForeignKeyInfo{" +
                "name='" + name + '\'' +
                ", tableName='" + tableName + '\'' +
                ", columnName='" + columnName + '\'' +
                ", foreignTableName='" + foreignTableName + '\'' +
                ", foreignColumnName='" + foreignColumnName + '\'' +
                '}';
    }
}
