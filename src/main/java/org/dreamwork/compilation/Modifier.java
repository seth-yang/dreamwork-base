package org.dreamwork.compilation;

/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-2-2
 * Time: 11:37:08
 */
public interface Modifier   {
    int PUBLIC = 0;
    int DEFAULT = 1;
    int PROTECTED = 2;
    int PRIVATE = 3;

    String[] expression = {"public", "", "protected", "private"};
}