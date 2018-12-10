package org.dreamwork.util;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 11-8-4
 * Time: 下午4:24
 */
@Target ({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention (RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ConverterInfo {
    Class<?> type ();
    String format () default "";
    Class<? extends IConverter> converter () default DefaultConverter.class;
    Class<? extends IXMLConverter> xmlConverter () default IXMLConverter.NullXMLConverter.class;
    Class<? extends IJSONConverter> jsonConverter () default IJSONConverter.DefaultJSONConverter.class;
}