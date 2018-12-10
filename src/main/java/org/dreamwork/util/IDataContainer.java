package org.dreamwork.util;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-1-20
 * Time: 17:20:47
 */
public interface IDataContainer extends Serializable {
    void setValue (int index, Object value) throws Exception;
    void setValue (String name, Object value) throws Exception;
    Object getValue (int index) throws Exception;
    Object getValue (String name) throws Exception;
}