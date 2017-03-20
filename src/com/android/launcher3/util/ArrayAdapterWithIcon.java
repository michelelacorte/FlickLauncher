package com.android.launcher3.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.Launcher;
import com.android.launcher3.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.michelelacorte.androidshortcuts.util.Utils;

/**
 * Created by Michele on 16/03/2017.
 */

public class ArrayAdapterWithIcon extends ArrayAdapter<String> {

    private List<Bitmap> images;

    public ArrayAdapterWithIcon(Context context, List<String> items, List<Bitmap> images) {
        super(context, android.R.layout.select_dialog_item, items);
        this.images = images;
    }

    public ArrayAdapterWithIcon(Context context, String[] items, Bitmap[] images) {
        super(context, android.R.layout.select_dialog_item, items);
        this.images = Arrays.asList(images);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setTextSize(18);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            int dp50 = (int) (30 * getContext().getResources().getDisplayMetrics().density + 0.3f);
            Drawable icon = new BitmapDrawable(getContext().getResources(), Bitmap.createScaledBitmap(images.get(position), dp50, dp50, true));
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
        } else {
            int dp50 = (int) (50 * getContext().getResources().getDisplayMetrics().density + 0.5f);
            Drawable icon = new BitmapDrawable(getContext().getResources(), Bitmap.createScaledBitmap(images.get(position), dp50, dp50, true));
            textView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        }
        int dp5 = (int) (10 * getContext().getResources().getDisplayMetrics().density + 0.5f);
        textView.setCompoundDrawablePadding(dp5);
        return view;
    }

}