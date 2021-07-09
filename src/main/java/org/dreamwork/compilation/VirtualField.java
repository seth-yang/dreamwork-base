package org.dreamwork.compilation;

/**
 * 虚拟字段
 *
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-2-2
 * Time: 11:36:56
 */
public class VirtualField extends VirtualType<VirtualField> {
    private String type;

    /**
     * 获取字段类型
     * @return 合法的 java 类型
     */
    public String getType () {
        return type;
    }

    /**
     * 设置字段类型
     * @param type 合法的 java 类型
     * @return this
     */
    public VirtualField setType (String type) {
        this.type = type;
        return this;
    }

    /**
     * 创建对该字段的读方法，即 getter 方法
     * @return getter 方法
     */
    public VirtualMethod getReader () {
        VirtualMethod vm = new VirtualMethod ();
        vm.setModifier (Modifier.PUBLIC);
        vm.setName ("get" + Character.toUpperCase (name.charAt (0)) + name.substring (1));
        vm.setReturnType (type);
        vm.addStatement ("return " + name);
        return vm;
    }

    /**
     * 创建对给字段的写方法，即 setter 方法
     * @return setter 方法
     */
    public VirtualMethod getWriter () {
        VirtualMethod vm = new VirtualMethod ();
        vm.setModifier (Modifier.PUBLIC);
        vm.setName ("set" + Character.toUpperCase (name.charAt (0)) + name.substring (1));
        vm.setReturnType (null);
        VirtualParameter vp = new VirtualParameter ();
        vp.setName (name);
        vp.setType (type);
        vm.addParameter (vp);
        vm.addStatement ("this." + name + " = " + name);
        return vm;
    }

    @Override
    public VirtualField setAbs (boolean abs) {
        throw new NotSupportedException ();
    }

    /**
     * 创建 java 代码  
     * @return java 代码
     */
    @Override
    public String toString () {
        StringBuilder builder = new StringBuilder ();
        if (!annotations.isEmpty ()) {
            for (VirtualAnnotation va : annotations) {
                builder.append ('\t').append (va).append (CRLF);
            }
        }
        builder.append ('\t').append (Modifier.expression[modifier]);
        if (still) {
            builder.append (" static");
        }
        if (fin) {
            builder.append (" final");
        }
        builder.append (' ').append (type).append (' ').append (name);
        return builder.toString ();
    }
}