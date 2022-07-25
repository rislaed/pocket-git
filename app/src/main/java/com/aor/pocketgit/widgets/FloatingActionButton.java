package com.aor.pocketgit.widgets;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

public class FloatingActionButton extends View {
    Context context;
	Drawable mDrawable;
    Paint mButtonPaint;
    boolean mHidden = false;

    public FloatingActionButton(Context context2) {
        super(context2);
        this.context = context2;
        init(-1);
    }

    public void init(int color) {
        setWillNotDraw(false);
        setLayerType(1, (Paint) null);
        this.mButtonPaint = new Paint(1);
        this.mButtonPaint.setColor(color);
        this.mButtonPaint.setStyle(Paint.Style.FILL);
        this.mButtonPaint.setShadowLayer(10.0f, 0.0f, 3.5f, Color.argb(100, 0, 0, 0));
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        setClickable(true);
        canvas.drawCircle((float) (getWidth() / 2), (float) (getHeight() / 2), (float) (((double) getWidth()) / 2.6d), this.mButtonPaint);
		mDrawable.setBounds(new Rect(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom()));
		mDrawable.draw(canvas);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == 1) {
            setAlpha(1.0f);
        } else if (event.getAction() == 0) {
            setAlpha(0.6f);
        }
        return super.onTouchEvent(event);
    }

    public void setColor(int color) {
        init(color);
    }

    public void setDrawable(Drawable drawable) {
		this.mDrawable = drawable;
        invalidate();
    }

    public void hide() {
        if (!this.mHidden) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", new float[]{1.0f, 0.0f});
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", new float[]{1.0f, 0.0f});
            AnimatorSet animSetXY = new AnimatorSet();
            animSetXY.playTogether(new Animator[]{scaleX, scaleY});
            animSetXY.setInterpolator(new AccelerateInterpolator());
            animSetXY.setDuration(100);
            animSetXY.start();
            this.mHidden = true;
        }
    }

    public void show() {
        if (this.mHidden) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", new float[]{0.0f, 1.0f});
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", new float[]{0.0f, 1.0f});
            AnimatorSet animSetXY = new AnimatorSet();
            animSetXY.playTogether(new Animator[]{scaleX, scaleY});
            animSetXY.setInterpolator(new OvershootInterpolator());
            animSetXY.setDuration(200);
            animSetXY.start();
            this.mHidden = false;
        }
    }

    public boolean isHidden() {
        return this.mHidden;
    }

    public static class Builder {
        private final Activity activity;
        int color = -1;
        Drawable drawable;
        int gravity = 85;
        private FrameLayout.LayoutParams params;
        float scale = 0.0f;
        int size = 0;

        public Builder(Activity context) {
            this.scale = context.getResources().getDisplayMetrics().density;
            this.size = (int) ((72.0f * this.scale) + 0.5f);
            this.params = new FrameLayout.LayoutParams(this.size, this.size);
            this.params.gravity = this.gravity;
            this.activity = context;
        }

        public Builder withGravity(int gravity2) {
            this.gravity = gravity2;
            return this;
        }

        public Builder withMargins(int left, int top, int right, int bottom) {
            this.params.setMargins((int) ((((float) left) * this.scale) + 0.5f), (int) ((((float) top) * this.scale) + 0.5f), (int) ((((float) right) * this.scale) + 0.5f), (int) ((((float) bottom) * this.scale) + 0.5f));
            return this;
        }

        public Builder withDrawable(Drawable drawable2) {
            this.drawable = drawable2;
            return this;
        }

        public Builder withColor(int color2) {
            this.color = color2;
            return this;
        }

        public Builder withButtonSize(int size2) {
            int size3 = (int) ((((float) size2) * this.scale) + 0.5f);
            this.params = new FrameLayout.LayoutParams(size3, size3);
            return this;
        }

        public FloatingActionButton create() {
            FloatingActionButton button = new FloatingActionButton(this.activity);
            button.setColor(this.color);
            button.setDrawable(this.drawable);
            this.params.gravity = this.gravity;
			button.setPadding((int) (this.scale * 2), (int) (this.scale * 2), (int) (this.scale * 2), (int) (this.scale * 2));
            ((ViewGroup) this.activity.findViewById(16908290)).addView(button, this.params);
            return button;
        }
    }
}
