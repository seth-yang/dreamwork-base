package org.dreamwork.config;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-3-29
 * Time: 下午4:09
 */
public class DefaultKeyValueParser implements IXMLConfigParser {
    /**
     * 默认的键-值对解析器，可解析 &lt;property name="" value="" /&gt;和
     * &lt;name&gt;value&lt;/name&gt;形式的节点
     *
     * @param section 节点
     * @return 解析后的对象
     */
    public Serializable parse (Element section) throws ConfigParseException {
        short type = section.getNodeType ();
        if (type == Node.ELEMENT_NODE) {
            Element e = (Element) section;
            String nodeName = e.getNodeName ();
            if ("property".equals (nodeName)) {
                if (e.hasAttribute ("name") && e.hasAttribute ("value")) {
                    String name = e.getAttribute ("name").trim ();
                    String value = e.getAttribute ("value").trim ();
                    return new KeyValuePair<String> (name, value);
                }
                throw new ConfigParseException (nodeName);
            } else {
                String content = e.getTextContent ();
                return new KeyValuePair<String> (nodeName, content);
            }
        } else if (type == Node.ATTRIBUTE_NODE) {
            Attr a = (Attr) section;
            return new KeyValuePair<String> (a.getName (), a.getValue ().trim ());
        } else {
            return section.getNodeValue ();
        }
    }
}