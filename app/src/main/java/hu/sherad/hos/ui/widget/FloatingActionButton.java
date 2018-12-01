package hu.sherad.hos.ui.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import hu.sherad.hos.R;

public class FloatingActionButton extends AppCompatImageButton {

    private Animation mShowAnimation;
    private Animation mHideAnimation;

    public FloatingActionButton(Context context) {
        this(context, null);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mShowAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fab_scale_up);
        mHideAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fab_scale_down);
    }

    public void show(boolean animate) {
        if (isHidden()) {
            if (animate) {
                playShowAnimation();
            }
            super.setVisibility(VISIBLE);
        }
    }

    public void hide(boolean animate) {
        if (!isHidden()) {
            if (animate) {
                playHideAnimation();
            }
            super.setVisibility(INVISIBLE);
        }
    }

    void playHideAnimation() {
        mShowAnimation.cancel();
        startAnimation(mHideAnimation);
    }

    public boolean isHidden() {
        return getVisibility() == INVISIBLE;
    }

    void playShowAnimation() {
        mHideAnimation.cancel();
        startAnimation(mShowAnimation);
    }
}
