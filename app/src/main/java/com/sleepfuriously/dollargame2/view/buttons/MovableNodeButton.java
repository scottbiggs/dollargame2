package com.sleepfuriously.dollargame2.view.buttons;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.appcompat.content.res.AppCompatResources;

import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;

import com.sleepfuriously.dollargame2.R;
import com.sleepfuriously.dollargame2.view.SubButtonsBtn.ButtonData;
import com.sleepfuriously.dollargame2.view.SubButtonsBtn.SubButtonsBtn;


/**
 * This should be the final Button for this app.  It moves, it provides
 * expandable sub-buttons, it does everything!<br>
 * <br>
 * There are three modes ({@link Modes} which determine how the button behaves and the UI events it signals.
 * Depending on the mode, the button can be expandable, movable, or click-only.<br>
 * <br>
 * You need to register the {@link OnMoveListener} to get
 * movement events and register the {@link com.sleepfuriously.dollargame2.view.SubButtonsBtn.ButtonEventListener}
 *
 * to get click events.  It's pretty standard, but uses 2 listeners
 * instead of one.<br>
 * <br>
 * Aaaaannnd, while a button is movable (not expandable), only callbacks to {@link OnMoveListener}
 * are called.  And conversely while a button is expandable (not movable), only callbacks to
 * {@link com.sleepfuriously.dollargame2.view.SubButtonsBtn.ButtonEventListener} occur.
 */
public class MovableNodeButton extends SubButtonsBtn
        implements Runnable {

    //-------------------------------
    //  constants
    //-------------------------------

    private static final String TAG = MovableNodeButton.class.getSimpleName();

    /** The amount that a touch must move to be considered a real move and not just an inadvertant wiggle */
    private static final float MOVE_THRESHOLD = 5f;

    /** The number of milliseconds before a click becomes a long click */
    private static final long MILLIS_FOR_LONG_CLICK = 500L;

    /**
     * The modes of this button.  These modes completely determine the behavior of this
     * button and how it returns UI input to the implementer of the interfaces.<br>
     * <br>
     * The following modes are used:<br>
     * {@link #MOVABLE}<br>
     * {@link #EXPANDABLE}<br>
     * {@link #CLICKS_ONLY}
     */
    public enum Modes {
        /**
         * The button is movable. It registers movement, clicks, and long clicks.
         * No expanding buttons will display.
         */
        MOVABLE,
        /** Expanding buttons are displayed and will register. No movement will happen. */
        EXPANDABLE,
        /** Not movable nor will any expanding buttons will appear. Only registers clicks  and long clicks. */
        CLICKS_ONLY
    }


    /** thickness for drawing the circle outline of a button */
    private static final int OUTLINE_STROKE_WIDTH = 3;

    //-------------------------------
    //  data
    //-------------------------------

    private Context mCtx;

    /** The number of dollars this button currently holds */
    private int mAmount;

    /** raw coordinates of where a click starts */
    private float mStartRawX, mStartRawY;

    /** offsets from the button's view and raw coords */
    private float mOffsetX, mOffsetY;

    /**
     * Indicates the current mode of this button.
     * @see Modes
     */
    private Modes mCurrentMode;

    /** true iff the button is in the process of moving */
    private boolean mMoving;

    /** listener sent in to receive callbacks when the button has moved */
    private OnMoveListener mMoveListener;

    /** The actual color of the current highlight */
    private int mCurrentHighlightColor;

    /** background color of the main button */
    private int mCurrentBackgroundColor;

    /** Helper for responding to long-clicks */
    private Handler mLongClickHandler;

    /** When true, a long click event has been handled. Will be reset when a new long click wait begins */
    private boolean mLongClickFired = false;

    //-------------------------------
    //  methods
    //-------------------------------

    public MovableNodeButton(Context ctx) {
        this(ctx, null);
    }

    public MovableNodeButton(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        initialize(ctx, attrs);
    }


    private void initialize(Context ctx, AttributeSet attrs) {
        mCtx = ctx;
        mMoving = false;
        mCurrentMode = Modes.MOVABLE;

        mAmount = 0;

        mLongClickHandler = new Handler();

        // make the buttons go left/right
        setStartAngle(0);
        setEndAngle(180);
        setButtonGapPx(5); // make the buttons much closer than the default of 25

        setButtonDatas(createButtonImages(mAmount));
    }

    /**
     * Returns the current {@link Modes} of this button (all-important!).
     */
    public Modes getMode() {
        return mCurrentMode;
    }

    /**
     * Sets the current mode of this button. SO IMPORTANT!
     * @see Modes
     */
    public void setMode(Modes newMode) {
        mCurrentMode = newMode;
    }

    /**
     * Returns the amount that is currently displayed.
     */
    public int getAmount() {
        return mAmount;
    }

    /**
     * Sets the amount that this button displays.  Automatically
     * calls invalidate();
     *
     * @param amount    Should be a value on or between
     *                  MIN_AMOUNT and MAX_AMOUNT
     */
    public void setAmount(int amount) {
        mAmount = amount;
        setButtonDatas(createButtonImages(mAmount));
        invalidate();
    }

    /**
     * Increments the amount of this Node by 1.
     * Automatically calls invalidate().
     */
    public void incrementAmount() {
        setAmount(mAmount + 1);
    }

    /**
     * Decrements the amount by 1
     * Automatically calls invalidate().
     */
    public void decrementAmount() {
        setAmount(mAmount - 1);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (mCurrentMode) {
            case MOVABLE:
                return processMovableTouchEvent(event);

            case EXPANDABLE:
                return super.onTouchEvent(event);

            case CLICKS_ONLY:
                return processClickTouchEvent(event);

            default:
                Log.e(TAG, "unhandled onTouchEvent!");
                return true;
        }
    }

    /**
     * Process UI events when in CLICKS_ONLY mode (which is usually SOLVE mode).
     *
     * @param event     The original onTouch MotionEvent.
     *
     * @return  True - event completely consumed.       todo: currently always returns true!!
     *          False - continue processing this event down the UI chain.
     */
    private boolean processClickTouchEvent(MotionEvent event) {
//        Log.d(TAG, "processClickTouchEvent(), ACTION = " + event.getAction());

        if (event.getAction() == ACTION_DOWN) {
            mStartRawX = event.getRawX();
            mStartRawY = event.getRawY();
        }

        else if (event.getAction() == ACTION_UP) {
            // Don't count this as a click if the user has slid their hand around.
            float currentRawX = event.getRawX();
            float currentRawY = event.getRawY();

            // Only count clicks that haven't moved around much.  Otherwise it's
            // not really a click (it's probably a slide or something, which we're
            // no interested in.
            if ((Math.abs(currentRawX - mStartRawX) < MOVE_THRESHOLD) &&
                    (Math.abs(currentRawY - mStartRawY) < MOVE_THRESHOLD)) {
                mMoveListener.clicked();
            }

            else {
                Log.d(TAG, "not really a click (probably a move)--ignored.");
            }
        }

        return true;    // consume all events that come here
    }


    /**
     * Localizes the processing of a touch event while in MOVEABLE (Build) mode.
     *
     * @param event     The original onTouch MotionEvent.
     *
     * @return  True - event completely consumed.
     *          False - continue processing this event down the UI chain.
     */
    private boolean processMovableTouchEvent(MotionEvent event) {


        switch (event.getAction()) {
            case ACTION_DOWN:
//                Log.d(TAG, "processMovableTouchEvent() - ACTION_DOWN");

                mLongClickHandler.postDelayed(this, MILLIS_FOR_LONG_CLICK); // start timing
                mLongClickFired = false;

                mStartRawX = event.getRawX();
                mStartRawY = event.getRawY();

                // save the offset from the click in the button's context vs the raw location
                mOffsetX = mStartRawX - getX();
                mOffsetY = mStartRawY - getY();
                break;

            case ACTION_MOVE:
//                Log.d(TAG, "processMovableTouchEvent() - ACTION_MOVE");

                // only move if we're already movingTo AND the finger has moved enough to
                // be considered a move.
                if (mMoving || movedPastThreshold(event)) {
                    mMoving = true;

                    // stop waiting for a long click, this is a move.
                    mLongClickHandler.removeCallbacks(this);

                    // calc the move differences
                    float diffX = event.getRawX() - mOffsetX;
                    float diffY = event.getRawY() - mOffsetY;

                    mMoveListener.movingTo(diffX, diffY);

                    animate()
                            .x(diffX)
                            .y(diffY)
                            .setDuration(0)
                            .start();
                }
                else {
                    return false;
                }
                break;

            case ACTION_UP:
//                Log.d(TAG, "processMovableTouchEvent() - ACTION_UP");

                mLongClickHandler.removeCallbacks(this);

                // Handle the up event that occurs AFTER a long-click has been detected.
                if (mLongClickFired) {
                    mMoving = false;
                    Log.d(TAG, "encountered an ACTION_UP event after a long-click has fired");
                    break;
                }

                if (mMoving) {
                    mMoving = false;

                    // always calculate the moving difference for this final move
                    float diffX = event.getRawX() - mOffsetX;
                    float diffY = event.getRawY() - mOffsetY;

                    mMoveListener.moveEnded(diffX, diffY);

                    if ((diffX != 0f) || (diffY != 0f)) {
                        animate()
                                .x(diffX)
                                .y(diffY)
                                .setDuration(0)
                                .start();
                    }
                }
                else {
                    // Not moving (or has moved enough). So treat as a click.
                    mMoveListener.clicked();
                }
                break;

            default:
                mLongClickHandler.removeCallbacks(this);
                Log.d(TAG, "processMovableTouchEvent(), unknown event = " + event.getAction());
                break;
        }

        return true;    // event consumed
    }


    /**
     * Called when a Long Click event occurs.
     */
    @Override
    public void run() {
        // Signal that an up event has been handled and indicate the long click
        mLongClickFired = true;
        mMoveListener.longClicked();
    }


    /**
     * Returns TRUE iff the position of the given event is more than the
     * threshold from {@link #mStartRawX} and {@link #mStartRawY}.
     *
     * Does not worry about time.
     */
    @SuppressWarnings("RedundantIfStatement")
    private boolean movedPastThreshold(MotionEvent event) {
        float currentX = event.getRawX();
        float currentY = event.getRawY();

        // if we're within the threshold, return false (we have NOT moved past the threshold)
        if ((Math.abs(currentX - mStartRawX) < MOVE_THRESHOLD) &&
                (Math.abs(currentY - mStartRawY) < MOVE_THRESHOLD)) {
            return false;
        }

        return true;
    }

    /**
     * Use this to set the callback when implementing the {@link OnMoveListener}
     * interface.
     *
     * @param listener  The instance that is implementing the interface.
     */
    public void setOnMoveListener(OnMoveListener listener) {
        mMoveListener = listener;
    }


    /**
     * Sets the background color of the main button.<br>
     * <br>
     * NOTE: You need to call invalidate() after.
     *
     * @param resid     The resource id of the color
     */
    public void setBackgroundColorResource(int resid) {
        mCurrentBackgroundColor = getResources().getColor(resid);

        List<ButtonData> buttons = getButtonDatas();
        ButtonData mainButtonData = buttons.get(0);

        mainButtonData.setBackgroundColor(mCurrentBackgroundColor);
    }

    /**
     * Sets the color of the main button's outline.
     *
     * @param colorResource     Resource ID of the color to highlight
     *                          the primary button.
     */
    public void setOutlineColor(int colorResource) {

        Toast.makeText(mCtx, "setOutlineColor() NOT implemented!", Toast.LENGTH_SHORT).show();

        // todo

//        GradientDrawable gradientDrawable = (GradientDrawable)getBackground();
//        if (gradientDrawable == null) {
//            Log.d(TAG, "gradientDrawable is NULL!");
//            return;
//        }
//        gradientDrawable.setStroke(STROKE_WIDTH, mCurrentHighlightColor);
    }

    /**
     * Helper method that creates a list of the appropriate button images.
     *
     * @return  A list of button images suitable for sending to
     *          <code>setButtonDatas()</code>.
     */
    private List<ButtonData> createButtonImages(int amount) {

        // Create a drawable with the correct color
        Drawable highlightDrawable = AppCompatResources.getDrawable(mCtx,
                (amount < 0) ? R.drawable.circle_solve_negative : R.drawable.circle_black);

        List<ButtonData> buttonDataList = new ArrayList<>();

        ButtonData highlightButtonData = ButtonData.buildIconAndTextButton(mCtx, highlightDrawable, 0, amount);
        ButtonData firstPopup = ButtonData.buildIconButton(mCtx, R.drawable.ic_take_3, 0);
        ButtonData secondPopup = ButtonData.buildIconButton(mCtx, R.drawable.ic_give_3, 0);

        buttonDataList.add(highlightButtonData);
        buttonDataList.add(firstPopup);
        buttonDataList.add(secondPopup);

        return buttonDataList;
    }

    /**
     * Finds the center of this button in terms of its parent coordinates.
     */
    public float getCenterX() {
        return getCenter().x;
//        return getX() + (((float)getWidth()) / 2f);
    }

    /**
     * Finds the center of this button in terms of its parent coordinates.
     */
    public float getCenterY() {
        return getCenter().y;
//        return getY() + (((float)getHeight()) / 2f);
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  interfaces & classes
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public interface OnMoveListener {

        /**
         * Called when a move has been detected, but before any action has taken
         * place.  This will provide a way for the caller to know that a move is
         * happening and to prepare accordingly.
         *
         * This will be called many times during a single user's action--once
         * for each detected movement.  Essentially each time a MotionEvent.ACTION_MOVE
         * is initiated, this will be called.
         *
         * @param diffX     The difference between where the button currently is
         *                  and where it will be once the move is complete. X axis.
         *
         * @param diffY     Y axis
         */
        void movingTo(float diffX, float diffY);

        /**
         * Signals that a move has been completed with this button.
         *
         * Coincides with MotionEvent.ACTION_UP.
         *
         * @param diffX     If there is a difference in location from the last moveTo
         *                  event, this will record the difference.
         *
         * @param diffY     Same for y-axis
         */
        void moveEnded(float diffX, float diffY);

        /**
         * The user has clicked on this button, not moved it.
         */
        void clicked();

        /**
         * User has long-clicked (over a second) a button, but has not moved it.
         */
        void longClicked();
    }


}
