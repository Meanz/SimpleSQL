package com.meanz.simplesql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.meanz.simplesql.exception.SimpleSQLException;
import com.meanz.simplesql.reflection.TableParser;
import com.meanz.simplesql.reflection.db.Column;
import com.meanz.simplesql.reflection.db.Constraint;
import com.meanz.simplesql.reflection.db.Table;
import com.meanz.simplesql.reflection.db.TableDefinition;
import com.meanz.simplesql.util.QueryBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.HashMap;

import no.hin.dt.oblig3.db.simple.AndroidSQLHelper;


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
     * Temporary
     */
    private AndroidSQLHelper helper;

    /**
     *
     */
    public SimpleSQL(Context context, String databaseName, int version) {
    }


    /**
     * Open the database
     *
     * @throws java.sql.SQLException
     */
    public void open() throws SQLException {
        writeDatabase = helper.getWritableDatabase();
        readDatabase = helper.getReadableDatabase();
    }

    public void close() {

    }

    /**
     * Load a single object using id in the WHERE clause
     * This is a somewhat more costly operation than Table.load() since this method uses
     * reflection to automatically invoke the constructors
     * @param clazz
     * @param id
     * @return
     */
    public Object load(Class<? extends Table> clazz, int id) throws SimpleSQLException {
        //Get the definition
        TableDefinition definition = getTableDefinition(clazz); //If this fails, it will throw

        //Find constructors
        Constructor[] constructors = clazz.getConstructors();

        Constructor paramConstructor = null;
        Constructor emptyConstructor = null;

        for (Constructor constructor : constructors) {
            Type[] types = constructor.getParameterTypes();
            if (types.length > 1)
                continue; //This one does not work
            if (types.length == 0) {
                //Don't break, we would preferably use a SimpleSQL constructor
                emptyConstructor = constructor;
                continue;
            }
            for (int i = 0; i < types.length; i++) {
                if (types[i] == SimpleSQL.class) {
                    //This one works
                    paramConstructor = constructor;
                    break;
                }
            }
            if (paramConstructor != null)
                break;
        }

        if (paramConstructor == null && emptyConstructor == null) {
            throw new SimpleSQLException("No valid constructors found for table " + definition.tableName);
        }

        //Prioritize SimpleSQL constructor over empty
        Table table = null;
        if (paramConstructor != null) {
            try {
                table = (Table) paramConstructor.newInstance(this);
            } catch (InstantiationException e) {
                e.printStackTrace();
                throw new SimpleSQLException(e.getMessage() + " :: for table table " + definition.tableName);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new SimpleSQLException(e.getMessage() + " :: for table table " + definition.tableName);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                throw new SimpleSQLException(e.getMessage() + " :: for table table " + definition.tableName);
            }
        } else if (emptyConstructor != null) {
            try {
                table = (Table) paramConstructor.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
                throw new SimpleSQLException(e.getMessage() + " :: for table table " + definition.tableName);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new SimpleSQLException(e.getMessage() + " :: for table table " + definition.tableName);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                throw new SimpleSQLException(e.getMessage() + " :: for table table " + definition.tableName);
            }
        } else {
            throw new SimpleSQLException("Bad code! @SimpleSQL#load(table, id)");
        }
        try {
            //TODO: Revise, don't think we need the null check
            if (definition.primaryKey == null) {
                throw new SimpleSQLException("PrimaryKey for table " + definition.tableName + " is null");
            }
            definition.primaryKey.setValue(table, id);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new SimpleSQLException(e.getMessage() + " :: for table table " + definition.tableName);
        }
        table.load();
        return table;
    }

    /**
     * @param clazz
     * @return
     * @throws SimpleSQLException
     */
    public int[] getRowIds(Class<? extends Table> clazz) throws SimpleSQLException {
        TableDefinition definition = getTableDefinition(clazz);
        if (definition.primaryKey == null) {
            throw new SimpleSQLException("No primary key for table " + definition.tableName);
        }
        //TODO: We need a system to determine whether the pk is an int or not, and we need an alternative to list rows
        //TODO: This can be achieved by forcing an id column on the table's, for example
        Cursor cursor = getReadDatabase().rawQuery("SELECT " + definition.primaryKey.name + " FROM " + definition.tableName, null);

        //Try to read the results
        int[] ids = new int[cursor.getCount()];
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            int id = cursor.getInt(cursor.getColumnIndex("_id"));
            ids[i] = id;
        }
        cursor.close();
        return ids;
    }

    /**
     * @param clazz
     * @return
     * @throws SimpleSQLException
     */
    public int[] getRowIdsWhere(Class<? extends Table> clazz, String key, String value) throws SimpleSQLException {
        TableDefinition definition = getTableDefinition(clazz);
        if (definition.primaryKey == null) {
            throw new SimpleSQLException("No primary key for table " + definition.tableName);
        }
        //TODO: We need a system to determine whether the pk is an int or not, and we need an alternative to list rows
        //TODO: This can be achieved by forcing an id column on the table's, for example
        Cursor cursor = getReadDatabase().rawQuery("SELECT " + definition.primaryKey.name + " FROM " + definition.tableName + " WHERE " + key + "= ?", new String[]{value});

        //Try to read the results
        int[] ids = new int[cursor.getCount()];
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
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
     *
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
