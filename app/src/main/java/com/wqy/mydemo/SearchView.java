package com.wqy.mydemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2019/10/14 0014.
 */

public class SearchView extends FrameLayout implements View.OnClickListener {

    private final long DEFAULT_ANIMATION_DURATION = 1000; // 动画时长
    private final long DEFAULT_SEARCH_SPACE = 3000; // 搜索切换时长

    private long mSearchDuration = DEFAULT_SEARCH_SPACE;
    private int mTextColor;
    private float mTextSize;

    private String targetContent;
    private List<EditText> mSearchList;
    private int mCurrentSearch;
    private AnimationSet mEnterAnimSet;
    private AnimationSet mExitAnimSet;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private SearchRunnable mSearchRunnable;
    private OnItemClickListener mListener;
    private TextPaint textPaint;
    private boolean mIsRunning; // 是否正已经start()

    public SearchView(Context context) {
        this(context, null);
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.SearchView);
        mTextColor = array.getColor(R.styleable.SearchView_textColor, 0xff000000);
        mTextSize = array.getDimension(R.styleable.SearchView_textSize, 15);
        array.recycle();

        // 初始化动画
        createExitAnimation();
        createEnterAnimation();

        // 初始化一个画笔，用于测量高度
        textPaint = new TextPaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 如果是未指定大小，那么设置宽为300px
        int exceptWidth = 300;
        int exceptHeight = 0;

        // 计算高度，如果将高度设置为textSize会很丑，因为文字有默认的上下边距
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            if (mTextSize > 0) {
                textPaint.setTextSize(mTextSize);
                Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
                exceptHeight = (int) (fontMetrics.bottom - fontMetrics.top);
            }
        }

        int width = resolveSize(exceptWidth, widthMeasureSpec);
        int height = resolveSize(exceptHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private void createEnterAnimation() {
        mEnterAnimSet = new AnimationSet(false);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, 0,
                TranslateAnimation.RELATIVE_TO_PARENT, 1f, TranslateAnimation.RELATIVE_TO_SELF, 0f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        mEnterAnimSet.addAnimation(translateAnimation);
        mEnterAnimSet.addAnimation(alphaAnimation);
        mEnterAnimSet.setDuration(DEFAULT_ANIMATION_DURATION);
    }

    private void createExitAnimation() {
        mExitAnimSet = new AnimationSet(false);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, 0,
                TranslateAnimation.RELATIVE_TO_SELF, 0f, TranslateAnimation.RELATIVE_TO_PARENT, -1f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
        mExitAnimSet.addAnimation(translateAnimation);
        mExitAnimSet.addAnimation(alphaAnimation);
        mExitAnimSet.setDuration(DEFAULT_ANIMATION_DURATION);
    }

    /**
     * 当和目标内容相等时则暂停动画
     *
     * @param content 目标内容
     */
    public void setTargetContent(String content) {
        this.targetContent = content;
    }

    /**
     * 设置搜索的集合
     */
    public void setSearchList(List<String> list) {
        // 设置集合的时候，要将上一次的集合清除
        if (list == null || list.size() == 0) {
            return;
        }

        // 暂停轮播
        pause();

        // 移除所有搜索
        removeAllViews();
        if (mSearchList == null) {
            mSearchList = new ArrayList<>();
        }
        mSearchList.clear();

        // 创建EditText
        for (int i = 0; i < list.size(); i++) {
            EditText textView = createTextView(list.get(i));
            mSearchList.add(textView);
            addView(textView);
        }
        // 显示第一条搜索
        mCurrentSearch = 0;
        mSearchList.get(mCurrentSearch).setVisibility(VISIBLE);
        // 启动轮播
        start();
    }

    /**
     * 设置条目点击监听
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        setOnClickListener(this);
        mListener = listener;
    }

    /**
     * 设置搜索的切换间隔
     */
    public void setSearchDuration(long duration) {
        if (duration > 0) {
            mSearchDuration = duration;
        }
    }

    /**
     * 设置动画的时长
     */
    public void setAnimationDuration(long duration) {
        if (duration > 0) {
            if (mEnterAnimSet != null) {
                mEnterAnimSet.setDuration(duration);
            }
            if (mExitAnimSet != null) {
                mExitAnimSet.setDuration(duration);
            }
        }
    }

    /**
     * 设置搜索的进入动画
     */
    public void setEnterAnimation(AnimationSet animation) {
        mEnterAnimSet = animation;
    }

    /**
     * 设置搜索的退出动画
     */
    public void setExitAnimation(AnimationSet animation) {
        mExitAnimSet = animation;
    }

    /**
     * 开始循环播放搜索
     * 推荐和pause()配合在生命周期中使用
     */
    public void start() {
        // 如果轮播正在运行中，不重复执行
        if (mIsRunning) {
            return;
        }

        if (mSearchRunnable == null) {
            mSearchRunnable = new SearchRunnable();
        } else {
            mHandler.removeCallbacks(mSearchRunnable);
        }
        mHandler.postDelayed(mSearchRunnable, mSearchDuration);
        mIsRunning = true;
    }

    /**
     * 暂停循环播放搜索
     * 推荐和start()配合在生命周期中使用
     */
    public void pause() {
        // 如果轮播已经停止，不重复执行
        if (!mIsRunning) {
            return;
        }

        if (mSearchRunnable != null) {
            mHandler.removeCallbacks(mSearchRunnable);
        }

        mIsRunning = false;
    }

    /**
     * 当前是否正在轮播内容
     */
    public boolean isRunning() {
        return mIsRunning;
    }

    private EditText createTextView(String text) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_search, this, false);
        EditText editInput = view.findViewById(R.id.edit_input);
        editInput.setSingleLine();
        editInput.setEllipsize(TextUtils.TruncateAt.END);
        editInput.setTextColor(mTextColor);
        editInput.setVisibility(GONE);
        editInput.setText(text);
        if (mTextSize > 0) {
            editInput.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        }
        return editInput;
    }

    /**
     * 获取当前显示View的内容
     */
    public String getCurrentSearchText() {
        if (mSearchList != null) {
            return mSearchList.get(mCurrentSearch).getText().toString();
        }
        return "";
    }

    /**
     * 动画开始的一瞬间就代表这个内容已经退出，下一个内容开始进入，点击事件也是给了下一个内容
     */
    class SearchRunnable implements Runnable {
        @Override
        public void run() {
            // 隐藏当前的EditText
            EditText currentView = mSearchList.get(mCurrentSearch);
            currentView.setVisibility(GONE);
            if (mExitAnimSet != null) {
                currentView.startAnimation(mExitAnimSet);
            }
            mCurrentSearch++;
            if (mCurrentSearch >= mSearchList.size()) {
                mCurrentSearch = 0;
            }

            // 显示下一个EditText
            EditText nextView = mSearchList.get(mCurrentSearch);
            nextView.setVisibility(VISIBLE);
            if (mEnterAnimSet != null) {
                nextView.startAnimation(mEnterAnimSet);
            }
            mHandler.postDelayed(this, mSearchDuration);

            // 若当前显示内容为目标内容则暂停
            if (nextView.getText().toString().equals(targetContent)) {
                pause();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (mListener != null && mSearchList != null && mSearchList.size() > 0) {
            mListener.onItemClick(mSearchList.get(mCurrentSearch), mCurrentSearch);
        }
    }

    /**
     * 点击的回调
     */
    public interface OnItemClickListener {
        void onItemClick(EditText view, int position);
    }
}


