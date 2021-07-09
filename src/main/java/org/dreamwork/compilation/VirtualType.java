package org.dreamwork.compilation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seth.yang on 2017/6/27
 */
@SuppressWarnings ("all")
public abstract class VirtualType<T extends VirtualType> {
    public static final String CRLF = String.format ("%n");

    protected int     modifier;
    protected boolean still, fin, abs;
    protected String  name;
    protected List<VirtualAnnotation> annotations = new ArrayList<> ();

    public T addAnnotation (VirtualAnnotation va) {
        annotations.add (va);
        return (T) this;
    }

    /**
     * 获取参数名称
     * @return 合法的 java 标识符
     */
    public String getName () {
        return name;
    }

    /**
     * 设置参数名称
     * @param name 合法的 java 标识符
     * @return this
     */
    public T setName (String name) {
        this.name = name;
        return (T) this;
    }

    /**
     * 获取虚拟类的访问修饰符
     *
     * @return 访问修饰符
     */
    public int getModifier () {
        return modifier;
    }

    /**
     * 设置虚拟类的访问修饰符
     *
     * @param modifier 访问修饰符
     * @return this
     */
    public T setModifier (int modifier) {
        this.modifier = modifier;
        return (T) this;
    }

    public boolean isStill () {
        return still;
    }

    public T setStill (boolean still) {
        this.still = still;
        return (T) this;
    }

    public boolean isFin () {
        return fin;
    }

    public T setFin (boolean fin) {
        this.fin = fin;
        return (T) this;
    }

    public boolean isAbs () {
        return abs;
    }

    public T setAbs (boolean abs) {
        this.abs = abs;
        return (T) this;
    }
}