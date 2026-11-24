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
public class Shot3 {
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
        a.s.wrong.add(3); a.s.wrong.add(700); a.s.favorites.add(5);
        org.json.JSONObject e = new org.json.JSONObject();
        e.put("t",1L); e.put("b","6"); e.put("m","study"); e.put("r",90); e.put("n",147); e.put("s",100);
        a.s.history.put(e);
        a.s.setupSection = "0"; a.begin();
        a.pick(a.qs.get(0).c); a.s.index = 2; a.pick(1);
        a.s.dark = true; a.s.fontPercent = 110;
        String code = Backup.make(a);
        System.out.println("CODE_LEN " + code.length() + " " + code.substring(0, 12));
        java.util.TreeSet<Integer> w = new java.util.TreeSet<>(a.s.wrong);
        int[] ids = a.s.ids.clone(); int[] pk = a.s.picked.clone();
        a.s.wrong.clear(); a.s.favorites.clear(); a.s.history = new org.json.JSONArray(); a.s.dark=false; a.s.fontPercent=100;
        a.s.picked = new int[a.s.ids.length]; java.util.Arrays.fill(a.s.picked,-1);
        String m = Backup.restore(a, code);
        System.out.println("RESTORE " + m + " wrong=" + a.s.wrong + " fav=" + a.s.favorites + " hist=" + a.s.history.length()
          + " dark=" + a.s.dark + " font=" + a.s.fontPercent + " idsEq=" + java.util.Arrays.equals(ids,a.s.ids) + " pickedEq=" + java.util.Arrays.equals(pk,a.s.picked)
          + " wrongEq=" + w.equals(a.s.wrong) + " run=" + a.s.run + " idx=" + a.s.index);
        java.nio.file.Files.write(java.nio.file.Paths.get("/tmp/rb/native_code.txt"), code.getBytes("UTF-8"));
        String web = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("/tmp/rb/web_code.txt")), "UTF-8");
        String m2 = Backup.restore(a, web);
        System.out.println("WEB2NATIVE " + (m2.startsWith("\u0627\u062a\u0633") ? "ok" : "FAIL") + " ids=" + a.s.ids.length + " idx=" + a.s.index + " pk=" + a.s.picked[0] + a.s.picked[1] + a.s.picked[2] + " wrong=" + a.s.wrong + " fav=" + a.s.favorites + " hist=" + a.s.history.length() + " dark=" + a.s.dark + " font=" + a.s.fontPercent + " started=" + a.s.started + " elapsed=" + a.s.elapsed);
    }
}
