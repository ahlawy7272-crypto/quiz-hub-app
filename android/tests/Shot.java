package com.quizhub.app;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import java.io.FileOutputStream;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33, qualifiers = "w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class Shot {
    static void shot(View root, String name) throws Exception {
        int w = 1170, h = 2532;
        root.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
                     View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.AT_MOST));
        int rh = Math.min(h, Math.max(root.getMeasuredHeight(), 400));
        root.layout(0, 0, w, rh);
        Bitmap bmp = Bitmap.createBitmap(w, rh, Bitmap.Config.ARGB_8888);
        root.draw(new Canvas(bmp));
        try (FileOutputStream o = new FileOutputStream("/tmp/rb/shots/" + name + ".png")) {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, o);
        }
        System.out.println("SHOT " + name + " " + w + "x" + rh);
    }
    static Button findBtn(View v, String prefix) {
        if (v instanceof Button && ((Button) v).getText().toString().startsWith(prefix)) return (Button) v;
        if (v instanceof ViewGroup) for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++) {
            Button b = findBtn(((ViewGroup) v).getChildAt(i), prefix); if (b != null) return b; }
        return null;
    }
    @Test public void run() throws Exception {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        org.robolectric.shadows.ShadowLooper.idleMainLooper();
        shot(a.getWindow().getDecorView(), "01-home");
        a.s.setupSection = "0"; a.begin();
        org.robolectric.shadows.ShadowLooper.idleMainLooper();
        shot(a.getWindow().getDecorView(), "02-quiz");
        // pick the correct answer of Q1 (gid 0 has a generated clip)
        a.pick(a.qs.get(a.s.index).c);
        org.robolectric.shadows.ShadowLooper.idleMainLooper();
        Screens.showExplain(a);
        org.robolectric.shadows.ShadowLooper.idleMainLooper();
        android.app.Dialog d = org.robolectric.shadows.ShadowDialog.getLatestDialog();
        System.out.println("GID " + a.qs.get(a.s.index).gid + " dialog=" + (d != null));
        try { String[] l=a.getAssets().list("audio"); System.out.println("LIST " + (l==null?-1:l.length));
          a.getAssets().openFd("audio/g0.mp3").close(); System.out.println("OPENFD ok"); } catch (Exception e) { System.out.println("OPENFD " + e); }
        shot(d.getWindow().getDecorView(), "03-explain-generated");
        Button g = findBtn(d.getWindow().getDecorView(), "\u25B6");
        System.out.println("AUDIO_BUTTON " + (g == null ? "NONE" : g.getText()));
        if (g != null) { g.performClick(); org.robolectric.shadows.ShadowLooper.idleMainLooper();
            System.out.println("AFTER_CLICK " + g.getText() + " playing=" + ClipPlayer.isPlaying("g0.mp3"));
            shot(d.getWindow().getDecorView(), "04-explain-playing");
            g.performClick(); System.out.println("AFTER_2ND " + g.getText()); }
        d.dismiss();
        // a lecturer-clip question: gid 598 is in a later bank
        for (Bank.BankMeta m : a.bank.banks) for (int id : m.ids) if (id == 598) { a.s.setupSection = m.id; }
        a.begin();
        int idx = -1; for (int i = 0; i < a.s.ids.length; i++) if (a.s.ids[i] == 598) idx = i;
        a.s.index = idx; a.trReset(); a.renderAll();
        a.pick(a.qs.get(idx).c);
        org.robolectric.shadows.ShadowLooper.idleMainLooper();
        Screens.showExplain(a);
        org.robolectric.shadows.ShadowLooper.idleMainLooper();
        android.app.Dialog d2 = org.robolectric.shadows.ShadowDialog.getLatestDialog();
        System.out.println("LECT_BUTTON " + (findBtn(d2.getWindow().getDecorView(), "\u25B6") == null ? "NONE" : findBtn(d2.getWindow().getDecorView(), "\u25B6").getText()));
        shot(d2.getWindow().getDecorView(), "05-explain-lecturer");
    }
}
