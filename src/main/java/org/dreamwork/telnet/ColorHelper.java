package org.dreamwork.telnet;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 11-11-10
 * Time: 下午3:58
 * To change this template use File | Settings | File Templates.
 */
public class ColorHelper {
    /**
     * Creates a string with the given textcolor.
     *
     * @param str   String to be colorized.
     * @param color Constant defined color (see constants).
     * @return String with internal markup-sequences.
     */
    public static String colorizeText( String str, String color) {
        return INTERNAL_MARKER + color + str + INTERNAL_MARKER + RESET_ALL;
    }//colorizeText

    /**
     * Creates a string with the given textcolor.
     *
     * @param str    String to be colorized.
     * @param color  Constant defined color (see constants).
     * @param autoff boolean that toggles automatical appending of "attributes off"
     *               sequence.{@code true} if "attributes off" should be appended, false
     *               otherwise.
     * @return String with internal markup-sequences.
     */
    public static String colorizeText( String str, String color, boolean autoff) {
        if( autoff) {
            return colorizeText( str, color);
        }
        else {
            return INTERNAL_MARKER + color + str;
        }
    }//colorizeText

    /**
     * Creates a string with the given backgroundcolor.
     *
     * @param str   String to be colorized.
     * @param color Constant defined color (see constants).
     * @return String with internal markup-sequences.
     */
    public static String colorizeBackground( String str, String color) {
        return INTERNAL_MARKER + color.toLowerCase() + str + INTERNAL_MARKER + RESET_ALL;
    }//colorizeBackground

    /**
     * Creates a string with the given backgroundcolor.
     *
     * @param str    String to be colorized.
     * @param color  Constant defined color (see constants).
     * @param autoff boolean that toggles automatical appending of "attributes off"
     *               sequence.{@code true} if "attributes off" should be appended, false
     *               otherwise.
     * @return String with internal markup-sequences.
     */
    public static String colorizeBackground( String str, String color, boolean autoff) {
        if( autoff) {
            return colorizeBackground( str, color);
        }
        else {
            return INTERNAL_MARKER + color.toLowerCase() + str;
        }
    }//colorizeBackground

    /**
     * Creates a string with the given foreground and backgroundcolor.
     *
     * @param str String to be colorized.
     * @param fgc Constant defined color (see constants). Will be textcolor.
     * @param bgc Constant defined color (see constants). Will be backgroundcolor.
     * @return String with internal markup-sequences.
     */
    public static String colorizeText( String str, String fgc, String bgc) {
        return INTERNAL_MARKER + fgc + INTERNAL_MARKER + bgc.toLowerCase() + str + INTERNAL_MARKER + RESET_ALL;
    }//colorizeText

    /**
     * Creates a string with the given foreground and backgroundcolor.
     *
     * @param str    String to be colorized.
     * @param fgc    Constant defined color (see constants). Will be textcolor.
     * @param bgc    Constant defined color (see constants). Will be backgroundcolor.
     * @param autoff boolean that toggles automatical appending of "attributes off"
     *               sequence.{@code true} if "attributes off" should be appended, false
     *               otherwise.
     * @return String with internal markup-sequences.
     */
    public static String colorizeText( String str, String fgc, String bgc, boolean autoff) {
        if( autoff) {
            return colorizeText( str, fgc, bgc);
        }
        else {
            return INTERNAL_MARKER + fgc + INTERNAL_MARKER + bgc.toLowerCase() + str;
        }
    }//colorizeText

    /**
     * Creates a string with high intensity (bold) in the given textcolor.
     *
     * @param str   String to be boldcolorized.
     * @param color Constant defined color (see constants).
     * @return String with internal markup-sequences.
     */
    public static String boldcolorizeText( String str, String color) {
        return INTERNAL_MARKER + BOLD + INTERNAL_MARKER + color + str + INTERNAL_MARKER + RESET_ALL;
    }//colorizeBoldText

    /**
     * Creates a string with the given high intensity foregroundcolor
     * and the given backgroundcolor.
     *
     * @param str String to be colorized.
     * @param fgc Constant defined color (see constants). Will be bold textcolor.
     * @param bgc Constant defined color (see constants). Will be backgroundcolor.
     * @return String with internal markup-sequences.
     */
    public static String boldcolorizeText( String str, String fgc, String bgc) {
        return INTERNAL_MARKER + BOLD + INTERNAL_MARKER + fgc + INTERNAL_MARKER + bgc.toLowerCase() + str
               + INTERNAL_MARKER + RESET_ALL;
    }//colorizeBoldText

    /**
     * Creates a string with high intensity (bold).
     *
     * @param str String to be styled bold.
     * @return String with internal markup-sequences.
     */
    public static String boldText( String str) {
        return INTERNAL_MARKER + BOLD + str + INTERNAL_MARKER + BOLD_OFF;
    }//boldText

    /**
     * Creates a string with italic style.
     *
     * @param str String to be styled italic.
     * @return String with internal markup-sequences.
     */
    public static String italicText( String str) {
        return INTERNAL_MARKER + ITALIC + str + INTERNAL_MARKER + ITALIC_OFF;
    }//italicText

    /**
     * Creates an underlined string.
     *
     * @param str String to be styled underlined.
     * @return String with internal markup-sequences.
     */
    public static String underlinedText( String str) {
        return INTERNAL_MARKER + UNDERLINED + str + INTERNAL_MARKER + UNDERLINED_OFF;
    }//underlinedText

    /**
     * Creates a blinking string.
     *
     * @param str String to be styled blinking.
     * @return String with internal markup-sequences.
     */
    public static String blinkingText( String str) {
        return INTERNAL_MARKER + BLINK + str + INTERNAL_MARKER + BLINK_OFF;
    }//blinkingText

    /**
     * Returns the length of the visible string calculated
     * from the internal marked-up string passed as parameter.
     *
     * @param str String with internal color/style markups.
     * @return long Representing the length of the visible string..
     */
    public static long getVisibleLength( String str) {
        int counter = 0;
        int parsecursor = 0;
        int foundcursor;

        boolean done = false;

        while( !done) {
            foundcursor = str.indexOf( MARKER_CODE, parsecursor);
            if( foundcursor != -1) {
                //increment counter
                counter++;
                //parseon from the next char
                parsecursor = foundcursor + 1;
            }
            else {
                done = true;
            }
        }

        return (str.length() - (counter * 2));
    }//getVisibleLength

    /**
     * Defines the internal marker string
     * for style/color markups.
     */
    public static final String INTERNAL_MARKER = "\001";
    /**
     * Defines the internal marker character code.
     */
    public static final int MARKER_CODE = 1;

    /**
     * Defines the markup representation of the color
     * black.
     */
    public static final String BLACK = "S";

    /**
     * Defines the markup representation of the color
     * red.
     */
    public static final String RED = "R";

    /**
     * Defines the markup representation of the color
     * green.
     */
    public static final String GREEN = "G";

    /**
     * Defines the markup representation of the color
     * yellow.
     */
    public static final String YELLOW = "Y";

    /**
     * Defines the markup representation of the color
     * blue.
     */
    public static final String BLUE = "B";

    /**
     * Defines the markup representation of the color magenta.
     */
    public static final String MAGENTA = "M";

    /**
     * Defines the markup representation of the color
     * cyan.
     */
    public static final String CYAN = "C";

    /**
     * Defines the markup representation of the color
     * white.
     */
    public static final String WHITE = "W";

    /**
     * Defines the markup representation of the activator
     * for style bold (normally represented by high intensity).
     */
    public static final String BOLD = "f";

    /**
     * Defines the markup representation of the deactivator
     * for style bold.
     */
    public static final String BOLD_OFF = "d"; //normal color or normal intensity

    /**
     * Defines the markup representation of the activator
     * for style italic.
     */
    public static final String ITALIC = "i";

    /**
     * Defines the markup representation of the deactivator
     * for style italic.
     */
    public static final String ITALIC_OFF = "j";

    /**
     * Defines the markup representation of the activator
     * for style underlined.
     */
    public static final String UNDERLINED = "u";

    /**
     * Defines the markup representation of the deactivator
     * for style underlined.
     */
    public static final String UNDERLINED_OFF = "v";

    /**
     * Defines the markup representation of the activator
     * for style blinking.
     */
    public static final String BLINK = "e";

    /**
     * Defines the markup representation of the deactivator
     * for style blinking (i.e. steady)
     */
    public static final String BLINK_OFF = "n";

    /**
     * Defines the markup representation of the graphics rendition
     * reset.<br>
     * It will reset all set colors and styles.
     */
    public static final String RESET_ALL = "a";
}
