package org.dreamwork.config;

public interface IConfiguration {
    /**
     * 获取指定参数
     * @param key    参数名
     * @param params 可能的参数值
     * @return 字符串类型的参数值
     */
    String getString (String key, Object... params);

    String getString (String key, KeyValuePair<?>... params);

    int getInt (String key, int defaultValue);

    long getLong (String key, long defaultValue);

    double getDouble (String key, double defaultValue);

    boolean getBoolean (String key, boolean defaultValue);
}