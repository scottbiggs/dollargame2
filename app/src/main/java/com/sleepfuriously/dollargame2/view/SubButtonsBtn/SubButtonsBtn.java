package com.sleepfuriously.dollargame2.view.SubButtonsBtn;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import androidx.annotation.IntDef;
import androidx.core.graphics.ColorUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sleepfuriously.dollargame2.R;


/**
 * General version of a button class that creates sub-buttons when
 * pressed.  Each sub-button animates and is customizable.
 *
 * todo: improve docs
 */
public class SubButtonsBtn extends View implements ValueAnimator.AnimatorUpdateListener {

    //---------------------------------
    //  constants
    //---------------------------------

    private final static String TAG = SubButtonsBtn.class.getSimpleName();

    //---------------------------------
    //  data
    //---------------------------------

    private List<ButtonData> buttonDatas;
    private Map<ButtonData, RectF> buttonRects;
    protected ButtonEventListener buttonEventListener;

    private static final int BUTTON_SHADOW_COLOR = 0xff000000;
    private static final int BUTTON_SHADOW_ALPHA = 32;

    //default attribute values
    private static final int DEFAULT_EXPAND_ANIMATE_DURATION = 225;
    private static final int DEFAULT_ROTATE_ANIMATE_DURATION = 300;
    private static final int DEFAULT_BUTTON_GAP_DP = 25;
    private static final int DEFAULT_BUTTON_MAIN_SIZE_DP = 60;
    private static final int DEFAULT_BUTTON_SUB_SIZE_DP = 60;
    private static final int DEFAULT_BUTTON_ELEVATION_DP = 4;
    private static final int DEFAULT_BUTTON_TEXT_SIZE_SP = 20;
    private static final int DEFAULT_START_ANGLE = 90;
    private static final int DEFAULT_END_ANGLE = 90;
    private static final int DEFAULT_BUTTON_TEXT_COLOR = Color.BLACK;
    private static final int DEFAULT_MASK_BACKGROUND_COLOR = Color.TRANSPARENT;
    private static final int DEFAULT_BLUR_RADIUS = 10;

    private boolean expanded = false;

    //attributes can be set in xml
    private float startAngle;
    private float endAngle;
    private int buttonGapPx;
    private int mainButtonRotateDegree;
    private int rotateAnimDuration;
    private int mainButtonSizePx;
    private int subButtonSizePx;
    private int mainButtonTextSize;
    private int subButtonTextSize;
    private int mainButtonTextColor;
    private int subButtonTextColor;
    private int expandAnimDuration;
    private int maskBackgroundColor;
    private int buttonElevationPx;
    private boolean isSelectionMode;
    private boolean rippleEffect;
    private int rippleColor = Integer.MIN_VALUE;
    private boolean blurBackground;
    private float blurRadius;

    private Bitmap mainShadowBitmap = null;
    private Bitmap subShadowBitmap = null;
    Matrix shadowMatrix;

    private int buttonSideMarginPx;

    private Paint paint;
    private Paint textPaint;

    private SubButtonAngleCalculator subButtonAngleCalculator;
    private boolean animating = false;
    private boolean maskAttached = false;
    private float expandProgress;
    private float rotateProgress;
    private ValueAnimator expandValueAnimator;
    private ValueAnimator collapseValueAnimator;
    private ValueAnimator rotateValueAnimator;
    private Interpolator overshootInterpolator;
    private Interpolator anticipateInterpolator;
    private Path ripplePath;
    private RippleInfo rippleInfo;
    private MaskView maskView;
    private Blur blur;
    private ImageView blurImageView;
    private ObjectAnimator blurAnimator;
    private Animator.AnimatorListener blurListener;
    private PointF pressPointF;
    protected Rect rawButtonRect; //act as the param of getGlobalVisibleRect(Rect rect) method
    protected RectF rawButtonRectF;
    private int pressTmpColor;
    private boolean pressInButton;

    protected FastClickChecker checker;
    private int checkThreshold;


    //---------------------------------
    //  methods
    //---------------------------------

    public SubButtonsBtn(Context context) {
        this(context, null);
    }

    public SubButtonsBtn(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context, attrs);
    }

    public SubButtonsBtn(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SubButtonsBtn);
        startAngle = ta.getInteger(R.styleable.SubButtonsBtn_aebStartAngleDegree, DEFAULT_START_ANGLE);
        endAngle = ta.getInteger(R.styleable.SubButtonsBtn_aebEndAngleDegree, DEFAULT_END_ANGLE);

        buttonGapPx = ta.getDimensionPixelSize(R.styleable.SubButtonsBtn_aebButtonGapDp, dp2px(context, DEFAULT_BUTTON_GAP_DP));
        mainButtonSizePx = ta.getDimensionPixelSize(R.styleable.SubButtonsBtn_aebMainButtonSizeDp, dp2px(context, DEFAULT_BUTTON_MAIN_SIZE_DP));
        subButtonSizePx = ta.getDimensionPixelSize(R.styleable.SubButtonsBtn_aebSubButtonSizeDp, dp2px(context, DEFAULT_BUTTON_SUB_SIZE_DP));
        buttonElevationPx = ta.getDimensionPixelSize(R.styleable.SubButtonsBtn_aebButtonElevation, dp2px(context, DEFAULT_BUTTON_ELEVATION_DP));
        buttonSideMarginPx = buttonElevationPx * 2;
        mainButtonTextSize = ta.getDimensionPixelSize(R.styleable.SubButtonsBtn_aebMainButtonTextSizeSp, sp2px(context, DEFAULT_BUTTON_TEXT_SIZE_SP));
        subButtonTextSize = ta.getDimensionPixelSize(R.styleable.SubButtonsBtn_aebSubButtonTextSizeSp, sp2px(context, DEFAULT_BUTTON_TEXT_SIZE_SP));
        mainButtonTextColor = ta.getColor(R.styleable.SubButtonsBtn_aebMainButtonTextColor, DEFAULT_BUTTON_TEXT_COLOR);
        subButtonTextColor = ta.getColor(R.styleable.SubButtonsBtn_aebSubButtonTextColor, DEFAULT_BUTTON_TEXT_COLOR);

        expandAnimDuration = ta.getInteger(R.styleable.SubButtonsBtn_aebAnimDurationMillis, DEFAULT_EXPAND_ANIMATE_DURATION);
        rotateAnimDuration = ta.getInteger(R.styleable.SubButtonsBtn_aebMainButtonRotateAnimDurationMillis, DEFAULT_ROTATE_ANIMATE_DURATION);
        maskBackgroundColor = ta.getInteger(R.styleable.SubButtonsBtn_aebMaskBackgroundColor, DEFAULT_MASK_BACKGROUND_COLOR);
        mainButtonRotateDegree = ta.getInteger(R.styleable.SubButtonsBtn_aebMainButtonRotateDegree, mainButtonRotateDegree);
        isSelectionMode = ta.getBoolean(R.styleable.SubButtonsBtn_aebIsSelectionMode, false);
        rippleEffect = ta.getBoolean(R.styleable.SubButtonsBtn_aebRippleEffect, true);
        rippleColor = ta.getColor(R.styleable.SubButtonsBtn_aebRippleColor, rippleColor);
        blurBackground = ta.getBoolean(R.styleable.SubButtonsBtn_aebBlurBackground, false);
        blurRadius = ta.getFloat(R.styleable.SubButtonsBtn_aebBlurRadius, DEFAULT_BLUR_RADIUS);

        ta.recycle();

        if (blurBackground) {
            blur = new Blur();
            blurImageView = new ImageView(getContext());
        }

        if (mainButtonRotateDegree != 0) {
            checkThreshold = expandAnimDuration > rotateAnimDuration ? expandAnimDuration : rotateAnimDuration;
        } else {
            checkThreshold = expandAnimDuration;
        }
        checker = new FastClickChecker(checkThreshold);

        rippleInfo = new RippleInfo();
        pressPointF = new PointF();
        rawButtonRect = new Rect();
        rawButtonRectF = new RectF();
        shadowMatrix = new Matrix();

        initViewTreeObserver();
        initAnimators();
    }

    private void initViewTreeObserver() {
        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getGlobalVisibleRect(rawButtonRect);
                rawButtonRectF.set(rawButtonRect.left, rawButtonRect.top, rawButtonRect.right, rawButtonRect.bottom);
            }
        });
    }

    private void initAnimators() {
        overshootInterpolator = new OvershootInterpolator();
        anticipateInterpolator = new AnticipateInterpolator();

        expandValueAnimator = ValueAnimator.ofFloat(0, 1);
        expandValueAnimator.setDuration(expandAnimDuration);
        expandValueAnimator.setInterpolator(overshootInterpolator);
        expandValueAnimator.addUpdateListener(this);
        expandValueAnimator.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                animating = true;
                attachMask();
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animating = false;
                expanded = true;
            }
        });

        collapseValueAnimator = ValueAnimator.ofFloat(1, 0);
        collapseValueAnimator.setDuration(expandAnimDuration);
        collapseValueAnimator.setInterpolator(anticipateInterpolator);
        collapseValueAnimator.addUpdateListener(this);
        collapseValueAnimator.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                animating = true;
                hideBlur();
                maskView.reset();
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animating = false;
                expanded = false;
                if (rotateValueAnimator == null) {
                    detachMask();
                } else {
                    if (expandAnimDuration >= rotateAnimDuration) {
                        detachMask();
                    }//else call detachMask() until rotateValueAnimator ended
                }
            }
        });

        if (mainButtonRotateDegree == 0) {
            return;
        }

        rotateValueAnimator = ValueAnimator.ofFloat(0, 1);
        rotateValueAnimator.setDuration(rotateAnimDuration);
        rotateValueAnimator.addUpdateListener(this);
        rotateValueAnimator.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                if (!expanded && expandAnimDuration < rotateAnimDuration) {
                    //call detachMask() when rotateValueAnimator ended
                    detachMask();
                }
            }
        });
    }

    /**
     * !!! Call THIS method to create the callback, NOT setOnClickListener !!!
     */
    public void setButtonEventListener(ButtonEventListener listener) {
        buttonEventListener = listener;
    }

    public void setExpandAnimatorInterpolator(Interpolator interpolator) {
        if (interpolator != null) {
            expandValueAnimator.setInterpolator(interpolator);
        }
    }

    public void setCollapseAnimatorInterpolator(Interpolator interpolator) {
        if (interpolator != null) {
            collapseValueAnimator.setInterpolator(interpolator);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public SubButtonsBtn setButtonDatas(List<ButtonData> buttonDatas) {
        if (buttonDatas == null || buttonDatas.isEmpty()) {
            return this;
        }
        this.buttonDatas = new ArrayList<>(buttonDatas);
        if (isSelectionMode) {
            try {
                this.buttonDatas.add(0, (ButtonData) buttonDatas.get(0).clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        buttonRects = new HashMap<>(this.buttonDatas.size());
        for (int i = 0, size = this.buttonDatas.size(); i < size; i++) {
            ButtonData buttonData = this.buttonDatas.get(i);
            buttonData.setIsMainButton(i == 0);
            int buttonSizePx = buttonData.isMainButton() ? mainButtonSizePx : subButtonSizePx;
            RectF rectF = new RectF(buttonSideMarginPx, buttonSideMarginPx
                    , buttonSizePx + buttonSideMarginPx, buttonSizePx + buttonSideMarginPx);
            buttonRects.put(buttonData, rectF);
        }
        subButtonAngleCalculator = new SubButtonAngleCalculator(startAngle, endAngle, this.buttonDatas.size() - 1);
        return this;
    }

    public List<ButtonData> getButtonDatas() {
        return this.buttonDatas;
    }

    private ButtonData getMainButtonData() {
        return this.buttonDatas.get(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //the button size is only decided by mainButtonSizePx and buttonSideMarginPx that you configure in xml
        int desiredWidth = mainButtonSizePx + buttonSideMarginPx * 2;
        int desiredHeight = mainButtonSizePx + buttonSideMarginPx * 2;

        setMeasuredDimension(desiredWidth, desiredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawButton(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initButtonInfo();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        pressPointF.set(event.getRawX(), event.getRawY());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (checker.isFast()) {
                    return false;
                }

                pressInButton = true;
                boolean executeActionUp = !animating && buttonDatas != null && !buttonDatas.isEmpty();
                if (executeActionUp) {
                    updatePressState(0, true);
                }
                return executeActionUp;

            case MotionEvent.ACTION_MOVE:
                updatePressPosition(0, rawButtonRectF);
                break;

            case MotionEvent.ACTION_UP:
                if (!isPointInRectF(pressPointF, rawButtonRectF)) {
                    return true;    // event consumed
                }
                updatePressState(0, false);
                expand();
                return true;
        }
        return super.onTouchEvent(event);
    }


    /**
     * used for update press effect when finger move
     *
     * @param rectF the rectF of the button to present button position
     */
    private void updatePressPosition(int buttonIndex, RectF rectF) {
        if (buttonIndex < 0) {
            return;
        }
        if (isPointInRectF(pressPointF, rectF)) {
            if (!pressInButton) {
                updatePressState(buttonIndex, true);
                pressInButton = true;
            }
        } else {
            if (pressInButton) {
                updatePressState(buttonIndex, false);
                pressInButton = false;
            }
        }
    }

    /**
     * Returns if the given point is within the bounds of the given rect.
     * The edges are considered INSIDE.
     */
    private boolean isPointInRectF(PointF pointF, RectF rectF) {
        return (pointF.x >= rectF.left) &&
                (pointF.x <= rectF.right) &&
                (pointF.y >= rectF.top) &&
                (pointF.y <= rectF.bottom);
    }

    private void updatePressState(int buttonIndex, boolean down) {
        if (buttonIndex < 0) {
            return;
        }
        ButtonData buttonData = buttonDatas.get(buttonIndex);
        if (down) {
            pressTmpColor = buttonData.getBackgroundColor();
            buttonData.setBackgroundColor(getPressedColor(pressTmpColor));
        } else {
            buttonData.setBackgroundColor(pressTmpColor);
        }
        if (expanded) {
            maskView.invalidate();
        } else {
            invalidate();
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        if (valueAnimator == expandValueAnimator || valueAnimator == collapseValueAnimator) {
            expandProgress = (float) valueAnimator.getAnimatedValue();
        }
        if (valueAnimator == rotateValueAnimator) {
            rotateProgress = (float) valueAnimator.getAnimatedValue();
        }
        if (maskAttached) {
            maskView.updateButtons();
            maskView.invalidate();
        }
    }

    private void expand() {
        if (expandValueAnimator.isRunning()) {
            expandValueAnimator.cancel();
        }
        expandValueAnimator.start();
        startRotateAnimator(true);
        if (buttonEventListener != null) {
            buttonEventListener.onExpand();
        }
    }

    private void collapse() {
        if (collapseValueAnimator.isRunning()) {
            collapseValueAnimator.cancel();
        }
        collapseValueAnimator.start();
        startRotateAnimator(false);
        if (buttonEventListener != null) {
            buttonEventListener.onCollapse();
        }
    }

    private void startRotateAnimator(boolean expand) {
        if (rotateValueAnimator != null) {
            if (rotateValueAnimator.isRunning()) {
                rotateValueAnimator.cancel();
            }
            if (expand) {
                rotateValueAnimator.setInterpolator(overshootInterpolator);
                rotateValueAnimator.setFloatValues(0, 1);
            } else {
                rotateValueAnimator.setInterpolator(anticipateInterpolator);
                rotateValueAnimator.setFloatValues(1, 0);
            }
            rotateValueAnimator.start();
        }
    }

    private void attachMask() {
        if (maskView == null) {
            maskView = new MaskView(getContext(), this);
        }

        if (!maskAttached && !showBlur()) {
            ViewGroup root = (ViewGroup) getRootView();
            root.addView(maskView);
            maskAttached = true;
            maskView.reset();
            maskView.initButtonRect();
            maskView.onClickMainButton();
        }
    }

    private boolean showBlur() {
        if (!blurBackground) {
            return false;
        }

        //set invisible to avoid be blurred that resulting in show the blurred button edge when expanded,
        //must be called before do blur
        setVisibility(INVISIBLE);

        final ViewGroup root = (ViewGroup) getRootView();
        root.setDrawingCacheEnabled(true);
        Bitmap bitmap = root.getDrawingCache();
        checkBlurRadius();

        blur.setParams(new Blur.Callback() {
            @Override
            public void onBlurred(Bitmap blurredBitmap) {
                blurImageView.setImageBitmap(blurredBitmap);
                root.setDrawingCacheEnabled(false);
                root.addView(blurImageView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                blurAnimator = ObjectAnimator.ofFloat(blurImageView, "alpha", 0.0f, 1.0f).setDuration(expandAnimDuration);
                if (blurListener != null) {
                    blurAnimator.removeListener(blurListener);
                }
                blurAnimator.start();

                root.addView(maskView);
                maskAttached = true;
                maskView.reset();
                maskView.initButtonRect();
                maskView.onClickMainButton();
            }
        }, getContext(), bitmap, blurRadius);
        blur.execute();

        return true;
    }

    private void checkBlurRadius() {
        if (blurRadius <= 0 || blurRadius > 25) {
            blurRadius = DEFAULT_BLUR_RADIUS;
        }
    }

    private void hideBlur() {
        if (!blurBackground) {
            return;
        }

        setVisibility(VISIBLE);

        final ViewGroup root = (ViewGroup) getRootView();
        blurAnimator.setFloatValues(1.0f, 0.0f);
        if (blurListener == null) {
            blurListener = new SimpleAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    root.removeView(blurImageView);
                }
            };
        }
        blurAnimator.addListener(blurListener);
        blurAnimator.start();
    }

    private void detachMask() {
        if (maskAttached) {
            ViewGroup root = (ViewGroup) getRootView();
            root.removeView(maskView);
            maskAttached = false;
            for (int i = 0; i < buttonDatas.size(); i++) {
                ButtonData buttonData = buttonDatas.get(i);
                RectF rectF = buttonRects.get(buttonData);
                int size = buttonData.isMainButton() ? mainButtonSizePx : subButtonSizePx;
                //noinspection ConstantConditions
                rectF.set(buttonSideMarginPx, buttonSideMarginPx, buttonSideMarginPx + size, buttonSideMarginPx + size);
            }
        }

        // inform anyone who cares that we're done collapsing. And give 'em a chance
        // to make changes before this button is redrawn.
        buttonEventListener.onCollapseFinished();
        invalidate();
    }

    private void resetRippleInfo() {
        rippleInfo.buttonIndex = Integer.MIN_VALUE;
        rippleInfo.pressX = 0;
        rippleInfo.pressY = 0;
        rippleInfo.rippleRadius = 0;
    }

    private void drawButton(Canvas canvas) {
        if (buttonDatas == null || buttonDatas.isEmpty()) {
            return;
        }

        ButtonData buttonData = getMainButtonData();
        drawButton(canvas, paint, buttonData);
    }

    /**
     * this method is called by both {@link SubButtonsBtn} and {@link MaskView} at draw process
     */
    private void drawButton(Canvas canvas, Paint paint, ButtonData buttonData) {
        drawShadow(canvas, paint, buttonData);
        drawContent(canvas, paint, buttonData);
        drawRipple(canvas, paint, buttonData);
    }

    private void drawShadow(Canvas canvas, Paint paint, ButtonData buttonData) {
        if (buttonElevationPx <= 0) {
            return;
        }

        float left, top;
        Bitmap bitmap;
        if (buttonData.isMainButton()) {
            mainShadowBitmap = getButtonShadowBitmap(buttonData);
            bitmap = mainShadowBitmap;
        } else {
            subShadowBitmap = getButtonShadowBitmap(buttonData);
            bitmap = subShadowBitmap;
        }

        int shadowOffset = buttonElevationPx / 2;
        RectF rectF = buttonRects.get(buttonData);
        //noinspection ConstantConditions
        left = rectF.centerX() - bitmap.getWidth() / 2f;
        top = rectF.centerY() - bitmap.getHeight() / 2 + shadowOffset;
        shadowMatrix.reset();
        if (!buttonData.isMainButton()) {
            shadowMatrix.postScale(expandProgress, expandProgress, bitmap.getWidth() / 2, bitmap.getHeight() / 2 + shadowOffset);
        }
        shadowMatrix.postTranslate(left, top);
        if (buttonData.isMainButton()) {
            //shadow did not need to perform rotate as button,so rotate reverse
            shadowMatrix.postRotate(-mainButtonRotateDegree * rotateProgress, rectF.centerX(), rectF.centerY());
        }
        paint.setAlpha(255);
        canvas.drawBitmap(bitmap, shadowMatrix, paint);
    }

    private void drawContent(Canvas canvas, Paint paint, ButtonData buttonData) {
        paint.setAlpha(255);
        paint.setColor(buttonData.getBackgroundColor());


        RectF rectF = buttonRects.get(buttonData);
        //noinspection ConstantConditions
        canvas.drawOval(rectF, paint);

        switch (buttonData.getButtonType()) {

            case BOTH:
                // the same as for the TEXT case
                if (buttonData.getTexts() == null) {
                    throw new IllegalArgumentException("buttonType uses text, text cannot be null");
                }
                {
                    String[] texts = buttonData.getTexts();
                    int sizePx = buttonData.isMainButton() ? mainButtonTextSize : subButtonTextSize;
                    int textColor = buttonData.isMainButton() ? mainButtonTextColor : subButtonTextColor;
                    textPaint = getTextPaint(sizePx, textColor);
                    drawTexts(texts, canvas, rectF.centerX(), rectF.centerY());
                }
                // falls through to ICON

            case ICON:
                Drawable drawable = buttonData.getIcon();
                if (drawable == null) {
                    throw new IllegalArgumentException("buttonType uses icon, drawable cannot be null");
                }
                int left = (int) rectF.left + dp2px(getContext(), buttonData.getIconPaddingDp());
                int right = (int) rectF.right - dp2px(getContext(), buttonData.getIconPaddingDp());
                int top = (int) rectF.top + dp2px(getContext(), buttonData.getIconPaddingDp());
                int bottom = (int) rectF.bottom - dp2px(getContext(), buttonData.getIconPaddingDp());
                drawable.setBounds(left, top, right, bottom);
                drawable.draw(canvas);
                break;

            case TEXT:
                if (buttonData.getTexts() == null) {
                    throw new IllegalArgumentException("buttonType uses text, text cannot be null");
                }
                String[] texts = buttonData.getTexts();
                int sizePx = buttonData.isMainButton() ? mainButtonTextSize : subButtonTextSize;
                int textColor = buttonData.isMainButton() ? mainButtonTextColor : subButtonTextColor;
                textPaint = getTextPaint(sizePx, textColor);
                drawTexts(texts, canvas, rectF.centerX(), rectF.centerY());
                break;

        }

    }

    /**
     * draw texts in rows
     */
    private void drawTexts(String[] strings, Canvas canvas, float x, float y) {
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;
        int length = strings.length;
        float total = (length - 1) * (-top + bottom) + (-fontMetrics.ascent + fontMetrics.descent);
        float offset = total / 2 - bottom;
        for (int i = 0; i < length; i++) {
            float yAxis = -(length - i - 1) * (-top + bottom) + offset;
            canvas.drawText(strings[i], x, y + yAxis, textPaint);
        }
    }

    private void drawRipple(Canvas canvas, Paint paint, ButtonData buttonData) {
        int pressIndex = buttonDatas.indexOf(buttonData);
        if (!rippleEffect || pressIndex == -1 || pressIndex != rippleInfo.buttonIndex) {
            return;
        }

        paint.setColor(rippleInfo.rippleColor);
        paint.setAlpha(128);
        canvas.save();
        if (ripplePath == null) {
            ripplePath = new Path();
        }
        ripplePath.reset();
        RectF rectF = buttonRects.get(buttonData);
        //noinspection ConstantConditions
        float radius = rectF.right - rectF.centerX();
        ripplePath.addCircle(rectF.centerX(), rectF.centerY(), radius, Path.Direction.CW);
        canvas.clipPath(ripplePath);
        canvas.drawCircle(rippleInfo.pressX, rippleInfo.pressY, rippleInfo.rippleRadius, paint);
        canvas.restore();
    }

    private Bitmap getButtonShadowBitmap(ButtonData buttonData) {
        if (buttonData.isMainButton()) {
            if (mainShadowBitmap != null) {
                return mainShadowBitmap;
            }
        } else {
            if (subShadowBitmap != null) {
                return subShadowBitmap;
            }
        }

        int buttonSizePx = buttonData.isMainButton() ? mainButtonSizePx : subButtonSizePx;
        int buttonRadius = buttonSizePx / 2;
        int bitmapRadius = buttonRadius + buttonElevationPx;
        int bitmapSize = bitmapRadius * 2;
        Bitmap bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(0x0);
        int colors[] = {ColorUtils.setAlphaComponent(BUTTON_SHADOW_COLOR, BUTTON_SHADOW_ALPHA),
                ColorUtils.setAlphaComponent(BUTTON_SHADOW_COLOR, 0)};
        float stops[] = {(float) (buttonRadius - buttonElevationPx) / (float) bitmapRadius, 1};
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new RadialGradient(bitmapRadius, bitmapRadius, bitmapRadius, colors, stops, Shader.TileMode.CLAMP));
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(0, 0, bitmapSize, bitmapSize, paint);
        if (buttonData.isMainButton()) {
            mainShadowBitmap = bitmap;
            return mainShadowBitmap;
        } else {
            subShadowBitmap = bitmap;
            return subShadowBitmap;
        }
    }

    private Paint getTextPaint(int sizePx, int color) {
        if (textPaint == null) {
            textPaint = new Paint();
            textPaint.setAntiAlias(true);
            textPaint.setTextAlign(Paint.Align.CENTER);
        }

        textPaint.setTextSize(sizePx);
        textPaint.setColor(color);
        return textPaint;
    }

    private void initButtonInfo() {
        getGlobalVisibleRect(rawButtonRect);
        rawButtonRectF.set(rawButtonRect.left, rawButtonRect.top, rawButtonRect.right, rawButtonRect.bottom);
    }

    /**
     * Sets the location of this button, using the center of the button as the
     * coordinate location (instead of the top left as usual).
     *
     * NOTE that the input coords are relative to the parent, not the screen.
     */
    public void setXYCenter(float parentX, float parentY) {
        measure(0,0);     // forces view to measure itself
        float offset = getMeasuredWidth() / 2f; // need to get the MEASURED width, not getWidth()

        setX(parentX - offset);
        setY(parentY - offset);
    }

    /** Returns the center of this view */
    public PointF getCenter() {
        return new PointF(getX() + (getWidth() / 2f), getY() + (getHeight() / 2f));
    }

    private int getLighterColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 1.1f;
        return Color.HSVToColor(hsv);
    }

    private int getDarkerColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f;
        return Color.HSVToColor(hsv);
    }

    private int getPressedColor(int color) {
        return getDarkerColor(color);
    }

    public int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public float getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(float startAngle) {
        this.startAngle = startAngle;
    }

    public float getEndAngle() {
        return endAngle;
    }

    public void setEndAngle(float endAngle) {
        this.endAngle = endAngle;
    }

    public int getButtonGapPx() {
        return buttonGapPx;
    }

    public void setButtonGapPx(int buttonGapPx) {
        this.buttonGapPx = buttonGapPx;
    }

    public int getMainButtonRotateDegree() {
        return mainButtonRotateDegree;
    }

    public void setMainButtonRotateDegree(int mainButtonRotateDegree) {
        this.mainButtonRotateDegree = mainButtonRotateDegree;
    }

    public int getRotateAnimDuration() {
        return rotateAnimDuration;
    }

    public void setRotateAnimDuration(int rotateAnimDuration) {
        this.rotateAnimDuration = rotateAnimDuration;
    }

    public int getMainButtonSizePx() {
        return mainButtonSizePx;
    }

    public void setMainButtonSizePx(int mainButtonSizePx) {
        this.mainButtonSizePx = mainButtonSizePx;
    }

    public int getSubButtonSizePx() {
        return subButtonSizePx;
    }

    public void setSubButtonSizePx(int subButtonSizePx) {
        this.subButtonSizePx = subButtonSizePx;
    }

    public int getMainButtonTextSize() {
        return mainButtonTextSize;
    }

    public void setMainButtonTextSize(int mainButtonTextSize) {
        this.mainButtonTextSize = mainButtonTextSize;
    }

    public int getSubButtonTextSize() {
        return subButtonTextSize;
    }

    public void setSubButtonTextSize(int subButtonTextSize) {
        this.subButtonTextSize = subButtonTextSize;
    }

    public int getMainButtonTextColor() {
        return mainButtonTextColor;
    }

    public void setMainButtonTextColor(int mainButtonTextColor) {
        this.mainButtonTextColor = mainButtonTextColor;
    }

    public int getSubButtonTextColor() {
        return subButtonTextColor;
    }

    public void setSubButtonTextColor(int subButtonTextColor) {
        this.subButtonTextColor = subButtonTextColor;
    }

    public int getExpandAnimDuration() {
        return expandAnimDuration;
    }

    public void setExpandAnimDuration(int expandAnimDuration) {
        this.expandAnimDuration = expandAnimDuration;
    }

    public int getMaskBackgroundColor() {
        return maskBackgroundColor;
    }

    public void setMaskBackgroundColor(int maskBackgroundColor) {
        this.maskBackgroundColor = maskBackgroundColor;
    }

    public int getButtonElevationPx() {
        return buttonElevationPx;
    }

    public void setButtonElevationPx(int buttonElevationPx) {
        this.buttonElevationPx = buttonElevationPx;
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public void setSelectionMode(boolean selectionMode) {
        isSelectionMode = selectionMode;
    }

    public boolean isRippleEffect() {
        return rippleEffect;
    }

    public void setRippleEffect(boolean rippleEffect) {
        this.rippleEffect = rippleEffect;
    }

    public int getRippleColor() {
        return rippleColor;
    }

    public void setRippleColor(int rippleColor) {
        this.rippleColor = rippleColor;
    }

    public boolean isBlurBackground() {
        return blurBackground;
    }

    public void setBlurBackground(boolean blurBackground) {
        this.blurBackground = blurBackground;
    }

    public float getBlurRadius() {
        return blurRadius;
    }

    public void setBlurRadius(float blurRadius) {
        this.blurRadius = blurRadius;
    }


    //---------------------------------
    //  classes
    //---------------------------------

    /**
     * Stores ripple effect params
     */
    private static class RippleInfo {
        float pressX;
        float pressY;
        float rippleRadius;
        int buttonIndex;
        int rippleColor = Integer.MIN_VALUE;
    }


    /**
     * Allows touch events to be captured outside of the main button area.  Only active
     * after expansion.
     */
    @SuppressLint("ViewConstructor")
    private static class MaskView extends View {
        private SubButtonsBtn subButtonsBtn;
        private RectF initialSubButtonRectF;//all of the sub button's initial rectF
        private RectF touchRectF;//set when one of buttons are touched
        private ValueAnimator touchRippleAnimator;
        private Paint paint;
        private Map<ButtonData, ExpandMoveCoordinate> expandedCoordinateMap;
        private int rippleState;
        private float rippleRadius;
        private int clickIndex = 0;
        private Matrix[] matrixArray;//each button has a Matrix to perform expand/collapse animation
        private FastClickChecker checker;

        private static final int IDLE = 0;
        private static final int RIPPLING = 1;
        private static final int RIPPLED = 2;

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({IDLE, RIPPLING, RIPPLED})
        private @interface RippleState {

        }

        private static class ExpandMoveCoordinate {
            float moveX;
            float moveY;

            /**
             * the members are set by getMoveX() and getMoveY() of {@link SubButtonAngleCalculator}
             */
            ExpandMoveCoordinate(float moveX, float moveY) {
                this.moveX = moveX;
                this.moveY = moveY;
            }
        }

        public MaskView(Context context, SubButtonsBtn button) {
            super(context);
            subButtonsBtn = button;

            checker = new FastClickChecker(subButtonsBtn.checkThreshold);

            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);

            matrixArray = new Matrix[subButtonsBtn.buttonDatas.size()];
            for (int i = 0; i < matrixArray.length; i++) {
                matrixArray[i] = new Matrix();
            }

            initialSubButtonRectF = new RectF();
            touchRectF = new RectF();

            expandedCoordinateMap = new HashMap<>(subButtonsBtn.buttonDatas.size());
            setBackgroundColor(subButtonsBtn.maskBackgroundColor);

            touchRippleAnimator = ValueAnimator.ofFloat(0, 1);
            touchRippleAnimator.setDuration((long) ((float) subButtonsBtn.expandAnimDuration * 0.9f));
            touchRippleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float animateProgress = (float) valueAnimator.getAnimatedValue();
                    subButtonsBtn.rippleInfo.rippleRadius = rippleRadius * animateProgress;
                }
            });
            touchRippleAnimator.addListener(new SimpleAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    subButtonsBtn.rippleInfo.rippleRadius = 0;
                    setRippleState(RIPPLED);
                }
            });
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            View root = getRootView();
            setMeasuredDimension(root.getWidth(), root.getHeight());
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            drawButtons(canvas, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            subButtonsBtn.pressPointF.set(event.getX(), event.getY());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (checker.isFast()) {
                        return false;
                    }
                    clickIndex = getTouchedButtonIndex();

                    if (subButtonsBtn.expanded) {
                        subButtonsBtn.updatePressState(clickIndex, true);
                    }
                    subButtonsBtn.pressInButton = true;
                    return subButtonsBtn.expanded;

                case MotionEvent.ACTION_MOVE:
                    subButtonsBtn.updatePressPosition(clickIndex, touchRectF);
                    break;

                case MotionEvent.ACTION_UP:
                    if (!subButtonsBtn.isPointInRectF(subButtonsBtn.pressPointF, touchRectF)) {
                        if (clickIndex < 0) {
                            subButtonsBtn.collapse();
                        }
                        return true;
                    }
                    subButtonsBtn.updatePressState(clickIndex, false);
                    onButtonPressed();
                    break;
            }
            return super.onTouchEvent(event);
        }

        private void reset() {
            setRippleState(IDLE);
        }

        private void setRippleState(@RippleState int state) {
            rippleState = state;
        }

        @RippleState
        private int getRippleState() {
            return rippleState;
        }

        public void onClickMainButton() {
            clickIndex = 0;
        }

        protected void onButtonPressed() {
            if (subButtonsBtn.buttonEventListener != null) {
                if (clickIndex > 0) {
                    subButtonsBtn.buttonEventListener.onPopupButtonClicked(clickIndex);
                }
            }

            if (subButtonsBtn.isSelectionMode) {
                if (clickIndex > 0) {
                    ButtonData buttonData = subButtonsBtn.buttonDatas.get(clickIndex);
                    ButtonData mainButton = subButtonsBtn.getMainButtonData();

                    switch (buttonData.getButtonType()) {
                        case ICON:
                            mainButton.setButtonType(ButtonData.ButtonType.ICON);
                            mainButton.setIcon(buttonData.getIcon());
                            break;

                        case TEXT:
                            mainButton.setButtonType(ButtonData.ButtonType.TEXT);
                            mainButton.setTexts(buttonData.getTexts());
                            break;

                        case BOTH:
                            mainButton.setButtonType(ButtonData.ButtonType.BOTH);
                            mainButton.setIcon(buttonData.getIcon());
                            mainButton.setTexts(buttonData.getTexts());
                            break;
                    }

                    mainButton.setBackgroundColor(buttonData.getBackgroundColor());
                }
            }
            subButtonsBtn.collapse();
        }

        private int getTouchedButtonIndex() {
            for (int i = 0; i < subButtonsBtn.buttonDatas.size(); i++) {
                ButtonData buttonData = subButtonsBtn.buttonDatas.get(i);
                ExpandMoveCoordinate coordinate = expandedCoordinateMap.get(buttonData);
                if (i == 0) {
                    RectF rectF = subButtonsBtn.buttonRects.get(buttonData);
                    touchRectF.set(rectF);
                } else {
                    if (coordinate == null) {
                        Log.e(TAG, "getTouchedButtonIndex() could not get ExpandMoveCoordinate!");
                    }
                    touchRectF.set(initialSubButtonRectF);
                    //noinspection ConstantConditions
                    touchRectF.offset(coordinate.moveX, -coordinate.moveY);
                }

                if (subButtonsBtn.isPointInRectF(subButtonsBtn.pressPointF, touchRectF)) {
                    return i;
                }
            }
            return -1;
        }

        private void initButtonRect() {
            for (int i = 0; i < subButtonsBtn.buttonDatas.size(); i++) {
                ButtonData buttonData = subButtonsBtn.buttonDatas.get(i);
                RectF rectF = subButtonsBtn.buttonRects.get(buttonData);
                if (i == 0) {
                    //noinspection ConstantConditions
                    rectF.left = subButtonsBtn.rawButtonRectF.left + subButtonsBtn.buttonSideMarginPx;
                    rectF.right = subButtonsBtn.rawButtonRectF.right - subButtonsBtn.buttonSideMarginPx;
                    rectF.top = subButtonsBtn.rawButtonRectF.top + subButtonsBtn.buttonSideMarginPx;
                    rectF.bottom = subButtonsBtn.rawButtonRectF.bottom - subButtonsBtn.buttonSideMarginPx;
                } else {
                    //noinspection ConstantConditions
                    float leftTmp = rectF.left;
                    float topTmp = rectF.top;
                    int buttonRadius = subButtonsBtn.subButtonSizePx / 2;
                    rectF.left = leftTmp + subButtonsBtn.rawButtonRectF.centerX() - subButtonsBtn.buttonSideMarginPx - buttonRadius;
                    rectF.right = leftTmp + subButtonsBtn.rawButtonRectF.centerX() - subButtonsBtn.buttonSideMarginPx + buttonRadius;
                    rectF.top = topTmp + subButtonsBtn.rawButtonRectF.centerY() - subButtonsBtn.buttonSideMarginPx - buttonRadius;
                    rectF.bottom = topTmp + subButtonsBtn.rawButtonRectF.centerY() - subButtonsBtn.buttonSideMarginPx + buttonRadius;
                    initialSubButtonRectF.set(rectF);
                    touchRectF.set(rectF);
                }
            }
        }

        /**
         * called before draw an expand/collapse frame
         */
        private void updateButtons() {
            List<ButtonData> buttonDatas = subButtonsBtn.buttonDatas;
            int mainButtonRadius = subButtonsBtn.mainButtonSizePx / 2;
            int subButtonRadius = subButtonsBtn.subButtonSizePx / 2;
            Matrix matrix = matrixArray[0];
            matrix.reset();
            matrix.postRotate(subButtonsBtn.mainButtonRotateDegree * subButtonsBtn.rotateProgress
                    , subButtonsBtn.rawButtonRectF.centerX(), subButtonsBtn.rawButtonRectF.centerY());
            for (int i = 1; i < buttonDatas.size(); i++) {
                matrix = matrixArray[i];
                ButtonData buttonData = buttonDatas.get(i);
                matrix.reset();
                if (subButtonsBtn.expanded) {
                    ExpandMoveCoordinate coordinate = expandedCoordinateMap.get(buttonData);

                    //noinspection ConstantConditions
                    float dx = subButtonsBtn.expandProgress * (coordinate.moveX);
                    float dy = subButtonsBtn.expandProgress * (-coordinate.moveY);
                    matrix.postTranslate(dx, dy);
                } else {
                    int radius = mainButtonRadius + subButtonRadius + subButtonsBtn.buttonGapPx;
                    float moveX;
                    float moveY;
                    ExpandMoveCoordinate coordinate = expandedCoordinateMap.get(buttonData);
                    if (coordinate == null) {
                        moveX = subButtonsBtn.subButtonAngleCalculator.getMoveX(radius, i);
                        moveY = subButtonsBtn.subButtonAngleCalculator.getMoveY(radius, i);
                        coordinate = new ExpandMoveCoordinate(moveX, moveY);
                        expandedCoordinateMap.put(buttonData, coordinate);
                    } else {
                        moveX = coordinate.moveX;
                        moveY = coordinate.moveY;
                    }
                    float dx = subButtonsBtn.expandProgress * (moveX);
                    float dy = subButtonsBtn.expandProgress * (-moveY);
                    matrix.postTranslate(dx, dy);
                }
            }
        }

        private void drawButtons(Canvas canvas, Paint paint) {
            for (int i = subButtonsBtn.buttonDatas.size() - 1; i >= 0; i--) {
                canvas.save();
                canvas.concat(matrixArray[i]);
                ButtonData buttonData = subButtonsBtn.buttonDatas.get(i);
                subButtonsBtn.drawButton(canvas, paint, buttonData);
                if (i == 0 && clickIndex == 0) {
                    performRipple();
                }
                canvas.restore();
            }
        }

        private void performRipple() {
            if (getRippleState() == IDLE) {
                ripple(0, subButtonsBtn.pressPointF.x, subButtonsBtn.pressPointF.y);
                setRippleState(RIPPLING);
            }
        }

        private void ripple(int index, float pressX, float pressY) {
            if (index < 0 || !subButtonsBtn.rippleEffect) {
                return;
            }
            subButtonsBtn.resetRippleInfo();
            ButtonData buttonData = subButtonsBtn.buttonDatas.get(index);
            RectF rectF = subButtonsBtn.buttonRects.get(buttonData);

            //noinspection ConstantConditions
            float centerX = rectF.centerX();
            float centerY = rectF.centerY();
            float radius = rectF.centerX() - rectF.left;
            float distanceX = pressX - centerX;
            float distanceY = pressY - centerY;
            float pressToCenterDistance = (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY);
            if (pressToCenterDistance > radius) {
                //press out of the button circle_solve_normal
                return;
            }
            subButtonsBtn.rippleInfo.pressX = pressX;
            subButtonsBtn.rippleInfo.pressY = pressY;
            subButtonsBtn.rippleInfo.buttonIndex = index;
            subButtonsBtn.rippleInfo.rippleRadius = radius + pressToCenterDistance;
            subButtonsBtn.rippleInfo.rippleColor = getRippleColor(subButtonsBtn.rippleColor == Integer.MIN_VALUE ?
                    buttonData.getBackgroundColor() : subButtonsBtn.rippleColor);

            rippleRadius = subButtonsBtn.rippleInfo.rippleRadius;
            startRippleAnimator();
        }

        private int getRippleColor(int color) {
            if (subButtonsBtn.rippleColor != Integer.MIN_VALUE) {
                return subButtonsBtn.rippleColor;
            }
            if (subButtonsBtn.rippleInfo.rippleColor != Integer.MIN_VALUE) {
                return subButtonsBtn.rippleInfo.rippleColor;
            }

            if (color == subButtonsBtn.getLighterColor(color)) {
                return subButtonsBtn.getDarkerColor(color);
            } else {
                return subButtonsBtn.getLighterColor(color);
            }
        }

        private void startRippleAnimator() {
            if (touchRippleAnimator.isRunning()) {
                touchRippleAnimator.cancel();
            }
            touchRippleAnimator.start();
        }
    } // class MaskView


    // todo
    private static class SimpleAnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            Log.d(TAG, "SimpleAnimatorListener.onAnimationEnd()");
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }

}
