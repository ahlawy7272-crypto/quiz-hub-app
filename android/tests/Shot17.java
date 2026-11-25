package com.quizhub.app;
import android.graphics.Bitmap; import android.graphics.Canvas; import android.view.View; import android.view.ViewGroup; import android.widget.*;
import org.junit.Test; import org.junit.runner.RunWith;
import org.robolectric.Robolectric; import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config; import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadows.ShadowLooper; import org.robolectric.shadows.ShadowDialog;
import java.io.FileOutputStream;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33, qualifiers = "w360dp-h800dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class Shot17 {
    static void png(View v, int w, int h, String name) throws Exception {
        v.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY)); v.layout(0, 0, w, h);
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); v.draw(new Canvas(b));
        try (FileOutputStream o = new FileOutputStream("/downloads/" + name + ".png")) { b.compress(Bitmap.CompressFormat.PNG, 100, o); }
    }
    static View find(View v, String txt) {
        if (v instanceof TextView && String.valueOf(((TextView) v).getText()).startsWith(txt)) return v;
        if (v instanceof ViewGroup) { ViewGroup g = (ViewGroup) v; for (int i = 0; i < g.getChildCount(); i++) { View r = find(g.getChildAt(i), txt); if (r != null) return r; } }
        return null;
    }
    @Test public void exportDialog() throws Exception {
        org.robolectric.android.controller.ActivityController<MainActivity> c0 = Robolectric.buildActivity(MainActivity.class).setup();
        MainActivity a0 = c0.get();
        String raw = "  {\"ids\" : [3121,5],\n \"picked\":\"1.\",  \"favorites\":[3000],\"bankKey\":\"99\" }\n";
        android.content.SharedPreferences p = a0.getSharedPreferences("quizhub_native_v1", android.content.Context.MODE_PRIVATE);
        c0.pause().stop().destroy(); p.edit().putString("state", raw).commit();
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        ShadowLooper.idleMainLooper(500, java.util.concurrent.TimeUnit.MILLISECONDS);
        assertEquals(raw, Session.legacyRaw(a));
        a.s.tab = "settings"; a.renderAll();
        ShadowLooper.idleMainLooper(500, java.util.concurrent.TimeUnit.MILLISECONDS);
        View btn = find(a.root, "تصدير البيانات القديمة"); assertNotNull(btn);
        a.root.measure(View.MeasureSpec.makeMeasureSpec(720, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(1600, View.MeasureSpec.EXACTLY)); a.root.layout(0, 0, 720, 1600);
        int top = 0; View cv = btn; while (cv != a.root) { top += cv.getTop(); View pr = (View) cv.getParent(); if (pr instanceof ScrollView) top -= pr.getScrollY(); cv = pr; }
        a.scroll.scrollTo(0, Math.max(0, top + a.scroll.getScrollY() - 700)); png(a.root, 720, 1600, "android-16-export-button");
        btn.performClick(); ShadowLooper.idleMainLooper(300, java.util.concurrent.TimeUnit.MILLISECONDS);
        android.app.AlertDialog d = (android.app.AlertDialog) ShadowDialog.getLatestDialog(); assertNotNull(d);
        png(d.getWindow().getDecorView(), 720, 900, "android-17-export-dialog");
        d.getButton(android.app.AlertDialog.BUTTON_POSITIVE).performClick(); ShadowLooper.idleMainLooper(300, java.util.concurrent.TimeUnit.MILLISECONDS);
        android.content.ClipboardManager cm = (android.content.ClipboardManager) a.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        assertEquals("clipboard equals exact raw", raw, cm.getPrimaryClip().getItemAt(0).getText().toString());
        System.out.println("COPY EXACT RAW OK; toast=" + org.robolectric.shadows.ShadowToast.getTextOfLatestToast());
    }
}
