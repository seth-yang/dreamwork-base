package org.dreamwork.gson;

import com.google.gson.*;
import org.dreamwork.util.IDataCollection;
import org.dreamwork.util.ListDataCollection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * IDataCollection&lt;T&gt;:
int getPageSize ();
int getPageNo ();
int getTotalRows ();
int getTotalPages ();
void setData (Collection<T> data);
List<T> getData ();
 * {pageSize:10,pageNo:1,totalRows:18;data:[1,2,3]}
 */
public class DataCollectionTranslator implements JsonSerializer<IDataCollection<?>>, JsonDeserializer<IDataCollection<?>> {
    @SuppressWarnings ("unchecked")
    public IDataCollection<?> deserialize (JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.toString ().length () == 0)
            return null;
        ListDataCollection coll = new ListDataCollection ();
        JsonObject o = json.getAsJsonObject ();

        JsonElement e = o.get ("pageNo");
        if (e != null) coll.setPageNo (e.getAsInt ());

        e = o.get ("pageSize");
        if (e != null) coll.setPageSize (e.getAsInt ());

        e = o.get ("totalRows");
        if (e != null) coll.setTotalRows (e.getAsInt ());

        e = o.get ("data");
        if (e != null) {
            JsonArray array = e.getAsJsonArray ();
            Type targetType = Object.class;
            if (typeOfT instanceof ParameterizedType) {
                targetType = ((ParameterizedType) typeOfT).getActualTypeArguments () [0];
            }
            ArrayList list = new ArrayList ();
            for (int i = 0; i < array.size (); i ++) {
                Object target = context.deserialize (array.get (i), targetType);
                list.add (target);
            }
            coll.setData (list);
        }
        return coll;
    }

    public JsonElement serialize (IDataCollection<?> src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize (src, typeOfSrc);
    }
}