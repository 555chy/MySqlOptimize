package com.sqlparse.optimizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseFunctionMapper {

    public static String mapFunctions(String sql, DatabaseType dbType) {
        String result = sql;
        
        result = mapNullFunctions(result, dbType);
        result = mapDateFunctions(result, dbType);
        result = mapStringFunctions(result, dbType);
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

    private static String convertLimitToOracleRownum(String sql) {
        Pattern pattern = Pattern.compile("(?i)(SELECT\\s+.+?)(\\s+FROM\\s+.+?)(\\s+LIMIT\\s+(\\d+))(\\s*.*)");
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String selectPart = matcher.group(1);
            String fromPart = matcher.group(2);
            String limitNum = matcher.group(4);
            String rest = matcher.group(5);
            return "SELECT * FROM (SELECT " + selectPart.substring(7) + ", ROWNUM as rn" + fromPart + rest + ") WHERE rn <= " + limitNum;
        }
        return sql;
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
}
