package com.quizhub.app;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

/** Light motion effects + haptics. All off when fx is off or calm (reduce motion) is on. */
public final class Fx {
    static final int[] COLORS = {0xff0b4f4a, 0xffD4A24C, 0xff20714a, 0xffffffff, 0xffb98a2f};

    public static boolean on(MainActivity a) { return a.s.fx && !a.s.calm; }

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

    /** Content rise: fade in and move up 8dp (120 ms, snappy). */
    public static void rise(MainActivity a, View v) {
        if (!on(a)) return;
        v.setAlpha(0f); v.setTranslationY(Ui.dp(a, 8));
        go(ObjectAnimator.ofPropertyValuesHolder(v, android.animation.PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
            android.animation.PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, Ui.dp(a, 8), 0f)), 120, 0, new DecelerateInterpolator());
    }

    /** Logo splash — gentle scale + fade-in on launch (~0.5 s), once. */
    public static void splash(MainActivity a, View v) {
        if (!on(a)) return;
        v.setAlpha(0f); v.setScaleX(0.85f); v.setScaleY(0.85f);
        go(ObjectAnimator.ofPropertyValuesHolder(v,
            android.animation.PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
            android.animation.PropertyValuesHolder.ofFloat(View.SCALE_X, 0.85f, 1f),
            android.animation.PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.85f, 1f)), 500, 0, new DecelerateInterpolator());
    }

    /** Gentle 18 ms haptic on select/confirm. VIBRATE is a normal permission, declared in the manifest. */
    public static void buzz(MainActivity a) {
        if (!on(a)) return;
        try {
            android.os.Vibrator vib = (android.os.Vibrator) a.getSystemService(android.content.Context.VIBRATOR_SERVICE);
            if (vib == null || !vib.hasVibrator()) return;
            if (android.os.Build.VERSION.SDK_INT >= 26)
                vib.vibrate(android.os.VibrationEffect.createOneShot(18, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            else vib.vibrate(18);
        } catch (Exception ignore) {}
    }

    /** Answer-result vibration: short buzz for correct, double-buzz for wrong. Gated on fx/calm. */
    public static void resultBuzz(MainActivity a, boolean correct) {
        if (!on(a)) return;
        try {
            android.os.Vibrator vib = (android.os.Vibrator) a.getSystemService(android.content.Context.VIBRATOR_SERVICE);
            if (vib == null || !vib.hasVibrator()) return;
            if (correct) {
                if (android.os.Build.VERSION.SDK_INT >= 26)
                    vib.vibrate(android.os.VibrationEffect.createOneShot(60, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                else vib.vibrate(60);
            } else {
                long[] pattern = {0, 60, 80, 60};
                if (android.os.Build.VERSION.SDK_INT >= 26)
                    vib.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1));
                else vib.vibrate(pattern, -1);
            }
        } catch (Exception ignore) {}
    }

    /** Small scale pop: 0.97 -> 1.012 -> 1. */
    public static void pop(MainActivity a, View v, long ms) {
        if (!on(a)) return;
        v.setScaleX(0.97f); v.setScaleY(0.97f);
        go(ObjectAnimator.ofPropertyValuesHolder(v,
            android.animation.PropertyValuesHolder.ofFloat(View.SCALE_X, 0.97f, 1.012f, 1f),
            android.animation.PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.97f, 1.012f, 1f)), ms, 0, new DecelerateInterpolator());
    }

    /** Confetti burst over the screen. 12 pieces, fall + fade only. */
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
