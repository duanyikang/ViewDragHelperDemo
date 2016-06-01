package com.guoguoquan.viewdraghelperdemo.View;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.guoguoquan.viewdraghelperdemo.R;


/**
 * 作者：小段果果 on 2016/3/21 09:12
 * 邮箱：duanyikang@mumayi.com
 */
public class MainLockViewGroup extends FrameLayout {

    private static final int MIN_FLING_VELOCITY = 100;
    private ViewGroup bottomMenuView;
    private ViewGroup mainView;
    private View topClearView;
    private ViewGroup rightMenu;
    private View leftMenu;

    private ViewDragHelper mHelper;

    private sliddirection temp;

    private int dragbutton;

    private Boolean BottomMenuIsOpen = false;
    private Boolean RightMenuIsOpen = false;

    private boolean menuIsOpen;

    public enum sliddirection {UP, Right, ANY}


    public MainLockViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;
        final float minVel = MIN_FLING_VELOCITY * density;


        mHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return child == bottomMenuView;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                if (menuIsOpen) {
                    return 0;
                }
                //锁屏的住布局右滑解锁
                if (child == mainView && temp == sliddirection.Right) {
                    final int leftBound = getPaddingLeft();
                    final int rightBound = leftMenu.getWidth();
                    final int newLeft = Math.min(Math.max(left, leftBound), rightBound);
                    return newLeft;
                }

                if (child == rightMenu) {
                    final int leftBound = 0;
                    final int rightBound = 2 * child.getWidth();
                    final int newLeft = Math.min(Math.max(left, leftBound), rightBound);
                    return newLeft;
                }
                return 0;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                //底部菜单滑动只能是自己的高度
                if (child == bottomMenuView) {
                    final int topBound = getHeight() - child.getHeight();
                    final int bottomBound = getHeight();
                    final int newtop = Math.min(Math.max(top, topBound), bottomBound);
                    return newtop;
                }
                if (child == mainView && temp == sliddirection.Right || child == rightMenu) {
                    return 0;
                }
                if (child == mainView && temp == sliddirection.UP) {
                    final int topBound = -child.getHeight();
                    final int bottomBound = 0;
                    final int newtop = Math.min(Math.max(top, topBound), bottomBound);
                    return newtop;
                }

                return top;
            }


            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                //底部菜单滑动释放
                if (releasedChild == bottomMenuView) {
                    if (yvel <= 0) {
                        mHelper.settleCapturedViewAt(0, getHeight() - releasedChild.getHeight());
                        BottomMenuIsOpen = true;

                        menuIsOpen = true;
                    } else if (yvel > 0) {
                        mHelper.settleCapturedViewAt(0, getHeight());
                        menuIsOpen = false;
                    }
                }


                //主页向上滑动解锁
                if (releasedChild == mainView && temp == sliddirection.UP && dragbutton == R.id.rl_main_lockly) {
                    System.out.println(releasedChild.getY());
                    if (yvel < -2000 || releasedChild.getY() < -getMeasuredHeight() / 2) {
                        mHelper.settleCapturedViewAt(0, -releasedChild.getHeight());

                    } else {
                        mHelper.settleCapturedViewAt(0, 0);

                    }
                }

                //右滑露出左边菜单
                if (releasedChild == mainView && temp == sliddirection.Right && dragbutton == R.id.rl_main_lockly) {
                    mHelper.settleCapturedViewAt(0, 0);
                    if (mainView.getLeft() > leftMenu.getWidth() / 2)

                    leftMenu.setTranslationX(leftMenu.getLeft());
                }

                if (releasedChild == rightMenu) {
                    if (xvel < -2000) {
                        mHelper.settleCapturedViewAt(0, 0);
                    } else {
                        mHelper.settleCapturedViewAt(getWidth(), 0);
                    }
                }

                temp = null;
                dragbutton = 0;
                invalidate();
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                final int childHeight = changedView.getHeight();
                float offset = (getHeight() - changedView.getTop()) * 1.0f / childHeight;
                changedView.setVisibility(offset == 0 ? View.INVISIBLE : View.VISIBLE);
                if (changedView == mainView && temp == sliddirection.Right) {
                    leftMenu.setTranslationX(left);
                }

                invalidate();
            }

            @Override
            public void onEdgeTouched(int edgeFlags, int pointerId) {
                if (!RightMenuIsOpen) {
                    mHelper.captureChildView(rightMenu, pointerId);
                    ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(rightMenu, "TranslationX", 0f, -40f).setDuration(500);
                    objectAnimator.reverse();
                }


            }
        });
        mHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_RIGHT);
        mHelper.setMinVelocity(minVel);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);

        mainView = (ViewGroup) getChildAt(0);
        MarginLayoutParams lp = (MarginLayoutParams) mainView.getLayoutParams();
        final int contentWidthSpec = MeasureSpec.makeMeasureSpec(widthSize - lp.leftMargin - lp.rightMargin, MeasureSpec.EXACTLY);
        final int contentHeightSpec = MeasureSpec.makeMeasureSpec(heightSize - lp.topMargin - lp.bottomMargin, MeasureSpec.EXACTLY);
        mainView.measure(contentWidthSpec, contentHeightSpec);

        bottomMenuView = (ViewGroup) getChildAt(1);
        MarginLayoutParams lp2 = (MarginLayoutParams) bottomMenuView.getLayoutParams();
        final int drawerWidthSpec = getChildMeasureSpec(widthMeasureSpec, lp2.leftMargin + lp2.rightMargin, lp2.width);
        final int drawerHeightSpec = getChildMeasureSpec(heightMeasureSpec, lp2.topMargin + lp2.bottomMargin, lp2.height);
        bottomMenuView.measure(drawerWidthSpec, drawerHeightSpec);

        topClearView = getChildAt(2);
        MarginLayoutParams lp3 = (MarginLayoutParams) topClearView.getLayoutParams();
        final int w3 = getChildMeasureSpec(widthMeasureSpec, lp3.leftMargin + lp3.rightMargin, lp3.width);
        final int h3 = getChildMeasureSpec(heightMeasureSpec, lp3.topMargin + lp3.bottomMargin, lp3.height);
        topClearView.measure(w3, h3);

        topClearView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mHelper.smoothSlideViewTo(bottomMenuView, 0, getHeight());
                menuIsOpen = false;
                invalidate();
            }
        });


        rightMenu = (ViewGroup) getChildAt(3);
        MarginLayoutParams lp4 = (MarginLayoutParams) rightMenu.getLayoutParams();
        final int w4 = getChildMeasureSpec(widthMeasureSpec, lp4.leftMargin + lp4.rightMargin, lp4.width);
        final int h4 = getChildMeasureSpec(heightMeasureSpec, lp4.topMargin + lp4.bottomMargin, lp4.height);
        rightMenu.measure(w4, h4);

        leftMenu = getChildAt(4);
        MarginLayoutParams lp5 = (MarginLayoutParams) leftMenu.getLayoutParams();
        final int w5 = getChildMeasureSpec(widthMeasureSpec, lp5.leftMargin + lp5.rightMargin, lp5.width);
        final int h5 = getChildMeasureSpec(heightMeasureSpec, lp5.topMargin + lp5.bottomMargin, lp5.height);
        leftMenu.measure(w5, h5);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        MarginLayoutParams lp = (MarginLayoutParams) mainView.getLayoutParams();

        mainView.layout(lp.leftMargin, lp.topMargin, lp.leftMargin + mainView.getMeasuredWidth(), lp.topMargin + mainView.getMeasuredHeight());


        MarginLayoutParams lp2 = (MarginLayoutParams) bottomMenuView.getLayoutParams();
        if (!menuIsOpen) {
            bottomMenuView.layout(lp2.leftMargin, mainView.getMeasuredHeight(), mainView.getMeasuredWidth(), mainView.getMeasuredHeight() + bottomMenuView.getMeasuredHeight());
        } else {
            bottomMenuView.layout(lp2.leftMargin, getMeasuredHeight() - bottomMenuView.getMeasuredHeight(), lp2.leftMargin + bottomMenuView.getMeasuredWidth(), getMeasuredHeight());
        }

        if (!menuIsOpen) {
            topClearView.layout(0, -topClearView.getMeasuredHeight(), topClearView.getMeasuredWidth(), 0);
        } else {
            topClearView.layout(0, 0, topClearView.getMeasuredWidth(), topClearView.getHeight());
        }

        if (!RightMenuIsOpen) {
            rightMenu.layout(rightMenu.getMeasuredWidth(), 0, rightMenu.getMeasuredWidth() * 2, rightMenu.getMeasuredHeight());
        } else {
            rightMenu.layout(0, 0, rightMenu.getMeasuredWidth(), rightMenu.getMeasuredHeight());
        }

        leftMenu.layout(-leftMenu.getMeasuredWidth(), 0, 0, getHeight());

    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mHelper.shouldInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            mHelper.processTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    @Override
    public void computeScroll() {
        if (mHelper.continueSettling(true)) {
            invalidate();
        }
    }


    public void upmenu(int pointerid) {
        if (!BottomMenuIsOpen && !RightMenuIsOpen)
            mHelper.captureChildView(bottomMenuView, pointerid);
    }


    public void openLock(sliddirection x, int pointerid, int id) {
        temp = x;
        dragbutton = id;
        if (!BottomMenuIsOpen) {
            mHelper.captureChildView(mainView, pointerid);
        }
    }

    public void closeRightMenu() {
        mHelper.smoothSlideViewTo(rightMenu, getWidth(), 0);
        RightMenuIsOpen = false;
        invalidate();
    }

    public void jumpRightMenu() {
        if (!BottomMenuIsOpen) {
            mHelper.captureChildView(rightMenu, 0);
        }

    }





}


