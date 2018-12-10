package org.dreamwork.util;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-1-20
 * Time: 17:07:10
 */
public interface IListView extends Serializable {
    ICollection getCollection ();
}