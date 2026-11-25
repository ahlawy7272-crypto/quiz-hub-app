package com.quizhub.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/** Round 5: tiny animated equalizer shown while explanation audio is playing
 *  (next to the audio chip, and inside the big explanation-audio button).
 *  When the animation is not running it draws static mid-height bars. */
public final class WaveView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ValueAnimator anim;
    private float phase = 0f;
    private boolean running = false;

    public WaveView(Context c, int color) {
        super(c);
        paint.setColor(color);
    }
    public void start() {
        if (running) return;
        running = true;
        anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(700);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.addUpdateListener(a -> { phase = (Float) a.getAnimatedValue(); invalidate(); });
        anim.start();
    }
    public void stop() {
        running = false;
        if (anim != null) { anim.cancel(); anim = null; }
        phase = 0f;
        invalidate();
    }
    @Override protected void onMeasure(int w, int h) {
        setMeasuredDimension(Ui.dp(getContext(), 36), Ui.dp(getContext(), 28));
    }
    @Override protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight();
        float bw = Ui.dp(getContext(), 3);
        float gap = Ui.dp(getContext(), 3);
        for (int i = 0; i < 4; i++) {
            float f = 0.35f + 0.65f * Math.abs((float) Math.sin((phase + i * 0.25f) * Math.PI * 2));
            float bh = h * f;
            float x = i * (bw + gap);
            float top = (h - bh) / 2f;
            c.drawRoundRect(new RectF(x, top, x + bw, top + bh), bw / 2, bw / 2, paint);
        }
    }
}
