package com.sqlparse.optimizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseFunctionMapper {

    public static String mapFunctions(String sql, DatabaseType dbType) {
        String result = sql;
        
        result = mapNullFunctions(result, dbType);
        result = mapDateFunctions(result, dbType);
        result = mapStringFunctions(result, dbType);
        result = mapStringConcat(result, dbType);
        result = mapTopSyntax(result, dbType);
        result = mapFetchFirstSyntax(result, dbType);
        result = mapLimitSyntax(result, dbType);
        
        return result;
    }

    private static String mapNullFunctions(String sql, DatabaseType dbType) {
        String result = sql;
        
        switch (dbType) {
            case ORACLE:
                result = ifnullToNvl(result);
                result = coalesceToNvl(result);
                break;
            case POSTGRESQL:
                result = ifnullToCoalesce(result);
                break;
            case MYSQL:
            case SQLITE:
                result = nvlToIfnull(result);
                result = coalesceToIfnull(result);
                break;
        }
        
        return result;
    }

    private static String mapDateFunctions(String sql, DatabaseType dbType) {
        String result = sql;
        
        switch (dbType) {
            case ORACLE:
                result = dateAddToOracleInterval(result);
                result = dateDiffToOracle(result);
                result = nowToSysdate(result);
                result = currentDateToSysdate(result);
                break;
            case POSTGRESQL:
                result = dateAddToPostgresInterval(result);
                result = dateDiffToPostgres(result);
                result = nowToNow(result);
                result = currentDateToCurrentDate(result);
                break;
            case MYSQL:
                result = dateAddToDateAdd(result);
                result = dateDiffToDateDiff(result);
                result = nowToNow(result);
                result = currentDateToCurrentDate(result);
                break;
            case SQLITE:
                result = dateAddToSqliteDate(result);
                result = dateDiffToSqliteJulianday(result);
                result = nowToNow(result);
                result = currentDateToDateNow(result);
                break;
        }
        
        return result;
    }

    private static String mapStringFunctions(String sql, DatabaseType dbType) {
        String result = sql;
        
        switch (dbType) {
            case ORACLE:
                result = substringToSubstr(result);
                result = charLengthToLength(result);
                result = concatToConcat(result);
                break;
            case POSTGRESQL:
                result = charLengthToLength(result);
                break;
            case MYSQL:
            case SQLITE:
                result = substrToSubstring(result);
                result = lengthToCharLength(result);
                break;
        }
        
        return result;
    }

    private static String mapStringConcat(String sql, DatabaseType dbType) {
        String result = sql;
        
        switch (dbType) {
            case ORACLE:
                result = concatToConcat(result);
                break;
            case POSTGRESQL:
                break;
            case MYSQL:
            case SQLITE:
                result = doublePipeToConcat(result);
                break;
        }
        
        return result;
    }
    
    private static String mapTopSyntax(String sql, DatabaseType dbType) {
        String result = sql;
        
        switch (dbType) {
            case ORACLE:
                result = topToOracleRownum(result);
                break;
            case POSTGRESQL:
                result = topToLimit(result);
                break;
            case MYSQL:
            case SQLITE:
                result = topToLimit(result);
                break;
        }
        
        return result;
    }
    
    private static String mapFetchFirstSyntax(String sql, DatabaseType dbType) {
        String result = sql;
        
        switch (dbType) {
            case ORACLE:
                break;
            case POSTGRESQL:
                break;
            case MYSQL:
            case SQLITE:
                result = fetchFirstToLimit(result);
                break;
        }
        
        return result;
    }
    
    private static String mapLimitSyntax(String sql, DatabaseType dbType) {
        if (dbType == DatabaseType.ORACLE) {
            return convertLimitToOracleRownum(sql);
        }
        return sql;
    }

    private static String ifnullToNvl(String sql) {
        return sql.replaceAll("(?i)IFNULL\\s*\\(", "NVL(");
    }

    private static String coalesceToNvl(String sql) {
        Pattern pattern = Pattern.compile("(?i)COALESCE\\s*\\(([^,]+),\\s*([^)]+)\\)");
        Matcher matcher = pattern.matcher(sql);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "NVL(" + matcher.group(1) + ", " + matcher.group(2) + ")");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String nvlToIfnull(String sql) {
        return sql.replaceAll("(?i)NVL\\s*\\(", "IFNULL(");
    }

    private static String coalesceToIfnull(String sql) {
        Pattern pattern = Pattern.compile("(?i)COALESCE\\s*\\(([^,]+),\\s*([^)]+)\\)");
        Matcher matcher = pattern.matcher(sql);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "IFNULL(" + matcher.group(1) + ", " + matcher.group(2) + ")");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String ifnullToCoalesce(String sql) {
        return sql.replaceAll("(?i)IFNULL\\s*\\(", "COALESCE(");
    }

    private static String dateAddToOracleInterval(String sql) {
        Pattern pattern = Pattern.compile("(?i)DATE_ADD\\s*\\(\\s*([^,]+)\\s*,\\s*INTERVAL\\s+(\\d+)\\s+(DAY|MONTH|YEAR|HOUR|MINUTE|SECOND)\\s*\\)");
        Matcher matcher = pattern.matcher(sql);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String unit = matcher.group(3).toUpperCase();
            String intervalUnit = getOracleIntervalUnit(unit);
            matcher.appendReplacement(sb, matcher.group(1) + " + INTERVAL '" + matcher.group(2) + "' " + intervalUnit);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String dateDiffToOracle(String sql) {
        Pattern pattern = Pattern.compile("(?i)DATEDIFF\\s*\\(\\s*(DAY|MONTH|YEAR)\\s*,\\s*([^,]+)\\s*,\\s*([^)]+)\\s*\\)");
        Matcher matcher = pattern.matcher(sql);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String unit = matcher.group(1).toUpperCase();
            if ("DAY".equals(unit)) {
                matcher.appendReplacement(sb, "TRUNC(" + matcher.group(3) + ") - TRUNC(" + matcher.group(2) + ")");
            } else if ("MONTH".equals(unit)) {
                matcher.appendReplacement(sb, "MONTHS_BETWEEN(" + matcher.group(3) + ", " + matcher.group(2) + ")");
            } else if ("YEAR".equals(unit)) {
                matcher.appendReplacement(sb, "EXTRACT(YEAR FROM " + matcher.group(3) + ") - EXTRACT(YEAR FROM " + matcher.group(2) + ")");
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String dateAddToPostgresInterval(String sql) {
        Pattern pattern = Pattern.compile("(?i)DATE_ADD\\s*\\(\\s*([^,]+)\\s*,\\s*INTERVAL\\s+(\\d+)\\s+(DAY|MONTH|YEAR|HOUR|MINUTE|SECOND)\\s*\\)");
        Matcher matcher = pattern.matcher(sql);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String unit = matcher.group(3).toLowerCase();
            matcher.appendReplacement(sb, matcher.group(1) + " + INTERVAL '" + matcher.group(2) + " " + unit + "'");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String dateDiffToPostgres(String sql) {
        Pattern pattern = Pattern.compile("(?i)DATEDIFF\\s*\\(\\s*(DAY|MONTH|YEAR)\\s*,\\s*([^,]+)\\s*,\\s*([^)]+)\\s*\\)");
        Matcher matcher = pattern.matcher(sql);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String unit = matcher.group(1).toUpperCase();
            matcher.appendReplacement(sb, "(" + matcher.group(3) + " - " + matcher.group(2) + ")::INTERVAL");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String dateAddToDateAdd(String sql) {
        return sql;
    }

    private static String dateDiffToDateDiff(String sql) {
        return sql;
    }

    private static String dateAddToSqliteDate(String sql) {
        Pattern pattern = Pattern.compile("(?i)DATE_ADD\\s*\\(\\s*([^,]+)\\s*,\\s*INTERVAL\\s+(\\d+)\\s+(DAY|MONTH|YEAR|HOUR|MINUTE|SECOND)\\s*\\)");
        Matcher matcher = pattern.matcher(sql);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String unit = matcher.group(3).toLowerCase();
            String sqliteUnit = getSqliteDateUnit(unit);
            matcher.appendReplacement(sb, "DATE(" + matcher.group(1) + ", '+" + matcher.group(2) + " " + sqliteUnit + "')");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String dateDiffToSqliteJulianday(String sql) {
        Pattern pattern = Pattern.compile("(?i)DATEDIFF\\s*\\(\\s*(DAY|MONTH|YEAR)\\s*,\\s*([^,]+)\\s*,\\s*([^)]+)\\s*\\)");
        Matcher matcher = pattern.matcher(sql);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String unit = matcher.group(1).toUpperCase();
            if ("DAY".equals(unit)) {
                matcher.appendReplacement(sb, "CAST(JULIANDAY(" + matcher.group(3) + ") - JULIANDAY(" + matcher.group(2) + ") AS INTEGER)");
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String nowToSysdate(String sql) {
        return sql.replaceAll("(?i)\\bNOW\\s*\\(\\s*\\)", "SYSDATE");
    }

    private static String nowToNow(String sql) {
        return sql;
    }

    private static String currentDateToSysdate(String sql) {
        return sql.replaceAll("(?i)\\bCURRENT_DATE\\b", "SYSDATE");
    }

    private static String currentDateToCurrentDate(String sql) {
        return sql;
    }

    private static String currentDateToDateNow(String sql) {
        return sql.replaceAll("(?i)\\bCURRENT_DATE\\b", "DATE('now')");
    }

    private static String substringToSubstr(String sql) {
        return sql.replaceAll("(?i)SUBSTRING\\s*\\(", "SUBSTR(");
    }

    private static String substrToSubstring(String sql) {
        return sql.replaceAll("(?i)SUBSTR\\s*\\(", "SUBSTRING(");
    }

    private static String charLengthToLength(String sql) {
        return sql.replaceAll("(?i)CHAR_LENGTH\\s*\\(", "LENGTH(");
    }

    private static String lengthToCharLength(String sql) {
        return sql.replaceAll("(?i)LENGTH\\s*\\(", "CHAR_LENGTH(");
    }

    private static String concatToConcat(String sql) {
        Pattern pattern = Pattern.compile("(?i)([^\\s]+)\\s*\\|\\|\\s*([^\\s]+)");
        Matcher matcher = pattern.matcher(sql);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "CONCAT(" + matcher.group(1) + ", " + matcher.group(2) + ")");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String getOracleIntervalUnit(String unit) {
        switch (unit) {
            case "DAY": return "DAY";
            case "MONTH": return "MONTH";
            case "YEAR": return "YEAR";
            case "HOUR": return "HOUR";
            case "MINUTE": return "MINUTE";
            case "SECOND": return "SECOND";
            default: return "DAY";
        }
    }

    private static String getSqliteDateUnit(String unit) {
        switch (unit) {
            case "day": return "days";
            case "month": return "months";
            case "year": return "years";
            case "hour": return "hours";
            case "minute": return "minutes";
            case "second": return "seconds";
            default: return "days";
        }
    }
    
    private static String doublePipeToConcat(String sql) {
        String result = sql;
        Pattern pattern = Pattern.compile("(?i)([^\\s]+)\\s*\\|\\|\\s*([^\\s]+)");
        Matcher matcher = pattern.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "CONCAT(" + matcher.group(1) + ", " + matcher.group(2) + ")");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    private static String topToOracleRownum(String sql) {
        String result = sql;
        Pattern pattern = Pattern.compile("(?i)(SELECT)\\s+TOP\\s+(\\d+)\\s+(.+?)(\\s+FROM\\s+.+)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(result);
        if (matcher.find()) {
            String selectPart = matcher.group(1);
            String topNum = matcher.group(2);
            String columns = matcher.group(3);
            String fromRest = matcher.group(4);
            result = "SELECT * FROM (SELECT " + selectPart.substring(7) + " " + columns + fromRest + ") WHERE ROWNUM <= " + topNum;
        }
        return result;
    }
    
    private static String topToLimit(String sql) {
        String result = sql;
        Pattern pattern = Pattern.compile("(?i)(SELECT)\\s+TOP\\s+(\\d+)\\s+(.+)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(result);
        if (matcher.find()) {
            String selectPart = matcher.group(1);
            String topNum = matcher.group(2);
            String rest = matcher.group(3);
            result = "SELECT " + selectPart.substring(7) + " " + rest + " LIMIT " + topNum;
        }
        return result;
    }
    
    private static String fetchFirstToLimit(String sql) {
        String result = sql;
        Pattern pattern = Pattern.compile("(?i)(SELECT\\s+.+?)\\s+FETCH\\s+FIRST\\s+(\\d+)\\s+(ROW|ROWS)\\s+ONLY", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(result);
        if (matcher.find()) {
            String selectPart = matcher.group(1);
            String limitNum = matcher.group(2);
            result = selectPart + " LIMIT " + limitNum;
        }
        return result;
    }
    
    private static String convertLimitToOracleRownum(String sql) {
        String result = sql;
        
        Pattern limitOffsetPattern = Pattern.compile("(?i)(SELECT\\s+)(.+?)(\\s+FROM\\s+.+?)(\\s+LIMIT\\s+(\\d+)\\s+OFFSET\\s+(\\d+))(\\s*.*)", Pattern.DOTALL);
        Matcher limitOffsetMatcher = limitOffsetPattern.matcher(result);
        if (limitOffsetMatcher.find()) {
            String selectPart = limitOffsetMatcher.group(1);
            String columns = limitOffsetMatcher.group(2);
            String fromPart = limitOffsetMatcher.group(3);
            String limitNum = limitOffsetMatcher.group(5);
            String offsetNum = limitOffsetMatcher.group(6);
            String rest = limitOffsetMatcher.group(7);
            int lowerBound = Integer.parseInt(offsetNum) + 1;
            int upperBound = Integer.parseInt(offsetNum) + Integer.parseInt(limitNum);
            result = "SELECT * FROM (" +
                    "SELECT a.*, ROWNUM rn FROM (" +
                    "SELECT " + columns + fromPart + rest +
                    ") a WHERE ROWNUM <= " + upperBound +
                    ") WHERE rn >= " + lowerBound;
            return result;
        }
        
        Pattern limitPattern = Pattern.compile("(?i)(SELECT\\s+)(.+?)(\\s+FROM\\s+.+?)(\\s+LIMIT\\s+(\\d+))(\\s*.*)", Pattern.DOTALL);
        Matcher limitMatcher = limitPattern.matcher(result);
        if (limitMatcher.find()) {
            String selectPart = limitMatcher.group(1);
            String columns = limitMatcher.group(2);
            String fromPart = limitMatcher.group(3);
            String limitNum = limitMatcher.group(5);
            String rest = limitMatcher.group(6);
            result = "SELECT * FROM (SELECT " + columns + fromPart + rest + ") WHERE ROWNUM <= " + limitNum;
        }
        
        return result;
    }
}
