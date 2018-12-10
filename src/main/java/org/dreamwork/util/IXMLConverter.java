package org.dreamwork.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 11-8-5
 * Time: 下午12:10
 */
@Deprecated
public interface IXMLConverter<T> {
    public T convert (Class<T> type, Node node);
    public void convert (Class<T> type, T bean, Node parent);

    public class NullXMLConverter implements IXMLConverter<Object> {
        public Object convert (Class<Object> type, Node node) {
            throw new UnsupportedOperationException ();
        }

        public void convert (Class<Object> type, Object bean, Node parent) {
            throw new UnsupportedOperationException ();
        }
    }
}
