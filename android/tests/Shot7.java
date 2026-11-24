package com.quizhub.app;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadows.ShadowLooper;
import java.util.concurrent.TimeUnit;
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33, qualifiers = "w390dp-h844dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class Shot7 {
    @Test public void run() throws Exception {
        ActivityController<MainActivity> ctl = Robolectric.buildActivity(MainActivity.class).setup();
        MainActivity a = ctl.get();
        a.s.setupSection = "6"; a.s.setupMode = "exam"; a.begin();
        a.s.countdown = true; a.s.timer = true; a.s.minutes = 1; a.s.elapsed = 0;
        ShadowLooper.idleMainLooper(5, TimeUnit.SECONDS);
        System.out.println("T1 elapsed after 5s foreground=" + a.s.elapsed);
        int e0 = a.s.elapsed;
        ctl.pause(); ctl.stop();
        ShadowLooper.idleMainLooper(30, TimeUnit.SECONDS);
        System.out.println("T2 elapsed after 30s background=" + a.s.elapsed + " (delta " + (a.s.elapsed - e0) + ")");
        ctl.start(); ctl.resume();
        ShadowLooper.idleMainLooper(3, TimeUnit.SECONDS);
        System.out.println("T3 after resume +3s elapsed=" + a.s.elapsed);
        a.s.elapsed = 59;
        ShadowLooper.idleMainLooper(2, TimeUnit.SECONDS);
        System.out.println("T4 expiry elapsed=" + a.s.elapsed + " remaining=" + a.s.remaining() + " expired=" + a.s.expired());
        int idx = a.s.index; int pk = a.s.picked[idx];
        a.pick(0);
        System.out.println("T5 pick after expiry changed=" + (a.s.picked[idx] != pk));
        ClipPlayer.stop();
        System.out.println("T6 clip stop ok");
    }
}
