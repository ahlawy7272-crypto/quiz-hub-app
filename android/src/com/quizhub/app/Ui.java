package com.quizhub.app;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

/** Small view helpers: dp conversion, rounded backgrounds, Arabic-Indic digits. */
public final class Ui {
    public static int dp(Context c, float v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics());
    }
    public static GradientDrawable round(int color, float radiusPx) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radiusPx);
        return g;
    }
    public static GradientDrawable roundStroke(int color, float radiusPx, int strokeColor, int strokePx) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radiusPx);
        g.setStroke(strokePx, strokeColor);
        return g;
    }
    public static TextView tv(Context c, String text, float sp, android.graphics.Typeface tf, int color) {
        TextView t = new TextView(c);
        t.setText(text);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTypeface(tf);
        t.setTextColor(color);
        return t;
    }

    // Arabic-Indic digits, matching the web app.
    private static final char[] AR = {'٠','١','٢','٣','٤','٥','٦','٧','٨','٩'};
    public static String toAr(String s) {
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            b.append(ch >= '0' && ch <= '9' ? AR[ch - '0'] : ch);
        }
        return b.toString();
    }
    public static String formatTime(int total) {
        int m = total / 60, s = total % 60;
        return toAr(m + ":" + (s < 10 ? "0" + s : s));
    }

    // A time value renders in its own LTR TextView wrapped in LRE..PDF:
    // the platform's bidi flips an Arabic-Indic digit run that starts with ٠
    // inside RTL text, so "0:45" would show as "45:0".
    public static String lre(String s) { return "\u202A" + s + "\u202C"; }
    public static TextView timeView(Context c, int seconds, float sp, android.graphics.Typeface tf, int color) {
        TextView t = tv(c, lre(formatTime(seconds)), sp, tf, color);
        t.setTextDirection(View.TEXT_DIRECTION_LTR);
        t.setFontFeatureSettings("tnum"); // tabular numbers so the timer never wobbles
        return t;
    }
}
