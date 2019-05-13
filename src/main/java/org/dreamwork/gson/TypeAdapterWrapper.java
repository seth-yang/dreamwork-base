package org.dreamwork.gson;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 14-3-28
 * Time: 下午5:00
 */
public class TypeAdapterWrapper {
    Class<?> baseType;
    AdapterType adapterType;
    Object adapter;

    public enum AdapterType {
        Normal, Hierarchy
    }

    public TypeAdapterWrapper () {
    }

    public TypeAdapterWrapper (Class<?> baseType, AdapterType adapterType, Object adapter) {
        this.baseType = baseType;
        this.adapterType = adapterType;
        this.adapter = adapter;
    }

    public Class<?> getBaseType () {
        return baseType;
    }

    public void setBaseType (Class<?> baseType) {
        this.baseType = baseType;
    }

    public AdapterType getAdapterType () {
        return adapterType;
    }

    public void setAdapterType (AdapterType adapterType) {
        this.adapterType = adapterType;
    }

    public Object getAdapter () {
        return adapter;
    }

    public void setAdapter (Object adapter) {
        this.adapter = adapter;
    }
}