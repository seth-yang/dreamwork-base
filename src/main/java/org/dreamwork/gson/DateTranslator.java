package org.dreamwork.gson;

import com.google.gson.*;
import org.apache.log4j.Logger;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-5-17
 * Time: 上午11:22
 */
public class DateTranslator {
    private static final DateFormat df = new SimpleDateFormat ("yyyy-MM-dd");
    private static final DateFormat tf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");

    private static final Logger logger = Logger.getLogger (DateTranslator.class);

    public static class UtilDateTranslator implements JsonSerializer<java.util.Date>, JsonDeserializer<java.util.Date> {
        public java.util.Date deserialize (JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            String expression = json.getAsString ();
            try {
                return df.parse (expression);
            } catch (ParseException ex) {
                logger.warn (ex.getMessage (), ex);
                return null;
            }
        }

        public JsonElement serialize (java.util.Date src, Type type, JsonSerializationContext context) {
            return new JsonPrimitive (df.format (src));
        }
    }

    public static class SqlDateTranslator implements JsonSerializer<java.sql.Date>, JsonDeserializer<java.sql.Date> {
        public java.sql.Date deserialize (JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String expression = json.getAsString ();
            try {
                java.util.Date date = df.parse (expression);
                return new java.sql.Date (date.getTime ());
            } catch (ParseException ex) {
                logger.warn (ex.getMessage (), ex);
                return null;
            }
        }

        public JsonElement serialize (java.sql.Date src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive (df.format (src));
        }
    }

    public static class TimestampTranslator implements JsonSerializer<Timestamp>, JsonDeserializer<Timestamp> {
        public Timestamp deserialize (JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String expression = json.getAsString ();
            try {
                java.util.Date date = tf.parse (expression);
                return new Timestamp (date.getTime ());
            } catch (ParseException ex) {
                logger.warn (ex.getMessage (), ex);
                return null;
            }
        }

        public JsonElement serialize (Timestamp src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive (tf.format (src));
        }
    }

    public static class LongDateTranslator implements JsonSerializer<java.util.Date>, JsonDeserializer<java.util.Date> {
        @Override
        public Date deserialize (JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            long value = jsonElement.getAsLong ();
            return new Date (value);
        }

        @Override
        public JsonElement serialize (Date date, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive (date.getTime ());
        }
    }
}