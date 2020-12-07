package com.example.tjarnav.ar.arcore.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.animation.LinearInterpolator;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.math.Vector3Evaluator;

public class TranslatingNode extends Node {
    private TranslatingNode instance = this;
    private ObjectAnimator animation=null;
    private boolean isAutoDismiss = true; // 是否自动消失
    private Vector3 start;
    private Vector3 end;
    private long duration;


    /**
     *
     * @param start
     * @param end
     * @param duration
     */
    public TranslatingNode(Vector3 start, Vector3 end, long duration){
        this.start=start;
        this.end=end;
        this.duration=duration;
    }

    @Override
    public void onActivate() {
        startAnimation();
    }

    @Override
    public void onDeactivate() {
        stopAnimation();
    }

    private void startAnimation() {
        if (animation != null) {
            return;
        }

        animation = createAnimator();
        animation.setTarget(this);
        animation.setDuration(duration);
        animation.start();
    }

    private void stopAnimation(){
        if (animation == null) {
            return;
        }
        animation.cancel();
        animation = null;
    }

    private ObjectAnimator createAnimator() {
        ObjectAnimator objectAnimator=new ObjectAnimator();
        objectAnimator.setObjectValues(start,end);
        objectAnimator.setPropertyName("LocalPosition");
        objectAnimator.setEvaluator(new Vector3Evaluator());
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setAutoCancel(true);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // 上升到最顶部的时候，自动消失
                if (isAutoDismiss) {
                    instance.setEnabled(false);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return objectAnimator;
    }
    private ObjectAnimator createLeftAnimator() {
        ObjectAnimator objectAnimator=new ObjectAnimator();
        objectAnimator.setObjectValues(start,end);
        objectAnimator.setPropertyName("LocalPosition");
        objectAnimator.setEvaluator(new Vector3Evaluator());
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setAutoCancel(true);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // 上升到最顶部的时候，自动消失
                if (isAutoDismiss) {
                    instance.setEnabled(false);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return objectAnimator;
    }
}
