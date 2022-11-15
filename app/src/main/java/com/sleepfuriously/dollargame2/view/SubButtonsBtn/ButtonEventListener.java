package com.sleepfuriously.dollargame2.view.SubButtonsBtn;

/**
 * Created by dear33 on 2016/9/11.
 */
public interface ButtonEventListener {
    /**
     * When the user clicks on one of the popup buttons, this is fired.<br>
     * <br>
     * Note: The popup buttons are enabled ONLY if RAW mode is turned OFF.
     *
     * @param index     button index, count from startAngle to endAngle,
     *                  value is 1 to expandButtonCount
     */
    void onPopupButtonClicked(int index);

    void onExpand();
    void onCollapse();

    /**
     * Called when all the collapse animations have completed but before invalidate
     * is called for this button.  You have a chance to do some changes here!
     */
    void onCollapseFinished();

    /**
     * A touch event has happened with the button (actually, just passed
     * along from the View to here).  It's up to the caller to handle it.<br>
     * <br>
     * NOTE:  This will ONLY fire is RAW mode is turned ON.
     */
//    void onTouch(MotionEvent event);

}
