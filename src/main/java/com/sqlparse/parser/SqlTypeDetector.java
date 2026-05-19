package com.sqlparse.parser;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

public class SqlTypeDetector {

    public enum SqlType {
        SELECT, INSERT, UPDATE, DELETE, CREATE_TABLE, CREATE_INDEX, CREATE_VIEW,
        ALTER, DROP, TRUNCATE, MERGE, REPLACE, COMMENT, UNKNOWN
    }

    public static SqlType detectType(Statement statement) {
        if (statement == null) {
            return SqlType.UNKNOWN;
        }

        if (statement instanceof Select) {
            return SqlType.SELECT;
        } else if (statement instanceof Insert) {
            return SqlType.INSERT;
        } else if (statement instanceof Update) {
            return SqlType.UPDATE;
        } else if (statement instanceof Delete) {
            return SqlType.DELETE;
        } else if (statement instanceof CreateTable) {
            return SqlType.CREATE_TABLE;
        } else if (statement instanceof CreateIndex) {
            return SqlType.CREATE_INDEX;
        } else if (statement instanceof CreateView) {
            return SqlType.CREATE_VIEW;
        } else if (statement instanceof Alter) {
            return SqlType.ALTER;
        } else if (statement instanceof Drop) {
            return SqlType.DROP;
        } else if (statement instanceof Truncate) {
            return SqlType.TRUNCATE;
        } else if (statement instanceof Merge) {
            return SqlType.MERGE;
        } else if (statement instanceof Replace) {
            return SqlType.REPLACE;
        } else if (statement instanceof Comment) {
            return SqlType.COMMENT;
        }

        return SqlType.UNKNOWN;
    }

    public static String getTypeName(SqlType type) {
        switch (type) {
            case SELECT:
                return "SELECT";
            case INSERT:
                return "INSERT";
            case UPDATE:
                return "UPDATE";
            case DELETE:
                return "DELETE";
            case CREATE_TABLE:
                return "CREATE TABLE";
            case CREATE_INDEX:
                return "CREATE INDEX";
            case CREATE_VIEW:
                return "CREATE VIEW";
            case ALTER:
                return "ALTER";
            case DROP:
                return "DROP";
            case TRUNCATE:
                return "TRUNCATE";
            case MERGE:
                return "MERGE";
            case REPLACE:
                return "REPLACE";
            case COMMENT:
                return "COMMENT";
            default:
                return "UNKNOWN";
        }
    }

    public static boolean isSelectStatement(Statement statement) {
        return detectType(statement) == SqlType.SELECT;
    }

    public static boolean isDmlStatement(Statement statement) {
        SqlType type = detectType(statement);
        return type == SqlType.SELECT || type == SqlType.INSERT || 
               type == SqlType.UPDATE || type == SqlType.DELETE || 
               type == SqlType.MERGE || type == SqlType.REPLACE;
    }

    public static boolean isDdlStatement(Statement statement) {
        SqlType type = detectType(statement);
        return type == SqlType.CREATE_TABLE || type == SqlType.CREATE_INDEX || 
               type == SqlType.CREATE_VIEW || type == SqlType.ALTER || 
               type == SqlType.DROP || type == SqlType.TRUNCATE || 
               type == SqlType.COMMENT;
    }
}
