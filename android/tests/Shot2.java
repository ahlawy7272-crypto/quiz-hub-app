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
public class Shot2 {
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
        a.s.wrong.add(0); a.s.wrong.add(3); a.s.wrong.add(120); a.s.wrong.add(700);
        a.s.favorites.add(5); a.s.favorites.add(18);
        org.json.JSONObject e = new org.json.JSONObject();
        e.put("t",1L); e.put("b","6"); e.put("m","study"); e.put("r",90); e.put("n",147); e.put("s",100);
        a.s.history.put(e);
        org.json.JSONObject e2 = new org.json.JSONObject(e.toString()); e2.put("r",120); a.s.history.put(e2);
        org.json.JSONObject e3 = new org.json.JSONObject(e.toString()); e3.put("b","notebook"); e3.put("r",2); e3.put("n",4); a.s.history.put(e3);
        a.renderAll();
        org.robolectric.shadows.ShadowLooper.idleMainLooper();
        shot(a.getWindow().getDecorView(), "10-home-learn");
        a.startRun("notebook", new int[]{0,3});
        org.robolectric.shadows.ShadowLooper.idleMainLooper();
        System.out.println("RUN " + a.s.run + " ids=" + a.s.ids.length + " qs=" + a.qs.size());
        shot(a.getWindow().getDecorView(), "11-notebook-quiz");
        a.pick(a.qs.get(0).c); a.pick(a.qs.get(0).c);
        a.s.index = 1; a.pick((a.qs.get(1).c+1)%4);
        a.doFinishConfirmed();
        System.out.println("WRONG " + a.s.wrong + " HIST " + a.s.history.length());
    }
}
