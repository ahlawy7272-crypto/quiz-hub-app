package com.quizhub.app;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadows.ShadowDialog;
import java.io.FileOutputStream;
import java.util.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33, qualifiers = "w360dp-h720dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class Audit1 {
    static void save(View v, int w, int h, String name) throws Exception {
        v.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(h==0?12000:h, h==0?View.MeasureSpec.AT_MOST:View.MeasureSpec.EXACTLY));
        int rh = h == 0 ? Math.min(12000, v.getMeasuredHeight()) : h;
        v.layout(0, 0, w, rh);
        Bitmap bmp = Bitmap.createBitmap(w, rh, Bitmap.Config.ARGB_8888);
        v.draw(new Canvas(bmp));
        try (FileOutputStream o = new FileOutputStream("/tmp/rb/shots/" + name + ".png")) { bmp.compress(Bitmap.CompressFormat.PNG, 100, o); }
        System.out.println("SHOT " + name + " " + w + "x" + rh);
    }
    static void screen(MainActivity a, String name) throws Exception {
        // fixed device frame
        save(a.root, 1080, 2160, name);
    }
    static void full(MainActivity a, String name) throws Exception {
        View c = a.content; android.widget.FrameLayout wrap = new android.widget.FrameLayout(a);
        wrap.setBackgroundColor(a.theme.bg());
        ViewGroup p = (ViewGroup) c.getParent(); int idx = p.indexOfChild(c); ViewGroup.LayoutParams lp = c.getLayoutParams();
        p.removeView(c); wrap.addView(c);
        save(wrap, 1080, 0, name);
        wrap.removeView(c); p.addView(c, idx, lp);
    }
    static void dialog(String name) throws Exception {
        Dialog d = ShadowDialog.getLatestDialog();
        if (d == null) { System.out.println("NODIALOG " + name); return; }
        View v = d.getWindow().getDecorView(); save(v, 1000, 0, name);
        d.dismiss();
    }
    static void lay(MainActivity a){ a.root.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(2160, View.MeasureSpec.EXACTLY)); a.root.layout(0,0,1080,2160); }
    static int taps(View v, String tag, List<String> out) {
        int bad = 0;
        if (v.hasOnClickListeners() && v.getVisibility() == View.VISIBLE) {
            float d = v.getResources().getDisplayMetrics().density;
            int w = v.getWidth(), h = v.getHeight();
            if (w / d < 44 || h / d < 44) { bad++; out.add(tag + " " + v.getClass().getSimpleName() + " " + Math.round(w / d) + "x" + Math.round(h / d) + "dp" + (v instanceof android.widget.TextView ? " '" + ((android.widget.TextView) v).getText() + "'" : "")); }
        }
        if (v instanceof ViewGroup) for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++) bad += taps(((ViewGroup) v).getChildAt(i), tag, out);
        return bad;
    }
    static { ClipPlayer.existsOverride = true; }
    MainActivity mk(boolean dark, int font, boolean spacious) {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        a.s.dark = dark; a.s.fontPercent = font; a.s.spacious = spacious; a.s.sounds = false; a.s.fx = false; a.renderAll();
        return a;
    }
    void quizStates(MainActivity a, String p) throws Exception {
        a.s.setupSection = "6"; a.s.setupMode = "study"; a.begin(); a.s.tab = "quiz"; a.renderAll();
        screen(a, p + "-quiz-fresh");
        Bank.Question q = a.qs.get(a.s.index);
        a.hintOpen = true; a.trOpen = true; a.renderAll(); screen(a, p + "-quiz-hint-translation"); full(a, p + "-quiz-hint-translation-full");
        a.hintOpen = false; a.trOpen = false;
        a.pick((q.c + 1) % q.o.length); screen(a, p + "-quiz-wrong");
        a.s.index = 1; a.renderAll(); Bank.Question q2 = a.qs.get(1); a.pick(q2.c); screen(a, p + "-quiz-correct");
        Screens.showExplain(a); org.robolectric.shadows.ShadowLooper.idleMainLooper(); dialog(p + "-explain");
        Screens.showMap(a, new ArrayList<>(Arrays.asList(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39))); dialog(p + "-map");
    }
    @Test public void light() throws Exception {
        MainActivity a = mk(false, 100, false);
        screen(a, "a1-home"); full(a, "a1-home-full");
        a.s.tab = "settings"; a.renderAll(); full(a, "a1-settings-full");
        a.s.tab = "home"; a.renderAll();
        List<String> out = new ArrayList<>();
        lay(a); int b = taps(a.root, "home", out);
        quizStates(a, "a2");
        a.renderAll(); lay(a); b += taps(a.root, "quiz", out);
        System.out.println("TAPS bad=" + b); for (String s : out) System.out.println("TAP " + s);
        // exam + finish low score
        MainActivity e = mk(false, 100, false);
        e.s.setupSection = "6"; e.s.setupMode = "exam"; e.begin(); e.s.tab = "quiz";
        for (int i = 0; i < 10; i++) { e.s.index = i; e.pick(i % 2 == 0 ? e.qs.get(i).c : (e.qs.get(i).c + 1) % e.qs.get(i).o.length); }
        e.s.index = 3; e.renderAll(); screen(e, "a3-exam-mid");
        e.finishQuiz(); screen(e, "a3-exam-finish-confirm"); e.doFinishConfirmed(); screen(e, "a3-result-low"); full(e, "a3-result-low-full");
        e.openReview("wrong"); screen(e, "a3-review-wrong");
    }
    @Test public void dark() throws Exception {
        MainActivity a = mk(true, 100, false);
        screen(a, "b1-home-dark"); a.s.tab = "settings"; a.renderAll(); full(a, "b1-settings-dark-full"); a.s.tab = "home"; a.renderAll();
        quizStates(a, "b2");
    }
    @Test public void bigFont() throws Exception {
        MainActivity a = mk(false, 150, true);
        screen(a, "c1-home-150"); quizStates(a, "c2");
    }
}
