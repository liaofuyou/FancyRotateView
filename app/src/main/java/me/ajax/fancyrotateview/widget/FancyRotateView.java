package me.ajax.fancyrotateview.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aj on 2018/4/2
 */

public class FancyRotateView extends View {

    int CIRCLE_COUNT = 12;
    Paint mPaint = new Paint();
    List<ValueAnimator> animators = new ArrayList<>(CIRCLE_COUNT);
    List<RectF> circleRectFs = new ArrayList<>(CIRCLE_COUNT);


    public FancyRotateView(Context context) {
        super(context);
        init();
    }

    public FancyRotateView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FancyRotateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {

        //画笔
        mPaint.setColor(0xFFFF00FF);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(dp2Dx(12));
        mPaint.setStyle(Paint.Style.STROKE);

        initAnimator();

        for (int i = 0; i < CIRCLE_COUNT; i++) {
            float r = dp2Dx(160) * (1 - i / (float) CIRCLE_COUNT);
            circleRectFs.add(new RectF(-r, -r, r, r));
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < CIRCLE_COUNT; i++) {
                    Animator a = animators.get(i);
                    if (a != null) {
                        a.cancel();
                        a.setStartDelay(i * 80);
                        a.start();
                    }
                }
            }
        });

        post(new Runnable() {
            @Override
            public void run() {
                performClick();
            }
        });
    }

    public void initAnimator() {

        for (int i = 0; i < CIRCLE_COUNT; i++) {
            ValueAnimator animator = ValueAnimator.ofFloat(0F, 360F, 360F, 0F);
            animator.setDuration(6000);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    invalidateView();
                }
            });
            animators.add(animator);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int mWidth = getWidth();
        int mHeight = getHeight();

        canvas.save();
        canvas.translate(mWidth / 2, mHeight / 2);
        canvas.rotate(-90, 0, 0);

        for (int i = 0; i < CIRCLE_COUNT; i++) {
            drawColorfulArc(canvas, (float) animators.get(i).getAnimatedValue(), circleRectFs.get(i));
        }

        canvas.restore();
    }

    void drawColorfulArc(Canvas canvas, float animatedValue, RectF rectF) {

        if (animatedValue == 0) return;

        //圆弧 1
        float startAngle = animatedValue;
        float sweepAngle = getSweepAngle(startAngle);
        mPaint.setColor(0xFFFEFB76);
        canvas.drawArc(rectF, startAngle, sweepAngle - (sweepAngle / 5), false, mPaint);

        //圆弧 2
        startAngle = startAngle - Math.abs(sweepAngle);
        sweepAngle = getSweepAngle(startAngle);
        mPaint.setColor(0xFF8BEAFC);
        canvas.drawArc(rectF, startAngle, sweepAngle - (sweepAngle / 5), false, mPaint);

        //圆弧 3
        startAngle = startAngle - Math.abs(sweepAngle);
        sweepAngle = getSweepAngle(startAngle);
        mPaint.setColor(Color.WHITE);
        canvas.drawArc(rectF, startAngle, sweepAngle - (sweepAngle / 5), false, mPaint);
    }


    float getSweepAngle(float startAngle) {
        float sweepAngle;
        if (startAngle < 180) {
            sweepAngle = -90 * startAngle / 180F;
        } else if (startAngle >= 180 && startAngle <= 360) {
            sweepAngle = -90 * (1 - (startAngle - 180F) / 180F);
        } else {
            sweepAngle = 0;
        }
        return sweepAngle;
    }

    int dp2Dx(int dp) {
        return (int) (getResources().getDisplayMetrics().density * dp);
    }

    void l(Object o) {
        Log.e("######", o.toString());
    }


    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            //  当前线程是主UI线程，直接刷新。
            invalidate();
        } else {
            //  当前线程是非UI线程，post刷新。
            postInvalidate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimAndRemoveCallbacks();
    }

    private void stopAnimAndRemoveCallbacks() {

        for (Animator a : animators) {
            if (a != null) {
                a.end();
            }
        }

        Handler handler = this.getHandler();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
