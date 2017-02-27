package it.michelelacorte.androidshortcuts;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;

import java.util.List;

import it.michelelacorte.androidshortcuts.util.GridSize;
import it.michelelacorte.androidshortcuts.util.Utils;

/**
 * Created by Michele on 24/11/2016.
 */

public class ShortcutsCreation {
    private final String TAG = "ShorctusCreation";
    private static final int MAX_NUMBER_OF_SHORTCUTS = 5;
    private static final int PADDING = 20;
    public static boolean USE_SHORTCUTS_FOR_LAUNCHER_3 = false;

    private static RelativeLayout[] layout = new RelativeLayout[MAX_NUMBER_OF_SHORTCUTS];
    private static RelativeLayout triangle;

    private int maxXScreen;
    private int maxYScreen;
    private float displayDensity;

    private int toolbarHeight;
    private int DIM_WIDTH = 840;
    private int DIM_HEIGHT = 200;

    private ShortcutsBuilder shortcutsBuilder;

    public ShortcutsCreation(ShortcutsBuilder shortcutsBuilder) {
        this.shortcutsBuilder = shortcutsBuilder;
    }

    public void init() throws NullPointerException{
        if(shortcutsBuilder.isNormal() && shortcutsBuilder.getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            initNormalShortcuts();
        }else if(shortcutsBuilder.isLauncher3() && shortcutsBuilder.getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            initLauncher3Shortcuts();
        }else if(shortcutsBuilder.getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            Log.e(TAG, "Shortcuts not working in LANDSCAPE mode!");
        }else{
            throw new NullPointerException(TAG + "No normal shortcuts/laucher3 shortcuts defined!");
        }
    }

    private void initNormalShortcuts(){
        if(shortcutsBuilder.getShortcutsArray() != null){

            createShortcuts(shortcutsBuilder.getActivity(),
                    shortcutsBuilder.getPackageImage(),
                    shortcutsBuilder.getMasterLayout(),
                    shortcutsBuilder.getCurrentXPosition(),
                    shortcutsBuilder.getCurrentYPosition(),
                    shortcutsBuilder.getRowHeight(),
                    shortcutsBuilder.getGridView(),
                    shortcutsBuilder.getOptionLayoutStyle(),
                    shortcutsBuilder.getShortcutsArray());

        }else if(shortcutsBuilder.getShortcutsList() != null){

            createShortcuts(shortcutsBuilder.getActivity(),
                    shortcutsBuilder.getPackageImage(),
                    shortcutsBuilder.getMasterLayout(),
                    shortcutsBuilder.getCurrentXPosition(),
                    shortcutsBuilder.getCurrentYPosition(),
                    shortcutsBuilder.getRowHeight(),
                    shortcutsBuilder.getGridView(),
                    shortcutsBuilder.getOptionLayoutStyle(),
                    shortcutsBuilder.getShortcutsList());

        }
    }

    private void initLauncher3Shortcuts(){
        if(shortcutsBuilder.getShortcutsArray() != null){

            createShortcutsForLauncher3(shortcutsBuilder.getActivity(),
                    shortcutsBuilder.getPackageImage(),
                    shortcutsBuilder.getMasterLayout(),
                    shortcutsBuilder.getPositionInGrid(),
                    shortcutsBuilder.getRowHeight(),
                    shortcutsBuilder.getBottomSpace(),
                    shortcutsBuilder.isHotseatTouched(),
                    shortcutsBuilder.getGridSize(),
                    shortcutsBuilder.getOptionLayoutStyle(),
                    shortcutsBuilder.getShortcutsArray());

        }else if(shortcutsBuilder.getShortcutsList() != null){

            createShortcutsForLauncher3(shortcutsBuilder.getActivity(),
                    shortcutsBuilder.getPackageImage(),
                    shortcutsBuilder.getMasterLayout(),
                    shortcutsBuilder.getPositionInGrid(),
                    shortcutsBuilder.getRowHeight(),
                    shortcutsBuilder.getBottomSpace(),
                    shortcutsBuilder.isHotseatTouched(),
                    shortcutsBuilder.getGridSize(),
                    shortcutsBuilder.getOptionLayoutStyle(),
                    shortcutsBuilder.getShortcutsList());

        }
    }



    private void createShortcutsForLauncher3(Activity activity, Drawable packageImage, ViewGroup masterLayout, int positionInGrid, int rowHeight, int bottomSpace, boolean isHotseatTouched, GridSize gridSize, int optionLayoutStyle, List<Shortcuts> shortcuts) {
        if(shortcuts.size() > MAX_NUMBER_OF_SHORTCUTS){
            Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
            return;
        }
        if(rowHeight < 0){
            Log.e(TAG, "Invalid Row Height, it must be greater than 0");
            return;
        }
        if(shortcuts.size() == 0){
            Log.e(TAG, "Shortcuts must be at least one!");
            return;
        }

        USE_SHORTCUTS_FOR_LAUNCHER_3 = true;
        getScreenDimension(activity);

        if (layout != null || triangle != null)
            clearAllLayout();
        switch (maxXScreen) {
            case 720:
                DIM_WIDTH = 360;
                DIM_HEIGHT = 100;
                break;
            case 1080:
                DIM_WIDTH = 580;
                DIM_HEIGHT = 150;
                break;
            case 1440:
                DIM_WIDTH = 780;
                DIM_HEIGHT = 200;
                break;
            default:
                Log.e(TAG, "Resolution of screen not supported!");
                break;
        }

        LayoutInflater inflater = LayoutInflater.from(activity.getApplicationContext());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(DIM_WIDTH, DIM_HEIGHT);
        RelativeLayout.LayoutParams paramsTriangle = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int mIconWidth = maxXScreen / gridSize.getColumnCount();
        int dim = (positionInGrid) * mIconWidth;
        int layoutHeightTotal = DIM_HEIGHT * shortcuts.size() + PADDING;

        triangle = (RelativeLayout) inflater.inflate(R.layout.shortcuts_triangle, null, false);

        //Scale animation right to left
        ScaleAnimation animationRightToLeft = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
        animationRightToLeft.setDuration(200);

        //Scale animation left to right
        ScaleAnimation animationLeftToRight = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animationLeftToRight.setDuration(200);

        for (int i = 0; i < shortcuts.size(); i++) {
            layout[i] = (RelativeLayout) inflater.inflate(R.layout.shortcuts, null, false);
            shortcuts.get(i).init(layout[i], optionLayoutStyle, activity, packageImage, this);
            if ((dim + DIM_WIDTH) >= maxXScreen) {
                //Destra
                layout[i].setX(dim - DIM_WIDTH + (mIconWidth) - mIconWidth / 4);
                triangle.setX((float) (dim + mIconWidth - mIconWidth / 1.5));
                triangle.setRotation(180);

                //Start Animation
                layout[i].startAnimation(animationRightToLeft);
                triangle.startAnimation(animationRightToLeft);
            } else {
                //Sinistra

                layout[i].setX(dim + mIconWidth / 4);
                triangle.setX((float) (dim + mIconWidth / 2));
                triangle.setRotation(180);

                //Start Animation
                layout[i].startAnimation(animationLeftToRight);
                triangle.startAnimation(animationLeftToRight);
            }

            if ((toolbarHeight = Utils.getToolbarHeight(activity)) >= 0) {
                int maxYScreenWithToolbar = maxYScreen - toolbarHeight * 2;
                if (rowHeight + layoutHeightTotal > maxYScreenWithToolbar-bottomSpace+PADDING && !isHotseatTouched) {
                    //Alto
                    switch (maxXScreen) {
                        case 720:
                            switch (shortcuts.size()) {
                                case 1:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * -0.05));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.05 + 93));
                                    break;
                                case 2:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * -0.05 - 110 * i));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.05 + 105 * i));
                                    break;
                                case 3:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * -0.05 - 110 * i));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.05 + 57 * i));
                                    break;
                                case 4:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * -0.05 - 110 * i));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.05 + 42 * i));
                                    break;
                                case 5:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * -0.05 - 110 * i));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.05 + 34 * i));
                                    break;
                                default:
                                    Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                    break;
                            }
                            triangle.setRotation(0);
                            break;
                        case 1080:
                            switch (shortcuts.size()) {
                                case 1:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * -0.05));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.05 + 137));
                                    break;
                                case 2:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * -0.05 - 160 * i));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.05 + 157 * i));
                                    break;
                                case 3:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * -0.05 - 160 * i));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.05 + 83 * i));
                                    break;
                                case 4:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * -0.05 - 160 * i));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.05 + 62 * i));
                                    break;
                                case 5:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * -0.05 - 160 * i));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.05 + 50 * i));
                                    break;
                                default:
                                    Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                    break;
                            }
                            triangle.setRotation(0);
                            break;
                        case 1440:
                            switch (shortcuts.size()) {
                                case 1:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * +0.8));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.92 + 187));
                                    break;
                                case 2:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * +0.3 - 220 * i));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.4 + 203 * i));
                                    break;
                                case 3:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * +0.3 - 220 * i));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.4 + 113 * i));
                                    break;
                                case 4:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * +0.2 - 220 * i));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.31 + 83 * i));
                                    break;
                                case 5:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * -0.05 - 220 * i));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.05 + 65 * i));
                                    break;
                                default:
                                    Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                    break;
                            }
                            triangle.setRotation(0);
                            break;
                        default:
                            Log.e(TAG, "Resolution of screen not supported!");
                            break;
                    }
                } else if (!isHotseatTouched){
                    //Basso
                    switch (maxXScreen) {
                        case 720:
                            switch (shortcuts.size()) {
                                case 1:
                                    layout[i].setY((float)(rowHeight + layoutHeightTotal * 2.5));
                                    triangle.setY(rowHeight + layoutHeightTotal + 163);
                                    break;
                                case 2:
                                    if (i >= 1) {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.9 - 110 * i));
                                    } else {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.9));
                                    }
                                    triangle.setY((float)(rowHeight + layoutHeightTotal * 1.9 - 127 * i));
                                    break;
                                case 3:
                                    if (i >= 1) {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.6 - 110 * i));
                                    } else {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.6));
                                    }
                                    triangle.setY((float)(rowHeight + layoutHeightTotal * 1.6 - 120 * i));
                                    break;
                                case 4:
                                    if (i >= 1) {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.5 - 110 * i));
                                    } else {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.5));
                                    }
                                    triangle.setY((float)(rowHeight + layoutHeightTotal * 1.5 - 117 * i));
                                    break;
                                case 5:
                                    if (i >= 1) {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.4 - 110 * i));
                                    } else {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.4));
                                    }
                                    triangle.setY((float)(rowHeight + layoutHeightTotal * 1.4 - 115 * i));
                                    break;
                                default:
                                    Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                    break;
                            }
                            break;
                        case 1080:
                            switch (shortcuts.size()) {
                                case 1:
                                    layout[i].setY((float)(rowHeight + layoutHeightTotal * 2.5));
                                    triangle.setY(rowHeight + layoutHeightTotal + 225);
                                    break;
                                case 2:
                                    if (i >= 1) {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.9 - 160 * i));
                                    } else {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.9));
                                    }
                                    triangle.setY(rowHeight + layoutHeightTotal * i + 100 * i);
                                    break;
                                case 3:
                                    if (i >= 1) {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.6 - 160 * i));
                                    } else {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.6));
                                    }
                                    triangle.setY(rowHeight + layoutHeightTotal * i - 267 * i);
                                    break;
                                case 4:
                                    if (i >= 1) {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.5 - 160 * i));
                                    } else {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.5));
                                    }
                                    triangle.setY(rowHeight + layoutHeightTotal * i - 480 * i);
                                    break;
                                case 5:
                                    if (i >= 1) {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.4 - 160 * i));
                                    } else {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.4));
                                    }
                                    triangle.setY(rowHeight + layoutHeightTotal * i - 668 * i);
                                    break;
                                default:
                                    Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                    break;
                            }
                            break;
                        case 1440:
                            switch (shortcuts.size()) {
                                case 1:
                                    layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.6));
                                    triangle.setY(rowHeight + layoutHeightTotal + 100);
                                    break;
                                case 2:
                                    if (i >= 1) {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.4 - 220 * i));
                                    } else {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.4));
                                    }
                                    triangle.setY((float)(rowHeight + layoutHeightTotal * 1.4 - 250 * i));
                                    break;
                                case 3:
                                    if (i >= 1) {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.4 - 220 * i));
                                    } else {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.4));
                                    }
                                    triangle.setY((float)(rowHeight + layoutHeightTotal * 1.4 - 240 * i));
                                    break;
                                case 4:
                                    if (i >= 1) {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.3 - 220 * i));
                                    } else {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.3));
                                    }
                                    triangle.setY((float)(rowHeight + layoutHeightTotal * 1.2 - 205 * i));
                                    //triangle.setY(rowHeight + layoutHeightTotal * i - 643 * i);
                                    break;
                                case 5:
                                    if (i >= 1) {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.4 - 220 * i));
                                    } else {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.4));
                                    }
                                    triangle.setY(rowHeight + layoutHeightTotal * i - 893 * i);
                                    break;
                                default:
                                    Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                    break;
                            }
                            break;
                        default:
                            Log.e(TAG, "Resolution of screen not supported!");
                            break;
                    }
                }else if(isHotseatTouched){
                    //Alto
                    switch (maxXScreen) {
                        case 720:
                            switch (shortcuts.size()) {
                                case 1:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 2.6 + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 2.6 + 83 + maxYScreenWithToolbar));
                                    break;
                                case 2:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 1.4 - 110 * i + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 1.4 + 82 * i + maxYScreenWithToolbar));
                                    break;
                                case 3:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 0.95 - 110 * i + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 0.95 + 42 * i + maxYScreenWithToolbar));
                                    break;
                                case 4:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 0.7 - 110 * i + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 0.7 + 28 * i  + maxYScreenWithToolbar));
                                    break;
                                case 5:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 0.6 - 110 * i + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 0.6 + 20 * i  + maxYScreenWithToolbar));
                                    break;
                                default:
                                    Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                    break;
                            }
                            triangle.setRotation(0);
                            break;
                        case 1080:
                            switch (shortcuts.size()) {
                                case 1:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 2.6 + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 2.6 + 125 + maxYScreenWithToolbar));
                                    break;
                                case 2:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 1.4 - 160 * i + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 1.4 + 125 * i + maxYScreenWithToolbar));
                                    break;
                                case 3:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 0.95 - 160 * i + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 0.95 + 62 * i + maxYScreenWithToolbar));
                                    break;
                                case 4:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 0.7 - 160 * i + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 0.7 + 42 * i  + maxYScreenWithToolbar));
                                    break;
                                case 5:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 0.6 - 160 * i + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 0.6 + 30 * i  + maxYScreenWithToolbar));
                                    break;
                                default:
                                    Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                    break;
                            }
                            triangle.setRotation(0);
                            break;
                        case 1440:
                            switch (shortcuts.size()) {
                                case 1:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 2.6 + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 2.6 + 162 + maxYScreenWithToolbar));
                                    break;
                                case 2:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 1.4 - 220 * i + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 1.4 + 155 * i + maxYScreenWithToolbar));
                                    break;
                                case 3:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 0.95 - 220 * i + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 0.95 + 80 * i + maxYScreenWithToolbar));
                                    break;
                                case 4:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 0.7 - 220 * i + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 0.7 + 53 * i  + maxYScreenWithToolbar));
                                    break;
                                case 5:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 0.6 - 220 * i + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 0.6 + 40 * i  + maxYScreenWithToolbar));
                                    break;
                                default:
                                    Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                    break;
                            }
                            triangle.setRotation(0);
                            break;
                        default:
                            Log.e(TAG, "Resolution of screen not supported!");
                            break;
                    }
                }
            }
            masterLayout.addView(layout[i], params);
        }
        masterLayout.addView(triangle, paramsTriangle);
        Log.d(TAG, "Shortcuts Created!");
    }

    private void createShortcutsForLauncher3(Activity activity, Drawable packageImage, ViewGroup masterLayout, int positionInGrid, int rowHeight, int bottomSpace, boolean isHotseatTouched, GridSize gridSize, int optionLayoutStyle, Shortcuts... shortcuts) {
        if(shortcuts.length > MAX_NUMBER_OF_SHORTCUTS){
            Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
            return;
        }
        if(rowHeight < 0){
            Log.e(TAG, "Invalid Row Height, it must be greater than 0");
            return;
        }
        if(shortcuts.length == 0){
            Log.e(TAG, "Shortcuts must be at least one!");
            return;
        }

        USE_SHORTCUTS_FOR_LAUNCHER_3 = true;
        getScreenDimension(activity);
        int positionInGridDefault = positionInGrid;

        if (layout != null || triangle != null)
            clearAllLayout();
        switch (maxXScreen) {
            case 720:
                DIM_WIDTH = 360;
                DIM_HEIGHT = 90;
                break;
            case 1080:
                DIM_WIDTH = 640;
                DIM_HEIGHT = 150;
                break;
            case 1440:
                DIM_WIDTH = 780;
                DIM_HEIGHT = 200;
                break;
            default:
                Log.e(TAG, "Resolution of screen not supported!");
                break;
        }

        LayoutInflater inflater = LayoutInflater.from(activity.getApplicationContext());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(DIM_WIDTH, DIM_HEIGHT);
        RelativeLayout.LayoutParams paramsTriangle = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        //int mIconHeight = ((GridView) gridView).getColumnWidth();
        int mIconHeight;
        int mIconWidth = maxXScreen / gridSize.getColumnCount();
        int dim = (positionInGrid) * mIconWidth;
        int layoutHeightTotal = DIM_HEIGHT * shortcuts.length + PADDING;

        triangle = (RelativeLayout) inflater.inflate(R.layout.shortcuts_triangle, null, false);

        //Scale animation right to left
        ScaleAnimation animationRightToLeft = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
        animationRightToLeft.setDuration(200);

        //Scale animation left to right
        ScaleAnimation animationLeftToRight = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animationLeftToRight.setDuration(200);

        for (int i = 0; i < shortcuts.length; i++) {
            layout[i] = (RelativeLayout) inflater.inflate(R.layout.shortcuts, null, false);
            shortcuts[i].init(layout[i], optionLayoutStyle, activity, packageImage, this);
            if ((dim + DIM_WIDTH) >= maxXScreen) {
                //Destra
                layout[i].setX(dim - DIM_WIDTH + (mIconWidth) - mIconWidth / 4);
                triangle.setX((float) (dim + mIconWidth - mIconWidth / 1.5));
                triangle.setRotation(180);

                //Start Animation
                layout[i].startAnimation(animationRightToLeft);
                triangle.startAnimation(animationRightToLeft);
            } else {
                //Sinistra

                layout[i].setX(dim + mIconWidth / 4);
                triangle.setX((float) (dim + mIconWidth / 2));
                triangle.setRotation(180);

                //Start Animation
                layout[i].startAnimation(animationLeftToRight);
                triangle.startAnimation(animationLeftToRight);
            }

            if ((toolbarHeight = Utils.getToolbarHeight(activity)) >= 0) {
                int maxYScreenWithToolbar = maxYScreen - toolbarHeight * 2;
                positionInGrid = positionInGridDefault;
                //positionInGrid /= gridSize.getColumnCount();
                mIconHeight = rowHeight * (positionInGrid + 1);
                if (rowHeight + layoutHeightTotal > maxYScreenWithToolbar-bottomSpace+PADDING && !isHotseatTouched) {
                    //Alto
                    switch (maxXScreen) {
                        case 720:
                            switch (shortcuts.length) {
                                case 2:
                                    if (i >= 1) {
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2 - 75);
                                    } else {
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2 + 20);
                                    }
                                    triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2 + 94);
                                    break;
                                case 3:
                                    layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 95);
                                    triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 23);
                                    break;
                                case 4:
                                    layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 275);
                                    triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 200);
                                    break;
                                case 5:
                                    layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 475);
                                    triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 400);
                                    break;
                                default:
                                    Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                    break;
                            }
                            triangle.setRotation(0);
                            break;
                        case 1080:
                            switch (shortcuts.length) {
                                case 2:
                                    if (i >= 1) {
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 160 + toolbarHeight * 2);
                                    } else {
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2);
                                    }
                                    triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2 + 120);
                                    break;
                                case 3:
                                    layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 180 + toolbarHeight * i);
                                    triangle.setY(+layoutHeightTotal + mIconHeight / 5 + 140 * i);
                                    break;
                                case 4:
                                    layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 160 * 3 + toolbarHeight * i);
                                    triangle.setY(+layoutHeightTotal + mIconHeight / 5 - 177 * 2 + toolbarHeight * i);
                                    break;
                                case 5:
                                    layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 160 * 5 + toolbarHeight * i);
                                    triangle.setY(+layoutHeightTotal + mIconHeight / 5 - 337 * 2 + toolbarHeight * i);
                                    break;
                                default:
                                    Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                    break;
                            }
                            triangle.setRotation(0);
                            break;
                        case 1440:
                            switch (shortcuts.length) {
                                case 1:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * -0.05));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.05 + 187));
                                    break;
                                case 2:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * -0.05 - 220 * i));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.05 + 203 * i));
                                    break;
                                case 3:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * -0.05 - 220 * i));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.05 + 113 * i));
                                    break;
                                case 4:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * -0.05 - 220 * i));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.05 + 83 * i));
                                    break;
                                case 5:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * -0.05 - 220 * i));
                                    triangle.setY((float)(rowHeight +layoutHeightTotal * -0.05 + 65 * i));
                                    break;
                                default:
                                    Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                    break;
                            }
                            triangle.setRotation(0);
                            break;
                        default:
                            Log.e(TAG, "Resolution of screen not supported!");
                            break;
                    }
                } else if (!isHotseatTouched){
                    //Basso
                    positionInGrid = positionInGridDefault;
                    //positionInGrid /= gridSize.getColumnCount();
                    mIconHeight = rowHeight * (positionInGrid + 1);
                    switch (maxXScreen) {
                        case 720:
                            if (i >= 1) {
                                layout[i].setY(+mIconHeight * 3 / 4 + layoutHeightTotal + toolbarHeight / 2 - 100 * i);
                            } else {
                                layout[i].setY(+mIconHeight * 3 / 4 + layoutHeightTotal + toolbarHeight / 2);
                            }
                            switch (shortcuts.length) {
                                case 1:
                                    triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 36));
                                    break;
                                case 2:
                                    triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 46));
                                    break;
                                case 3:
                                    triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 56));
                                    break;
                                case 4:
                                    triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 68));
                                    break;
                                case 5:
                                    triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 80));
                                    break;
                                default:
                                    Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                    break;
                            }
                            break;
                        case 1080:
                            if (i >= 1) {
                                layout[i].setY(+mIconHeight * 3 / 4 + layoutHeightTotal + toolbarHeight / 2 - 160 * i);
                            } else {
                                layout[i].setY(+mIconHeight * 3 / 4 + layoutHeightTotal + toolbarHeight / 2);
                            }
                            triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 50 + 10 * i));
                            break;
                        case 1440:
                            switch (shortcuts.length) {
                                case 1:
                                    layout[i].setY((float)(rowHeight + layoutHeightTotal * 2.3));
                                    triangle.setY(rowHeight + layoutHeightTotal + 255);
                                    break;
                                case 2:
                                    if (i >= 1) {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.9 - 220 * i));
                                    } else {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.9));
                                    }
                                    triangle.setY(rowHeight + layoutHeightTotal * i + 120 * i);
                                    break;
                                case 3:
                                    if (i >= 1) {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.6 - 220 * i));
                                    } else {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.6));
                                    }
                                    triangle.setY(rowHeight + layoutHeightTotal * i - 363 * i);
                                    break;
                                case 4:
                                    if (i >= 1) {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.5 - 220 * i));
                                    } else {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.5));
                                    }
                                    triangle.setY(rowHeight + layoutHeightTotal * i - 643 * i);
                                    break;
                                case 5:
                                    if (i >= 1) {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.4 - 220 * i));
                                    } else {
                                        layout[i].setY((float)(rowHeight + layoutHeightTotal * 1.4));
                                    }
                                    triangle.setY(rowHeight + layoutHeightTotal * i - 893 * i);
                                    break;
                                default:
                                    Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                    break;
                            }
                            break;
                        default:
                            Log.e(TAG, "Resolution of screen not supported!");
                            break;
                    }
                }else if(isHotseatTouched){
                    //Alto
                    switch (maxXScreen) {
                        case 720:
                            switch (shortcuts.length) {
                                case 2:
                                    if (i >= 1) {
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2 - 75);
                                    } else {
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2 + 20);
                                    }
                                    triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2 + 94);
                                    break;
                                case 3:
                                    layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 95);
                                    triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 23);
                                    break;
                                case 4:
                                    layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 275);
                                    triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 200);
                                    break;
                                case 5:
                                    layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 475);
                                    triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 400);
                                    break;
                                default:
                                    Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                    break;
                            }
                            triangle.setRotation(0);
                            break;
                        case 1080:
                            switch (shortcuts.length) {
                                case 2:
                                    if (i >= 1) {
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 160 + toolbarHeight * 2);
                                    } else {
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2);
                                    }
                                    triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2 + 120);
                                    break;
                                case 3:
                                    layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 180 + toolbarHeight * i);
                                    triangle.setY(+layoutHeightTotal + mIconHeight / 5 + 140 * i);
                                    break;
                                case 4:
                                    layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 160 * 3 + toolbarHeight * i);
                                    triangle.setY(+layoutHeightTotal + mIconHeight / 5 - 177 * 2 + toolbarHeight * i);
                                    break;
                                case 5:
                                    layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 160 * 5 + toolbarHeight * i);
                                    triangle.setY(+layoutHeightTotal + mIconHeight / 5 - 337 * 2 + toolbarHeight * i);
                                    break;
                                default:
                                    Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                    break;
                            }
                            triangle.setRotation(0);
                            break;
                        case 1440:
                            switch (shortcuts.length) {
                                case 1:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 2.6 + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 2.6 + 162 + maxYScreenWithToolbar));
                                    break;
                                case 2:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 1.4 - 220 * i + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 1.4 + 155 * i + maxYScreenWithToolbar));
                                    break;
                                case 3:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 0.95 - 220 * i + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 0.95 + 80 * i + maxYScreenWithToolbar));
                                    break;
                                case 4:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 0.7 - 220 * i + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 0.7 + 53 * i  + maxYScreenWithToolbar));
                                    break;
                                case 5:
                                    layout[i].setY((float)(rowHeight -layoutHeightTotal * 0.6 - 220 * i + maxYScreenWithToolbar));
                                    triangle.setY((float)(rowHeight -layoutHeightTotal * 0.6 + 40 * i  + maxYScreenWithToolbar));
                                    break;
                                default:
                                    Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                    break;
                            }
                            triangle.setRotation(0);
                            break;
                        default:
                            Log.e(TAG, "Resolution of screen not supported!");
                            break;
                    }
                }
            }
            masterLayout.addView(layout[i], params);
        }
        masterLayout.addView(triangle, paramsTriangle);
        Log.d(TAG, "Shortcuts Created!");
    }

    private void createShortcuts(Activity activity, Drawable packageImage, ViewGroup masterLayout, int currentXPosition, int currentYPosition, int rowHeight, AdapterView gridView, int optionLayoutStyle, List<Shortcuts> shortcuts){
        if(shortcuts.size() > MAX_NUMBER_OF_SHORTCUTS){
            Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
            return;
        }
        if(rowHeight < 0){
            Log.e(TAG, "Invalid Row Height, it must be greater than 0");
            return;
        }
        if(shortcuts.size() == 0){
            Log.e(TAG, "Shortcuts must be at least one!");
            return;
        }

        getScreenDimension(activity);
        int positionInGrid = getPositionInGrid(currentXPosition, currentYPosition, gridView);

        GridSize gridSize = Utils.getGridSize(gridView);

        if(layout != null || triangle != null)
            clearAllLayout();
        switch (maxXScreen){
            case 720:
                DIM_WIDTH = 360;
                DIM_HEIGHT = 100;
                break;
            case 1080:
                DIM_WIDTH = 640;
                DIM_HEIGHT = 150;
                break;
            case 1440:
                DIM_WIDTH = 840;
                DIM_HEIGHT = 200;
                break;
            default:
                Log.e(TAG, "Resolution of screen not supported!");
                break;
        }

        if (isClickOnItem(currentXPosition, currentYPosition, gridSize, gridView)) {
            LayoutInflater inflater = LayoutInflater.from(activity.getApplicationContext());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(DIM_WIDTH, DIM_HEIGHT);
            RelativeLayout.LayoutParams paramsTriangle = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            //int mIconHeight = ((GridView) gridView).getColumnWidth();
            int mIconHeight;
            int mIconWidth = maxXScreen / ((GridView) gridView).getNumColumns();
            int dim = (positionInGrid) * mIconWidth;
            int layoutHeightTotal = DIM_HEIGHT * shortcuts.size() + 20;

            triangle = (RelativeLayout) inflater.inflate(R.layout.shortcuts_triangle, null, false);

            //Scale animation right to left
            ScaleAnimation animationRightToLeft = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
            animationRightToLeft.setDuration(200);

            //Scale animation left to right
            ScaleAnimation animationLeftToRight = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animationLeftToRight.setDuration(200);

            for (int i = 0; i < shortcuts.size(); i++) {
                layout[i] = (RelativeLayout) inflater.inflate(R.layout.shortcuts, null, false);
                shortcuts.get(i).init(layout[i], optionLayoutStyle, activity, packageImage, this);
                if ((dim + DIM_WIDTH) >= maxXScreen) {
                    //Destra
                    layout[i].setX(dim - DIM_WIDTH + (mIconWidth) - mIconWidth / 4);
                    triangle.setX((float) (dim + mIconWidth - mIconWidth / 1.5));
                    triangle.setRotation(180);

                    //Start Animation
                    layout[i].startAnimation(animationRightToLeft);
                    triangle.startAnimation(animationRightToLeft);
                } else {
                    //Sinistra

                    layout[i].setX(dim + mIconWidth / 4);
                    triangle.setX((float) (dim + mIconWidth / 2));
                    triangle.setRotation(180);

                    //Start Animation
                    layout[i].startAnimation(animationLeftToRight);
                    triangle.startAnimation(animationLeftToRight);
                }

                if ((toolbarHeight = Utils.getToolbarHeight(activity)) > 0) {
                    int maxYScreenWithToolbar = maxYScreen - toolbarHeight * 2;
                    positionInGrid = ((GridView) gridView).pointToPosition((int) currentXPosition, (int) currentYPosition);
                    positionInGrid /= gridSize.getColumnCount();
                    mIconHeight = Math.round(displayDensity * rowHeight) * positionInGrid + 1;
                    if (mIconHeight + layoutHeightTotal > maxYScreenWithToolbar) {
                        //Alto
                        switch (maxXScreen){
                            case 720:
                                switch (shortcuts.size()){
                                    case 2:
                                        if (i >= 1) {
                                            layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2 - 75);
                                        } else {
                                            layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2 + 20);
                                        }
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2 + 94);
                                        break;
                                    case 3:
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 95);
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 23);
                                        break;
                                    case 4:
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 275);
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 200);
                                        break;
                                    case 5:
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 475);
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 400);
                                        break;
                                    default:
                                        Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                        break;
                                }
                                triangle.setRotation(0);
                                break;
                            case 1080:
                                switch (shortcuts.size()){
                                    case 2:
                                        if (i >= 1) {
                                            layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 160 + toolbarHeight * 2);
                                        } else {
                                            layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2);
                                        }
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2 + 120);
                                        break;
                                    case 3:
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 180 + toolbarHeight * i);
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 + 140 * i);
                                        break;
                                    case 4:
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 160 * 3 + toolbarHeight * i);
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 - 177 * 2 + toolbarHeight * i);
                                        break;
                                    case 5:
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 160 * 5 + toolbarHeight * i);
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 - 337 * 2 + toolbarHeight * i);
                                        break;
                                    default:
                                        Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                        break;
                                }
                                triangle.setRotation(0);
                                break;
                            case 1440:
                                switch (shortcuts.size()){
                                    case 2:
                                        if (i >= 1) {
                                            layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 220 + toolbarHeight * 2);
                                        } else {
                                            layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2);
                                        }
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2 + 160);
                                        break;
                                    case 3:
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 220 + toolbarHeight * i);
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 + 195 * i);
                                        break;
                                    case 4:
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 220 * 3 + toolbarHeight * i);
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 - 250 * 2 + toolbarHeight * i);
                                        break;
                                    case 5:
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 220 * 5 + toolbarHeight * i);
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 - 470 * 2 + toolbarHeight * i);
                                        break;
                                    default:
                                        Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                        break;
                                }
                                triangle.setRotation(0);
                                break;
                            default:
                                Log.e(TAG, "Resolution of screen not supported!");
                                break;
                        }
                    } else {
                        //Basso
                        positionInGrid = ((GridView) gridView).pointToPosition((int) currentXPosition, (int) currentYPosition);
                        positionInGrid /= gridSize.getColumnCount();
                        mIconHeight = Math.round(displayDensity * rowHeight) * positionInGrid + 1;
                        switch (maxXScreen){
                            case 720:
                                if (i >= 1) {
                                    layout[i].setY(+mIconHeight * 3 / 4 + layoutHeightTotal + toolbarHeight / 2 - 100 * i);
                                } else {
                                    layout[i].setY(+mIconHeight * 3 / 4 + layoutHeightTotal + toolbarHeight / 2);
                                }
                                switch (shortcuts.size()){
                                    case 1:
                                        triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 36));
                                        break;
                                    case 2:
                                        triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 46));
                                        break;
                                    case 3:
                                        triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 56));
                                        break;
                                    case 4:
                                        triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 68));
                                        break;
                                    case 5:
                                        triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 80));
                                        break;
                                    default:
                                        Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
                                        break;
                                }
                                break;
                            case 1080:
                                if (i >= 1) {
                                    layout[i].setY(+mIconHeight * 3 / 4 + layoutHeightTotal + toolbarHeight / 2 - 160 * i);
                                } else {
                                    layout[i].setY(+mIconHeight * 3 / 4 + layoutHeightTotal + toolbarHeight / 2);
                                }
                                triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 50 + 10*i));
                                break;
                            case 1440:
                                if (i >= 1) {
                                    layout[i].setY(+mIconHeight * 3 / 4 + layoutHeightTotal + toolbarHeight / 2 - 220 * i);
                                } else {
                                    layout[i].setY(+mIconHeight * 3 / 4 + layoutHeightTotal + toolbarHeight / 2);
                                }
                                triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 80));
                                break;
                            default:
                                Log.e(TAG, "Resolution of screen not supported!");
                                break;
                        }
                    }
                }
                masterLayout.addView(layout[i], params);
            }
            masterLayout.addView(triangle, paramsTriangle);
        }
        Log.d(TAG, "Shortcuts Created!");
    }

    private void createShortcuts(Activity activity, Drawable packageImage, ViewGroup masterLayout, int currentXPosition, int currentYPosition, int rowHeight, AdapterView gridView, int optionLayoutStyle, final Shortcuts... shortcuts){
        if(shortcuts.length > MAX_NUMBER_OF_SHORTCUTS){
            Log.e(TAG, "Invalid Shortcuts number, max value is " + String.valueOf(MAX_NUMBER_OF_SHORTCUTS) + "!");
            return;
        }
        if(rowHeight < 0){
            Log.e(TAG, "Invalid Row Height, it must be greater than 0");
            return;
        }
        if(shortcuts.length == 0){
            Log.e(TAG, "Shortcuts must be at least one!");
            return;
        }

        getScreenDimension(activity);
        int positionInGrid = getPositionInGrid(currentXPosition, currentYPosition, gridView);

        GridSize gridSize = Utils.getGridSize(gridView);

        if(layout != null || triangle != null)
            clearAllLayout();

        switch (maxXScreen){
            case 720:
                DIM_WIDTH = 360;
                DIM_HEIGHT = 100;
                break;
            case 1080:
                DIM_WIDTH = 640;
                DIM_HEIGHT = 150;
                break;
            case 1440:
                DIM_WIDTH = 840;
                DIM_HEIGHT = 200;
                break;
            default:
                Log.e(TAG, "Resolution of screen not supported!");
                break;
        }

        if (isClickOnItem(currentXPosition, currentYPosition, gridSize, gridView)) {
            LayoutInflater inflater = LayoutInflater.from(activity.getApplicationContext());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(DIM_WIDTH, DIM_HEIGHT);
            RelativeLayout.LayoutParams paramsTriangle = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            //int mIconHeight = ((GridView) gridView).getColumnWidth();
            int mIconHeight;
            int mIconWidth = maxXScreen / ((GridView) gridView).getNumColumns();
            int dim = (positionInGrid) * mIconWidth;
            int layoutHeightTotal = DIM_HEIGHT * shortcuts.length + 20;

            triangle = (RelativeLayout) inflater.inflate(R.layout.shortcuts_triangle, null, false);

            //Scale animation right to left
            ScaleAnimation animationRightToLeft = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
            animationRightToLeft.setDuration(200);

            //Scale animation left to right
            ScaleAnimation animationLeftToRight = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animationLeftToRight.setDuration(200);

            for (int i = 0; i < shortcuts.length; i++) {
                layout[i] = (RelativeLayout) inflater.inflate(R.layout.shortcuts, null, false);
                shortcuts[i].init(layout[i], optionLayoutStyle, activity, packageImage, this);
                if ((dim + DIM_WIDTH) >= maxXScreen) {
                    //Destra
                    layout[i].setX(dim - DIM_WIDTH + (mIconWidth) - mIconWidth / 4);
                    triangle.setX((float) (dim + mIconWidth - mIconWidth / 1.5));
                    triangle.setRotation(180);

                    //Start Animation
                    layout[i].startAnimation(animationRightToLeft);
                    triangle.startAnimation(animationRightToLeft);
                } else {
                    //Sinistra
                    layout[i].setX(dim + mIconWidth / 4);
                    triangle.setX((float) (dim + mIconWidth / 2));
                    triangle.setRotation(180);

                    //Start Animation
                    layout[i].startAnimation(animationLeftToRight);
                    triangle.startAnimation(animationLeftToRight);
                }

                if ((toolbarHeight = Utils.getToolbarHeight(activity)) > 0) {
                    int maxYScreenWithToolbar = maxYScreen - toolbarHeight * 2;
                    positionInGrid = ((GridView) gridView).pointToPosition((int) currentXPosition, (int) currentYPosition);
                    positionInGrid /= gridSize.getColumnCount();
                    mIconHeight = Math.round(displayDensity * rowHeight) * positionInGrid + 1;
                    if (mIconHeight + layoutHeightTotal > maxYScreenWithToolbar) {
                        //Alto
                        switch (maxXScreen){
                            case 720:
                                switch (shortcuts.length){
                                    case 2:
                                        if (i >= 1) {
                                            layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2 - 75);
                                        } else {
                                            layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2 + 20);
                                        }
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2 + 94);
                                        break;
                                    case 3:
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 95);
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 23);
                                        break;
                                    case 4:
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 275);
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 200);
                                        break;
                                    case 5:
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 475);
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * i - 400);
                                        break;
                                    default:
                                        Log.e(TAG, "Invalid numbers of shortcuts! Max is 5");
                                        break;
                                }
                                triangle.setRotation(0);
                                break;
                            case 1080:
                                switch (shortcuts.length){
                                    case 2:
                                        if (i >= 1) {
                                            layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 160 + toolbarHeight * 2);
                                        } else {
                                            layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2);
                                        }
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2 + 120);
                                        break;
                                    case 3:
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 180 + toolbarHeight * i);
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 + 140 * i);
                                        break;
                                    case 4:
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 160 * 3 + toolbarHeight * i);
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 - 177 * 2 + toolbarHeight * i);
                                        break;
                                    case 5:
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 160 * 5 + toolbarHeight * i);
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 - 337 * 2 + toolbarHeight * i);
                                        break;
                                    default:
                                        Log.e(TAG, "Invalid numbers of shortcuts! Max is 5");
                                        break;
                                }
                                triangle.setRotation(0);
                                break;
                            case 1440:
                                switch (shortcuts.length){
                                    case 2:
                                        if (i >= 1) {
                                            layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 220 + toolbarHeight * 2);
                                        } else {
                                            layout[i].setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2);
                                        }
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 + toolbarHeight * 2 + 160);
                                        break;
                                    case 3:
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 220 + toolbarHeight * i);
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 + 195 * i);
                                        break;
                                    case 4:
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 220 * 3 + toolbarHeight * i);
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 - 250 * 2 + toolbarHeight * i);
                                        break;
                                    case 5:
                                        layout[i].setY(+layoutHeightTotal + mIconHeight / 5 - 220 * 5 + toolbarHeight * i);
                                        triangle.setY(+layoutHeightTotal + mIconHeight / 5 - 470 * 2 + toolbarHeight * i);
                                        break;
                                    default:
                                        Log.e(TAG, "Invalid numbers of shortcuts! Max is 5");
                                        break;
                                }
                                triangle.setRotation(0);
                                break;
                            default:
                                Log.e(TAG, "Resolution of screen not supported!");
                                break;
                        }
                    } else {
                        //Basso
                        positionInGrid = ((GridView) gridView).pointToPosition((int) currentXPosition, (int) currentYPosition);
                        positionInGrid /= gridSize.getColumnCount();
                        mIconHeight = Math.round(displayDensity * rowHeight) * positionInGrid + 1;
                        switch (maxXScreen){
                            case 720:
                                if (i >= 1) {
                                    layout[i].setY(+mIconHeight * 3 / 4 + layoutHeightTotal + toolbarHeight / 2 - 100 * i);
                                } else {
                                    layout[i].setY(+mIconHeight * 3 / 4 + layoutHeightTotal + toolbarHeight / 2);
                                }
                                switch (shortcuts.length){
                                    case 1:
                                        triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 36));
                                        break;
                                    case 2:
                                        triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 46));
                                        break;
                                    case 3:
                                        triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 56));
                                        break;
                                    case 4:
                                        triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 68));
                                        break;
                                    case 5:
                                        triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 80));
                                        break;
                                    default:
                                        Log.e(TAG, "Invalid numbers of shortcuts! Max is 5");
                                        break;
                                }
                                break;
                            case 1080:
                                if (i >= 1) {
                                    layout[i].setY(+mIconHeight * 3 / 4 + layoutHeightTotal + toolbarHeight / 2 - 160 * i);
                                } else {
                                    layout[i].setY(+mIconHeight * 3 / 4 + layoutHeightTotal + toolbarHeight / 2);
                                }
                                triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 50 + 10*i));
                                break;
                            case 1440:
                                if (i >= 1) {
                                    layout[i].setY(+mIconHeight * 3 / 4 + layoutHeightTotal + toolbarHeight / 2 - 220 * i);
                                } else {
                                    layout[i].setY(+mIconHeight * 3 / 4 + layoutHeightTotal + toolbarHeight / 2);
                                }
                                triangle.setY((float) (+mIconHeight * 3 / 4 + layoutHeightTotal - toolbarHeight * i + 80));
                                break;
                            default:
                                Log.e(TAG, "Resolution of screen not supported!");
                                break;
                        }
                    }
                }
                masterLayout.addView(layout[i], params);
            }
            masterLayout.addView(triangle, paramsTriangle);
        }
        Log.d(TAG, "Shortcuts Created!");
    }

    /**
     * Check if click is on Item
     * @param currentXPosition int
     * @param currentYPosition int
     * @param gridSize GridSize
     * @return boolean
     */
    private boolean isClickOnItem(int currentXPosition, int currentYPosition, GridSize gridSize, AdapterView gridView){
        int positionPointed = ((GridView) gridView).pointToPosition(currentXPosition, currentYPosition);
        return positionPointed < gridSize.getColumnCount()*gridSize.getRowCount();
    }

    /**
     * Clear all shortcuts layout
     */
    public void clearAllLayout() {
        if(layout != null) {
            for (int i = 0; i < layout.length; i++) {
                if (layout[i] != null && ((ViewGroup) layout[i].getParent()) != null) {
                    ((ViewGroup) layout[i].getParent()).removeView(layout[i]);
                }
            }
            Log.d(TAG, "Layout clear!");
        }
        if(triangle != null && ((ViewGroup) triangle.getParent()) != null) {
            ((ViewGroup) triangle.getParent()).removeView(triangle);
            Log.d(TAG, "Layout clear!");
        }
    }

    /**
     * Get screen dimension
     */
    private void getScreenDimension(Activity activity){
        Display mdisp = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = activity.getApplicationContext().getResources().getDisplayMetrics();
        displayDensity = displayMetrics.density;
        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);
        maxXScreen = mdispSize.x;
        maxYScreen = mdispSize.y;
        Log.d(TAG, "Dimension acquired X:" + String.valueOf(maxXScreen) + " Y: " + String.valueOf(maxYScreen));
    }

    /**
     * Get position in grid
     * @param currentXPosition int
     * @param currentYPosition int
     * @param gridView AdapterView
     * @return int
     */
    private int getPositionInGrid(int currentXPosition, int currentYPosition, AdapterView gridView){
        int positionInGrid = 0;
        if(gridView != null) {
            positionInGrid = ((GridView) gridView).pointToPosition(currentXPosition, currentYPosition);
            positionInGrid -= ((GridView) gridView).getNumColumns();
            positionInGrid %= ((GridView) gridView).getNumColumns();
            Log.d(TAG, "Position In Grid: " + String.valueOf(positionInGrid));
            if(positionInGrid < 0){
                positionInGrid = ((GridView) gridView).pointToPosition(currentXPosition, currentYPosition);
                Log.w(TAG, "Position In Grid lower than 0, trying again, positionInGrid: " + String.valueOf(positionInGrid));
            }
        }
        return positionInGrid;
    }

}
