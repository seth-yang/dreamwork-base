package org.dreamwork.config;

public interface IConfiguration {
    /**
     * <p>获取指定参数</p>
     *
     * 当提供了 <code>params</code> 时， <code>key</code> 对应的格式化字符串必须符合 {@link java.lang.String#format(String, Object...)} 的规范
     * <p>获取值的顺序为
     * <ol>
     *     <li>System.getProperty (String)</li>
     *     <li>当前配置配置文件</li>
     *     <li>System.getEnv (String)</li>
     * </ol>
     * </p>
     * @param key    参数名
     * @param params 可能的参数值
     * @return 字符串类型的参数值
     */
    String getString (String key, Object... params);

    /**
     * <p>获取指定参数</p>
     * <i>关于格式化字符串</i>
     * 格式化字符串中允许出现 <code>${prop.name}</code> 格式的占位符，其搜索优先级如下：
     * <ol>
     *     <li>在提供的<code>params</code>中搜索</li>
     *     <li>在当前 <code>IConfiguration</code> 实例中 <i>通过调用</i> {@link #getString(String, Object...) getString(String)} 搜索</li>
     *     <li>在 <code>{@link System#getProperties()}</code> 中搜索</li>
     *     <li>在 <code>{@link System#getenv()}</code></li>
     *     <li>原样输出</li>
     * </ol>
     * @param key    参数名
     * @param params 可能的参数值
     * @return 字符串类型的参数值
     */
    String getString (String key, KeyValuePair<?>... params);

    /**
     * 获取指定的int类型的参数
     * @param key          参数名
     * @param defaultValue 若为找到名称的资源，或资源不是int类型时，返回的默认值
     * @return 参数值
     */
    int getInt (String key, int defaultValue);

    /**
     * 获取指定的 long 类型的参数
     * @param key          参数名
     * @param defaultValue 若为找到名称的资源，或资源不是 long 类型时，返回的默认值
     * @return 参数值
     */
    long getLong (String key, long defaultValue);

    /**
     * 获取指定的 double 类型的参数
     * @param key          参数名
     * @param defaultValue 若为找到名称的资源，或资源不是 double 类型时，返回的默认值
     * @return 参数值
     */
    double getDouble (String key, double defaultValue);

    /**
     * 获取指定的 boolean 类型的参数
     * @param key          参数名
     * @param defaultValue 若为找到名称的资源，或资源不是 boolean 类型时，返回的默认值
     * @return 参数值
     */
    boolean getBoolean (String key, boolean defaultValue);
}