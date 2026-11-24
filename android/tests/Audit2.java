package com.quizhub.app;

import android.app.Dialog;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadows.ShadowDialog;
import java.util.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33, qualifiers = "w360dp-h720dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class Audit2 {
    static { ClipPlayer.existsOverride = true; ClipPlayer.fakePlay = true; }
    static MainActivity mk(boolean dark, int font, boolean spacious) {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        a.s.dark = dark; a.s.fontPercent = font; a.s.spacious = spacious; a.s.sounds = false; a.s.fx = false; a.renderAll();
        return a;
    }
    static void bottom(MainActivity a, String tag) {
        Audit1.lay(a);
        a.scroll.scrollTo(0, 100000);
        Audit1.lay(a);
        int vp = a.scroll.getHeight(), ch = a.content.getHeight(), sy = a.scroll.getScrollY();
        System.out.println("SCROLL " + tag + " viewport=" + vp + " content=" + ch + " scrollY=" + sy + " bottomGap=" + (ch - sy - vp) + " navTop=" + a.tabbarWrap.getTop() + " scrollBottom=" + (a.scroll.getTop()+vp));
    }
    @Test public void bigFontScroll() throws Exception {
        MainActivity a = mk(false, 150, true);
        a.s.setupSection = "6"; a.s.setupMode = "study"; a.begin(); a.s.tab = "quiz"; a.renderAll();
        bottom(a, "c-fresh"); Audit1.screen(a, "d1-big-fresh-bottom");
        Bank.Question q = a.qs.get(a.s.index);
        a.pick((q.c + 1) % q.o.length); bottom(a, "c-wrong"); Audit1.screen(a, "d1-big-wrong-bottom");
        a.hintOpen = true; a.trOpen = true; a.s.index = 2; a.renderAll(); bottom(a, "c-hint-tr"); Audit1.screen(a, "d1-big-hinttr-bottom");
        // exam last-question finish button
        MainActivity e = mk(true, 150, true);
        e.s.setupSection = "6"; e.s.setupMode = "exam"; e.begin(); e.s.tab = "quiz"; e.s.index = e.qs.size() - 1; e.renderAll();
        bottom(e, "dark-exam-last"); Audit1.screen(e, "d1-big-dark-examlast-bottom");
    }
    @Test public void settingsBottom() throws Exception {
        for (int k = 0; k < 2; k++) {
            MainActivity a = mk(k == 1, k == 1 ? 150 : 100, k == 1);
            a.s.tab = "settings"; a.renderAll();
            bottom(a, "settings" + k); Audit1.screen(a, "d2-settings" + k + "-bottom");
            // find backup buttons and open dialogs
            Button mkb = find(a.content, "جهّز النسخة");
            System.out.println("BACKUP-BTN " + k + " " + (mkb != null));
            if (mkb != null) { mkb.performClick(); org.robolectric.shadows.ShadowLooper.idleMainLooper(); Audit1.dialog("d2-backup-make" + k); }
            Button rb = find(a.content, "استرجاع");
            if (rb == null) rb = findContains(a.content, "استرجاع");
            System.out.println("RESTORE-BTN " + k + " " + (rb != null));
            if (rb != null) { rb.performClick(); org.robolectric.shadows.ShadowLooper.idleMainLooper(); Audit1.dialog("d2-backup-restore" + k); }
        }
    }
    static Button find(View v, String text) {
        if (v instanceof Button && text.equals(String.valueOf(((Button) v).getText()))) return (Button) v;
        if (v instanceof android.view.ViewGroup) for (int i = 0; i < ((android.view.ViewGroup) v).getChildCount(); i++) { Button r = find(((android.view.ViewGroup) v).getChildAt(i), text); if (r != null) return r; }
        return null;
    }
    static Button findContains(View v, String text) {
        if (v instanceof Button && String.valueOf(((Button) v).getText()).contains(text)) return (Button) v;
        if (v instanceof android.view.ViewGroup) for (int i = 0; i < ((android.view.ViewGroup) v).getChildCount(); i++) { Button r = findContains(((android.view.ViewGroup) v).getChildAt(i), text); if (r != null) return r; }
        return null;
    }
    @Test public void learnStates() throws Exception {
        for (int k = 0; k < 2; k++) {
            boolean dark = k == 1;
            MainActivity a = mk(dark, 100, false);
            String p = dark ? "e-dark" : "e-light";
            a.s.tab = "home"; a.renderAll();
            System.out.println("EMPTY wrong=" + a.s.wrong.size() + " fav=" + a.s.favorites.size());
            bottom(a, p + "-empty-home"); Audit1.screen(a, p + "-1-empty-bottom");
            // exam with mixed answers then finish
            a.s.setupSection = "6"; a.s.setupMode = "exam"; a.begin(); a.s.tab = "quiz";
            for (int i = 0; i < 12; i++) { a.s.index = i; a.pick(i % 3 == 0 ? a.qs.get(i).c : (a.qs.get(i).c + 1) % a.qs.get(i).o.length); }
            a.s.index = 4; a.s.favorites.add(a.qs.get(4).gid); a.s.favorites.add(a.qs.get(9).gid); a.save();
            a.finishQuiz(); a.doFinishConfirmed();
            a.s.tab = "home"; a.renderAll();
            System.out.println("POP wrong=" + a.s.wrong.size() + " fav=" + a.s.favorites.size() + " hist=" + a.s.history.length());
            bottom(a, p + "-pop-home"); Audit1.screen(a, p + "-2-populated-bottom");
            // notebook run
            a.s.tab = "home"; a.s.started = false; a.renderAll();
            int wb = a.s.wrong.size();
            int[] ids = new int[wb]; int i = 0; for (int g : a.s.wrong) ids[i++] = g;
            a.startRun("notebook", ids); a.s.tab = "quiz"; a.renderAll();
            Audit1.screen(a, p + "-3-notebook-run");
            Bank.Question q = a.qs.get(a.s.index);
            a.pick(q.c);
            System.out.println("NOTEBOOK wrong before=" + wb + " after correct=" + a.s.wrong.size() + " run=" + a.s.run + " n=" + a.qs.size());
            // flag toggle in quiz
            int fb = a.s.favorites.size();
            Button fl = null; // flag via the same code path as the button
            Bank.Question q2 = a.qs.get(a.s.index);
            if (!a.s.favorites.remove(q2.gid)) a.s.favorites.add(q2.gid); a.save(); a.renderAll();
            Audit1.screen(a, p + "-4-flag-toggled");
            System.out.println("FLAG before=" + fb + " after=" + a.s.favorites.size());
            // flagged run
            a.s.started = false; a.s.tab = "home"; a.renderAll();
            int[] fids = new int[a.s.favorites.size()]; i = 0; for (int g : a.s.favorites) fids[i++] = g;
            a.startRun("flagged", fids); a.s.tab = "quiz"; a.renderAll();
            Audit1.screen(a, p + "-5-flagged-run");
            System.out.println("FLAGGED run=" + a.s.run + " n=" + a.qs.size());
        }
    }
    @Test public void notebookStudy() throws Exception {
        MainActivity a = mk(false, 100, false);
        a.s.wrong.clear(); a.s.setupSection = "6"; a.s.setupMode = "study"; a.begin(); a.s.tab = "quiz";
        Bank.Question q = a.qs.get(0); a.s.index = 0;
        a.pick((q.c + 1) % q.o.length);
        System.out.println("NB1 wrong after wrong pick (expect true) " + a.s.wrong.contains(q.gid));
        a.s.started = false; a.s.tab = "home"; a.renderAll();
        a.startRun("notebook", new int[]{q.gid}); a.s.tab = "quiz"; a.renderAll();
        Bank.Question q2 = a.qs.get(0); a.pick(q2.c);
        System.out.println("NB2 wrong after correct in notebook run (expect false) " + a.s.wrong.contains(q.gid));
        MainActivity b = Robolectric.buildActivity(MainActivity.class).setup().get();
        System.out.println("NB3 after restart wrong contains (expect false) " + b.s.wrong.contains(q.gid));
        a.s.started = false; a.s.setupMode = "exam"; a.s.run = "all"; a.s.setupSection = "6"; a.begin(); a.s.index = 0; a.pick((q.c + 1) % q.o.length);
        System.out.println("NB4 exam pick doesn't touch notebook yet (expect false) " + a.s.wrong.contains(q.gid));
    }
    static View findText(View v, String text) {
        if (v instanceof android.widget.TextView && String.valueOf(((android.widget.TextView) v).getText()).startsWith(text)) return v;
        if (v instanceof android.view.ViewGroup) for (int i = 0; i < ((android.view.ViewGroup) v).getChildCount(); i++) { View r = findText(((android.view.ViewGroup) v).getChildAt(i), text); if (r != null) return r; }
        return null;
    }
    @Test public void learnPanels() throws Exception {
        for (int k = 0; k < 2; k++) {
            MainActivity a = mk(k == 1, 100, false);
            String p = k == 1 ? "g-dark" : "g-light";
            for (int st = 0; st < 2; st++) {
                a.s.wrong.clear(); a.s.favorites.clear(); a.s.history = new org.json.JSONArray();
                if (st == 1) { a.s.wrong.addAll(Arrays.asList(3, 8, 15, 40, 300, 700)); a.s.favorites.addAll(Arrays.asList(5, 321)); }
                a.s.started = false; a.s.tab = "home"; a.renderAll(); Audit1.lay(a);
                View t = findText(a.content, "دفتر الغلطات");
                int top = 0; View x = t; while (x != null && x != a.content) { top += x.getTop(); x = (View) x.getParent(); }
                a.scroll.scrollTo(0, Math.max(0, top - 20)); Audit1.lay(a);
                System.out.println("PANEL " + p + " st=" + st + " top=" + top);
                Audit1.screen(a, p + "-" + (st == 0 ? "empty" : "populated"));
            }
        }
    }
    @Test public void audioStates() throws Exception {
        for (int k = 0; k < 2; k++) {
            MainActivity a = mk(k == 1, k == 1 ? 150 : 100, false);
            a.s.setupSection = "6"; a.s.setupMode = "study"; a.begin(); a.s.tab = "quiz"; a.renderAll();
            Bank.Question q = a.qs.get(a.s.index); a.pick(q.c);
            Screens.showExplain(a); org.robolectric.shadows.ShadowLooper.idleMainLooper();
            Dialog d = ShadowDialog.getLatestDialog(); View root = d.getWindow().getDecorView();
            Button r = findContains(root, "تسجيل الدكتور"); Button g = findContains(root, "مولّد");
            System.out.println("AUDIO rows " + (r != null) + " " + (g != null));
            r.performClick(); System.out.println("A1 r='" + r.getText() + "' g='" + g.getText() + "' playing r=" + ClipPlayer.isPlaying("r" + q.gid + ".mp3"));
            Audit1.save(root, 1000, 0, "f1-audio-r-playing" + k);
            g.performClick(); System.out.println("A2 r='" + r.getText() + "' g='" + g.getText() + "' playing g=" + ClipPlayer.isPlaying("g" + q.gid + ".mp3"));
            Audit1.save(root, 1000, 0, "f1-audio-g-playing" + k);
            g.performClick(); System.out.println("A3 r='" + r.getText() + "' g='" + g.getText());
            r.performClick(); d.dismiss(); org.robolectric.shadows.ShadowLooper.idleMainLooper(); System.out.println("A4 after dismiss playing=" + ClipPlayer.isPlaying("r" + q.gid + ".mp3"));
        }
    }
}
