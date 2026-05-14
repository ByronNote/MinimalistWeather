package com.byronzhang.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author byronzhang (byron[dot]zhanglei[at]gmail[dot]com)
 *         2017/8/3
 */
public class TitleView extends LinearLayout {

    private int defaultTitleTextSize = 7;// 默认文字大小
    private int defaultTitleBackgroundColorId = R.color.default_title_background_color;// 默认背景颜色
    private int defaultTitleTextColorId = R.color.default_title_text_color;// 默认文字颜色
    private int defaultTitleLineColorId = R.color.default_title_line_color;// 默认底部线条颜色

    private String title;
    private int titleTextSize;
    private int titleTextColor;
    private int titleBackgroundColor;
    private int titleLineColor;

    public TitleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initAttrs(context, attrs);

        initView();
    }

    private void initAttrs(Context context, @Nullable AttributeSet attrs) {

        DisplayMetrics dm = getResources().getDisplayMetrics();
        this.defaultTitleTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, defaultTitleTextSize, dm);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleView);
        title = typedArray.getString(R.styleable.TitleView_titleViewText);
        titleTextSize = typedArray.getDimensionPixelSize(R.styleable.TitleView_titleViewTextSize, defaultTitleTextSize);
        titleTextColor = typedArray.getColor(R.styleable.TitleView_titleViewTextColor, getResources().getColor(defaultTitleTextColorId));
        titleBackgroundColor = typedArray.getColor(R.styleable.TitleView_titleViewBackground, getResources().getColor(defaultTitleBackgroundColorId));
        titleLineColor = typedArray.getColor(R.styleable.TitleView_titleViewLineColor, getResources().getColor(defaultTitleLineColorId));
        typedArray.recycle();
    }

    private void initView() {

        LayoutInflater.from(getContext()).inflate(R.layout.layout_title_view, this, true);
        this.setOrientation(VERTICAL);
        this.setBackgroundColor(titleBackgroundColor);

        TextView titleTextView = (TextView) findViewById(R.id.title_text_view);
        titleTextView.setText(title);
//        titleTextView.setTextSize(titleTextSize);
        titleTextView.setTextColor(titleTextColor);

        View view = findViewById(R.id.title_line_view);
        view.setBackgroundColor(titleLineColor);
    }
}

