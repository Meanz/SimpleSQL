package com.meanz.simplesql.reflection.db;

/**
 * @Ref https://www.sqlite.org/datatype3.html
 * Created by Meanz on 05/03/2015.
 */
public enum DataType {

    /**
     * INTEGER. The value is a signed integer, stored in 1, 2, 3, 4, 6, or 8 bytes depending on the magnitude of the value.
     */
    INTEGER("INTEGER", int.class, Integer.class),

    /**
     * TEXT. The value is a text string, stored using the database encoding (UTF-8, UTF-16BE or UTF-16LE).
     */
    TEXT("TEXT", String.class, char.class, Character.class),

    /**
     * REAL. The value is a floating point value, stored as an 8-byte IEEE floating point number.
     */
    REAL("REAL", float.class, Float.class),

    /**
     * BLOB. The value is a blob of data, stored exactly as it was input.
     */
    // NYI BLOB(Object.class) //The rest goes here? shall we say serialization?
    ;

    /**
     * The data type name for use with amongst CREATE TABLE
     */
    private String dataTypeName;

    /**
     * The supported classes for this datatype
     */
    private Class[] supportedClasses;

    /**
     * @param supportedClasses
     */
    private DataType(String dataTypeName, Class... supportedClasses) {
        this.dataTypeName = dataTypeName;
        this.supportedClasses = supportedClasses;
    }

    /**
     * Get the supported classes for this DataType
     *
     * @return
     */
    public Class[] getSupportedClasses() {
        return supportedClasses;
    }

    /**
     * Get a string representation of this Data Type
     * @return
     */
    public String toString() {
        return dataTypeName;
    }

}
