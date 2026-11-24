package com.quizhub.app;
import org.junit.Test; import org.junit.runner.RunWith; import org.robolectric.*; import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk = 33) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class Dbg1 { @Test public void run() throws Exception {
 MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
 a.s.setupSection="6"; a.begin(); Bank.Question q=a.qs.get(0);
 String[] l=a.getAssets().list("audio"); System.out.println("D g1="+ClipPlayer.exists(a,"g1.mp3")+" r0="+ClipPlayer.exists(a,"r0.mp3")); System.out.println("D gid="+q.gid+" audioListed="+(l==null?-1:l.length)+" r="+ClipPlayer.exists(a,"r"+q.gid+".mp3")+" g="+ClipPlayer.exists(a,"g"+q.gid+".mp3")+" mapExists="+(a.getAssets().list("").length));
 for(String s: a.getAssets().list("")) System.out.println("D asset "+s);
}}
