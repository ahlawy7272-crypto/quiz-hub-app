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
        assertEquals(a.bank.defaultBank().ids.length, s.ids.length);
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

    /** byId() must return null for an unknown bank instead of silently switching banks. */
    @Test public void unknownBankIdReturnsNull() throws Exception {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        assertNull(a.bank.byId("42"));
        assertNull(a.bank.byId(""));
        assertNotNull(a.bank.byId("6"));
        assertEquals("6", a.bank.byId("6").id);
    }

    /** forIds() must skip unknown gids instead of adding nulls. */
    @Test public void forIdsFiltersUnknownIds() throws Exception {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        java.util.List<Bank.Question> qs = a.bank.forIds(new int[]{598, 9999, 599, -1});
        assertEquals(2, qs.size());
        for (Bank.Question q : qs) assertNotNull(q);
        assertEquals(598, qs.get(0).gid);
        assertEquals(599, qs.get(1).gid);
    }

    /** Picked values outside '.' / '0'..'4' must be reset to unanswered, not kept as odd values. */
    @Test public void invalidPickedValuesAreReset() throws Exception {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        a.s.setupSection = "6"; a.begin(); a.save();
        android.content.SharedPreferences p = a.getSharedPreferences("quizhub_native_v1", android.content.Context.MODE_PRIVATE);
        JSONObject j = new JSONObject(p.getString("state", null));
        int n = a.s.ids.length;
        StringBuilder pk = new StringBuilder();
        for (int i = 0; i < n; i++) pk.append(i == 0 ? '9' : i == 1 ? 'x' : i == 2 ? '2' : '.');
        j.put("picked", pk.toString());
        p.edit().putString("state", j.toString()).commit();
        Session s = Session.load(a, a.bank);
        assertEquals(-1, s.picked[0]);
        assertEquals(-1, s.picked[1]);
        assertEquals(2, s.picked[2]);
        assertNull("a single bad answer must not trigger a whole-session reset", s.notice);
    }

    /** One bad gid in favorites/wrong/ids must be dropped, not wipe the notebook, history or answers. */
    @Test public void singleBadIdIsDroppedNotWiped() throws Exception {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        a.s.setupSection = "6"; a.begin(); a.save();
        android.content.SharedPreferences p = a.getSharedPreferences("quizhub_native_v1", android.content.Context.MODE_PRIVATE);
        JSONObject j = new JSONObject(p.getString("state", null));
        int n = a.s.ids.length;
        int gid0 = a.s.ids[0], gid2 = a.s.ids[2];
        StringBuilder pk = new StringBuilder();
        for (int i = 0; i < n; i++) pk.append(i == 0 ? '1' : i == 2 ? '2' : '.');
        j.put("picked", pk.toString());
        // one bad id in the middle of the run, one bad gid in favorites and in wrong
        JSONArray ids = j.getJSONArray("ids");
        ids.put(1, 9999);
        j.put("ids", ids);
        j.put("favorites", new JSONArray(new int[]{598, 599, 99999}));
        j.put("wrong", new JSONArray(new int[]{600, 2500}));
        JSONObject h = new JSONObject();
        h.put("t", 1234567890L); h.put("b", "6"); h.put("m", "study"); h.put("r", 5); h.put("n", 10); h.put("s", 42);
        j.put("history", new JSONArray().put(h));
        p.edit().putString("state", j.toString()).commit();
        Session s = Session.load(a, a.bank);
        assertNull("one bad id must not trigger a whole-session reset", s.notice);
        assertEquals("the bad run id is dropped, the rest of the run survives", n - 1, s.ids.length);
        assertEquals(gid0, s.ids[0]);
        assertEquals("the run keeps its order past the dropped id", gid2, s.ids[1]);
        assertEquals("the answer before the dropped id survives", 1, s.picked[0]);
        assertEquals("the answer after the dropped id stays aligned with its question", 2, s.picked[1]);
        assertEquals(new java.util.HashSet<>(java.util.Arrays.asList(598, 599)), new java.util.HashSet<>(s.favorites));
        assertEquals(new java.util.HashSet<>(java.util.Collections.singletonList(600)), new java.util.HashSet<>(s.wrong));
        assertEquals("history survives a single bad id", 1, s.history.length());
    }

    /** A backup code naming a bank that does not exist in this build is refused, state untouched. */
    @Test public void backupRestoreWithUnknownSectionIsRefused() throws Exception {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        String code = fixture();
        byte[] z = android.util.Base64.decode(code.substring(4), android.util.Base64.DEFAULT);
        java.util.zip.Inflater inf = new java.util.zip.Inflater(true); inf.setInput(z);
        java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream(); byte[] buf = new byte[8192];
        while (!inf.finished()) { int n = inf.inflate(buf); if (n == 0) break; bo.write(buf, 0, n); }
        JSONObject o = new JSONObject(new String(bo.toByteArray(), "UTF-8"));
        o.getJSONObject("s").getJSONObject("setup").put("section", "42");
        java.util.zip.Deflater df = new java.util.zip.Deflater(java.util.zip.Deflater.DEFAULT_COMPRESSION, true);
        byte[] in = o.toString().getBytes("UTF-8"); df.setInput(in); df.finish();
        java.io.ByteArrayOutputStream bo2 = new java.io.ByteArrayOutputStream(); byte[] buf2 = new byte[8192];
        while (!df.finished()) { int n = df.deflate(buf2); bo2.write(buf2, 0, n); }
        df.end();
        String bad = "QH2." + android.util.Base64.encodeToString(bo2.toByteArray(), android.util.Base64.NO_WRAP);
        a.save();
        String before = a.getSharedPreferences("quizhub_native_v1", android.content.Context.MODE_PRIVATE).getString("state", null);
        String msg = Backup.restore(a, bad);
        assertTrue(msg.contains("مش موجود"));
        assertEquals(before, a.getSharedPreferences("quizhub_native_v1", android.content.Context.MODE_PRIVATE).getString("state", null));
    }

    /** S1-1: a notebook run must survive the next launch — bankKey must stay a real bank id, not the run kind. */
    @Test public void notebookRunSurvivesReload() throws Exception {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        a.startRun("notebook", new int[]{598, 599, 600});
        assertEquals("notebook", a.s.run);
        assertTrue("bankKey must be a real bank id", a.bank.has(a.s.bankKey));
        // answer one question wrong so there is real progress to lose
        a.pick((a.qs.get(0).c + 1) % a.qs.get(0).o.length);
        int wrongGid = a.qs.get(0).gid;
        a.save();
        Session s2 = Session.load(a, a.bank);
        assertNull("a notebook run is this build's own session, not foreign data", s2.notice);
        assertEquals("notebook", s2.run);
        assertTrue(a.bank.has(s2.bankKey));
        assertEquals(3, s2.ids.length);
        assertEquals(598, s2.ids[0]);
        assertTrue("the wrong answer must still be in the notebook", s2.wrong.contains(wrongGid));
        assertTrue("the picked answer must survive", s2.picked[0] >= 0);
    }

    /** S1-1 via backup: a notebook code round-trips, and an old-format code with bank="notebook" restores too. */
    @Test public void notebookBackupRoundTripAndOldBankKind() throws Exception {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        a.startRun("notebook", new int[]{598, 599});
        String code = Backup.make(a);
        assertEquals("اتسترجع التقدم.", Backup.restore(a, code));
        assertEquals("notebook", a.s.run);
        assertTrue(a.bank.has(a.s.bankKey));
        assertEquals(2, a.s.ids.length);
        // old format: the code's "bank" field carries the run kind instead of a bank id
        byte[] z = android.util.Base64.decode(code.substring(4), android.util.Base64.DEFAULT);
        java.util.zip.Inflater inf = new java.util.zip.Inflater(true); inf.setInput(z);
        java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream(); byte[] buf = new byte[8192];
        while (!inf.finished()) { int n = inf.inflate(buf); if (n == 0) break; bo.write(buf, 0, n); }
        JSONObject o = new JSONObject(new String(bo.toByteArray(), "UTF-8"));
        o.getJSONObject("s").put("bank", "notebook");
        java.util.zip.Deflater df = new java.util.zip.Deflater(java.util.zip.Deflater.DEFAULT_COMPRESSION, true);
        byte[] in = o.toString().getBytes("UTF-8"); df.setInput(in); df.finish();
        java.io.ByteArrayOutputStream bo2 = new java.io.ByteArrayOutputStream(); byte[] buf2 = new byte[8192];
        while (!df.finished()) { int n = df.deflate(buf2); bo2.write(buf2, 0, n); }
        df.end();
        String oldCode = "QH2." + android.util.Base64.encodeToString(bo2.toByteArray(), android.util.Base64.NO_WRAP);
        assertEquals("اتسترجع التقدم.", Backup.restore(a, oldCode));
        assertEquals("notebook", a.s.run);
        assertTrue(a.bank.has(a.s.bankKey));
        assertEquals(2, a.s.ids.length);
    }

    /** S3-1: leaving the result screen for home must not wipe the result or the attempt. */
    @Test public void goHomeKeepsResult() throws Exception {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        a.s.setupSection = "6"; a.begin();
        a.pick(a.qs.get(0).c);
        a.s.submitted = true; a.s.resultElapsed = 42; a.save();
        a.goHome();
        assertTrue(a.s.started);
        assertTrue(a.s.submitted);
        assertEquals(42, a.s.resultElapsed);
        assertEquals(a.qs.get(0).c, a.s.picked[0]);
        assertEquals("home", a.s.tab);
    }

    /** S3-7: resetting settings must reset the minutes too. */
    @Test public void resetSettingsResetsMinutes() throws Exception {
        MainActivity a = Robolectric.buildActivity(MainActivity.class).setup().get();
        a.s.minutes = 5; a.s.volume = 10; a.s.dark = true;
        Screens.resetSettings(a);
        assertEquals(120, a.s.minutes);
        assertEquals(80, a.s.volume);
        assertFalse(a.s.dark);
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
