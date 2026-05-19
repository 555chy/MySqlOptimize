package com.sqlparse.metadata;

public class ColumnInfo {
    private String name;
    private String type;
    private int columnSize;
    private boolean nullable;
    private boolean primaryKey;
    private String defaultValue;
    private String remarks;

    public ColumnInfo() {
    }

    public ColumnInfo(String name, String type, int columnSize, boolean nullable, boolean primaryKey) {
        this.name = name;
        this.type = type;
        this.columnSize = columnSize;
        this.nullable = nullable;
        this.primaryKey = primaryKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "ColumnInfo{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", columnSize=" + columnSize +
                ", nullable=" + nullable +
                ", primaryKey=" + primaryKey +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }
}
