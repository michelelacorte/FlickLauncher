package com.android.launcher3.util;

/**
 * Created by Michele on 17/03/2017.
 */

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class OnSwipeTouchListener implements OnTouchListener {

    private final GestureDetector gestureDetector;

    private static int fingerCount = 1;

    public OnSwipeTouchListener (Context ctx){
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public GestureDetector getGestureDetector(){
        return  gestureDetector;
    }

    public int getFingerCount() {
        return fingerCount;
    }

    public static void setFingerCount(int fingerCount) {
        OnSwipeTouchListener.fingerCount = fingerCount;
    }


    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            //Do not use this (swipe right)
                        } else {
                            //Do not use this (swipe left)
                        }
                        result = true;
                    }
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        if(getFingerCount() > 1){
                            setFingerCount(1);
                            onSwipeBottomTwoFingers();
                        }else {
                            onSwipeBottom();
                        }
                    } else {
                        if(getFingerCount() > 1) {
                            setFingerCount(1);
                            onSwipeTopTwoFingers();
                        }else {
                            onSwipeTop();
                        }
                    }
                    result = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public void onSwipeBottomTwoFingers() {
    }

    public void onSwipeTopTwoFingers() {
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }
}