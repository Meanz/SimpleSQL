package com.meanz.simplesql.example;

import android.support.v7.app.ActionBarActivity;

import com.meanz.simplesql.SimpleSQL;
import com.meanz.simplesql.exception.SimpleSQLException;

import no.hin.dt.oblig3.Config;

/**
 * A class that show's an example implementation fo SimpleSQL
 * Created by Meanz on 06/03/2015.
 */
public class AndroidApp extends ActionBarActivity {

    /**
     * The name of the database
     */
    private static final String DATABASE_NAME = "myDatabase";

    /**
     * The name of the user table
     */
    public static final String USER_TABLE_NAME = "user";

    /**
     * The SimpleSQL instance
     */
    private SimpleSQL sql;

    /**
     * Called after onCreate or onRestart
     */
    @Override
    protected void onStart() {
        super.onStart();

        //Database initialization
        sql = new SimpleSQL(this, DATABASE_NAME, 1);
        try {
            sql.open();
        } catch (SimpleSQLException e) {
            e.printStackTrace();
        }

        try {

            //If the table exists drop it
            if (sql.tableExists(USER_TABLE_NAME)) {
                sql.dropTable(User.class);
            }
            //Create the table
            sql.createTable(User.class);

            //Populate the database with some data
            User insertUser = new User(Config.SQL);
            insertUser.setUsername("Meanz");
            insertUser.setUserFloatValue(5.1f);
            insertUser.insert(); //Insert's the user into the database and returns the row id

            //Print some information
            insertUser.printMe();

            //The id is automatically assigned to the primary key when inserted into the database
            //So it's a nice way to obtain the id of the element you inserted into the table
            //It can also be retrieved like this
            //int userId = insertUser.insert(); The function that assigns the value also returns it
            int userId = insertUser.getId();

            //You can load a single object using SimpleSQL.load
            //Provided that the class contains a constructor with one of these two patterns
            //Constructor(SimpleSQL) or Constructor() where the Constructor() pattern assumes
            //You are setting the SimpleSQL instance from a static instance to the super constructor.
            //Method two is by setting the primary key and then calling load from an instance
            User user = null;

            //Method one
            user = (User)sql.load(User.class, userId); //sql.load(Table, id)

            //Method two
            user = new User(Config.SQL);
            user.setId(userId);
            user.load();

            //Loaded!
            user.printMe();

        } catch (SimpleSQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * The activity is no longer visible
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * On application process kill
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        sql.close(); //Close our SimpleSQL connection here
    }

}
