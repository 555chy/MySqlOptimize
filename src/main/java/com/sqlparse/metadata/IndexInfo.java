package com.sqlparse.metadata;

import java.util.ArrayList;
import java.util.List;

public class IndexInfo {
    private String name;
    private String tableName;
    private boolean unique;
    private String type;
    private List<String> columns = new ArrayList<>();

    public IndexInfo() {
    }

    public IndexInfo(String name, String tableName, boolean unique) {
        this.name = name;
        this.tableName = tableName;
        this.unique = unique;
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

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public void addColumn(String column) {
        this.columns.add(column);
    }

    @Override
    public String toString() {
        return "IndexInfo{" +
                "name='" + name + '\'' +
                ", tableName='" + tableName + '\'' +
                ", unique=" + unique +
                ", type='" + type + '\'' +
                ", columns=" + columns +
                '}';
    }
}
