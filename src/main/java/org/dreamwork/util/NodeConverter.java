package org.dreamwork.util;

import org.w3c.dom.*;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 11-8-5
 * Time: 下午1:51
 */
public class NodeConverter {
    @SuppressWarnings ("unchecked")
    public static <T> T parseNode (Class<T> type, Node node) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        ConverterInfo ci = ReferenceUtil.getAnnotation (type, ConverterInfo.class);
        if (ci != null) {
            Class<? extends IXMLConverter> cc = ci.xmlConverter ();
            if (cc != IXMLConverter.NullXMLConverter.class) {
                IXMLConverter c = cc.newInstance ();
                return (T) c.convert (type, node);
            }
        }

        T t = type.newInstance ();
        NodeList children = node.getChildNodes ();
        IConverter converter = new DefaultConverter ();

        HashMap<String, List<Object>> pooledData = new HashMap<String, List<Object>> ();

        for (int i = 0; i < children.getLength (); i++) {
            Node child = children.item (i);
            if (child.getNodeType () != Node.ELEMENT_NODE) continue;
            String name = child.getNodeName ();

            ConverterInfo pi = ReferenceUtil.getAnnotation (type, ConverterInfo.class, name);
            if (pi != null) {
                Class<? extends IXMLConverter> xmlConverter = pi.xmlConverter ();
                if (xmlConverter != IXMLConverter.NullXMLConverter.class) {
                    IXMLConverter c = xmlConverter.newInstance ();
                    Class target = pi.type ();
                    Object v = c.convert (target, child);
                    ReferenceUtil.set (t, name, v);
                    continue;
                }
            }

            Method setter = ReferenceUtil.getSetter (type, name);
            Class<?> parameterType = setter.getParameterTypes ()[0];

            Object o;
            try {
                o = converter.cast (parameterType, child.getTextContent ());
                ReferenceUtil.set (t, name, o);
                continue;
            } catch (ClassCastException ex) {
                // ignore
            }

            if (parameterType.isArray () || Collection.class.isAssignableFrom (parameterType)) { // 数组或集合
                if (pi == null)
                    throw new UnsupportedOperationException ();
                Class<?> target = pi.type ();
                o = parseNode (target, child);
                List<Object> list = pooledData.get (name);
                if (list == null) {
                    list = new ArrayList<Object> ();
                    pooledData.put (name, list);
                }
                list.add (o);
            } else {
                o = parseNode (parameterType, child);
                ReferenceUtil.set (t, name, o);
            }
        }

        for (String name : pooledData.keySet ()) {
            Method setter = ReferenceUtil.getSetter (type, name);
            Class<?> parameterType = setter.getParameterTypes ()[0];
            ConverterInfo pi = ReferenceUtil.getAnnotation (type, ConverterInfo.class, name);
            Class<?> target = pi.type ();
            List<Object> list = pooledData.get (name);
            if (parameterType.isArray ()) {
                Object a = Array.newInstance (target, list.size ());
                for (int i = 0; i < list.size (); i++) {
                    Array.set (a, i, list.get (i));
                }
                ReferenceUtil.set (t, name, a);
            } else if (List.class.isAssignableFrom (parameterType)) {
                ReferenceUtil.set (t, name, list);
            } else if (Set.class.isAssignableFrom (parameterType)) {
                Set<Object> set = new HashSet<Object> ();
                set.addAll (list);
                ReferenceUtil.set (t, name, set);
            }
        }

        return t;
    }

    @SuppressWarnings ("unchecked")
    public static <T> void parseObject (Class<T> type, T bean, Node parent) {
        try {
            ConverterInfo ci = ReferenceUtil.getAnnotation (type, ConverterInfo.class);
            if (ci != null) {
                Class<? extends IXMLConverter> cc = ci.xmlConverter ();
                if (cc != IXMLConverter.NullXMLConverter.class) {
                    IXMLConverter c = cc.newInstance ();
                    c.convert (type, bean, parent);
                    return;
                }
            }

            Document doc = parent.getOwnerDocument ();
            IConverter converter = new DefaultConverter ();

            if (type.isArray () || bean instanceof Collection) {
                Collection c;
                if (type.isArray ()) {
                    List list = new ArrayList ();
                    int length = Array.getLength (bean);
                    for (int i = 0; i < length; i ++) list.add (Array.get (bean, i));
                    c = list;
                } else {
                    c = (Collection) bean;
                }

                for (Object o : c) {
                    Element e = doc.createElement ("item");
                    parent.appendChild (e);
                    if (o instanceof CharSequence || ReferenceUtil.isKnownType (o.getClass ()))
                        e.appendChild (doc.createCDATASection (xmlEncode (o.toString ())));
                    else
                        parseObject ((Class<Object>) o.getClass (), o, e);
                }

                return;
            }


            Collection<String> propNames = ReferenceUtil.getPropertyNames (type);
            for (String name : propNames) {
                Element e = doc.createElement (name);

                Object v = ReferenceUtil.get (bean, name);
                if (v == null) continue;
                Class target = v.getClass ();

                ConverterInfo pi = ReferenceUtil.getAnnotation (type, ConverterInfo.class, name);
                if (pi != null) {
                    Class<? extends IXMLConverter> cc = pi.xmlConverter ();
                    if (cc != IXMLConverter.NullXMLConverter.class) {
                        IXMLConverter c = cc.newInstance ();
                        parent.appendChild (e);
                        c.convert (target, v, e);
                        continue;
                    }
                }
                if (v instanceof CharSequence) {
                    parent.appendChild (e);
                    e.appendChild (doc.createCDATASection (xmlEncode (v.toString ())));
                } else if (ReferenceUtil.isKnownType (target)) {
                    parent.appendChild (e);
                    e.appendChild (doc.createTextNode (converter.cast (v)));
                } else if (target.isArray ()) {
                    int length = Array.getLength (v);
                    Collection<Object> c = new ArrayList<Object> (length);
                    for (int i = 0; i < length; i++)
                        c.add (Array.get (v, i));
                    parseCollection (name, c, parent);
                } else if (Collection.class.isAssignableFrom (target)) {
                    parseCollection (name, (Collection<Object>) v, parent);
                } else {
                    parent.appendChild (e);
                    parseObject (target, v, e);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }

    @SuppressWarnings ("unchecked")
    private static void parseCollection (String name, Collection<Object> c, Node parent) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        Document doc = parent.getOwnerDocument ();
        for (Object o : c) {
            Class t = o.getClass ();
            Node p = doc.createElement (name);
            parent.appendChild (p);
            parseObject (t, o, p);
        }
    }

    private static String xmlEncode (String text) {
        return StringUtil.isEmpty (text) ? "" :
               text.replace ("&", "&amp;").replace ("<", "&lt;").replace (">", "&gt;");
    }
}