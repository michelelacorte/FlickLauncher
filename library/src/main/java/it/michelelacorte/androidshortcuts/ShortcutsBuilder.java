package it.michelelacorte.androidshortcuts;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import it.michelelacorte.androidshortcuts.util.GridSize;

/**
 * Created by Michele on 20/02/2017.
 */

public class ShortcutsBuilder {
    private static final String TAG = "ShortcutsBuilder";

    private AdapterView gridView;
    private Activity activity;
    private ViewGroup masterLayout;
    private GridSize gridSize;
    private int currentXPosition;
    private int currentYPosition;
    private int rowHeight;
    private int optionLayoutStyle;
    private Shortcuts[] shortcutsArray;
    private List<Shortcuts> shortcutsList;
    private Drawable packageImage;
    private int bottomSpace;
    private boolean isHotseatTouched;
    private int positionInGrid;


    private boolean IS_NORMAL = false;
    private boolean IS_LAUNCHER3 = false;


    public ShortcutsBuilder(NormalShortcuts normalShortcuts)
    {
        if(normalShortcuts.gridView == null){
            Log.e(TAG, "Impossible to set AdapterView to NULL! Please Check it");
        }
        if(normalShortcuts.activity == null){
            Log.e(TAG, "Impossible to set Activity to NULL! Please Check it");
        }
        if(normalShortcuts.masterLayout == null){
            Log.e(TAG, "Impossible to set ViewGroup to NULL! Please Check it");
        }
        if(shortcutsArray == null || shortcutsArray.length == 0){
            Log.e(TAG, "Impossible to set ShortcutsArray to NULL! Please Check it");
        }
        if(shortcutsList == null || shortcutsList.size() == 0){
            Log.e(TAG, "Impossible to set ShortcutsList to NULL! Please Check it");
        }
        if(packageImage == null){
            Log.e(TAG, "Impossible to set package image to NULL! Please Check it");
        }
        this.gridView = normalShortcuts.gridView;
        this.activity = normalShortcuts.activity;
        this.masterLayout = normalShortcuts.masterLayout;
        this.currentXPosition = normalShortcuts.currentXPosition;
        this.currentYPosition = normalShortcuts.currentYPosition;
        this.rowHeight = normalShortcuts.rowHeight;
        this.optionLayoutStyle = normalShortcuts.optionLayoutStyle;
        this.shortcutsArray = normalShortcuts.shortcutsArray;
        this.shortcutsList = normalShortcuts.shortcutsList;
        this.packageImage = normalShortcuts.packageImage;
        IS_NORMAL = true;
    }

    public ShortcutsBuilder(Launcher3Shortcuts launcher3Shortcuts)
    {
        if(launcher3Shortcuts.gridSize == null){
            Log.e(TAG, "Impossible to set GridSize to NULL! Please Check it");
        }
        if(launcher3Shortcuts.activity == null){
            Log.e(TAG, "Impossible to set Activity to NULL! Please Check it");
        }
        if(launcher3Shortcuts.masterLayout == null){
            Log.e(TAG, "Impossible to set ViewGroup to NULL! Please Check it");
        }
        if(shortcutsArray == null || shortcutsArray.length == 0){
            Log.e(TAG, "Impossible to set ShortcutsArray to NULL! Please Check it");
        }
        if(shortcutsList == null || shortcutsList.size() == 0){
            Log.e(TAG, "Impossible to set ShortcutsList to NULL! Please Check it");
        }
        if(packageImage == null){
            Log.e(TAG, "Impossible to set package image to NULL! Please Check it");
        }
        this.gridSize = launcher3Shortcuts.gridSize;
        this.activity = launcher3Shortcuts.activity;
        this.masterLayout = launcher3Shortcuts.masterLayout;
        this.rowHeight = launcher3Shortcuts.rowHeight;
        this.optionLayoutStyle = launcher3Shortcuts.optionLayoutStyle;
        this.shortcutsArray = launcher3Shortcuts.shortcutsArray;
        this.shortcutsList = launcher3Shortcuts.shortcutsList;
        this.packageImage = launcher3Shortcuts.packageImage;
        this.isHotseatTouched = launcher3Shortcuts.isHotseatTouched;
        this.bottomSpace = launcher3Shortcuts.bottomSpace;
        this.positionInGrid = launcher3Shortcuts.positionInGrid;
        IS_LAUNCHER3 = true;
    }

    public static class Builder{
        private Activity activity;
        private ViewGroup masterLayout;

        public Builder(Activity activity, ViewGroup masterLayout){
            this.activity = activity;
            this.masterLayout = masterLayout;
        }

        public NormalShortcuts normalShortcuts(AdapterView gridView, int currentXPosition, int currentYPosition, int rowHeight)
        {
            return new NormalShortcuts(activity, masterLayout, gridView, currentXPosition, currentYPosition, rowHeight);
        }

        public NormalShortcuts normalShortcuts(GridView gridView, int currentXPosition, int currentYPosition, int rowHeight)
        {
            return new NormalShortcuts(activity, masterLayout, gridView, currentXPosition, currentYPosition, rowHeight);
        }
        public Launcher3Shortcuts launcher3Shortcuts(GridSize gridSize, int positionInGrid, int rowHeight, int bottomSpace, boolean isHotseatTouched)
        {
            return new Launcher3Shortcuts(activity, masterLayout, gridSize, positionInGrid, rowHeight, bottomSpace, isHotseatTouched);
        }
    }


    public static class NormalShortcuts{
        private AdapterView gridView;
        private Activity activity;
        private ViewGroup masterLayout;
        private int currentXPosition;
        private int currentYPosition;
        private int rowHeight;
        private int optionLayoutStyle;
        private Shortcuts[] shortcutsArray;
        private List<Shortcuts> shortcutsList;
        private Drawable packageImage;

        public NormalShortcuts(Activity activity, ViewGroup masterLayout, AdapterView gridView, int currentXPosition, int currentYPosition, int rowHeight){
            this.activity = activity;
            this.masterLayout = masterLayout;
            this.gridView = gridView;
            this.currentXPosition = currentXPosition;
            this.currentYPosition = currentYPosition;
            this.rowHeight = rowHeight;
        }

        public NormalShortcuts(Activity activity, ViewGroup masterLayout, GridView gridView, int currentXPosition, int currentYPosition, int rowHeight){
            this.activity = activity;
            this.masterLayout = masterLayout;
            this.gridView = gridView;
            this.currentXPosition = currentXPosition;
            this.currentYPosition = currentYPosition;
            this.rowHeight = rowHeight;
        }

        public NormalShortcuts setShortcutsArray(@NotNull Shortcuts... shortcuts){
            this.shortcutsArray = shortcuts;
            return this;
        }


        public NormalShortcuts setShortcutsList(@NotNull List<Shortcuts> shortcuts){
            this.shortcutsList = new ArrayList<>(shortcuts);
            return this;
        }

        public NormalShortcuts setPackageImage(Drawable packageImage){
            this.packageImage = packageImage;
            return this;
        }


        public NormalShortcuts setOptionLayoutStyle(int optionLayoutStyle)
        {
            this.optionLayoutStyle = optionLayoutStyle;
            return this;
        }

        public ShortcutsBuilder build() {
            return new ShortcutsBuilder(this);
        }
    }


    public static class Launcher3Shortcuts{
        private Activity activity;
        private ViewGroup masterLayout;
        private GridSize gridSize;
        private int optionLayoutStyle;
        private Shortcuts[] shortcutsArray;
        private List<Shortcuts> shortcutsList;
        private Drawable packageImage;
        private int rowHeight;
        private int bottomSpace;
        private boolean isHotseatTouched;
        private int positionInGrid;

        public Launcher3Shortcuts(Activity activity, ViewGroup masterLayout, GridSize gridSize, int positionInGrid, int rowHeight, int bottomSpace, boolean isHotseatTouched){
            this.activity = activity;
            this.masterLayout = masterLayout;
            this.gridSize = gridSize;
            this.rowHeight = rowHeight;
            this.bottomSpace = bottomSpace;
            this.isHotseatTouched = isHotseatTouched;
            this.positionInGrid = positionInGrid;
        }

        public Launcher3Shortcuts setShortcutsArray(@NotNull Shortcuts... shortcuts){
            this.shortcutsArray = shortcuts;
            return this;
        }


        public Launcher3Shortcuts setShortcutsList(@NotNull List<Shortcuts> shortcuts){
            this.shortcutsList = new ArrayList<>(shortcuts);
            return this;
        }

        public Launcher3Shortcuts setPackageImage(Drawable packageImage){
            this.packageImage = packageImage;
            return this;
        }


        public Launcher3Shortcuts setOptionLayoutStyle(int optionLayoutStyle)
        {
            this.optionLayoutStyle = optionLayoutStyle;
            return this;
        }

        public ShortcutsBuilder build() {
            return new ShortcutsBuilder(this);
        }
    }


    public Activity getActivity() {
        return activity;
    }

    public AdapterView getGridView() {
        return gridView;
    }

    public ViewGroup getMasterLayout() {
        return masterLayout;
    }

    public GridSize getGridSize() {
        return gridSize;
    }

    public int getCurrentXPosition() {
        return currentXPosition;
    }

    public int getOptionLayoutStyle() {
        return optionLayoutStyle;
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public int getCurrentYPosition() {
        return currentYPosition;
    }

    public Shortcuts[] getShortcutsArray() {
        return shortcutsArray;
    }

    public List<Shortcuts> getShortcutsList() {
        return shortcutsList;
    }

    public boolean isLauncher3() {
        return IS_LAUNCHER3;
    }

    public boolean isNormal() {
        return IS_NORMAL;
    }

    public Drawable getPackageImage() {
        return packageImage;
    }

    public boolean isHotseatTouched() {
        return isHotseatTouched;
    }

    public int getPositionInGrid() {
        return positionInGrid;
    }

    public int getBottomSpace() {
        return bottomSpace;
    }

}
