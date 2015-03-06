package com.meanz.simplesql.annotations;

import com.meanz.simplesql.reflection.db.Table;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a foreign key
 * Created by Meanz on 06/03/2015.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ForeignKey {

    /**
     * A reference to the foreign table
     * @return
     */
    Class<? extends Table> table();

    /**
     * The key that is used to connect to the foreign table
     * @return
     */
    int key();

}
