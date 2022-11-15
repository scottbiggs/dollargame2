package com.sleepfuriously.dollargame2.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import com.sleepfuriously.dollargame2.R;


/**
 * The layout that holds the main area of the game.  It captures touch
 * events to make new buttons and draws the lines between the nodes.
 *
 * todo: this would be a good place to animate the dots!!!
 */
public class PlayAreaFrameLayout extends FrameLayout {

    //--------------------------
    //  constants
    //--------------------------

    @SuppressWarnings("unused")
    private static final String TAG = PlayAreaFrameLayout.class.getSimpleName();

    //--------------------------
    //  data
    //--------------------------

    /** When true, draw lines during onDraw() */
    private boolean mDrawLines = true;

    /** list of lines to draw */
    private List<Line> mLines;

    /** Paint for drawing the lines that connect various nodes */
    private Paint mLinePaint;

    private Context mCtx;

    //--------------------------
    //  methods
    //--------------------------

    public PlayAreaFrameLayout(@NonNull Context ctx) {
        super(ctx);
        init(ctx, null);
    }

    public PlayAreaFrameLayout(@NonNull Context ctx, @Nullable AttributeSet attrs) {
        super(ctx, attrs);
        init(ctx, attrs);
    }

    public PlayAreaFrameLayout(@NonNull Context ctx, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(ctx, attrs, defStyleAttr);
        init(ctx, attrs);
    }


    private void init(@NonNull Context ctx, @Nullable AttributeSet attrs) {

        mCtx = ctx;

        mLines = new ArrayList<>();
        mLinePaint = new Paint();

        int color = mCtx.getResources().getColor(R.color.line_color_normal); // old
        mLinePaint.setColor(color);
        mLinePaint.setAntiAlias(false);
        mLinePaint.setStrokeWidth(7f);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLines(canvas);
    }


    /**
     * Draws all the lines for this class.
     *
     * preconditions:
     *      mLines      Holds the lines to draw
     *
     *      mLinePaint  initialized correctly
     */
    private void drawLines(Canvas canvas) {
        if (!mDrawLines) {
            return;
        }

        for (int i = 0; i < mLines.size(); i++) {
            Line line = mLines.get(i);
            canvas.drawLine(line.start.x, line.start.y,
                            line.end.x, line.end.y,
                            mLinePaint);
        }

    }

    /**
     * Turns line drawing on or off.  Start or stop drawing
     * all the lines in the list of lines.
     *
     * See {@link #addLine(PointF, PointF)}
     *
     * @param drawLinesOn   TRUE --> yes, draw the lines
     *                      FALSE --> do not draw the lines
     */
    public void setToDrawLines(boolean drawLinesOn) {
        mDrawLines = drawLinesOn;
    }

    /**
     * Returns whether this class is currently drawing lines or not
     * as a setting.
     *
     * Don't confuse this with having 0 lines to draw (which won't do
     * anything).  Thus there are 2 reasons this class may not draw
     * any lines.
     */
    public boolean getToDrawLines() {
        return mDrawLines;
    }

    /**
     * Adds the given line to the line list.
     *
     * NOTE: this does not automatically turn the line draw on.
     * You gotta call {@link #setToDrawLines(boolean)} yourself.
     */
    public void addLine(PointF start, PointF end) {
        Line line = new Line(start, end);
        mLines.add(line);
        Log.d(TAG, "addline(), start = " + start + ", end = " + end);
    }

    public void removeLine(PointF start, PointF end) {
        Log.d(TAG, "removeLine() begin: start = " + start + ", end = " + end);

        // find the line...
        for (int i = 0; i < mLines.size(); i++) {
            Line line = mLines.get(i);

            // Remove the line if the the two endpoints match (assuming
            // undirected graph).
            if ((line.start.equals(start) && line.end.equals(end)) ||
                (line.start.equals(end) && line.end.equals(start))) {
                mLines.remove(i);
                Log.d(TAG, "   --removed");
                break;
            }
        }

    }

    /** Removes all the lines from the line list */
    public void removeAllLines() {
        mLines.clear();
//        Log.d(TAG, "removeAllLines()");
    }

    /**
     * Searches through the list, finding every occurrance of the
     * origPoint, and replaces it with the newPoint.
     *
     * Assumes that all occurrences of a origPoint ARE IN FACT references
     * to the same point.  So if you have a graph with multiple uses
     * of the same point, this will probably fail (but that's a rather
     * rare situation).
     */
    @Deprecated // seems to have problems--needs testing
    public void updateLines(PointF origPoint, PointF newPoint) {
        Log.d(TAG, "updateLines()");
        for (int i = 0; i < mLines.size(); i++) {
            Line line = mLines.get(i);
            if (line.start.equals(origPoint.x, origPoint.y)) {
                line.start = newPoint;
                mLines.remove(i--);
                mLines.add(line);
            }
            else if (line.end.equals(origPoint.x, origPoint.y)) {
                line.end = newPoint;
                mLines.remove(i--);
                mLines.add(line);
            }
        }
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  classes
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @SuppressWarnings("WeakerAccess")
    private class Line {
        public PointF start, end;

        Line (PointF _start, PointF _end) {
            start = _start;
            end = _end;
        }
    }

}
