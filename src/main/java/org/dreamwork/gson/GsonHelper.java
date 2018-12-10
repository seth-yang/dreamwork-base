package org.dreamwork.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.dreamwork.util.IDataCollection;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-11-7
 * Time: 上午10:49
 */
public class GsonHelper {
    public static Gson getGson () {
        return getGson (false);
    }

    public static Gson getGson (boolean excludeNonExpose) {
        return getGson (excludeNonExpose, false);
    }

    public static Gson getGson (boolean excludeNonExpose, boolean dateAsLong, TypeAdapterWrapper... adapters) {
        GsonBuilder builder = new GsonBuilder ()
                .registerTypeAdapter (IDataCollection.class, new DataCollectionTranslator ())
                .registerTypeHierarchyAdapter (Enum.class, new EnumTranslator ());

        if (dateAsLong)
            builder.registerTypeHierarchyAdapter (java.util.Date.class, new DateTranslator.LongDateTranslator ());
        else
            builder.registerTypeAdapter (java.util.Date.class, new DateTranslator.UtilDateTranslator ())
                   .registerTypeAdapter (java.sql.Date.class, new DateTranslator.SqlDateTranslator ())
                   .registerTypeAdapter (java.sql.Timestamp.class, new DateTranslator.TimestampTranslator ());

        for (TypeAdapterWrapper wrapper : adapters) {
            if (wrapper.adapterType == TypeAdapterWrapper.AdapterType.Normal)
                builder.registerTypeAdapter (wrapper.baseType, wrapper.adapter);
            else if (wrapper.getAdapterType () == TypeAdapterWrapper.AdapterType.Hierarchy)
                builder.registerTypeHierarchyAdapter (wrapper.baseType, wrapper.adapter);
        }
        if (excludeNonExpose)
            builder.excludeFieldsWithoutExposeAnnotation ();
        return builder.create ();
    }
}
