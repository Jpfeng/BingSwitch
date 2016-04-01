package com.jpfeng.bingswitch;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.CompoundButton;
import android.widget.Scroller;

public class BingSwitch extends CompoundButton {

    private static final String TAG = "BingSwitch";

    private static final int INNER_MARGIN_DP = 5;
    private static final int ANIM_DURATION = 250;

    private static final int IDLE = 0x0;
    private static final int TOUCHING = 0x1;
    private static final int DRAGGING = 0x2;

    private static Drawable mTrackUncheckedNormal;
    private static Drawable mTrackCheckedNormal;
    private static Drawable mTrackTouching;
    private static Drawable mThumbWhite;
    private static Drawable mThumbBlack;

    private int mMinWidth;
    private int mMinHeight;
    private int mThumbWidth;
    private int mThumbHeight;
    private int mInnerMarginPx;

    private int mMeasuredWidth;
    private int mSwitchTop;
    private int mPaddingTop;
    private int mPaddingRight;
    private int mThumbPositionOn;
    private int mThumbPositionOff;
    private int mThumbPositionHalf;

    private boolean mIsChecked = false;
    private int mTouchMode = IDLE;
    private int mDownX;
    private int mDownThumbX;
    private int mCurrentThumbPosition;

    private OnCheckedChangeListener mCheckedChangeListener;
    private Scroller mScroller;
    private boolean mPrintLog = false;

    public BingSwitch(Context context) {
        this(context, null, 0);
    }

    public BingSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BingSwitch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        Resources res = getResources();
        mTrackUncheckedNormal = res.getDrawable(R.drawable.track_unchecked_normal);
        mTrackCheckedNormal = res.getDrawable(R.drawable.track_checked_normal);
        mTrackTouching = res.getDrawable(R.drawable.track_touching);
        mThumbBlack = res.getDrawable(R.drawable.thumb_black);
        mThumbWhite = res.getDrawable(R.drawable.thumb_white);

        float density = context.getResources().getDisplayMetrics().density;
        mInnerMarginPx = dp2px(INNER_MARGIN_DP, density);

        mScroller = new Scroller(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);

        mPaddingTop = getPaddingTop();
        mPaddingRight = getPaddingRight();
        int paddingLeft = getPaddingLeft();
        int paddingBottom = getPaddingBottom();

        if (null != mTrackUncheckedNormal) {
            mMinWidth = mTrackUncheckedNormal.getIntrinsicWidth();
            mMinHeight = mTrackUncheckedNormal.getIntrinsicHeight();
        } else {
            mMinWidth = 0;
            mMinHeight = 0;
        }

        if (null != mThumbBlack) {
            mThumbWidth = mThumbBlack.getIntrinsicWidth();
            mThumbHeight = mThumbBlack.getIntrinsicHeight();
        } else {
            mThumbWidth = 0;
            mThumbHeight = 0;
        }

        int switchWidth = paddingLeft + mPaddingRight + mMinWidth;
        int switchHeight = mPaddingTop + paddingBottom + mMinHeight;

        switch (wMode) {
            case MeasureSpec.EXACTLY:
                mMeasuredWidth = Math.max(wSize, mMinWidth);
                break;

            case MeasureSpec.AT_MOST:
                mMeasuredWidth = Math.min(wSize, switchWidth);
                break;

            default:
                mMeasuredWidth = Math.max(switchWidth, mMinWidth);
                break;
        }

        int measuredHeight;
        switch (hMode) {
            case MeasureSpec.EXACTLY:
                measuredHeight = Math.max(hSize, mMinHeight);
                break;

            case MeasureSpec.AT_MOST:
                measuredHeight = Math.min(hSize, switchHeight);
                break;

            default:
                measuredHeight = Math.max(switchHeight, mMinHeight);
                break;
        }

        switch (getGravity() & Gravity.VERTICAL_GRAVITY_MASK) {
            default:
            case Gravity.CENTER_VERTICAL:
                mSwitchTop = (measuredHeight - mPaddingTop - mMinHeight - paddingBottom) / 2 + mPaddingTop;
                break;

            case Gravity.TOP:
                mSwitchTop = mPaddingTop;
                break;

            case Gravity.BOTTOM:
                mSwitchTop = measuredHeight - paddingBottom - mMinHeight;
                break;
        }

        mThumbPositionOff = mMeasuredWidth - mPaddingRight - mMinWidth + mInnerMarginPx;
        mThumbPositionOn = mMeasuredWidth - mPaddingRight - mInnerMarginPx - mThumbWidth;
        mThumbPositionHalf = mThumbPositionOff + (mThumbPositionOn - mThumbPositionOff) / 2;
        mCurrentThumbPosition = mIsChecked ? mThumbPositionOn : mThumbPositionOff;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(mMeasuredWidth, measuredHeight);

        printDebugLog("onMeasure: done");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Drawable track;
        Drawable thumb;

        switch (mTouchMode) {
            case IDLE:
            default:
                track = mIsChecked ? mTrackCheckedNormal : mTrackUncheckedNormal;
                thumb = mIsChecked ? mThumbWhite : mThumbBlack;
                break;

            case TOUCHING:
                track = mTrackTouching;
                thumb = mThumbWhite;
                break;

            case DRAGGING:
                track = mTrackTouching;
                thumb = mThumbWhite;
                break;
        }

        int trackR = mMeasuredWidth - mPaddingRight;
        int trackT = mSwitchTop;
        int trackL = trackR - mMinWidth;
        int trackB = trackT + mMinHeight;
        track.setBounds(trackL, trackT, trackR, trackB);
        track.draw(canvas);

        int thumbL = mCurrentThumbPosition;
        int thumbT = mSwitchTop + mInnerMarginPx;
        int thumbR = thumbL + mThumbWidth;
        int thumbB = thumbT + mThumbHeight;
        thumb.setBounds(thumbL, thumbT, thumbR, thumbB);
        thumb.draw(canvas);

        printDebugLog("onDraw: done");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                printDebugLog("onTouchEvent: down >> mDownX = " + (int) event.getX());
                mTouchMode = TOUCHING;
                mDownX = (int) event.getX();
                mDownThumbX = mCurrentThumbPosition;
                break;

            case MotionEvent.ACTION_MOVE:
                if ((int) event.getX() == mDownX) {
                    break;
                }

                mTouchMode = DRAGGING;
                int moveX = (int) event.getX();
                int deltaX = mDownX - moveX;
                mCurrentThumbPosition = mDownThumbX - deltaX;

                if (mCurrentThumbPosition < mThumbPositionOff) {
                    mCurrentThumbPosition = mThumbPositionOff;

                } else if (mCurrentThumbPosition > mThumbPositionOn) {
                    mCurrentThumbPosition = mThumbPositionOn;
                }

                printDebugLog("onTouchEvent: dragging >> mCurrentThumbPosition = " + mCurrentThumbPosition);
                break;

            case MotionEvent.ACTION_UP:
                if (TOUCHING == mTouchMode) {

                    printDebugLog("onTouchEvent: click");
                    mIsChecked = !mIsChecked;
                    callOnCheckedChangeListener();
                    animThumb();

                } else if (DRAGGING == mTouchMode) {
                    if ((mCurrentThumbPosition < mThumbPositionHalf && mIsChecked)
                            || (mCurrentThumbPosition >= mThumbPositionHalf && !mIsChecked)) {
                        printDebugLog("onTouchEvent: up >>s toggle");
                        mIsChecked = !mIsChecked;
                        callOnCheckedChangeListener();
                        animThumb();

                    } else {
                        printDebugLog("onTouchEvent: up >> nothing");
                        animThumb();
                    }
                }

                mTouchMode = IDLE;
                break;
        }

        invalidate();
        return true;
    }

    private int dp2px(float dp, float density) {
        return (int) (dp * density + 0.5f);
    }

    private void animThumb() {
        printDebugLog("animThumb: go");
        int targetThumbPos = mIsChecked ? mThumbPositionOn : mThumbPositionOff;
        mScroller.startScroll(mCurrentThumbPosition, mPaddingTop + mInnerMarginPx, targetThumbPos - mCurrentThumbPosition, 0, ANIM_DURATION);
        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        if (mScroller.computeScrollOffset()) {
            printDebugLog("computeScroll: go >> " + mScroller.getCurrX());
            mCurrentThumbPosition = mScroller.getCurrX();
            invalidate();
        }
    }

    private void callOnCheckedChangeListener() {

        if (null != mCheckedChangeListener) {
            mCheckedChangeListener.onCheckedChange(mIsChecked);
        }
    }

    private void printDebugLog(String msg) {
        if (mPrintLog) {
            Log.d(TAG + " ->", msg);
        }
    }

    public void printDebugLog(Boolean b) {
        mPrintLog = b;
    }

    public boolean getChecked() {
        return mIsChecked;
    }

    public void setChecked(boolean isChecked) {
        if (mIsChecked != isChecked) {
            mCurrentThumbPosition = mIsChecked ? mThumbPositionOn : mThumbPositionOff;
            mIsChecked = isChecked;
            callOnCheckedChangeListener();
            animThumb();
        }
    }

    public void toggle() {
        mCurrentThumbPosition = mIsChecked ? mThumbPositionOn : mThumbPositionOff;
        mIsChecked = !mIsChecked;
        callOnCheckedChangeListener();
        animThumb();
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.mCheckedChangeListener = listener;
    }

    public interface OnCheckedChangeListener {
        void onCheckedChange(boolean isChecked);
    }
}
