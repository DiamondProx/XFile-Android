package com.huangjiang.view;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * 动画效果帮助类
 */
public class AnimationHelper {


    public static void startSendFileAnimation(final View startView, final View endView, int startLocationX, int startLocationY, int endLocationX, int endLocationY) {
        // 目标位置
        int endX = endLocationX + endView.getMeasuredWidth() / 2;
        int endY = endLocationY + endView.getMeasuredHeight() / 2;
        // 起始位置
        int startX = startLocationX + startView.getMeasuredWidth() / 2;
        int startY = startLocationY + startView.getMeasuredHeight() / 2;
        // 移动偏移位置
        int moveX = endX - startX;
        int moveY = endY - startY;

        AnimationSet animationSet = new AnimationSet(false);
        // 位移动画
        Animation translateAnimation = new TranslateAnimation(0, moveX, 0, moveY);
        // 旋转动画
        Animation rotateAnimation = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        // 缩放动画
        Animation scaleAnimation = new ScaleAnimation(1.0f, 0.3f, 1.0f, 0.3f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animationSet.addAnimation(rotateAnimation);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(translateAnimation);
        animationSet.setDuration(600);
        animationSet.setFillAfter(false);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 完成后隐藏图标
                startView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        startView.startAnimation(animationSet);

    }

}
