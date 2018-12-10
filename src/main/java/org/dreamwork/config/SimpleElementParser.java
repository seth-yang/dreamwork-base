package org.dreamwork.config;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-3-30
 * Time: 下午9:14
 */
public class SimpleElementParser implements IXMLConfigParser {
    /**
     * /WEB-INF/raychn.xml中的章节的解析器
     *
     * @param section 配置章节
     * @return 解析后的对象
     * @throws ConfigParseException parse exception
     */
    public ConfigEntry parse (Element section) throws ConfigParseException {
        NamedNodeMap list = section.getAttributes ();
        if (list.getLength () == 0) return null;

        ConfigEntry entry = new ConfigEntry ();
        for (int i = 0; i < list.getLength (); i ++) {
            Attr a = (Attr) list.item (i);
            entry.addValue (a.getName (), a.getValue ());
        }
        return entry;
    }
}
