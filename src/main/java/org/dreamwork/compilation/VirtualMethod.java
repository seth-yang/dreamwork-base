package org.dreamwork.compilation;

import java.util.ArrayList;
import java.util.List;

/**
 * 虚拟方法
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-2-2
 * Time: 11:39:56
 */
public class VirtualMethod extends VirtualType<VirtualMethod> {
    private String returnType;
    private List<VirtualParameter> parameters = new ArrayList<> ();
    private List<CharSequence> statements = new ArrayList<> ();
    private List<String> exceptions = new ArrayList<> ();

    /**
     * 获取方法返回类型
     * @return 合法的 java 类型
     */
    public String getReturnType () {
        return returnType;
    }

    /**
     * 设置方法的返回类型
     * @param returnType 合法的 java 类型
     * @return this
     */
    public VirtualMethod setReturnType (String returnType) {
        this.returnType = returnType;
        return this;
    }

    /**
     * 向方法的参数列表中添加一个虚拟参数
     * @param param 虚拟参数
     * @return this
     */
    public VirtualMethod addParameter (VirtualParameter param) {
        parameters.add (param);
        return this;
    }

    /**
     * 向虚拟方法中添加一个合法的 java 语句
     * @param stmt 合法的 java 语句
     * @return this
     */
    public VirtualMethod addStatement (CharSequence stmt) {
        statements.add (stmt);
        return this;
    }

    public VirtualMethod addStatement (String format, Object... args) {
        return addStatement (String.format (format, args));
    }

    /**
     * 向虚拟方法中声明异常列表中添加一个合法的 java 异常
     * @param ex 合法的 java 异常
     * @return this
     */
    public VirtualMethod addException (String ex) {
        exceptions.add (ex);
        return this;
    }

    /**
     * 生成 java 代码
     * @return java 代码
     */
    @Override
    public String toString () {
        StringBuilder buff = new StringBuilder ();

        for (VirtualAnnotation va : annotations) {
            buff.append ('\t').append (va).append (CRLF);
        }
        buff.append ('\t').append (Modifier.expression[modifier]);
        if (still) {
            buff.append (" static");
        }
        if (fin) {
            buff.append (" final");
        }
        if (abs) {
            buff.append (" abstract");
        }
        buff.append (" ");
        buff.append (returnType == null ? "void" : returnType).append (" ");
        buff.append (name).append (" (");
        for (int i = 0; i < parameters.size (); i ++) {
            if (i != 0) buff.append (", ");
            buff.append (parameters.get (i));
        }
        buff.append (")");
        if (exceptions.size () > 0) {
            buff.append (" throws ");
            for (int i = 0; i < exceptions.size (); i ++) {
                if (i != 0) buff.append (", ");
                buff.append (exceptions.get (i));
            }
        }

        if (abs) {
            buff.append (";").append (CRLF);
        } else {
            buff.append (" {").append (CRLF);
            for (CharSequence statement : this.statements) {
                statement = statement.toString ().trim ();
                buff.append ("\t\t").append (statement);
                char ch = statement.charAt (statement.length () - 1);
                if (ch != ';' && ch != '{' && ch != '}')
                    buff.append (";");
                buff.append (CRLF);
            }
            buff.append ("\t}").append (CRLF);
        }
        return buff.toString ();
    }
}