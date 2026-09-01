package org.dreamwork.persistence;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DatabaseFieldDefinition implements Serializable {
    public String name;
    public Field field;
    public Method getter;
    public Method setter;
    public ISchemaField annotation;
}