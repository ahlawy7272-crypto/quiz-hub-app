package com.quizhub.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/** One thin rail + fill + one small numbered marker travelling right to left
 *  (RTL) as the user answers. No separate "سؤال X من Y" text — the circle
 *  on the bar IS the question indicator. */
public final class ProgressView extends View {
    private final Paint rail = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint marker = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerRing = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float progress = 0f; // 0..1
    private String label = "1";

    public ProgressView(Context c, Theme t) {
        super(c);
        applyTheme(t);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTypeface(t.disp());
    }
    public void applyTheme(Theme t) {
        rail.setColor(t.rail());
        fill.setColor(t.dark ? Theme.D_SOFT : Theme.L_TEAL);
        marker.setColor(t.dark ? Theme.D_SOFT : Theme.L_TEAL);
        markerRing.setColor(t.bg());
        text.setColor(t.dark ? Theme.D_BG : Theme.L_BG);
        invalidate();
    }
    public void set(int pos, int total, Theme t) {
        progress = total > 1 ? Math.max(0, pos) / (float)(total - 1) : 1f;
        label = Ui.toAr(String.valueOf(pos + 1)); // Arabic-Indic digits, inside the marker
        text.setTextSize(Ui.dp(getContext(), 11));
        invalidate();
    }
    @Override protected void onMeasure(int w, int h) {
        setMeasuredDimension(resolveSize(Ui.dp(getContext(), 100), w), Ui.dp(getContext(), 28));
    }
    @Override protected void onDraw(Canvas c) {
        float h = getHeight(), w = getWidth();
        float railH = Ui.dp(getContext(), 4);
        float cy = h / 2f;
        float mR = Ui.dp(getContext(), 14);
        float inset = Ui.dp(getContext(), 15);
        float x0 = inset, x1 = w - inset;
        c.drawRoundRect(new RectF(x0, cy - railH/2, x1, cy + railH/2), railH/2, railH/2, rail);
        float mx = x1 - (x1 - x0) * progress; // RTL: progress advances from the right
        // Keep the whole marker circle on screen: clamp its centre inside the
        // view (the unclamped circle nicked 2dp off the edge at the first/last question).
        float mEdge = mR + Ui.dp(getContext(), 3);
        mx = Math.max(mEdge, Math.min(w - mEdge, mx));
        c.drawRoundRect(new RectF(Math.min(mx, x1 - railH), cy - railH/2, x1, cy + railH/2), railH/2, railH/2, fill);
        c.drawCircle(mx, cy, mR + Ui.dp(getContext(), 3), markerRing);
        c.drawCircle(mx, cy, mR, marker);
        Paint.FontMetrics fm = text.getFontMetrics();
        c.drawText(label, mx, cy - (fm.ascent + fm.descent)/2f, text);
    }
}
