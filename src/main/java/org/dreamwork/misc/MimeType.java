package org.dreamwork.misc;

/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2010-4-26
 * Time: 14:19:33
 */
public class MimeType {
    private String ext;
    private String name;

    public MimeType (String ext, String name) {
        this.ext = ext;
        this.name = name;
    }

    public String getExt () {
        return ext;
    }

    public void setExt (String ext) {
        this.ext = ext;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }
}