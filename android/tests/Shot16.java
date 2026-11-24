package com.quizhub.app;
import android.graphics.Bitmap; import android.graphics.Canvas; import android.view.View; import android.view.ViewGroup; import android.widget.*;
import org.junit.Test; import org.junit.runner.RunWith;
import org.robolectric.Robolectric; import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config; import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadows.ShadowLooper;
import java.io.FileOutputStream;
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33, qualifiers = "w360dp-h800dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class Shot16 {
    static void lay(MainActivity a, int w, int h) {
        a.root.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY));
        a.root.layout(0, 0, w, h);
    }
    static void shot(MainActivity a, String name) throws Exception {
        int w = 720, h = 1600; lay(a, w, h);
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        a.root.draw(new Canvas(bmp));
        try (FileOutputStream o = new FileOutputStream("/tmp/rb/shots/" + name + ".png")) { bmp.compress(Bitmap.CompressFormat.PNG, 100, o); }
    }
    static View find(View v, String txt) {
        if (v instanceof TextView && txt.equals(String.valueOf(((TextView) v).getText()))) return v;
        if (v instanceof ViewGroup) { ViewGroup g = (ViewGroup) v; for (int i = 0; i < g.getChildCount(); i++) { View r = find(g.getChildAt(i), txt); if (r != null) return r; } }
        return null;
    }
    static int[] pos(View v, View root) { // top/bottom relative to root, accounting for scroll
        int top = 0; View c = v; while (c != root) { top += c.getTop(); View p = (View) c.getParent(); if (p instanceof ScrollView) top -= p.getScrollY(); c = p; }
        return new int[]{top, top + v.getHeight()};
    }
    MainActivity start(int font) {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        a.s.fontPercent = font; a.theme.scale = font / 100f; a.s.calm = true; a.s.fx = false; a.s.sounds = false; a.s.confirm = false;
        a.s.setupSection = "6"; a.s.setupMode = "study"; a.begin(); a.s.tab = "quiz"; a.s.index = 11; a.renderAll();
        ShadowLooper.idleMainLooper(1000, java.util.concurrent.TimeUnit.MILLISECONDS);
        Bank.Question q = a.qs.get(11); a.commit((q.c + 2) % q.o.length); ShadowLooper.idleMainLooper(500, java.util.concurrent.TimeUnit.MILLISECONDS);
        return a;
    }
    @Test public void finishReach() throws Exception {
        for (int font : new int[]{100, 125}) {
            MainActivity a = start(font); lay(a, 720, 1600);
            View fin = find(a.root, "إنهاء وعرض النتيجة"); int[] p = pos(fin, a.root);
            int viewportBottom = a.scroll.getBottom() + ((View) a.scroll.getParent()).getTop();
            System.out.println("FIN font=" + font + " button y=" + p[0] + ".." + p[1] + " scrollViewportBottom=" + viewportBottom + " visibleWithoutScroll=" + (p[1] <= viewportBottom) + " scrollRange=" + a.content.getHeight() + "/" + a.scroll.getHeight());
            shot(a, "k1-font" + font + "-top");
            a.scroll.scrollTo(0, 100000); lay(a, 720, 1600); p = pos(fin, a.root);
            System.out.println("FIN font=" + font + " after scroll y=" + p[0] + ".." + p[1] + " reachable=" + (p[1] <= viewportBottom && p[0] >= 0));
            shot(a, "k1-font" + font + "-bottom");
        }
    }
    @Test public void timer() throws Exception {
        int[] vals = {5, 754, 3725, 7200};
        for (int v : vals) {
            MainActivity a = start(100); a.s.elapsed = v; a.s.countdown = false; a.s.timer = true; a.renderAll(); lay(a, 720, 1600);
            android.graphics.Paint pt = a.timerChipTime.getPaint();
            float tw = pt.measureText(String.valueOf(a.timerChipTime.getText()));
            System.out.println("TIMER elapsed=" + v + " text=" + a.timerChipTime.getText().toString().replace("\u202A","").replace("\u202C","") + " textW=" + tw + " viewW=" + a.timerChipTime.getWidth() + " chipW=" + a.timerChip.getWidth() + " chipX=" + a.timerChip.getLeft() + " clipped=" + (a.timerChipTime.getWidth() < tw - 1));
            if (v == 754 || v == 7200) shot(a, "k2-timer-" + v);
        }
        MainActivity a = start(100); a.s.countdown = true; a.s.timer = true; a.renderAll(); lay(a, 720, 1600);
        System.out.println("TIMER countdown text=" + a.timerChipTime.getText().toString().replace("\u202A","").replace("\u202C","") + " chipW=" + a.timerChip.getWidth()); shot(a, "k2-timer-countdown");
    }
    @Test public void realAssets() throws Exception {
        ClipPlayer.existsOverride = false; ClipPlayer.fakePlay = false;
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        int total = 0, ok = 0, fdok = 0; java.util.List<String> bad = new java.util.ArrayList<>();
        org.json.JSONObject idx = new org.json.JSONObject(new String(a.getAssets().open("bank/index.json").readAllBytes(), "UTF-8"));
        org.json.JSONArray banks = idx.getJSONArray("banks");
        for (int b = 0; b < banks.length(); b++) { org.json.JSONArray ids = banks.getJSONObject(b).getJSONArray("ids");
            for (int i = 0; i < ids.length(); i++) { int g = ids.getInt(i); total++;
                boolean has = ClipPlayer.exists(a, "r" + g + ".mp3") || ClipPlayer.exists(a, "g" + g + ".mp3");
                if (has) ok++; else bad.add(String.valueOf(g)); } }
        System.out.println("REAL exists(no override): " + ok + "/" + total + " missing=" + bad.size());
        String n = "r609.mp3"; String r = "audio/" + n;
        try { android.content.res.AssetFileDescriptor f = a.getAssets().openFd(r); System.out.println("REAL openFd(r609) OK len=" + f.getLength() + " off=" + f.getStartOffset()); f.close(); fdok = 1; } catch (Exception e) { System.out.println("REAL openFd(r609) FAILED " + e); }
        byte[] bytes = a.getAssets().open(r).readAllBytes(); System.out.println("REAL open stream r609 bytes=" + bytes.length);
        boolean on = ClipPlayer.toggle(a, n, null); System.out.println("REAL toggle(r609) returned " + on + " lastError=" + ClipPlayer.lastError + " fdFallbacks=" + ClipPlayer.fdFallbacks);
        ClipPlayer.stop();
    }
}
