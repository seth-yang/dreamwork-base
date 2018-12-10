package org.dreamwork.i18n;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-10-19
 * Time: 下午3:49
 */
public abstract class AbstractResourceManager implements IResourceManager {
    protected final Map<String, IResourceAdapter> softCache = new HashMap<String, IResourceAdapter> ();
    protected SortedSet<LocaleWarp> locales = new TreeSet<LocaleWarp> ();

    /**
     * 获取资源绑定器的工厂方法.
     * <p>根据给定的基础包名和默认的区域设置，以某种方式返回资源绑定适配器.
     * 若<code>defaultLocale</code>无法载入，实现类应该抛出异常
     *
     * @param baseName      基础语言包
     * @param defaultLocale 默认区域设置
     */
    public void createResourceAdapter (String baseName, Locale defaultLocale) {
        IResourceAdapter adapter = createResourceAdapter (baseName);
        adapter.loadResources (baseName, defaultLocale);
        softCache.put (baseName, adapter);
    }

    /**
     * 获取资源绑定器的工厂方法.
     * <p>根据给定的基础包名和默认的区域设置，以某种方式返回资源绑定器.
     * 若<code>defaultLocale</code>无法载入，实现类应该抛出异常
     *
     * @param baseName 基础语言包
     * @return IResourceBundle 实例
     */
    public IResourceAdapter getResourceAdapter (String baseName) {
        return softCache.get (baseName);
    }

    /**
     * 获取当前资源管理器所有支持的区域设置
     *
     * @return 所有支持的区域设置
     */
    public Collection<LocaleWarp> getSupportedLocales () {
        if (locales.size () == 0) {
            for (IResourceAdapter adapter : softCache.values ()) {
                locales.addAll (adapter.getSupportedLocales ());
            }
        }
        return locales;
    }

    public void mergeAdapter (String baseName, IResourceAdapter adapter) {
        softCache.put (baseName, adapter);
    }

    /**
     * 根据给定的资源名称，返回特定的 IResourceAdapter 实现
     * @param baseName 资源名称
     * @return 资源适配器
     */
    protected abstract IResourceAdapter createResourceAdapter (String baseName);
}