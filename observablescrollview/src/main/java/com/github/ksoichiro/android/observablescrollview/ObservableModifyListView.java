/*
 * Copyright 2014 Soichiro Kashima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ksoichiro.android.observablescrollview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.ListView;
import com.github.ksoichiro.android.observablescrollview.internal.LogUtils;

public class ObservableModifyListView extends ListView {
    private static final String TAG = ObservableModifyListView.class.getSimpleName();

    private ObservableScrollViewCallbacks mCallbacks;
    //private int mPrevFirstVisiblePosition;
    //private int mPrevFirstVisibleChildHeight = -1;
    //private int mPrevScrolledChildrenHeight;
    //private SparseIntArray mChildrenHeights;
    private int mPrevScrollY;
    private int mScrollY;
    private ScrollState mScrollState;
    private boolean mFirstScroll;
    private boolean mDragging;

    private OnScrollListener mOriginalScrollListener;
    private OnScrollListener mScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (mOriginalScrollListener != null) {
                mOriginalScrollListener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
            if (mOriginalScrollListener != null) {
                mOriginalScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
                        totalItemCount);
            }
            // AbsListView#invokeOnItemScrollListener calls onScrollChanged(0, 0, 0, 0)
            // on Android 4.0+, but Android 2.3 is not. (Android 3.0 is unknown)
            // So call it with onScrollListener.
            onScrollChanged();
        }
    };

    @Override protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        LogUtils.v(TAG, "onScrollChanged t: " + t + ";oldt :" + oldt);
    }

    public ObservableModifyListView(Context context) {
        super(context);
        init();
    }

    public ObservableModifyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ObservableModifyListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mCallbacks != null) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    LogUtils.v(TAG, "onTouchEvent: ACTION_DOWN");
                    mFirstScroll = mDragging = true;
                    mCallbacks.onDownMotionEvent();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    LogUtils.v(TAG, "onTouchEvent: ACTION_UP|ACTION_CANCEL");
                    mDragging = false;
                    mCallbacks.onUpOrCancelMotionEvent(mScrollState);
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        // Don't set l to super.setOnScrollListener().
        // l receives all events through mScrollListener.
        mOriginalScrollListener = l;
    }

    public void setScrollViewCallbacks(ObservableScrollViewCallbacks listener) {
        mCallbacks = listener;
    }

    public int getCurrentScrollY() {
        return mScrollY;
    }

    private void init() {
        //mChildrenHeights = new SparseIntArray();
        super.setOnScrollListener(mScrollListener);
    }

    private void onScrollChanged() {
        if (mCallbacks != null) {
            if (getChildCount() > 0) {
                mScrollY = computeVerticalScrollOffset();

                if (mPrevScrollY < mScrollY) {
                    //down
                    mScrollState = ScrollState.UP;
                } else if (mScrollY < mPrevScrollY) {
                    //up
                    mScrollState = ScrollState.DOWN;
                } else {
                    mScrollState = ScrollState.STOP;
                }

                LogUtils.v(TAG, "mPrevScrollY: " + mPrevScrollY + ";mScrollY :" + mScrollY);

                mCallbacks.onScrollChanged(mScrollY, mFirstScroll, mDragging);

                mPrevScrollY = mScrollY;
                
                if (mFirstScroll) {
                    mFirstScroll = false;
                }
            }
        }
    }
}
