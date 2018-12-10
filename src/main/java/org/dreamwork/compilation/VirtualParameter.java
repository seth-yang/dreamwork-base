package org.dreamwork.compilation;

/**
 * 虚拟参数
 *
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-2-2
 * Time: 11:41:24
 */
public class VirtualParameter extends VirtualType<VirtualParameter> {
    private String type;


    /**
     * 获取参数类型
     * @return 合法的 java 类型
     */
    public String getType () {
        return type;
    }

    /**
     * 设置参数类型
     * @param type 合法的 java 类型
     */
    public VirtualParameter setType (String type) {
        this.type = type;
        return this;
    }

    @Override
    public VirtualParameter setStill (boolean still) {
        throw new NotSupportedException ();
    }

    @Override
    public VirtualParameter setAbs (boolean abs) {
        throw new NotSupportedException ();
    }

    /**
     * 生成 java 代码
     * @return  java 代码
     */
    @Override
    public String toString () {
        StringBuilder builder = new StringBuilder ();
        for (VirtualAnnotation va : annotations) {
            builder.append (va).append (CRLF);
        }
        if (fin) {
            builder.append ("final ");
        }
        return builder.append (type).append (' ').append (name).toString ();
    }
}