package org.dreamwork.config;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-3-29
 * Time: 下午3:08
 */
public interface IXMLConfigParser {
    /**
     *
     * @param section 配置章节
     * @return 解析后的对象
     * @throws ConfigParseException parse exception
     */
    Object parse (Element section) throws ConfigParseException ;
}