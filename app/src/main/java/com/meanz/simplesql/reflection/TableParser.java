package com.meanz.simplesql.reflection;

import com.meanz.simplesql.annotations.Ignore;
import com.meanz.simplesql.annotations.Name;
import com.meanz.simplesql.annotations.NotNull;
import com.meanz.simplesql.annotations.PrimaryKey;
import com.meanz.simplesql.annotations.TableName;
import com.meanz.simplesql.exception.SimpleSQLException;
import com.meanz.simplesql.reflection.db.Column;
import com.meanz.simplesql.reflection.db.Constraint;
import com.meanz.simplesql.reflection.db.DataType;
import com.meanz.simplesql.reflection.db.Table;
import com.meanz.simplesql.reflection.db.TableDefinition;

import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * Created by Meanz on 05/03/2015.
 */
public class TableParser {

    /**
     * Parses a table and get's a definition from it
     *
     * @param table
     * @return
     */
    public static TableDefinition parseTable(Class<? extends Table> table) throws SimpleSQLException {
        //Construct a new table definition instance
        TableDefinition tableDef = new TableDefinition();

        //Find the table name for this table definition
        TableName tableName = table.getAnnotation(TableName.class);
        if(tableName == null) {
            //throw new SimpleSQLException("No @TableName annotation for table with class name " + table.getName());
        }
        //Temporary fix TODO: Revise
        tableDef.tableName = tableName != null ? tableName.value() : table.getSimpleName();

        //Parse columns
        tableDef.columns = parseColumns(table.getFields());

        //Find the primary key
        for(Column column : tableDef.columns)
        {
            if(column.hasConstraint(Constraint.PRIMARY_KEY))
            {
                if(tableDef.primaryKey != null)
                {
                    throw new SimpleSQLException("Duplicate primary key for table " + tableDef.tableName);
                }
                tableDef.primaryKey = column;
            }
        }

        return tableDef;
    }

    /**
     * Convert fields into columns
     *
     * @param fields
     * @return
     */
    public static Column[] parseColumns(Field[] fields) throws SimpleSQLException {
        LinkedList<Column> columns = new LinkedList<Column>();
        for (Field field : fields) {
            //Search the field for annotations
            //Check the ignore annotation first
            if (field.getAnnotation(Ignore.class) != null) {
                continue;
            }

            //Local variable allocation
            String name = null;
            DataType dataType = null;

            //Create an instance of the column
            Column column = new Column();

            //TODO: Revise a dynamic method for sorting constraints
            //Primary Key Constraint
            if (field.getAnnotation(PrimaryKey.class) != null) {
                column.addConstraint(Constraint.PRIMARY_KEY);
            }
            //Not Null Constraint
            if (field.getAnnotation(NotNull.class) != null) {
                column.addConstraint(Constraint.NOT_NULL);
            }
            //Name annotation
            if(field.getAnnotation(Name.class) != null)
            {
                name = field.getAnnotation(Name.class).value();
            }

            //Find the data type for this field
            Class fieldType = field.getType();
            for (DataType dt : DataType.values()) {
                for (Class c : dt.getSupportedClasses()) {
                    if (fieldType == c) {
                        dataType = dt;
                        break;
                    }
                }
                if (dataType != null)
                    break; //No need to continue searching
            }

            //Assign the values to the column
            column.name = name != null ? name : field.getName();
            column.dataType = dataType;
            column.field = field;

            //Add the column to our column list
            columns.add(column);
        }
        //REVISE: A bit expensive
        Column[] _columns = new Column[columns.size()];
        columns.toArray(_columns);
        return _columns;
    }

}
