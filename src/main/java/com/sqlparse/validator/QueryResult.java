package com.sqlparse.validator;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QueryResult {
    private List<String> columnNames;
    private List<RowData> rows;

    public QueryResult() {
        this.columnNames = new ArrayList<>();
        this.rows = new ArrayList<>();
    }

    public static QueryResult fromResultSet(ResultSet resultSet) throws SQLException {
        QueryResult queryResult = new QueryResult();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            queryResult.columnNames.add(metaData.getColumnName(i));
        }

        while (resultSet.next()) {
            RowData row = new RowData();
            for (int i = 1; i <= columnCount; i++) {
                row.addValue(resultSet.getObject(i));
            }
            queryResult.rows.add(row);
        }

        return queryResult;
    }

    public void addColumn(String columnName) {
        this.columnNames.add(columnName);
    }

    public void addRow(RowData row) {
        this.rows.add(row);
    }

    public List<String> getColumnNames() {
        return new ArrayList<>(columnNames);
    }

    public List<RowData> getRows() {
        return new ArrayList<>(rows);
    }

    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    public RowData getRow(int index) {
        return rows.get(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryResult that = (QueryResult) o;
        if (getColumnCount() != that.getColumnCount()) return false;
        if (getRowCount() != that.getRowCount()) return false;
        for (int i = 0; i < rows.size(); i++) {
            if (!rows.get(i).equals(that.rows.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = columnNames.hashCode();
        result = 31 * result + rows.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "QueryResult{" +
                "columnCount=" + columnNames.size() +
                ", rowCount=" + rows.size() +
                ", columnNames=" + columnNames +
                '}';
    }
}
