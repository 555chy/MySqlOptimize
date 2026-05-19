package com.sqlparse.metadata;

import java.util.ArrayList;
import java.util.List;

public class TableInfo {
    private String name;
    private String schema;
    private String type;
    private String remarks;
    private long rowCount;
    private List<ColumnInfo> columns = new ArrayList<>();
    private List<IndexInfo> indexes = new ArrayList<>();
    private List<ForeignKeyInfo> foreignKeys = new ArrayList<>();

    public TableInfo() {
    }

    public TableInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public long getRowCount() {
        return rowCount;
    }

    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }

    public List<ColumnInfo> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnInfo> columns) {
        this.columns = columns;
    }

    public void addColumn(ColumnInfo column) {
        this.columns.add(column);
    }

    public List<IndexInfo> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<IndexInfo> indexes) {
        this.indexes = indexes;
    }

    public void addIndex(IndexInfo index) {
        this.indexes.add(index);
    }

    public List<ForeignKeyInfo> getForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(List<ForeignKeyInfo> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    public void addForeignKey(ForeignKeyInfo foreignKey) {
        this.foreignKeys.add(foreignKey);
    }

    @Override
    public String toString() {
        return "TableInfo{" +
                "name='" + name + '\'' +
                ", schema='" + schema + '\'' +
                ", type='" + type + '\'' +
                ", rowCount=" + rowCount +
                ", columns=" + columns.size() +
                ", indexes=" + indexes.size() +
                ", foreignKeys=" + foreignKeys.size() +
                '}';
    }
}
