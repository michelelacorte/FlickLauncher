package it.michelelacorte.androidshortcuts.util;

import android.util.Log;

import it.michelelacorte.androidshortcuts.R;

/**
 * Created by Michele on 12/01/2017.
 */

public class StyleOption {
    private static final String TAG = "StyleOption";

    public static final int NONE = -1;
    public static final int LINE_LAYOUT = 0;
    public static final int CIRCLE_LAYOUT = 1;
    public static final int CIRCLE_LAYOUT_ALTERNATIVE = 2;

    public static int getStyleFromInt(int optionLayoutStyle){
        switch (optionLayoutStyle){
            case StyleOption.NONE:
                return -1;
            case StyleOption.LINE_LAYOUT:
                return R.drawable.shortcuts_options;
            case StyleOption.CIRCLE_LAYOUT:
                return R.drawable.shortcuts_options_2;
            case StyleOption.CIRCLE_LAYOUT_ALTERNATIVE:
                return R.drawable.shortcuts_options_3;
            default:
                Log.d(TAG, "Option invalid, restore default!");
                return R.drawable.shortcuts_options;
        }
    }
}
