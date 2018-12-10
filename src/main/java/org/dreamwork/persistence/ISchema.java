package org.dreamwork.persistence;

import java.lang.annotation.*;

/**
 * Created by game on 2016/9/7
 */
@Inherited
@Documented
@Target (ElementType.TYPE)
@Retention (RetentionPolicy.RUNTIME)
public @interface ISchema {
    Class<? extends DatabaseSchema> value ();
}