package com.sqlparse.optimizer;

import java.util.ArrayList;
import java.util.List;

public class OptimizationLog {
    private List<String> logEntries;
    private static OptimizationLog instance;

    private OptimizationLog() {
        this.logEntries = new ArrayList<>();
    }

    public static synchronized OptimizationLog getInstance() {
        if (instance == null) {
            instance = new OptimizationLog();
        }
        return instance;
    }

    public void log(String message) {
        logEntries.add(message);
    }

    public void log(String ruleName, String location, String action) {
        String message = String.format("[%s] %s at %s", ruleName, action, location);
        logEntries.add(message);
    }

    public List<String> getLogEntries() {
        return new ArrayList<>(logEntries);
    }

    public void clear() {
        logEntries.clear();
    }

    public boolean isEmpty() {
        return logEntries.isEmpty();
    }

    public String getFormattedLog() {
        StringBuilder sb = new StringBuilder();
        for (String entry : logEntries) {
            sb.append(entry).append("\n");
        }
        return sb.toString();
    }
}
