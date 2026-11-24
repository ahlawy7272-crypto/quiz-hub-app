package com.quizhub.app;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.android.controller.ActivityController;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33, qualifiers = "w360dp-h720dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class Persist1 {
    static String snap(Session s) {
        return s.dark+"|"+s.calm+"|"+s.fx+"|"+s.sounds+"|"+s.volume+"|"+s.fontPercent+"|"+s.confirm+"|"+s.countdown+"|"+s.minutes+"|"+s.mode+"|"+s.run+"|"+s.bankKey+"|"+s.index+"|"+java.util.Arrays.toString(s.picked).hashCode()+"|"+s.favorites+"|"+s.wrong+"|"+s.history+"|"+s.submitted+"|"+s.ids.length;
    }
    @Test public void run() throws Exception {
        ActivityController<MainActivity> ctl = Robolectric.buildActivity(MainActivity.class).setup();
        MainActivity a = ctl.get();
        a.s.dark = true; a.s.calm = true; a.s.fx = false; a.s.sounds = false; a.s.volume = 35; a.s.fontPercent = 120; a.s.confirm = true; a.s.countdown = true; a.s.minutes = 90;
        a.s.setupSection = "6"; a.s.setupMode = "exam"; a.begin();
        a.s.confirm = false;
        for (int i = 0; i < 12; i++) { a.s.index = i; a.pick(i % 3 == 0 ? a.qs.get(i).c : (a.qs.get(i).c + 1) % a.qs.get(i).o.length); }
        a.s.favorites.add(5); a.s.favorites.add(321); a.s.index = 7; a.save();
        String before = snap(a.s);
        System.out.println("P before  " + before);
        // process restart: destroy activity, build a new one that loads from SharedPreferences
        ctl.pause().stop().destroy();
        MainActivity b = Robolectric.buildActivity(MainActivity.class).setup().get();
        String after = snap(b.s);
        System.out.println("P restart " + after);
        System.out.println("P RESTART_EQUAL " + before.equals(after));
        // finish the quiz so history/wrong exist, then backup
        b.doFinishConfirmed();
        String mid = snap(b.s);
        String code = Backup.make(b);
        System.out.println("P code prefix " + code.substring(0, 8) + " len=" + code.length());
        // wipe via a second activity with cleared prefs, then restore
        b.getSharedPreferences("quizhub_native_v1", 0).edit().clear().commit();
        for (String n : new String[]{"quizhub_state","qh","state"}) b.getSharedPreferences(n,0).edit().clear().commit();
        ctl = Robolectric.buildActivity(MainActivity.class).setup();
        MainActivity c = ctl.get();
        System.out.println("P fresh   " + snap(c.s));
        String msg = Backup.restore(c, code);
        System.out.println("P restore msg=" + msg);
        String restored = snap(c.s);
        System.out.println("P mid     " + mid);
        System.out.println("P restored" + restored);
        System.out.println("P RESTORE_EQUAL " + mid.equals(restored));
        // restart after restore
        ctl.pause().stop().destroy();
        MainActivity d = Robolectric.buildActivity(MainActivity.class).setup().get();
        System.out.println("P RESTORE_THEN_RESTART_EQUAL " + mid.equals(snap(d.s)));
        System.out.println("P settings after: fx=" + d.s.fx + " calm=" + d.s.calm + " volume=" + d.s.volume + " sounds=" + d.s.sounds);
    }
}
