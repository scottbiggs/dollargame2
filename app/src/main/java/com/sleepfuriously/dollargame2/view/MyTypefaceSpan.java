package com.sleepfuriously.dollargame2.view;


import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;


/**
 * This class is an easy way to insert custom fonts into a
 * SpannableStringBuilder (might work on a SpannableString,
 * but I haven't tested it yet).<br>
 * <br>
 * <strong>USAGE</strong><br>
 *  - Get your custom Typeface any way you like.  (I prefer using
 *  the FontCache class as it avoids memory leaks.)<br>
 *
 *  - Get a SpannableStringBuilder setup with the proper string
 *  put in.<br>
 *
 *  - Instantiate this class with your Typeface.<br>
 *
 *  - Set the span in your SpannableStringBuilder, using <em>this class instance</em>
 *  instead of a Typeface instance.  That's the key trick!<br>
 *
 *  - Use this anywhere you like--the font will have been changed for the
 *  designated span.<br>
 *  <br>
 *  <strong>example</strong><br>
 *  <code>
 *  // Changes the font of the word 'favorite' to a fancy one for a TextView<br>
 *  Typeface tf = FontCache.get("fonts/my_fancy_font.ttf", getApplicationContext());<br>
 *  MyTypefaceSpan myTypefaceSpan = new MyTypefaceSpan(tf);<br>
 *  SpannableStringBuilder sb = new SpannableStringBuilder("my favorite bug");<br>
 *  sb.setSpan(myTypefaceSpan, 3, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);<br>
 *  myTextView.setText(sb);
 *  </code><br>
 *  <br>
 *  NOTE: Some fonts just don't work with Android. Hard to tell which
 *  are which without trying a few of 'em. But if Android likes 'em,
 *  this class will show 'em.
 */
public class MyTypefaceSpan extends MetricAffectingSpan {

    private final Typeface mTypeface;

    /**
     * Constructor. This is generally the only method you'll need to use.
     * After instiating, just use this like you'd use any Typeface.
     *
     * @param tf    The Typeface you want to use within a SpannedString.
     *              See class description {@link MyTypefaceSpan} for an example.
     */
    public MyTypefaceSpan(Typeface tf) {
        mTypeface = tf;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        applyCustomTypeFace(ds, mTypeface);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        applyCustomTypeFace(paint, mTypeface);
    }

    private static void applyCustomTypeFace(Paint paint, Typeface tf) {
        paint.setTypeface(tf);
    }
}