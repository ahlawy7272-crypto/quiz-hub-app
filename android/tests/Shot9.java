package com.quizhub.app;
import org.junit.Test; import org.junit.runner.RunWith; import org.robolectric.*; import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk = 33, qualifiers = "w390dp-h844dp-xxhdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class Shot9 { @Test public void run() throws Exception {
  MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
  String web = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("/tmp/rw/web_export.txt")), "UTF-8");
  String m = Backup.restore(a, web);
  System.out.println("WEBEXPORT2NATIVE msg=" + m.length() + " ids=" + a.s.ids.length + " idx=" + a.s.index + " wrong=" + a.s.wrong + " fav=" + a.s.favorites + " hist=" + a.s.history.length() + " dark=" + a.s.dark + " font=" + a.s.fontPercent + " started=" + a.s.started + " picked=" + a.s.picked[0] + a.s.picked[1] + a.s.picked[2]);
}}
