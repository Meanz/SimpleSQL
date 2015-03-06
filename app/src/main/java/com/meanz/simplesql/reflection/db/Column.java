package com.meanz.simplesql.reflection.db;

import com.meanz.simplesql.exception.SimpleSQLException;

import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * Created by Meanz on 05/03/2015.
 */
public class Column {

    /**
     * The name of this column
     */
    public String name;

    /**
     * The datatype of this column
     */
    public DataType dataType;

    /**
     * The constraints of this column
     */
    public LinkedList<Constraint> constraints = new LinkedList<Constraint>();

    /**
     * The field associated with this column
     */
    public Field field;

    /**
     * Get an int from this field
     *
     * @param instance
     * @return
     */
    public int getInt(Object instance) throws IllegalAccessException {
        return field.getInt(instance);
    }

    /**
     * Get a float from this field
     *
     * @param instance
     * @return
     * @throws IllegalAccessException
     */
    public float getFloat(Object instance) throws IllegalAccessException {
        return field.getFloat(instance);
    }

    /**
     * Get a string from this field
     *
     * @param instance
     * @return
     */
    public String getString(Object instance) throws IllegalAccessException {
        return (String) field.get(instance);
    }

    /**
     * Get a value from this field
     *
     * @param instance
     * @return
     */
    public Object getValue(Object instance) throws IllegalAccessException {
        return field.get(instance);
    }

    /**
     * Set the value of this field
     *
     * @param instance
     * @param value
     */
    public void setValue(Object instance, Object value) throws IllegalAccessException {
        field.set(instance, value);
    }

    /**
     * Add a constraint to this column,
     * REVISE: This will only be reflected upon CreateTable
     *
     * @param constraint
     */
    public void addConstraint(Constraint constraint) throws SimpleSQLException {
        if (constraints.contains(constraint))
            throw new SimpleSQLException("Duplicate constraint " + constraint.toString());
        constraints.add(constraint);
    }

    /**
     * Check whether this column has a constraint or not
     *
     * @param constraint
     * @return
     */
    public boolean hasConstraint(Constraint constraint) {
        for (Constraint c : constraints) {
            if (c == constraint)
                return true;
        }
        return false;
    }

}
