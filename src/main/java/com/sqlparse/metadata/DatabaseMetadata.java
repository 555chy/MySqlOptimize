package com.sqlparse.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseMetadata {
    private String databaseName;
    private String databaseProductName;
    private String databaseProductVersion;
    private String driverName;
    private String driverVersion;
    private List<TableInfo> tables = new ArrayList<>();
    private Map<String, TableInfo> tableMap = new HashMap<>();

    public DatabaseMetadata() {
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabaseProductName() {
        return databaseProductName;
    }

    public void setDatabaseProductName(String databaseProductName) {
        this.databaseProductName = databaseProductName;
    }

    public String getDatabaseProductVersion() {
        return databaseProductVersion;
    }

    public void setDatabaseProductVersion(String databaseProductVersion) {
        this.databaseProductVersion = databaseProductVersion;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverVersion() {
        return driverVersion;
    }

    public void setDriverVersion(String driverVersion) {
        this.driverVersion = driverVersion;
    }

    public List<TableInfo> getTables() {
        return tables;
    }

    public void setTables(List<TableInfo> tables) {
        this.tables = tables;
        this.tableMap.clear();
        for (TableInfo table : tables) {
            this.tableMap.put(table.getName().toLowerCase(), table);
        }
    }

    public void addTable(TableInfo table) {
        this.tables.add(table);
        this.tableMap.put(table.getName().toLowerCase(), table);
    }

    public TableInfo getTable(String tableName) {
        return tableMap.get(tableName.toLowerCase());
    }

    public boolean hasTable(String tableName) {
        return tableMap.containsKey(tableName.toLowerCase());
    }

    @Override
    public String toString() {
        return "DatabaseMetadata{" +
                "databaseName='" + databaseName + '\'' +
                ", databaseProductName='" + databaseProductName + '\'' +
                ", tables=" + tables.size() +
                '}';
    }
}
