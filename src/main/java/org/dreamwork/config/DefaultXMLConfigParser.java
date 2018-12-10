package org.dreamwork.config;

import org.w3c.dom.*;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-3-29
 * Time: 下午4:46
 */
public class DefaultXMLConfigParser implements IXMLConfigParser {
    private ConfigEntry parent;

    private static final PropertyConfigParser parser = new PropertyConfigParser ();
    private static final SimpleElementParser sep = new SimpleElementParser ();

    public DefaultXMLConfigParser (ConfigEntry parent) {
        this.parent = parent;
    }

    /**
     * 缺省的解析器
     *
     *
     * @param section 配置章节
     * @return 解析后的对象
     * @throws ConfigParseException
     *          parse exception
     */
    public ConfigEntry parse (Element section) throws ConfigParseException {
        parseElementAttributes (section, parent);

        NodeList list = section.getChildNodes ();
        for (int i = 0; i < list.getLength (); i ++) {
            Node node = list.item (i);
            String name = node.getNodeName ();
            short type = node.getNodeType ();
            if (type != Node.ELEMENT_NODE) continue;

            if ("property".equals (name)) {
                KeyValuePair<String> pair = parser.parse ((Element) node);
                parent.addValue (pair.getName (), pair.getValue ());
                continue;
            }

            Element e = (Element) node;
            ConfigType ct = ConfigType.getElementType (e);
            ConfigEntry entry;
            switch (ct) {
                case EmptyElement:
//                    parent.addValue ();
                    entry = sep.parse (e);
                    if (entry != null)
                        parent.addValue (e.getNodeName (), entry);
                    break;
//                case SimpleElement:
                case ContentElement:
                    parent.addValue (e.getNodeName (), e.getTextContent ());
                    break;
                case ComplexElement:
                    entry = new ConfigEntry ();
                    new DefaultXMLConfigParser (entry).parse (e);
                    parent.addValue (e.getNodeName (), entry);
                    break;
            }
        }
        return parent;
    }

    private void parseElementAttributes (Element e, ConfigEntry entry) {
        NamedNodeMap list = e.getAttributes ();
        for (int i = 0; i < list.getLength (); i ++) {
            Attr a = (Attr) list.item (i);
            entry.addValue (a.getName (), a.getValue ());
        }
    }
}