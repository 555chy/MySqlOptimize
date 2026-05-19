package com.sqlparse.optimizer;

import java.time.LocalDateTime;

public class RuleApplication {
    private String ruleName;
    private String ruleDescription;
    private LocalDateTime applicationTime;
    private String location;
    private boolean applied;

    public RuleApplication(String ruleName, String ruleDescription, String location, boolean applied) {
        this.ruleName = ruleName;
        this.ruleDescription = ruleDescription;
        this.applicationTime = LocalDateTime.now();
        this.location = location;
        this.applied = applied;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleDescription() {
        return ruleDescription;
    }

    public void setRuleDescription(String ruleDescription) {
        this.ruleDescription = ruleDescription;
    }

    public LocalDateTime getApplicationTime() {
        return applicationTime;
    }

    public void setApplicationTime(LocalDateTime applicationTime) {
        this.applicationTime = applicationTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isApplied() {
        return applied;
    }

    public void setApplied(boolean applied) {
        this.applied = applied;
    }

    @Override
    public String toString() {
        return "RuleApplication{" +
                "ruleName='" + ruleName + '\'' +
                ", location='" + location + '\'' +
                ", applied=" + applied +
                '}';
    }
}
