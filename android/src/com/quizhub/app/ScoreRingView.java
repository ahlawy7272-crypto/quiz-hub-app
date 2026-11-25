package com.quizhub.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/** Round 5: gold score ring for the result screen — fills to the achieved
 *  percentage, replacing the plain percentage text as the hero element. */
public final class ScoreRingView extends View {
    private final Paint rail = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arc = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float pct = 0f; // 0..1
    private float shown = 0f; // animated fill (0..pct)
    private String label = "٠٪";

    public ScoreRingView(Context c, Theme t) {
        super(c);
        applyTheme(t);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTypeface(t.dispB());
    }
    public void applyTheme(Theme t) {
        rail.setColor(t.rail());
        rail.setStyle(Paint.Style.STROKE);
        rail.setStrokeWidth(Ui.dp(getContext(), 10));
        rail.setStrokeCap(Paint.Cap.ROUND);
        arc.setColor(t.dark ? Theme.D_GOLD : Theme.L_GOLD);
        arc.setStyle(Paint.Style.STROKE);
        arc.setStrokeWidth(Ui.dp(getContext(), 10));
        arc.setStrokeCap(Paint.Cap.ROUND);
        text.setColor(t.ink());
        invalidate();
    }
    public void set(int percent, Theme t) {
        pct = Math.max(0f, Math.min(1f, percent / 100f));
        shown = pct;
        label = Ui.toAr(String.valueOf(percent)) + "٪";
        text.setTextSize(Ui.dp(getContext(), 26));
        invalidate();
    }
    /** Suggestion 32: the ring fills gradually up to the score (~1 s, gentle
     *  ease-out). When animate is false (fx off / calm) it draws the final
     *  value immediately, like set(). */
    public void animateTo(int percent, Theme t, boolean animate) {
        pct = Math.max(0f, Math.min(1f, percent / 100f));
        label = Ui.toAr(String.valueOf(percent)) + "٪";
        text.setTextSize(Ui.dp(getContext(), 26));
        if (!animate) { shown = pct; invalidate(); return; }
        shown = 0f;
        android.animation.ValueAnimator an = android.animation.ValueAnimator.ofFloat(0f, pct);
        an.setDuration(1000);
        an.setInterpolator(new android.view.animation.DecelerateInterpolator());
        an.addUpdateListener(v -> { shown = (Float) v.getAnimatedValue(); invalidate(); });
        an.start();
    }
    @Override protected void onMeasure(int w, int h) {
        int s = Ui.dp(getContext(), 150);
        setMeasuredDimension(resolveSize(s, w), resolveSize(s, h));
    }
    @Override protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight();
        float pad = Ui.dp(getContext(), 8);
        RectF r = new RectF(pad, pad, w - pad, h - pad);
        c.drawArc(r, 0, 360, false, rail);
        c.drawArc(r, -90, 360 * shown, false, arc);
        Paint.FontMetrics fm = text.getFontMetrics();
        c.drawText(label, w / 2f, h / 2f - (fm.ascent + fm.descent) / 2f, text);
    }
}
