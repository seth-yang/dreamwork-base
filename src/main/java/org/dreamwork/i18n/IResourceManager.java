package org.dreamwork.i18n;

import java.util.Collection;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-10-18
 * Time: 下午4:03
 */
public interface IResourceManager {
    /**
     * 获取资源绑定器的工厂方法.
     * <p>根据给定的基础包名和默认的区域设置，以某种方式返回资源绑定器.
     * 若<code>defaultLocale</code>无法载入，实现类应该抛出异常
     * @param baseName 基础语言包
     * @param defaultLocale 默认区域设置. 若请求资源时，无法找到对应的资源是，应返回该区域设置所对应的资源包
     */
    void createResourceAdapter (String baseName, Locale defaultLocale);

    /**
     * 获取资源绑定器的工厂方法.
     * <p>根据给定的基础包名和默认的区域设置，以某种方式返回资源绑定器.
     * 若<code>defaultLocale</code>无法载入，实现类应该抛出异常
     * @param baseName 基础语言包
     * @return IResourceBundle 实例
     */
    IResourceAdapter getResourceAdapter (String baseName);

    /**
     * 获取当前资源管理器所有支持的区域设置
     * @return 所有支持的区域设置
     */
    Collection<LocaleWarp> getSupportedLocales ();
}