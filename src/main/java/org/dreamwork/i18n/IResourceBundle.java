package org.dreamwork.i18n;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-10-18
 * Time: 下午5:09
 */
public interface IResourceBundle {
    /**
     * 获取适配器中，指定名称的区域资源。若为找到，则返回<code>defaultValue</code>
     * @param name 资源名称
     * @param defaultValue 默认值
     * @return 资源
     */
    String getString (String name, String defaultValue);

    boolean isResourcePresent (String name);
}