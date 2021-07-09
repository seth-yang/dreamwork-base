package org.dreamwork.compilation;

import org.dreamwork.util.StringUtil;

import java.util.List;
import java.util.ArrayList;

/**
 * 虚拟类，用于中间代码生成。
 *
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-2-2
 * Time: 11:03:40
 */
public class VirtualClass extends VirtualType<VirtualClass> {
    private String packageName = "";
    private String superClassName;
    private final List<String> interfaces = new ArrayList<> ();
    private final List<VirtualField> fields = new ArrayList<> ();
    private final List<VirtualMethod> methods = new ArrayList<> ();
    private final List<String> imports = new ArrayList<> ();
    private final List<VirtualConstruction> constructions = new ArrayList<> ();
    private final List<String> codeSnip = new ArrayList<> ();
    private ClassType type = ClassType.CLASS;

    /**
     * 获取虚拟类包名
     * @return 虚拟类包名
     */
    public String getPackageName () {
        return packageName;
    }

    /**
     * 设置虚拟类包名
     * @param packageName 虚拟类包名
     * @return this
     */
    public VirtualClass setPackageName (String packageName) {
        this.packageName = packageName;
        return this;
    }

    public String getReferenceName () {
        if (StringUtil.isEmpty (packageName)) {
            return name;
        } else {
            return packageName + '.' + name;
        }
    }

    /**
     * 获取虚拟类的父类名称
     * @return  父类名称
     */
    public String getSuperClassName () {
        return superClassName;
    }

    /**
     * 设置虚拟类父类名称
     * @param superClassName 父类名称
     * @return this
     */
    public VirtualClass setSuperClassName (String superClassName) {
        this.superClassName = superClassName;
        return this;
    }

    /**
     * 向虚拟类中添加一个字段
     * @param field 虚拟字段
     * @return this
     */
    public VirtualClass addField (VirtualField field) {
        if (!fields.contains (field)) fields.add (field);
        return this;
    }

    public VirtualClass addField (VirtualField field, boolean accessor) {
        if (accessor) {
            addMethod (field.getReader ());
            addMethod (field.getWriter ());
        }

        return addField (field);
    }

    /**
     * 向虚拟类中添加一个方法
     * @param method 虚拟方法
     * @return this
     */
    public VirtualClass addMethod (VirtualMethod method) {
        if (!methods.contains (method)) methods.add (method);
        return this;
    }

    /**
     * 向虚拟类中添加一个导入条目
     * @param importItem 导入条目
     * @return this
     */
    public VirtualClass addImport (String importItem) {
        if (!imports.contains (importItem)) imports.add (importItem);
        return this;
    }

    /**
     * 添加虚拟类所实现的接口名称
     * @param interfaceName 接口名称
     * @return this
     */
    public VirtualClass addInterface (String interfaceName) {
        if (!interfaces.contains (interfaceName))
            interfaces.add (interfaceName);
        return this;
    }

    /**
     * 添加虚拟类的缺省构造函数
     * @return 缺省构造函数
     */
    public VirtualConstruction addDefaultConstruction () {
        VirtualConstruction vc = new VirtualConstruction ();
        vc.setName (name);
        constructions.add (vc);
        return vc;
    }

    /**
     * 向虚拟类添加一个指定参数的构造函数
     * @param vfs 构造函数的指定参数表
     * @return 添加的构造函数
     */
    public VirtualConstruction addConstruction (VirtualField[] vfs) {
        VirtualConstruction vc = new VirtualConstruction ();
        vc.setName (name);

        for (VirtualField vf : vfs) {
            VirtualParameter vp = new VirtualParameter ();
            vp.setName (vf.getName ());
            vp.setType (vf.getType ());
            vc.addParameter (vp);
            vc.addStatement ("this." + vp.getName () + " = " + vp.getName ());
        }
        constructions.add (vc);
        return vc;
    }

    /**
     * 向虚拟类添加一段代码片段
     * @param code 合法的 java 代码片段
     * @return this
     */
    public VirtualClass addCodeSnip (String code) {
        codeSnip.add (code);
        return this;
    }

    public VirtualClass addCodeSnip (String fmt, Object... args) {
        return addCodeSnip (String.format (fmt, args));
    }

    public ClassType getType () {
        return type;
    }

    public VirtualClass setType (ClassType type) {
        this.type = type;
        return this;
    }

    /**
     * 重载 java.lang.Object.toString 方法，生成符合 java 规范的代码。
     * @return java 代码
     */
    public String toString () {
        StringBuilder buff = new StringBuilder ();
        buff.append ("package ").append (packageName).append (";").append (CRLF).append (CRLF);
        for (Object anImport : imports)
            buff.append ("import ").append (anImport).append (";").append (CRLF);
        buff.append (CRLF);

        for (VirtualAnnotation va : annotations) {
            buff.append (va).append (CRLF);
        }
        buff.append (Modifier.expression[modifier]);
        if (still) {
            buff.append (" static");
        }
        if (fin) {
            buff.append (" final");
        } else if (abs) {
            buff.append (" abstract");
        }
        switch (type) {
            case ANNOTATION: buff.append (" @interface "); break;
            case INTERFACE:  buff.append (" interface "); break;
            default        : buff.append (" class "); break;
        }
        buff.append (name).append (" ");
        if (superClassName != null)
            buff.append ("extends ").append (superClassName).append (" ");
        if (interfaces.size () > 0) {
            buff.append ("implements ");
            for (int i = 0; i < interfaces.size (); i ++) {
                if (i != 0) buff.append (", ");
                buff.append (interfaces.get (i));
            }
        }
        buff.append ("{").append (CRLF);
        if (!fields.isEmpty ()) {
            for (VirtualField field : fields)
                buff.append (field).append (";").append (CRLF).append (CRLF);
        }
        if (!constructions.isEmpty ()) {
            for (VirtualConstruction construction : constructions)
                buff.append (construction);
            buff.append (CRLF);
        }

        for (String aCodeSnip : codeSnip)
            buff.append (aCodeSnip).append (CRLF);

        int index = 0;
        for (VirtualMethod method : methods) {
            if (index ++ > 0) {
                buff.append (CRLF);
            }
            buff.append (method);
        }
        buff.append ("}");
        return buff.toString ();
    }
}