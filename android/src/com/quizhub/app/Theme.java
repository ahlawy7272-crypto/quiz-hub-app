package com.quizhub.app;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Clean-rebuild design tokens. One design only (the agreed round-5 design):
 * soft ivory light background, gold accents, deep-teal dark mode.
 * Fonts: IBM Plex Sans Arabic for body/questions/options/explanations,
 * Readex Pro for headings/buttons/timer/numbers. No other fonts.
 */
public final class Theme {
    public boolean dark;
    public float scale = 1f; // font scale, 0.70..1.25

    // Light palette — soft ivory background, gold accents.
    public static final int L_IVORY   = 0xffF5F3EE; // the only light screen background
    public static final int L_BG      = 0xffF5F3EE; // ivory — text on teal fills ONLY, never a background
    public static final int L_TEAL    = 0xff0b4f4a;
    public static final int L_HEAD    = 0xff0b3f3b;
    public static final int L_MUTED   = 0xff2f6a5f;
    public static final int L_SOFT    = 0xffD4A24C; // warm gold accent
    public static final int L_CORRECT = 0xff20714a; // calm dark green — correct-answer fill
    public static final int L_RED     = 0xffd3383f;
    public static final int L_REDSOFT = 0xfffbeaea;
    public static final int L_GOLD    = 0xffb98a2f;
    public static final int L_GOLD_TINT = 0xffEEE6D7; // pending (tapped, unconfirmed) option fill
    public static final int L_GREEN   = 0xff2e7d5b;
    public static final int L_RED_CALM = 0xffb3402f;
    public static final int L_CARD    = 0xa6ffffff;
    public static final int L_CARD75  = 0xbfffffff;
    public static final int L_CARD55  = 0x8cffffff;
    public static final int L_BORDER  = 0x330b4f4a;
    public static final int L_BORDER3 = 0x4d0b4f4a;
    public static final int L_RAIL    = 0x2e0b4f4a;

    // Dark palette — deep teal background.
    public static final int D_BG      = 0xff0b3b37;
    public static final int D_CARD    = 0xff17483e;
    public static final int D_DIALOG  = 0xff113e35;
    public static final int D_TEXT    = 0xffF5F3EE; // ivory text on deep teal
    public static final int D_MUTED   = 0xa6F5F3EE; // muted ivory (65%) — secondary text on teal
    public static final int D_SOFT    = 0xffD4A24C; // warm gold accent
    public static final int D_BODY    = 0xffF5F3EE;
    public static final int D_OPTTEXT = 0xffF5F3EE;
    public static final int D_BORDER  = 0x33D4A24C;
    public static final int D_BORDER4 = 0x44D4A24C;
    public static final int D_TEAL_LT = 0xff8fd0c8; // light teal — تجريبي badge in dark mode
    public static final int D_MISSED  = 0xffffb2aa;
    public static final int D_PENDING = 0x29D4A24C; // gold tint — pending option fill in dark mode
    public static final int D_TABTXT  = 0xa6F5F3EE; // muted ivory (65%)
    public static final int D_GOLD    = 0xffd4a24c;
    public static final int D_GREEN   = 0xff7fd08a;
    public static final int D_RED_CALM = 0xffff9d94;

    /** Light screen background: soft ivory. This is the value the round-6
     *  audit caught — bg() used to return the old pale green here. */
    public int bg()       { return dark ? D_BG : L_IVORY; }
    public int rootBg()   { return dark ? D_BG : L_IVORY; }
    public int card()     { return dark ? D_CARD : L_CARD; }
    public int card75()   { return dark ? D_CARD : L_CARD75; }
    public int card55()   { return dark ? D_CARD : L_CARD55; }
    public int teal()     { return dark ? D_SOFT : L_TEAL; }
    public int ink()      { return dark ? D_TEXT : L_TEAL; }
    public int head()     { return dark ? D_TEXT : L_HEAD; }
    public int muted()    { return dark ? D_MUTED : L_MUTED; }
    public int bodyColor(){ return dark ? D_BODY : L_HEAD; }
    public int optText()  { return dark ? D_OPTTEXT : L_HEAD; }
    public int border()   { return dark ? D_BORDER : L_BORDER; }
    public int border3()  { return dark ? D_BORDER4 : L_BORDER3; }
    public int rail()     { return dark ? D_BORDER : L_RAIL; }
    public int onAccent() { return dark ? D_BG : L_BG; }
    public int primary()  { return dark ? D_SOFT : L_TEAL; }
    public int goldFill() { return dark ? D_GOLD : L_GOLD; }
    public int onGold()   { return dark ? D_BG : 0xffffffff; }
    /** Dialogs are ivory in light mode (the old pale-green sheet is gone). */
    public int sheet()    { return dark ? D_DIALOG : L_IVORY; }
    public int missed()   { return dark ? D_MISSED : L_RED; }
    public int tabText()  { return dark ? D_TABTXT : 0xff4a7a70; }
    /** Pending option fill: gold-tinted ivory in light mode. */
    public int pendingBg(){ return dark ? D_PENDING : L_GOLD_TINT; }

    // ---- fonts (only these two families) ----
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

    public float qSize()    { return 18f * scale; } // bigger question text, round 5
    public float bodySize() { return 15f * scale; }
    public float smallSize(){ return 12f * scale; }
}
