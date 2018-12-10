package org.dreamwork.misc;

import org.dreamwork.util.StringUtil;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 13-12-6
 * Time: 上午10:50
 */
public enum CaptchaType {
    Number_Only, Alpha_Chars, Question, Formula;

    public static CaptchaType parse (String text) {
        if (StringUtil.isEmpty (text))
            throw new IllegalArgumentException ("Unknown captcha type: " + text);
        text = text.trim ();
        if (text.equalsIgnoreCase ("number"))
            return Number_Only;
        if (text.equalsIgnoreCase ("alpha"))
            return Alpha_Chars;

        for (CaptchaType type : values ()) {
            if (text.equalsIgnoreCase (type.name ()))
                return type;
        }

        throw new IllegalArgumentException ("Unknown captcha type: " + text);
    }
}