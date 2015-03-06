package com.meanz.simplesql.reflection.db;

/**
 * Created by Meanz on 05/03/2015.
 */
public class TableDefinition {

    /**
     * The name of the table
     */
    public String tableName;

    /**
     * The columns of the table
     */
    public Column columns[];

    /**
     * The primary key of this table
     */
    public Column primaryKey = null;

    /**
     * Empty CTOR
     */
    public TableDefinition()
    {

    }

}
