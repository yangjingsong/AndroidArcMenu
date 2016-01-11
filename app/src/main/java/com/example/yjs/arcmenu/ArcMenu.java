package com.example.yjs.arcmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * Created by yjs on 2016/1/9.
 */
public class ArcMenu extends ViewGroup implements View.OnClickListener {

    private static final int POS_LEFT_TOP = 0;
    private static final int POS_LEFT_BOTTOM = 1;
    private static final int POS_RIGHT_TOP = 2;
    private static final int POS_RIGHT_BOTTOM = 3;

    private Position mPosition = Position.RIGHT_BOTTOM;
    private Status mCurrentStatus = Status.CLOSE;
    private int mRadius;
    private View mCButton;
    private OnMenuItemClickListener mOnMenuItemClickListener;


    private interface OnMenuItemClickListener{
       void onClick(View v,int pos);
    }
    public void setMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener){
        this.mOnMenuItemClickListener = onMenuItemClickListener;
    }

    public enum Position{
        LEFT_TOP,LEFT_BOTTOM,RIGHT_TOP,RIGHT_BOTTOM
    }

    public enum Status{
        OPEN,CLOSE
    }
    public ArcMenu(Context context) {
        this(context, null);
    }

    public ArcMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArcMenu, defStyleAttr, 0);
        int pos = a.getInt(R.styleable.ArcMenu_position, POS_RIGHT_BOTTOM);
        switch (pos){
            case POS_LEFT_TOP:
                mPosition = Position.LEFT_TOP;
                break;
            case POS_LEFT_BOTTOM:
                mPosition = Position.LEFT_BOTTOM;
                break;
            case POS_RIGHT_TOP:
                mPosition = Position.RIGHT_TOP;
                break;
            case POS_RIGHT_BOTTOM:
                mPosition = Position.RIGHT_BOTTOM;
                break;
        }
        mRadius = (int) a.getDimension(R.styleable.ArcMenu_radius,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,100,
                        context.getResources().getDisplayMetrics()));
        a.recycle();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int n = getChildCount();
        for(int i = 0;i<n;i++){
            measureChild(getChildAt(i),widthMeasureSpec,heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(changed){
            layoutCButton();
            int count = getChildCount();
            for (int i=0;i<count-1;i++){
                View child = getChildAt(i+1);
                child.setVisibility(GONE);
                int width = child.getMeasuredWidth();
                int height = child.getMeasuredHeight();
                int cl = (int) (mRadius*(Math.sin(Math.PI/2/(count-2)*i)));
                int ct = (int) (mRadius*(Math.cos(Math.PI/2/(count-2)*i)));
                if(mPosition == Position.RIGHT_BOTTOM || mPosition == Position.LEFT_BOTTOM){
                    ct = getMeasuredHeight() - height - ct;
                }
                if(mPosition == Position.RIGHT_BOTTOM || mPosition == Position.RIGHT_TOP ){
                    cl = getMeasuredWidth() - width - cl;
                }
                child.layout(cl,ct,cl+width,ct+width);


            }
        }

    }

    private void layoutCButton() {
        mCButton = getChildAt(0);
        mCButton.setOnClickListener(this);
        int mButtonWidth = mCButton.getMeasuredWidth();
        int mButtonHeight = mCButton.getMeasuredHeight();
        int l = 0;
        int t = 0;
        switch (mPosition){
            case LEFT_TOP:
                l = 0;
                t = 0;
                break;
            case LEFT_BOTTOM:
                l = 0;
                t = getMeasuredHeight()-mButtonHeight;
                break;
            case RIGHT_TOP:
                l = getMeasuredWidth() - mButtonWidth;
                t = 0;
                break;
            case RIGHT_BOTTOM:
                l = getMeasuredWidth() - mButtonWidth;
                t = getMeasuredHeight() - mButtonHeight;
                break;
        }
        mCButton.layout(l, t, l + mButtonWidth, t + mButtonHeight);

    }

    @Override
    public void onClick(View v) {
        rotateButton(v, 0f, 360f, 300);
        toggleMenu(300);

    }

    private void toggleMenu(int duration) {
        int count = getChildCount();
        for(int i = 0 ;i<count-1;i++){
            final View child = getChildAt(i+1);
            child.setVisibility(VISIBLE);
            int cl = (int) (mRadius*(Math.sin(Math.PI/2/(count-2)*i)));
            int ct = (int) (mRadius*(Math.cos(Math.PI / 2 / (count - 2) * i)));
            int xFlag = 1;
            int yFlag = 1;
            if(mPosition == Position.LEFT_TOP || mPosition == Position.LEFT_BOTTOM){
                xFlag = -1;
            }
            if(mPosition == Position.LEFT_TOP || mPosition == Position.RIGHT_TOP){
                yFlag = -1;
            }
            AnimationSet animationSet = new AnimationSet(true);
            Animation transAnim = null;
            if(mCurrentStatus == Status.OPEN){
                transAnim = new TranslateAnimation(0,xFlag*cl,0,yFlag*ct);
            }else{
                transAnim = new TranslateAnimation(xFlag * cl,0,yFlag*ct,0);
            }
            transAnim.setFillAfter(true);
            transAnim.setDuration(duration);
            transAnim.setStartOffset(i * 100 / count);
            transAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mCurrentStatus == Status.CLOSE) {
                        child.setVisibility(GONE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            RotateAnimation rotate = new RotateAnimation(0f,720f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            rotate.setDuration(duration);
            rotate.setFillAfter(true);
            animationSet.addAnimation(rotate);
            animationSet.addAnimation(transAnim);
            child.startAnimation(animationSet);
            final int pos = i + 1;
            child.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOnMenuItemClickListener!=null){
                        mOnMenuItemClickListener.onClick(child,pos);
                    }
                    menuItemAnimation(pos-1);
                    changeStatus();
                }

                
            });


        }
        changeStatus();
    }

    private void menuItemAnimation(int pos) {
        for(int i = 0;i<getChildCount()-1;i++){
            View childView = getChildAt(i+1);
            if(i == pos){
                childView.startAnimation(scaleBigAnimation());
            }else{
                childView.startAnimation(scaleSmallAnimation());
            }
        }
    }

    private Animation scaleSmallAnimation() {
        ScaleAnimation animation = new ScaleAnimation(1.0f,0f,1.0f,0f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        animation.setDuration(300);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f,0f);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(animation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setDuration(300);
        animationSet.setFillAfter(true);
        return animationSet;
    }

    private Animation scaleBigAnimation() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f,4.0f,1.0f,4.0f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f,0.0f);
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(scaleAnimation);
        set.addAnimation(alphaAnimation);
        set.setDuration(300);
        set.setFillAfter(true);
        return set;
    }

    private void changeStatus() {
        mCurrentStatus = (mCurrentStatus==Status.CLOSE)?Status.OPEN:Status.CLOSE;
    }

    private void rotateButton(View v, float start, float end, int duration) {
        RotateAnimation anim = new RotateAnimation(start,end, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        anim.setDuration(duration);
        anim.setFillAfter(true);
        v.startAnimation(anim);
    }


}
