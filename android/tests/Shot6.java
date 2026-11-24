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
@Config(sdk = 33, qualifiers = "w320dp-h640dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class Shot6 {
    static void shot(View root, String name) throws Exception {
        int w = 960, h = 9000;
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
        android.content.res.Configuration cf=new android.content.res.Configuration(); MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get(); cf.fontScale=1.5f; a.getResources().getConfiguration().fontScale=1.5f; a.getResources().getDisplayMetrics().scaledDensity=a.getResources().getDisplayMetrics().density*1.5f;
        int[] rs={60,90,75,120,130,100,45}; for(int q=0;q<rs.length;q++){ org.json.JSONObject e=new org.json.JSONObject(); try{e.put("t",(long)q);e.put("b","6");e.put("m","study");e.put("r",rs[q]);e.put("n",147);e.put("s",100);}catch(Exception x){} a.s.history.put(e);}
        a.s.wrong.add(0); a.s.favorites.add(5);
        a.s.tab = "settings"; a.renderAll();
        org.robolectric.shadows.ShadowLooper.idleMainLooper();
        java.lang.reflect.Field f = MainActivity.class.getDeclaredField("content"); f.setAccessible(true);
        View c = (View) f.get(a);
        android.widget.FrameLayout wrap = new android.widget.FrameLayout(a); wrap.setBackgroundColor(0xffe4f5a4);
        ((ViewGroup) c.getParent()).removeView(c); wrap.addView(c);
        shot(wrap, "40-narrow-settings");
        wrap.removeView(c); a.s.tab = "home"; a.renderAll();
        wrap.addView(c);
        shot(wrap, "41-narrow-home");
    }
}
