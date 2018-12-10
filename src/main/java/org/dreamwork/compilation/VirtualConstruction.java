package org.dreamwork.compilation;

/**
 * 虚拟构造函数
 *
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-2-2
 * Time: 12:11:48
 */
public class VirtualConstruction extends VirtualMethod {
    public String getReturnType () {
        return "";
    }

    public VirtualConstruction setReturnType (String returnType) {
        super.setReturnType ("");
        return this;
    }

    @Override
    public String toString () {
        setReturnType ("");
        return super.toString ();
    }

    @Override
    public VirtualConstruction setStill (boolean still) {
        throw new NotSupportedException ();
    }

    @Override
    public VirtualConstruction setFin (boolean fin) {
        throw new NotSupportedException ();
    }

    @Override
    public VirtualConstruction setAbs (boolean abs) {
        throw new NotSupportedException ();
    }
}