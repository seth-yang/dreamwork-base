package org.dreamwork.persistence;

import java.lang.annotation.*;
import java.sql.Types;

/**
 * Created by game on 2016/9/7
 */
@Inherited
@Documented
@Target ({ElementType.FIELD, ElementType.METHOD})
@Retention (RetentionPolicy.RUNTIME)
public @interface ISchemaField {
    String value () default ""; // alias for name
    String name () default "";
    boolean id () default false;
    boolean autoincrement () default false;
    int type () default Types.VARCHAR;
}