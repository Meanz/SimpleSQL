package com.meanz.simplesql.reflection.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.meanz.simplesql.SimpleSQL;
import com.meanz.simplesql.annotations.Ignore;
import com.meanz.simplesql.exception.SimpleSQLException;

/**
 * Created by Meanz on 05/03/2015.
 */
public abstract class Table {

    /**
     * The table definition for this Table
     */
    @Ignore
    private TableDefinition tableDefinition;

    /**
     * The SQL instance for this table
     */
    @Ignore
    private SimpleSQL handler;

    /**
     * Creates a new table
     *
     * @param handler an instance of a SimpleSQL handler, forces the super call
     *                and makes operations on the table easier
     */
    public Table(SimpleSQL handler) {
        this.handler = handler;
        try {
            tableDefinition = SimpleSQL.getTableDefinition(this.getClass());
        } catch (SimpleSQLException e) {
            e.printStackTrace();
            //TODO: Better exception management
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Performs a SELECT on the table using the PrimaryKey in the WHERE clause
     */
    public void load() throws SimpleSQLException {
        try {
            Column pk = tableDefinition.primaryKey;
            if (pk == null) {
                throw new SimpleSQLException("No primary key for table " + tableDefinition.tableName);
            }
            String query = "SELECT * FROM " + tableDefinition.tableName + " WHERE " + pk.name + "=?";

            //Print debug information about the query
            System.err.println(query.replaceAll("[?]", pk.getValue(this).toString()));

            Cursor cursor = handler.getReadDatabase().rawQuery(query, new String[]{pk.getValue(this).toString()});
            cursor.moveToFirst();
            //Try to read the results
            if (cursor.getCount() > 0) {
                int idx = 0;
                //Move through all fields
                for (Column column : tableDefinition.columns) {
                    if (column.dataType == DataType.INTEGER) {
                        column.setValue(cursor.getInt(idx++), this);
                    } else if (column.dataType == DataType.REAL) {
                        column.setValue(cursor.getFloat(idx++), this);
                    } else if (column.dataType == DataType.TEXT) {
                        column.setValue(cursor.getString(idx++), this);
                    }
                }
            } else {
                //Throw some exception
                throw new SimpleSQLException("No results");
            }
            cursor.close();
        } catch (IllegalAccessException iae) {
            //Lazy exception conversion, but oh well
            //TODO: Make sure it's not lazy anymore!
            throw new SimpleSQLException("Error parsing table " + tableDefinition.tableName);
        }
    }

    /**
     * Performs an UPDATE on the table
     */
    public void save() throws SimpleSQLException {

    }

    /**
     * Performs an INSERT on the table
     *
     * @throws SimpleSQLException
     */
    public int insert() throws SimpleSQLException {
        Column[] columns = tableDefinition.columns;
        Column primaryKeyField = tableDefinition.primaryKey;
        ContentValues values = new ContentValues();
        for (int i = 0; i < columns.length; i++) {
            Column column = columns[i];
            try {
                if (column == primaryKeyField && column.dataType == DataType.INTEGER) //This is PK and AutoIncrement, can't change this
                    continue;
                values.put(column.name, column.getValue(this).toString());
            } catch (IllegalAccessException e) {
                throw new SimpleSQLException("Could not get value from field " + column.name);
            }
        }
        long insertId = handler.getWriteDatabase().insert(tableDefinition.tableName, null, values);
        try {
            //Hack
            primaryKeyField.setValue((int) insertId, this);
            System.err.println("INSERT INTO " + tableDefinition.tableName + " VALUES(" + values.toString() + ")");
        } catch (IllegalAccessException e) {
            throw new SimpleSQLException("Could not set insert value to " + primaryKeyField.name);
        }
        return (int) insertId; //TODO: The returned value is a long, once the auto increment id reaches INT.MAX_VALUE, Yes!
    }

    /**
     * Delete this row using the pk
     *
     * @throws SimpleSQLException
     */
    public void delete() throws SimpleSQLException {
        Column pk = tableDefinition.primaryKey;
        if (pk == null) {
            throw new SimpleSQLException("No primary key for table " + tableDefinition.tableName);
        }
        String query = "DELETE FROM " + tableDefinition.tableName + " WHERE " + pk.name + "=?";
        try {
            System.err.println(query.replaceAll("[?]", pk.getValue(this).toString()));
            //moveToFirst ref: http://stackoverflow.com/questions/7211158/why-does-a-delete-rawquery-need-a-movetofirst-in-order-to-actually-delete-the-ro
            handler.getWriteDatabase().rawQuery(query, new String[]{pk.getValue(this).toString()}).moveToFirst();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new SimpleSQLException("Could not get value from field " + pk.name);
        }
    }

}
