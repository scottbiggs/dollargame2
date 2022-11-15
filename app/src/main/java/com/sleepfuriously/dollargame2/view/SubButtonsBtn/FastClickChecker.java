package com.sleepfuriously.dollargame2.view.SubButtonsBtn;

/**
 * Use to determine clicks from drags and double clicks.
 *
 * todo: needs further testing--not sure it works in edge cases
 */
public class FastClickChecker {
    private int threshold;
    private long lastClickTime = 0;

    public FastClickChecker(int threshold) {
        this.threshold = threshold;
    }

    public boolean isFast() {
        boolean isQuick = System.currentTimeMillis() - lastClickTime <= threshold;
        lastClickTime = System.currentTimeMillis();
        return isQuick;
    }
}
