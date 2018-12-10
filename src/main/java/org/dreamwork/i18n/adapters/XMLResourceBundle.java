package org.dreamwork.i18n.adapters;

import org.dreamwork.i18n.IResourceBundle;
import org.dreamwork.util.StringUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-10-19
 * Time: 下午3:31
 */
public class XMLResourceBundle implements IResourceBundle {
    private Map<String, String> resource = new HashMap<String, String> ();

    public XMLResourceBundle (Node parent) {
        NodeList list = parent.getChildNodes ();
        for (int i = 0; i < list.getLength (); i ++) {
            Node node = list.item (i);
            if (node.getNodeType () != Node.ELEMENT_NODE) continue;

            String name = node.getNodeName ();
            if ("item".equals (name)) {
                Element element = (Element) node;
                String key = element.getAttribute ("key").trim ();
                String value = element.getAttribute ("value").trim ();
                resource.put (key, value);
            } else {
                String value = node.getTextContent ();
                if (!StringUtil.isEmpty (value))
                    resource.put (name, value.trim ());
                else
                    resource.put (name, "");
            }
        }
    }

    /**
     * 获取适配器中，指定名称的区域资源。若为找到，则返回<code>defaultValue</code>
     *
     * @param name         资源名称
     * @param defaultValue 默认值
     * @return 资源
     */
    public String getString (String name, String defaultValue) {
        return resource.containsKey (name) ? resource.get (name) : defaultValue;
    }

    public boolean isResourcePresent (String name) {
        return resource.containsKey (name);
    }
}