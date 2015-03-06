package com.meanz.simplesql.util;

/**
 * Created by Meanz on 04/03/2015.
 * Pretty much a string builder, but it provides a level of abstraction
 * At the moment it's just an alias for a string builder
 */
public class QueryBuilder
{
    private StringBuilder sb;

    public QueryBuilder()
    {
        sb = new StringBuilder();
    }

    public void add(String val)
    {
        sb.append(val);
    }

    public void add(String val, String... args)
    {
        sb.append(String.format(val, args));
    }

    public String get()
    {
        return sb.toString();
    }

}
