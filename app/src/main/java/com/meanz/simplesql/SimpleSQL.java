package com.meanz.simplesql;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.meanz.simplesql.reflection.TableParser;
import com.meanz.simplesql.reflection.db.Column;
import com.meanz.simplesql.reflection.db.Constraint;
import com.meanz.simplesql.reflection.db.Table;
import com.meanz.simplesql.reflection.db.TableDefinition;
import com.meanz.simplesql.util.QueryBuilder;

import java.util.HashMap;

import no.hin.dt.oblig3.db.simple.exception.SimpleSQLException;

/**
 * Created by Meanz on 05/03/2015.
 */
public class SimpleSQL {

    /**
     * Cached Table Definitions
     */
    private final static HashMap<String, TableDefinition> definitions = new HashMap<String, TableDefinition>();

    /**
     * Create or get a TableDefinition for the given Class<? extends table>
     *
     * @param clazz The class to be inspected
     * @return
     */
    public static TableDefinition getTableDefinition(Class<? extends Table> clazz) throws SimpleSQLException {
        String key = clazz.getCanonicalName();
        if (definitions.containsKey(key)) {
            return definitions.get(key);
        }
        TableDefinition def = TableParser.parseTable(clazz);
        //Put the definition into the cache
        definitions.put(key, def);
        return def;
    }

    /**
     * The read database for this SimpleSQL instance
     */
    private SQLiteDatabase readDatabase;

    /**
     * The write database for this SimpleSQL instance
     */
    private SQLiteDatabase writeDatabase;

    /**
     *
     */
    public SimpleSQL() {
    }

    /**
     *
     * @param clazz
     * @return
     * @throws SimpleSQLException
     */
    public int[] getRowIds(Class<? extends Table> clazz) throws SimpleSQLException {
        TableDefinition definition = getTableDefinition(clazz);
        if(definition.primaryKey == null)
        {
            throw new SimpleSQLException("No primary key for table " + definition.tableName);
        }
        //TODO: We need a system to determine whether the pk is an int or not, and we need an alternative to list rows
        //TODO: This can be achieved by forcing an id column on the table's, for example
        Cursor cursor = getReadDatabase().rawQuery("SELECT " + definition.primaryKey.name + " FROM " + definition.tableName, null);

        //Try to read the results
        int[] ids = new int[cursor.getCount()];
        cursor.moveToFirst();
        for(int i=0; i < cursor.getCount(); i++)
        {
            cursor.moveToPosition(i);
            int id = cursor.getInt(cursor.getColumnIndex("_id"));
            ids[i] = id;
        }
        cursor.close();
        return ids;
    }

    /**
     *
     * @param clazz
     * @return
     * @throws SimpleSQLException
     */
    public int[] getRowIdsWhere(Class<? extends Table> clazz, String key, String value) throws SimpleSQLException {
        TableDefinition definition = getTableDefinition(clazz);
        if(definition.primaryKey == null)
        {
            throw new SimpleSQLException("No primary key for table " + definition.tableName);
        }
        //TODO: We need a system to determine whether the pk is an int or not, and we need an alternative to list rows
        //TODO: This can be achieved by forcing an id column on the table's, for example
        Cursor cursor = getReadDatabase().rawQuery("SELECT " + definition.primaryKey.name + " FROM " + definition.tableName + " WHERE " + key + "= ?", new String[] { value });

        //Try to read the results
        int[] ids = new int[cursor.getCount()];
        cursor.moveToFirst();
        for(int i=0; i < cursor.getCount(); i++)
        {
            cursor.moveToPosition(i);
            int id = cursor.getInt(cursor.getColumnIndex("_id"));
            ids[i] = id;
        }
        cursor.close();
        return ids;
    }

    /**
     * Creates a table from the given Table class
     *
     * @param clazz
     * @throws SimpleSQLException
     */
    public void createTable(Class<? extends Table> clazz) throws SimpleSQLException {
        TableDefinition definition = getTableDefinition(clazz);
        QueryBuilder qb = new QueryBuilder();
        qb.add("CREATE TABLE " + definition.tableName + " (");
        Column[] columns = definition.columns;
        Column primaryKeyField = definition.primaryKey;
        for (int i = 0; i < columns.length; i++) {
            Column column = columns[i];
            boolean lastField = (i == columns.length - 1);
            //TODO: Update datatype name conversion
            //TODO: Update constraint system
            qb.add(column.name + " "
                    + (column.dataType) + ""
                    + (column.hasConstraint(Constraint.NOT_NULL) ? " NOT NULL" : "")
                    + (column.hasConstraint(Constraint.PRIMARY_KEY) ? "  PRIMARY KEY" : "")
                    + (column.hasConstraint(Constraint.AUTO_INCREMENT) ? " AUTO_INCREMENT" : "")
                    + (!lastField ? "," : ""));

        }
        qb.add(")");
        System.err.println(qb.get());
        getWriteDatabase().execSQL(qb.get());
    }

    /**
     * Drops the given table
     * @param clazz
     * @throws SimpleSQLException
     */
    public void dropTable(Class<? extends Table> clazz) throws SimpleSQLException {
        TableDefinition definition = getTableDefinition(clazz);
        if (tableExists(definition.tableName)) {
            getWriteDatabase().execSQL("DROP TABLE  " + definition.tableName);
        } else {
            throw new SimpleSQLException("Table " + definition.tableName + " doesn't exist, can't drop it.");
        }
    }

    /**
     * Ref: http://stackoverflow.com/questions/3058909/how-does-one-check-if-a-table-exists-in-an-android-sqlite-database
     * Check if a table exists
     *
     * @param tableName
     * @return
     */
    public boolean tableExists(String tableName) {
        Cursor cursor = getReadDatabase().rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }


    /**
     * Get the readable database
     *
     * @return
     */
    public SQLiteDatabase getReadDatabase() {
        return readDatabase;
    }

    /**
     * Get the writable database
     *
     * @return
     */
    public SQLiteDatabase getWriteDatabase() {
        return writeDatabase;
    }

}
