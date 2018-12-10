package org.dreamwork.config;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * Created with IntelliJ IDEA.
 * User: seth
 * Date: 13-1-14
 * Time: 下午2:48
 */
public class ListParser implements IXMLConfigParser{
    @Override
    public Object parse (Element section) throws ConfigParseException {
        ListEntry listEntry = new ListEntry ();
        NodeList list = section.getElementsByTagName ("item");
        for (int i = 0; i < list.getLength (); i ++) {
            Element e = (Element) list.item (i);
            ConfigEntry entry = new ConfigEntry ();
            NamedNodeMap map = e.getAttributes ();
            for (int j = 0; j < map.getLength (); j ++) {
                Attr a = (Attr) map.item (j);
                entry.addValue (a.getName (), a.getValue ());
            }
            listEntry.addValue (entry);
        }

        NamedNodeMap map = section.getAttributes ();
        for (int i = 0; i < map.getLength (); i ++) {
            Attr attribute = (Attr) map.item (i);
            if ("name".equals (attribute.getName ())) continue;
            String name = attribute.getName ();
            String value = attribute.getValue ();
            listEntry.addValue (name, value);
        }
        return listEntry;
    }
}