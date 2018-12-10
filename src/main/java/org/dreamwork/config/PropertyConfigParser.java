package org.dreamwork.config;

import org.w3c.dom.Element;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-3-30
 * Time: 下午9:08
 */
public class PropertyConfigParser implements IXMLConfigParser {
    /**
     * /WEB-INF/raychn.xml中的章节的解析器
     *
     * @param section 配置章节
     * @return 解析后的对象
     * @throws ConfigParseException
     *          parse exception
     */
    public KeyValuePair<String> parse (Element section) throws ConfigParseException {
        if (section.hasAttribute ("name")) {
            String attrName = section.getAttribute ("name").trim ();
            if (section.hasAttribute ("value")) {
                return new KeyValuePair<String> (attrName, section.getAttribute ("value").trim ());
            } else {
                return new KeyValuePair<String> (attrName, section.getTextContent ().trim ());
            }
        }
        throw new ConfigParseException ("Attribute 'name' of element 'property' must be specified!");
    }
}