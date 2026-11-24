package com.quizhub.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/** Curved-notch bottom tab bar background: white/card rounded rect with a circular notch
 *  cut out at the active tab position (gen18 .qz-tabbar-bg mask). Drawn under the tabs.
 *  Uses an even-odd path instead of a CLEAR xfermode layer, so it renders the same on
 *  hardware canvases and scaled test canvases. */
public final class TabBarBgView extends View {
    private final Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private float notchX = 0.5f; // 0..1 across width
    private final Theme t;

    public TabBarBgView(Context c, Theme theme) {
        super(c);
        this.t = theme;
        applyTheme();
    }
    public void applyTheme() {
        bg.setColor(t.dark ? Theme.D_CARD : 0xffffffff);
        invalidate();
    }
    public void setNotch(float x) { notchX = x; invalidate(); }

    @Override protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight();
        float r = Ui.dp(getContext(), 22);
        path.reset();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.addRoundRect(new RectF(0, 0, w, h), r, r, Path.Direction.CW);
        path.addCircle(notchX * w, Ui.dp(getContext(), 14), Ui.dp(getContext(), 28), Path.Direction.CW); // concentric with the raised icon (centre 14dp below bar top, ring radius 24dp)
        c.drawPath(path, bg);
    }
}
