package com.sleepfuriously.dollargame2.view.SubButtonsBtn;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

/**
 * Controls images etc. for the button and sub-buttons
 *
 * todo: needs cleaning and more formal dox
 */
public class ButtonData implements Cloneable{
    private static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE;

    private boolean isMainButton = false;//main button is the button you see when buttons are all collapsed
    private ButtonType buttonType;//true if the button use icon resource,else string resource

    private String[] texts;//String array that you want to show at button center,texts[i] will be shown at the ith row
    private Drawable icon;//icon drawable that will be shown at button center
    private float iconPaddingDp;//the padding of the icon drawable in button
    private int backgroundColor = DEFAULT_BACKGROUND_COLOR;//the background color of the button

    /** Replaces simple boolean for the type of button we're showing--I need both! */
    public enum ButtonType { ICON, TEXT, BOTH }


    @Override
    protected Object clone() throws CloneNotSupportedException {
        ButtonData buttonData = (ButtonData)super.clone();
        buttonData.setButtonType(this.buttonType);
        buttonData.setBackgroundColor(this.backgroundColor);
        buttonData.setIsMainButton(this.isMainButton);
        buttonData.setIcon(this.icon);
        buttonData.setIconPaddingDp(this.iconPaddingDp);
        buttonData.setTexts(this.texts);
        return buttonData;
    }

    public static ButtonData buildTextButton(String... text) {
        ButtonData buttonData = new ButtonData(ButtonType.TEXT);
        buttonData.buttonType = ButtonType.TEXT;
        buttonData.setText(text);
        return buttonData;
    }

    public static ButtonData buildIconButton(Context context, int iconResId, float iconPaddingDp) {
        ButtonData buttonData = new ButtonData(ButtonType.ICON);
        buttonData.buttonType = ButtonType.ICON;
        buttonData.iconPaddingDp = iconPaddingDp;
        buttonData.setIconResId(context, iconResId);
        return buttonData;
    }

    public static ButtonData buildIconButton(Context ctx, Drawable drawable, float iconPaddingDp) {
        ButtonData buttonData = new ButtonData(ButtonType.ICON);
        buttonData.buttonType = ButtonType.ICON;
        buttonData.iconPaddingDp = iconPaddingDp;
        buttonData.icon = drawable;
        return buttonData;
    }

    public static ButtonData buildIconAndTextButton(Context ctx, int iconResId, float iconPaddingDp,
                                                    String... text) {
        ButtonData buttonData = new ButtonData(ButtonType.BOTH);
        buttonData.buttonType = ButtonType.BOTH;
        buttonData.setText(text);

        buttonData.iconPaddingDp = iconPaddingDp;
        buttonData.setIconResId(ctx, iconResId);

        return buttonData;
    }

    public static ButtonData buildIconAndTextButton(Context ctx, Drawable drawable, float iconPaddingDp,
                                                    String... text) {
        ButtonData buttonData = new ButtonData(ButtonType.BOTH);
        buttonData.buttonType = ButtonType.BOTH;
        buttonData.setText(text);

        buttonData.iconPaddingDp = iconPaddingDp;
        buttonData.setIcon(drawable);

        return buttonData;
    }

    /**
     * Works like the other {@link #buildIconAndTextButton(Context, int, float, String...)},
     * but this one takes a number as the text string--works better for
     * my project.
     */
    public static ButtonData buildIconAndTextButton(Context ctx, Drawable drawable, float iconPaddingDp,
                                                    int amount) {
        ButtonData buttonData = new ButtonData(ButtonType.BOTH);
        buttonData.buttonType = ButtonType.BOTH;
        buttonData.setText(String.valueOf(amount));

        buttonData.iconPaddingDp = iconPaddingDp;
        buttonData.setIcon(drawable);

        return buttonData;
    }

    private ButtonData(ButtonType buttonType) {
        this.buttonType = buttonType;
    }

    public void setIsMainButton(boolean isMainButton) {
        this.isMainButton = isMainButton;
    }

    public boolean isMainButton() {
        return isMainButton;
    }

    public void setButtonType(ButtonType isIconButton) {
        buttonType = isIconButton;
    }

    public String[] getTexts() {
        return texts;
    }

    public void setTexts(String[] texts) {
        this.texts = texts;
    }

    public void setText(String... text) {
        this.texts = new String[text.length];
        for (int i = 0, length = text.length; i < length; i++) {
            this.texts[i] = text[i];
        }
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public Drawable getIcon() {
        return this.icon;
    }

    public void setIconResId(Context context, int iconResId) {
        this.icon = context.getResources().getDrawable(iconResId);
    }

    public ButtonType getButtonType() {
        return buttonType;
    }

    public float getIconPaddingDp() {
        return iconPaddingDp;
    }

    public void setIconPaddingDp(float padding) {
        this.iconPaddingDp = padding;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setBackgroundColorId(Context context, int backgroundColorId) {
        this.backgroundColor = context.getResources().getColor(backgroundColorId);
    }
}
