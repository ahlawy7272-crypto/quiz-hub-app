package com.quizhub.app;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

/** Light motion effects. All of them are off when fx is off or reduce-motion (calm) is on. */
public final class Fx {
    static final int[] COLORS = {0xff0b4f4a, 0xffe4f5a4, 0xff7fd08a, 0xffffffff, 0xffc9ef6b};

    public static boolean on(MainActivity a) { return a.s.fx && !a.s.calm; }

    /** Running animators (kept so a test can step them to a given time). */
    public static final java.util.List<android.animation.ValueAnimator> live = new java.util.concurrent.CopyOnWriteArrayList<>();

    static void go(final android.animation.ValueAnimator an, long ms, long delay, android.animation.TimeInterpolator ip) {
        an.setDuration(ms); an.setStartDelay(delay); an.setInterpolator(ip);
        live.add(an);
        an.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator x) { live.remove(an); }
            @Override public void onAnimationCancel(android.animation.Animator x) { live.remove(an); }
        });
        an.start();
    }

    /** Content rise: fade in and move up 8dp (web qz-rise). Round 4: 300 ms made
     *  every navigation feel delayed — 120 ms is still visible but snappy. */
    public static void rise(MainActivity a, View v) {
        if (!on(a)) return;
        v.setAlpha(0f); v.setTranslationY(Ui.dp(a, 8));
        go(ObjectAnimator.ofPropertyValuesHolder(v, android.animation.PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
            android.animation.PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, Ui.dp(a, 8), 0f)), 120, 0, new DecelerateInterpolator());
    }

    /** Small scale pop (web qz-pop / qz-press): 0.97 -> 1.012 -> 1. */
    public static void pop(MainActivity a, View v, long ms) {
        if (!on(a)) return;
        v.setScaleX(0.97f); v.setScaleY(0.97f);
        go(ObjectAnimator.ofPropertyValuesHolder(v,
            android.animation.PropertyValuesHolder.ofFloat(View.SCALE_X, 0.97f, 1.012f, 1f),
            android.animation.PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.97f, 1.012f, 1f)), ms, 0, new DecelerateInterpolator());
    }

    /** Confetti burst over the screen. big=true for the result screen.
     *  Round 4: 26 pieces x 3 animators (78 concurrent) stuttered on a real
     *  phone — now 12 pieces, fall + fade only (no spin), shorter and cheaper,
     *  still a visible little celebration. */
    public static void confetti(MainActivity a, boolean big) {
        if (!on(a)) return;
        final FrameLayout layer = new FrameLayout(a);
        layer.setClickable(false);
        final ViewGroup host = a.root;
        host.addView(layer, new FrameLayout.LayoutParams(-1, -1));
        int w = host.getWidth(), h = host.getHeight();
        if (w == 0 || h == 0) { w = a.getResources().getDisplayMetrics().widthPixels; h = a.getResources().getDisplayMetrics().heightPixels; }
        long maxEnd = 0;
        for (int i = 0; i < 12; i++) {
            int size = 6 + (i % 3) * 3;
            View p = new View(a);
            android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable();
            g.setColor(COLORS[i % 5]); g.setCornerRadius(Ui.dp(a, 2));
            p.setBackground(g);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.dp(a, size), (int) (Ui.dp(a, size) * 1.4f));
            lp.leftMargin = ((i * 37 + 13) % 100) * w / 100;
            layer.addView(p, lp);
            p.setAlpha(0f); p.setTranslationY(-Ui.dp(a, 16));
            long delay = (long) ((i % 9) * (big ? 90 : 40));
            long dur = (long) ((big ? 1400 : 1000) + (i % 5) * (big ? 200 : 150));
            ObjectAnimator fall = ObjectAnimator.ofFloat(p, View.TRANSLATION_Y, -Ui.dp(a, 16), h * 1.05f);
            ObjectAnimator fade = ObjectAnimator.ofFloat(p, View.ALPHA, 0f, 1f, 1f, 0f);
            fade.setFloatValues(0f, 1f, 1f, 0f);
            go(fall, dur, delay, new DecelerateInterpolator(0.8f));
            go(fade, dur, delay, new android.view.animation.LinearInterpolator());
            maxEnd = Math.max(maxEnd, delay + dur);
        }
        layer.postDelayed(() -> { if (layer.getParent() != null) host.removeView(layer); }, maxEnd + 200);
    }
}
