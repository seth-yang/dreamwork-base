package org.dreamwork.gson;

import com.google.gson.*;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-11-7
 * Time: 下午12:45
 */
public class EnumTranslator implements JsonSerializer<Enum<?>>, JsonDeserializer<Enum<?>> {
    public Enum<?> deserialize (JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if ((typeOfT instanceof Class) && Enum.class.isAssignableFrom ((Class<?>) typeOfT)) {
            Class<Enum> et = (Class<Enum>) typeOfT;
            return Enum.valueOf (et, json.getAsString ());
/*
            try {
                Class type = (Class) typeOfT;
                for (Field field : type.getDeclaredFields ()) {
                    Enum e = (Enum) field.get (null);
                    if (json.isJsonPrimitive ()) {
                        int index = json.getAsInt ();
                        if (e.ordinal () == index)
                            return e;
                    } else {
                        String expression = json.getAsString ();
                        if (expression.equalsIgnoreCase (e.name ()))
                            return e;
                    }
                }
            } catch (IllegalAccessException ex) {
                ex.printStackTrace ();
            }
*/
        }
        return context.deserialize (json, typeOfT);
    }

    public JsonElement serialize (Enum<?> src, Type typeOfSrc, JsonSerializationContext context) {
        return src == null ?
                context.serialize (null) :
                context.serialize (src.toString ());
    }
}
