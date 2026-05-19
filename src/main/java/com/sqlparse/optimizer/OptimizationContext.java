package com.sqlparse.optimizer;

import com.sqlparse.metadata.DatabaseMetadata;

import java.util.HashMap;
import java.util.Map;

public class OptimizationContext {
    private DatabaseMetadata databaseMetadata;
    private Map<String, Object> properties;

    public OptimizationContext() {
        this.properties = new HashMap<>();
    }

    public OptimizationContext(DatabaseMetadata databaseMetadata) {
        this.databaseMetadata = databaseMetadata;
        this.properties = new HashMap<>();
    }

    public DatabaseMetadata getDatabaseMetadata() {
        return databaseMetadata;
    }

    public void setDatabaseMetadata(DatabaseMetadata databaseMetadata) {
        this.databaseMetadata = databaseMetadata;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public void removeProperty(String key) {
        properties.remove(key);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
