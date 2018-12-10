package org.dreamwork.config;

import org.dreamwork.util.StringUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
* Created by IntelliJ IDEA.
* User: seth.yang
* Date: 12-3-30
* Time: 下午9:50
* To change this template use File | Settings | File Templates.
*/
enum ConfigType {
    SimpleElement, ComplexElement, ContentElement, EmptyElement;

    public static ConfigType getElementType (Node node) {
        NodeList list = node.getChildNodes ();
        String content = node.getTextContent ();

//        if (list.getLength () == 0) return EmptyElement;

        int count = 0;
//        boolean hasElement = false;
        for (int i = 0; i < list.getLength (); i ++) {
            Node n = list.item (i);
            if (n.getNodeType () == Node.ELEMENT_NODE) {
                count ++;
//                hasElement = true;
            }
        }

        if (count == 0)
            return StringUtil.isEmpty (content) ? EmptyElement : ContentElement;
/*
        else if (count == 1)
            return hasElement ? ComplexElement : SimpleElement;
*/
        else
            return ComplexElement;
    }
}
