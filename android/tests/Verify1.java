package com.quizhub.app;

import static org.junit.Assert.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

/** Real assertions (fails the run). Run with the Robolectric harness; see tests/README.md. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class Verify1 {
    static String fixture() throws Exception {
        return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(System.getProperty("qh.fixture", "tests/fixtures/web_qh2_code.txt"))), "UTF-8").trim();
    }

    /** Saved state from older/foreign builds (ids up to 3121, unknown bank) must not crash and must be cleaned with a notice. */
    @Test public void legacySavedStateIsSanitized() throws Exception {
        ActivityController<MainActivity> ctl = Robolectric.buildActivity(MainActivity.class).setup();
        MainActivity a = ctl.get();
        a.s.setupSection = "6"; a.begin(); a.save();
        android.content.SharedPreferences p = a.getSharedPreferences("quizhub_native_v1", android.content.Context.MODE_PRIVATE);
        JSONObject j = new JSONObject(p.getString("state", null));
        JSONArray ids = new JSONArray(); StringBuilder pk = new StringBuilder();
        int[] bad = {598, 599, 3121, -4, 2000, 600};
        for (int id : bad) { ids.put(id); pk.append(id == 599 ? '1' : '.'); }
        j.put("ids", ids); j.put("picked", pk.toString()); j.put("index", 2);
        j.put("favorites", new JSONArray(new int[]{5, 3121, 99999}));
        j.put("wrong", new JSONArray(new int[]{7, 2500}));
        j.put("bankKey", "42"); j.put("setupSection", "42");
        String rawBefore = j.toString();
        p.edit().putString("state", rawBefore).commit();
        Session s = Session.load(a, a.bank);
        assertEquals("the untouched old state must be kept", rawBefore, p.getString("state_legacy_backup", null));
        assertNotNull("a notice must tell the user the old state was not converted", s.notice);
        // nothing is mapped by identity: fresh start, no ids or answers carried over
        assertEquals(a.bank.banks.get(0).ids.length, s.ids.length);
        for (int v : s.picked) assertEquals(-1, v);
        assertTrue(s.favorites.isEmpty() && s.wrong.isEmpty());
        assertTrue(a.bank.has(s.bankKey) && a.bank.has(s.setupSection));
        for (Bank.Question q : a.bank.forIds(s.ids)) assertNotNull(q);
        assertEquals("the old raw state is exportable", rawBefore, Session.legacyRaw(a));
        // a fresh activity must start and show the notice flow without crashing
        ctl.pause().stop().destroy();
        MainActivity b = Robolectric.buildActivity(MainActivity.class).setup().get();
        for (int id : b.s.ids) assertTrue(id >= 0 && id < b.bank.total());
    }

    /** A backup made by the current web build (1,186-question QH2 code) restores on Android with the right content. */
    @Test public void webBackupRestoresOnAndroid() throws Exception {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        String msg = Backup.restore(a, fixture());
        assertEquals("اتسترجع التقدم.", msg);
        assertEquals("6", a.s.bankKey);
        assertEquals(147, a.s.ids.length);
        assertEquals(598, a.s.ids[0]);
        assertEquals(java.util.Collections.singleton(598), new java.util.HashSet<>(a.s.favorites));
        assertEquals(2, a.s.wrong.size());
        for (int g : a.s.wrong) assertTrue(g >= 0 && g < a.bank.total());
        assertEquals(1, a.s.history.length());
    }

    /** Android's own code round trips, and a 3,122-question (older web) code is refused with the right message. */
    @Test public void androidBackupRoundTripAndLegacyRefusal() throws Exception {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        a.s.setupSection = "6"; a.begin(); a.s.confirm = false;
        a.s.index = 3; a.pick((a.qs.get(3).c + 1) % a.qs.get(3).o.length);
        a.s.favorites.add(10);
        String code = Backup.make(a);
        assertTrue(code.startsWith("QH2."));
        java.util.List<Integer> wrongBefore = new java.util.ArrayList<>(a.s.wrong);
        a.s.favorites.clear(); a.s.wrong.clear();
        assertEquals("اتسترجع التقدم.", Backup.restore(a, code));
        assertTrue(a.s.favorites.contains(10));
        assertEquals(new java.util.HashSet<>(wrongBefore), new java.util.HashSet<>(a.s.wrong));
        String out = System.getProperty("qh.out");
        if (out != null) java.nio.file.Files.write(java.nio.file.Paths.get(out), code.getBytes("UTF-8"));
        // older 3,122-question code: refused, current state untouched
        StringBuilder pk = new StringBuilder(), fv = new StringBuilder();
        for (int i = 0; i < 3122; i++) { pk.append(i == 5 ? '1' : '.'); fv.append('0'); }
        JSONObject o = new JSONObject(); o.put("v", 1);
        JSONObject st = new JSONObject();
        st.put("picked", pk.toString()); st.put("favorites", fv.toString());
        st.put("ids", new JSONArray(new int[]{5, 6})); st.put("settings", new JSONObject());
        o.put("s", st); o.put("x", new JSONObject().put("w", "").put("h", new JSONArray()));
        String legacy = "QH1." + android.util.Base64.encodeToString(o.toString().getBytes("UTF-8"), android.util.Base64.NO_WRAP);
        a.save();
        String before = a.getSharedPreferences("quizhub_native_v1", android.content.Context.MODE_PRIVATE).getString("state", null);
        String msg = Backup.restore(a, legacy);
        assertTrue(msg.contains("٣١٢٢"));
        assertEquals(before, a.getSharedPreferences("quizhub_native_v1", android.content.Context.MODE_PRIVATE).getString("state", null));
        // garbage is refused
        assertTrue(Backup.restore(a, "QH1.AAAA").startsWith("الكود مش صحيح"));
    }

    /** If the font files are missing the app falls back to the system font instead of crashing at startup. */
    @Test public void missingFontsFallBack() throws Exception {
        String old = Theme.fontDir;
        try {
            Theme.fontDir = "no-such-fonts/";
            MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
            assertEquals(6, a.theme.fontFallbacks);
            assertNotNull(a.theme.body());
            assertNotNull(a.s);
        } finally { Theme.fontDir = old; }
    }

    /** If the question files cannot be read the app shows a message and does not crash (and onPause is safe). */
    @Test public void unreadableBankShowsMessage() throws Exception {
        Bank.assetDirOverride = "no-such-bank/";
        try {
            ActivityController<MainActivity> ctl = Robolectric.buildActivity(MainActivity.class).setup();
            assertNull(ctl.get().s);
            ctl.pause().stop().destroy();
        } finally { Bank.assetDirOverride = null; }
    }

    /** Every question has a clip (lecturer r or generated g, after clip_map), none empty. */
    @Test public void everyQuestionHasPlayableAsset() throws Exception {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        assertEquals(1186, a.bank.total());
        int missing = 0;
        for (int i = 0; i < 1186; i++) if (!ClipPlayer.exists(a, "r" + i + ".mp3") && !ClipPlayer.exists(a, "g" + i + ".mp3")) missing++;
        assertEquals(0, missing);
    }
}
