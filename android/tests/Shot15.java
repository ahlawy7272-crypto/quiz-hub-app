package com.quizhub.app;
import android.graphics.Bitmap; import android.graphics.Canvas; import android.view.View;
import org.junit.Test; import org.junit.runner.RunWith;
import org.robolectric.Robolectric; import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config; import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadows.ShadowLooper;
import java.io.FileOutputStream;
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33, qualifiers = "w360dp-h720dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class Shot15 {
    static void shot(MainActivity a, String name) throws Exception {
        View root = a.root; int w = 1080, h = 2160;
        root.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY));
        root.layout(0, 0, w, h);
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        root.draw(new Canvas(bmp));
        try (FileOutputStream o = new FileOutputStream("/tmp/rb/shots/" + name + ".png")) { bmp.compress(Bitmap.CompressFormat.PNG, 100, o); }
    }
    void run(boolean dark, String nm) throws Exception {
        ClipPlayer.existsOverride = true;
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        a.s.dark = dark; a.s.calm = true; a.s.fx = false; a.s.sounds = false; a.s.confirm = false;
        a.s.setupSection = "6"; a.s.setupMode = "study"; a.begin(); a.s.tab = "quiz"; a.s.index = 11; a.renderAll();
        ShadowLooper.idleMainLooper(1000, java.util.concurrent.TimeUnit.MILLISECONDS);
        Bank.Question q = a.qs.get(11); int wrong = (q.c + 2) % q.o.length;
        a.commit(wrong); ShadowLooper.idleMainLooper(500, java.util.concurrent.TimeUnit.MILLISECONDS);
        shot(a, nm + "-answered");
        ClipPlayer.fakePlay = true; ClipPlayer.toggle(a, "r" + q.gid + ".mp3", null); a.renderAll(); shot(a, nm + "-playing");
        ClipPlayer.fakePlay = false; ClipPlayer.existsOverride = false;
    }
    @Test public void light() throws Exception { run(false, "h1-light"); }
    @Test public void dark() throws Exception { run(true, "h1-dark"); }
}
