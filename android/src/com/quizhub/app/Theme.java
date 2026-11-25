package com.quizhub.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;

/** Design tokens extracted 1:1 from the published gen18 style.css. */
public final class Theme {
    public boolean dark;
    public float scale = 1f; // --qz-scale, 0.70..1.25

    // light palette
    public static final int L_BG      = 0xffe4f5a4;
    public static final int L_TEAL    = 0xff0b4f4a;
    public static final int L_HEAD    = 0xff0b3f3b;
    public static final int L_MUTED   = 0xff2f6a5f;
    public static final int L_SOFT    = 0xffcfe98a;
    public static final int L_RED     = 0xffd3383f;
    public static final int L_REDSOFT = 0xfffbeaea;
    public static final int L_GOLD    = 0xffb98a2f;
    public static final int L_CARD    = 0xa6ffffff; // rgba(255,255,255,.65)
    public static final int L_CARD75  = 0xbfffffff; // rgba(255,255,255,.75)
    public static final int L_CARD55  = 0x8cffffff; // rgba(255,255,255,.55)
    public static final int L_BORDER  = 0x330b4f4a; // rgba(11,79,74,.2)
    public static final int L_BORDER3 = 0x4d0b4f4a; // rgba(11,79,74,.3)
    public static final int L_RAIL    = 0x2e0b4f4a; // rgba(11,79,74,.18)

    // dark palette
    public static final int D_BG      = 0xff092f2c;
    public static final int D_CARD    = 0xff17483e;
    public static final int D_DIALOG  = 0xff113e35;
    public static final int D_TEXT    = 0xffe4f5a4;
    public static final int D_MUTED   = 0xffb9d5b4;
    public static final int D_SOFT    = 0xffcfe98a;
    public static final int D_BODY    = 0xffe0ebce;
    public static final int D_OPTTEXT = 0xffeef6db;
    public static final int D_BORDER  = 0x33cfe98a; // cfe98a33
    public static final int D_BORDER4 = 0x44cfe98a;
    public static final int D_MISSED  = 0xffffb2aa;
    public static final int D_PENDING = 0xff315c40;
    public static final int D_TABTXT  = 0xffc3d2b6;

    public int bg()      { return dark ? D_BG : L_BG; }
    public int card()    { return dark ? D_CARD : L_CARD; }
    public int card75()  { return dark ? D_CARD : L_CARD75; }
    public int card55()  { return dark ? D_CARD : L_CARD55; }
    public int teal()    { return dark ? D_SOFT : L_TEAL; }   // --qz-teal flips in dark
    public int ink()     { return dark ? D_TEXT : L_TEAL; }   // primary text
    public int head()    { return dark ? D_TEXT : L_HEAD; }
    public int muted()   { return dark ? D_MUTED : L_MUTED; }
    public int bodyColor() { return dark ? D_BODY : L_HEAD; }
    public int optText() { return dark ? D_OPTTEXT : L_HEAD; }
    public int border()  { return dark ? D_BORDER : L_BORDER; }
    public int border3() { return dark ? D_BORDER4 : L_BORDER3; }
    public int rail()    { return dark ? D_BORDER : L_RAIL; }
    public int onAccent(){ return dark ? D_BG : L_BG; }       // text on teal/soft fill
    public int primary() { return dark ? D_SOFT : L_TEAL; }   // primary button fill
    public int sheet()   { return dark ? D_DIALOG : 0xfff4fbd4; }
    public int missed()  { return dark ? D_MISSED : L_RED; }
    public int tabText() { return dark ? D_TABTXT : 0xff4a7a70; }
    public int pendingBg(){ return dark ? D_PENDING : L_SOFT; }

    // ---- fonts ----
    public Typeface plex400, plex500, plex600, readex500, readex600, readex700;
    /** Asset folder for fonts; tests point it at a missing folder to check the fallback. */
    static String fontDir = "fonts/";
    /** How many of the 6 fonts could not be loaded and fell back to the system font (0 normally). */
    public int fontFallbacks = 0;

    private Typeface font(Context c, String file, int style) {
        try { return Typeface.createFromAsset(c.getAssets(), fontDir + file); }
        catch (Exception e) { fontFallbacks++; return Typeface.create(Typeface.DEFAULT, style); }
    }

    public void loadFonts(Context c) {
        fontFallbacks = 0;
        plex400   = font(c, "ibm-plex-arabic-400.ttf", Typeface.NORMAL);
        plex500   = font(c, "ibm-plex-arabic-500.ttf", Typeface.NORMAL);
        plex600   = font(c, "ibm-plex-arabic-600.ttf", Typeface.BOLD);
        readex500 = font(c, "readex-pro-500.ttf", Typeface.NORMAL);
        readex600 = font(c, "readex-pro-600.ttf", Typeface.BOLD);
        readex700 = font(c, "readex-pro-700.ttf", Typeface.BOLD);
    }
    public Typeface body()   { return plex400; }
    public Typeface bodyM()  { return plex500; }
    public Typeface bodyB()  { return plex600; }
    public Typeface disp()   { return readex600; }
    public Typeface dispB()  { return readex700; }

    /** sp sizes already multiplied by --qz-scale where the CSS says calc(15px * scale). */
    public float qSize()  { return 16f * scale; } // Phase 2: web parity (was 15)
    public float bodySize() { return 15f * scale; }
    public float smallSize(){ return 12f * scale; }
}
