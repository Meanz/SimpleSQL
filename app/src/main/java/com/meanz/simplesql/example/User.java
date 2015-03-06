package com.meanz.simplesql.example;

import com.meanz.simplesql.SimpleSQL;
import com.meanz.simplesql.annotations.Ignore;
import com.meanz.simplesql.annotations.Length;
import com.meanz.simplesql.annotations.NotNull;
import com.meanz.simplesql.annotations.TableName;
import com.meanz.simplesql.reflection.db.Table;

import no.hin.dt.oblig3.db.simple.annotation.PrimaryKey;

/**
 * Example class for a user table
 * Created by Meanz on 06/03/2015.
 */

/**
 * The @TableName annotation can be omitted and the table name will be the same as the class name
 */
@TableName(AndroidApp.USER_TABLE_NAME)
public class User extends Table {

    /**
     * The id of the user
     * The @PrimaryKey annotation makes the id unique and makes it autoincrement
     * Using the SQLite auto increment system
     *
     * @See https://www.sqlite.org/autoinc.html for more information
     */
    @PrimaryKey
    private int id;

    /**
     * The username of the user
     * The @NotNull annotation adds the NOT NULL constraint to the table field
     * The @Length(int) annotation adds a length constraint to the table field
     * By default the length size is dynamic, so this is a special purpose parameter
     */
    @NotNull
    @Length(50)
    public String username;

    /**
     * A float value stored in the user table
     * SimpleSQL along with SQLite only supports a few select data types
     * The BLOB datatype will in a later version be added to support generic datatypes
     * using java serialization
     *
     * @See https://www.sqlite.org/datatype3.html for more information about SQLite datatypes
     */
    public float userFloatValue;

    /**
     * Some random data that has no purpose but to show the usage of the @Ignore annotation
     * By adding @Ignore the table parser does not include this field in the table
     * And you can add variables to your table class without any problems.
     * However if you don't use the @Ignore annotation the field will be added to the table
     */
    @Ignore
    public String someRandomVariable;

    /**
     * Constructor that initializes the User table
     *
     * @param instance
     */
    public User(SimpleSQL instance) {
        //For example if you have a static SQL connection you can save it in a config class
        //Or by a static reference and send that to the super class
        //Or you can add it as a parameter as used here
        super(instance);
    }

    /**
     * Get the id of this user
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * Set the id of this user
     *
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Prints the information from this User
     */
    public void printMe() {
        System.err.println("Id: " + id + " Username: " + username + " userFloatValue: " + userFloatValue);
    }

}
