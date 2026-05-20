package com.sqlparse.optimizer;

public enum DatabaseType {
    MYSQL("MySQL"),
    POSTGRESQL("PostgreSQL"),
    ORACLE("Oracle"),
    SQLITE("SQLite");

    private final String displayName;

    DatabaseType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DatabaseType fromJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null) {
            return MYSQL;
        }
        String url = jdbcUrl.toLowerCase();
        if (url.contains(":mysql:")) {
            return MYSQL;
        } else if (url.contains(":postgresql:")) {
            return POSTGRESQL;
        } else if (url.contains(":oracle:")) {
            return ORACLE;
        } else if (url.contains(":sqlite:")) {
            return SQLITE;
        }
        return MYSQL;
    }
}
