package com.android.launcher3.util;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.android.launcher3.R;

/**
 * Created by Michele on 13/03/2017.
 */

public class CustomPreferenceCategory extends PreferenceCategory {
    public CustomPreferenceCategory(Context context) {
        super(context);
    }

    public CustomPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomPreferenceCategory(Context context, AttributeSet attrs,
                                    int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        titleView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
    }
}