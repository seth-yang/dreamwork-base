package org.dreamwork.util;

import java.util.Map;

/**
 * Created by game on 2017/7/3
 */
public interface ITypedMap extends Map<String, Object> {
    <T> T value (String key);
}
