package org.dreamwork.i18n;

import java.util.Collection;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-10-18
 * Time: 下午4:05
 */
public interface IResourceAdapter {
    /**
     * 装载资源.
     * <p>根据给定的资源名称载入资源。<code>defaultLocale</code>用于指定强制载入的语种，若该语种资源无法载入，
     * 实现类应当抛出异常。</p>
     * <p>实现类可根据自身关联资源的情况选择所有语种全部载入或延迟载入.</p>
     * @param baseName 基础语言包
     * @param defaultLocale 默认区域设置. 若请求资源时，无法找到对应的资源是，应返回该区域设置所对应的资源包
     * @throws MissingResourceException 若<code>defaultLocale</code>无法载入，将抛出异常
     */
    void loadResources (String baseName, Locale defaultLocale) throws MissingResourceException;

    /**
     * 获取适配器中，指定名称的区域资源。若为找到，则返回空字符串
     * @param locale 指定区域
     * @param name 资源名称
     * @return 资源
     */
    String getString (Locale locale, String name);

    /**
     * 获取适配器中，指定名称的区域资源。若为找到，则返回空字符串.
     * 资源值中可出现 {n} 占位符，用于在资源获取时进行动态替换，序号从0开始
     * @param locale 指定区域
     * @param name 资源名称
     * @param parameters 替换占位符的列表。如果占位符个数和该参数的个数不匹配，
     *                   可能抛出 java.lang.ArrayIndexOutOfBoundsException 异常
     * @return 替换后的资源
     */
    String getString (Locale locale, String name, Object... parameters);

    /**
     * 获取适配器中，指定名称的区域资源。若为找到，则返回<code>defaultValue</code>
     * @param locale 指定区域
     * @param name 资源名称
     * @param defaultValue 默认值
     * @return 资源
     */
    String getString (Locale locale, String name, String defaultValue);

    /**
     * 获取适配器中，指定名称的区域资源。若为找到，则返回<code>defaultValue</code>
     * 资源值中可出现 {n}占位符，用于在资源获取时进行动态替换
     * @param locale 指定区域
     * @param name 资源名称
     * @param defaultValue 默认值
     * @param parameters 替换占位符的列表。如果占位符个数和该参数的个数不匹配，
     *                   可能抛出 java.lang.ArrayIndexOutOfBoundsException 异常
     * @return 替换后的资源
     */
    String getString (Locale locale, String name, String defaultValue, Object... parameters);

    Collection<LocaleWarp> getSupportedLocales ();
}