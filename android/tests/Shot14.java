package com.quizhub.app;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadows.ShadowLooper;
import java.io.FileOutputStream;
import java.time.Duration;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33, qualifiers = "w360dp-h720dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class Shot14 {
    static void shot(MainActivity a, String name) throws Exception {
        View root = a.root; int w = 1080, h = 2160;
        root.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY));
        root.layout(0, 0, w, h);
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        root.draw(new Canvas(bmp));
        try (FileOutputStream o = new FileOutputStream("/tmp/rb/shots/" + name + ".png")) { bmp.compress(Bitmap.CompressFormat.PNG, 100, o); }
        System.out.println("SHOT " + name + " rootChildren=" + a.root.getChildCount() + " contentAlpha=" + a.content.getAlpha());
    }
    static void dbg(MainActivity a,String t){ System.out.println("DBG "+t+" clock="+android.os.SystemClock.uptimeMillis()+" kids="+a.root.getChildCount()); if(a.root.getChildCount()>1){ android.view.ViewGroup g=(android.view.ViewGroup)a.root.getChildAt(a.root.getChildCount()-1); View p=g.getChildAt(5); System.out.println("DBG piece y="+p.getTranslationY()+" a="+p.getAlpha()+" n="+g.getChildCount()); } }
    static void at(long t){ for(android.animation.ValueAnimator v: Fx.live){ v.pause(); v.setCurrentPlayTime(t + v.getStartDelay() * 0 ); } }
    static void adv(long ms) { ShadowLooper.idleMainLooper(ms, java.util.concurrent.TimeUnit.MILLISECONDS); }

    MainActivity start(boolean calm, boolean confirm) {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        a.s.calm = calm; a.s.fx = true; a.s.sounds = false; a.s.confirm = confirm;
        a.s.setupSection = "6"; a.s.setupMode = "study"; a.begin(); a.s.tab = "quiz"; a.renderAll(); adv(1000);
        return a;
    }
    @Test public void fxOn() throws Exception {
        MainActivity a = start(false, true);
        shot(a, "70-idle");
        int c = a.qs.get(a.s.index).c; int wrong = (c + 1) % a.qs.get(a.s.index).o.length;
        a.pending = wrong; a.renderAll(); at(70); shot(a, "71-select-pulse-70ms");
        a.s.confirm = false; a.pending = null; a.commit(c); at(120); shot(a, "72-correct-pop-confetti-120ms");
        at(1100); shot(a, "73-confetti-1100ms");
        at(2000); shot(a, "74-confetti-2000ms");
        for(android.animation.ValueAnimator v: Fx.live) v.end(); a.s.index = 1; a.renderAll(); at(60); shot(a, "75-transition-60ms");
        at(300); shot(a, "76-transition-done");
        for (int i = 0; i < a.s.ids.length; i++) { a.s.index = i; if (a.s.picked[i] < 0) a.pick(a.qs.get(i).c); }
        adv(4000);
        for(android.animation.ValueAnimator v: Fx.live) v.end(); a.finishQuiz(); System.out.println("PRE live="+Fx.live.size()+" kids="+a.root.getChildCount()+" done="+a.confettiDone); org.robolectric.shadows.ShadowLooper.runUiThreadTasks(); System.out.println("POST live="+Fx.live.size()+" kids="+a.root.getChildCount()); System.out.println("LIVE result="+Fx.live.size()+" cel="+a.celebrate+" done="+a.confettiDone+" fx="+Fx.on(a)+" kids="+a.root.getChildCount()+" tab="+a.s.tab); android.view.ViewGroup lay=(android.view.ViewGroup)a.root.getChildAt(a.root.getChildCount()-1); System.out.println("HOOK result layer pieces="+lay.getChildCount()); for(android.animation.ValueAnimator v: Fx.live) v.end(); Fx.confetti(a,true); at(1200); shot(a, "77-result-confetti-1200ms"); at(2400); shot(a, "78-result-confetti-2400ms");
        adv(1500); shot(a, "78-result-celebrate-2000ms");
    }
    @Test public void calmOn() throws Exception {
        MainActivity a = start(true, false);
        int c = a.qs.get(a.s.index).c;
        a.commit(c); at(30); System.out.println("LIVE calm after pick="+Fx.live.size()); shot(a, "80-calm-correct-30ms");
        a.s.index = 1; a.renderAll(); adv(1); shot(a, "81-calm-transition-1ms");
        for (int i = 0; i < a.s.ids.length; i++) { a.s.index = i; if (a.s.picked[i] < 0) a.pick(a.qs.get(i).c); }
        a.finishQuiz(); adv(5); at(30); System.out.println("LIVE calm result="+Fx.live.size()); shot(a, "82-calm-result-30ms");
    }
}
