package com.sqlparse.validator;

import com.sqlparse.metadata.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ResultValidator {
    private DatabaseConnectionManager connectionManager;

    public ResultValidator(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public ValidationResult validate(String sql1, String sql2) {
        ValidationResult result = new ValidationResult();

        try {
            QueryResultWithStats stats1 = executeQueryWithStats(sql1);
            QueryResultWithStats stats2 = executeQueryWithStats(sql2);
            
            if (!stats1.isSuccess()) {
                result.setConsistent(false);
                result.setMessage("原始SQL执行异常: " + stats1.getErrorMessage());
                return result;
            }
            if (!stats2.isSuccess()) {
                result.setConsistent(false);
                result.setMessage("优化SQL执行异常: " + stats2.getErrorMessage());
                return result;
            }
            
            QueryResult queryResult1 = stats1.getQueryResult();
            QueryResult queryResult2 = stats2.getQueryResult();

            result.setQueryResult1(queryResult1);
            result.setQueryResult2(queryResult2);

            if (queryResult1.getColumnCount() != queryResult2.getColumnCount()) {
                result.setConsistent(false);
                result.setMessage(String.format("列数不一致：SQL1有%d列，SQL2有%d列", 
                    queryResult1.getColumnCount(), queryResult2.getColumnCount()));
                return result;
            }

            if (queryResult1.getRowCount() != queryResult2.getRowCount()) {
                result.setConsistent(false);
                result.setMessage(String.format("行数不一致：SQL1有%d行，SQL2有%d行", 
                    queryResult1.getRowCount(), queryResult2.getRowCount()));
                return result;
            }

            for (int i = 0; i < queryResult1.getRowCount(); i++) {
                RowData row1 = queryResult1.getRow(i);
                RowData row2 = queryResult2.getRow(i);
                
                if (!row1.equals(row2)) {
                    result.setConsistent(false);
                    result.setMessage(String.format("第%d行数据不一致", i + 1));
                    result.setFirstInconsistentRow(i);
                    return result;
                }
            }

            result.setConsistent(true);
            result.setMessage("结果完全一致");

        } catch (Exception e) {
            result.setConsistent(false);
            result.setMessage("SQL执行异常: " + e.getMessage());
        }

        return result;
    }

    public QueryResultWithStats executeQueryWithStats(String sql) {
        QueryResultWithStats stats = new QueryResultWithStats();
        long startTime = System.nanoTime();
        
        try {
            Connection connection = connectionManager.getConnection();
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                QueryResult queryResult = QueryResult.fromResultSet(resultSet);
                stats.setQueryResult(queryResult);
                stats.setSuccess(true);
            }
        } catch (SQLException e) {
            stats.setSuccess(false);
            stats.setErrorMessage(e.getMessage());
        }
        
        stats.setExecutionTimeNanos(System.nanoTime() - startTime);
        return stats;
    }
    
    public static class QueryResultWithStats {
        private QueryResult queryResult;
        private long executionTimeNanos;
        private boolean success;
        private String errorMessage;
        
        public QueryResult getQueryResult() {
            return queryResult;
        }
        
        public void setQueryResult(QueryResult queryResult) {
            this.queryResult = queryResult;
        }
        
        public long getExecutionTimeNanos() {
            return executionTimeNanos;
        }
        
        public void setExecutionTimeNanos(long executionTimeNanos) {
            this.executionTimeNanos = executionTimeNanos;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    public static class ValidationResult {
        private boolean consistent;
        private String message;
        private QueryResult queryResult1;
        private QueryResult queryResult2;
        private int firstInconsistentRow = -1;

        public boolean isConsistent() {
            return consistent;
        }

        public void setConsistent(boolean consistent) {
            this.consistent = consistent;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public QueryResult getQueryResult1() {
            return queryResult1;
        }

        public void setQueryResult1(QueryResult queryResult1) {
            this.queryResult1 = queryResult1;
        }

        public QueryResult getQueryResult2() {
            return queryResult2;
        }

        public void setQueryResult2(QueryResult queryResult2) {
            this.queryResult2 = queryResult2;
        }

        public int getFirstInconsistentRow() {
            return firstInconsistentRow;
        }

        public void setFirstInconsistentRow(int firstInconsistentRow) {
            this.firstInconsistentRow = firstInconsistentRow;
        }
    }
}
