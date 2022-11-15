package com.sleepfuriously.dollargame2.view;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;


/**
 * Use this to get custom fonts instead of accessing them directly.
 * Not only is it faster, but prevents memory leaks on older phones.<br>
 * <br>
 * This class just has one method, and it's static.  So there's no
 * need to do any fancy initializations or anything.  Just add this
 * class to your project and you're good to go.
 */
public class FontCache {

    private static Hashtable<String, Typeface> fontCache = new Hashtable<String, Typeface>();

    /**
     * Retrieves the given font from the cache or loads it if it's not there.
     *
     * @param name      The string text of the font in the assets directory (needs
     *                  to include "fonts/" as well!).
     *
     * @param context   Ye good 'ol context
     *
     * @return  A Typeface for this font, suitable for using in any way you like.
     *          Returns null on error.
     */
    public static Typeface get(String name, Context context) {
        Typeface tf = fontCache.get(name);
        if(tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getAssets(), name);
            }
            catch (Exception e) {
                return null;
            }
            fontCache.put(name, tf);
        }
        return tf;
    }

    /** Should never be called--just a pure static class */
    private FontCache() {};

}