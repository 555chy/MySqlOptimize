package com.sqlparse.metadata;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetadataFetcher {
    private DatabaseConnectionManager connectionManager;

    public MetadataFetcher(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public DatabaseMetadata fetchAllMetadata() throws SQLException {
        DatabaseMetadata metadata = new DatabaseMetadata();
        Connection conn = connectionManager.getConnection();

        java.sql.DatabaseMetaData dbMeta = conn.getMetaData();
        metadata.setDatabaseProductName(dbMeta.getDatabaseProductName());
        metadata.setDatabaseProductVersion(dbMeta.getDatabaseProductVersion());
        metadata.setDriverName(dbMeta.getDriverName());
        metadata.setDriverVersion(dbMeta.getDriverVersion());

        List<String> tableNames = getTableNames(dbMeta);
        for (String tableName : tableNames) {
            TableInfo tableInfo = fetchTableInfo(dbMeta, tableName);
            metadata.addTable(tableInfo);
        }

        return metadata;
    }

    public List<String> getTableNames(java.sql.DatabaseMetaData dbMeta) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        ResultSet rs = dbMeta.getTables(null, null, null, new String[]{"TABLE"});
        while (rs.next()) {
            tableNames.add(rs.getString("TABLE_NAME"));
        }
        rs.close();
        return tableNames;
    }

    public TableInfo fetchTableInfo(java.sql.DatabaseMetaData dbMeta, String tableName) throws SQLException {
        TableInfo tableInfo = new TableInfo(tableName);

        ResultSet tableRs = dbMeta.getTables(null, null, tableName, new String[]{"TABLE"});
        if (tableRs.next()) {
            tableInfo.setSchema(tableRs.getString("TABLE_SCHEM"));
            tableInfo.setType(tableRs.getString("TABLE_TYPE"));
            tableInfo.setRemarks(tableRs.getString("REMARKS"));
        }
        tableRs.close();

        tableInfo.setColumns(getColumnInfo(dbMeta, tableName));
        tableInfo.setIndexes(getIndexInfo(dbMeta, tableName));
        tableInfo.setForeignKeys(getForeignKeyInfo(dbMeta, tableName));
        tableInfo.setRowCount(getTableRowCount(tableName));

        return tableInfo;
    }

    private List<ColumnInfo> getColumnInfo(java.sql.DatabaseMetaData dbMeta, String tableName) throws SQLException {
        List<ColumnInfo> columns = new ArrayList<>();
        Map<String, Boolean> primaryKeys = getPrimaryKeys(dbMeta, tableName);

        ResultSet rs = dbMeta.getColumns(null, null, tableName, null);
        while (rs.next()) {
            ColumnInfo column = new ColumnInfo();
            column.setName(rs.getString("COLUMN_NAME"));
            column.setType(rs.getString("TYPE_NAME"));
            column.setColumnSize(rs.getInt("COLUMN_SIZE"));
            column.setNullable(rs.getInt("NULLABLE") == java.sql.DatabaseMetaData.columnNullable);
            column.setPrimaryKey(primaryKeys.containsKey(column.getName().toLowerCase()));
            column.setDefaultValue(rs.getString("COLUMN_DEF"));
            column.setRemarks(rs.getString("REMARKS"));
            columns.add(column);
        }
        rs.close();
        return columns;
    }

    private Map<String, Boolean> getPrimaryKeys(java.sql.DatabaseMetaData dbMeta, String tableName) throws SQLException {
        Map<String, Boolean> primaryKeys = new HashMap<>();
        ResultSet rs = dbMeta.getPrimaryKeys(null, null, tableName);
        while (rs.next()) {
            primaryKeys.put(rs.getString("COLUMN_NAME").toLowerCase(), true);
        }
        rs.close();
        return primaryKeys;
    }

    private List<IndexInfo> getIndexInfo(java.sql.DatabaseMetaData dbMeta, String tableName) throws SQLException {
        Map<String, IndexInfo> indexMap = new HashMap<>();
        ResultSet rs = dbMeta.getIndexInfo(null, null, tableName, false, false);

        while (rs.next()) {
            String indexName = rs.getString("INDEX_NAME");
            if (indexName == null) continue;

            boolean nonUnique = rs.getBoolean("NON_UNIQUE");
            String type = rs.getString("TYPE");
            IndexInfo index = indexMap.computeIfAbsent(indexName, name -> {
                IndexInfo idx = new IndexInfo();
                idx.setName(name);
                idx.setTableName(tableName);
                idx.setUnique(!nonUnique);
                idx.setType(type);
                return idx;
            });
            String columnName = rs.getString("COLUMN_NAME");
            if (columnName != null) {
                index.addColumn(columnName);
            }
        }
        rs.close();
        return new ArrayList<>(indexMap.values());
    }

    private List<ForeignKeyInfo> getForeignKeyInfo(java.sql.DatabaseMetaData dbMeta, String tableName) throws SQLException {
        List<ForeignKeyInfo> foreignKeys = new ArrayList<>();
        ResultSet rs = dbMeta.getImportedKeys(null, null, tableName);

        while (rs.next()) {
            ForeignKeyInfo fk = new ForeignKeyInfo();
            fk.setName(rs.getString("FK_NAME"));
            fk.setTableName(tableName);
            fk.setColumnName(rs.getString("FKCOLUMN_NAME"));
            fk.setForeignTableName(rs.getString("PKTABLE_NAME"));
            fk.setForeignColumnName(rs.getString("PKCOLUMN_NAME"));
            fk.setUpdateRule(getRuleName(rs.getShort("UPDATE_RULE")));
            fk.setDeleteRule(getRuleName(rs.getShort("DELETE_RULE")));
            foreignKeys.add(fk);
        }
        rs.close();
        return foreignKeys;
    }

    private String getRuleName(short rule) {
        switch (rule) {
            case java.sql.DatabaseMetaData.importedKeyCascade: return "CASCADE";
            case java.sql.DatabaseMetaData.importedKeyRestrict: return "RESTRICT";
            case java.sql.DatabaseMetaData.importedKeySetNull: return "SET NULL";
            case java.sql.DatabaseMetaData.importedKeyNoAction: return "NO ACTION";
            case java.sql.DatabaseMetaData.importedKeySetDefault: return "SET DEFAULT";
            default: return "UNKNOWN";
        }
    }

    public long getTableRowCount(String tableName) throws SQLException {
        Connection conn = connectionManager.getConnection();
        String sql = "SELECT COUNT(*) FROM \"" + tableName + "\"";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }

    public TableInfo getTableInfo(String tableName) throws SQLException {
        Connection conn = connectionManager.getConnection();
        java.sql.DatabaseMetaData dbMeta = conn.getMetaData();
        return fetchTableInfo(dbMeta, tableName);
    }
}
