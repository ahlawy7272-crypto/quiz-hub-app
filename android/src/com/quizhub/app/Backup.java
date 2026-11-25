package com.quizhub.app;

import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.zip.Inflater;

/** Backup code compatible with the web app format: "QH1." + base64(JSON {v:1,s,x}). Restores QH2 (deflate-raw) too. */
public final class Backup {
    static final int N = 1186;
    static final Charset U8 = Charset.forName("UTF-8");

    static String packBits(java.util.Set<Integer> set) {
        byte[] b = new byte[(N + 7) / 8];
        for (int i : set) if (i >= 0 && i < N) b[i >> 3] |= (byte) (1 << (i & 7));
        int last = b.length; while (last > 0 && b[last - 1] == 0) last--;
        return Base64.encodeToString(java.util.Arrays.copyOf(b, last), Base64.NO_WRAP);
    }
    static java.util.TreeSet<Integer> unpackBits(String str) {
        java.util.TreeSet<Integer> out = new java.util.TreeSet<>();
        if (str == null || str.isEmpty()) return out;
        byte[] b = Base64.decode(str, Base64.DEFAULT);
        for (int i = 0; i < b.length; i++) for (int k = 0; k < 8; k++)
            if ((b[i] & (1 << k)) != 0) { int id = i * 8 + k; if (id < N) out.add(id); }
        return out;
    }

    public static String make(MainActivity a) throws Exception {
        Session s = a.s;
        JSONObject st = new JSONObject();
        st.put("dark", s.dark); st.put("textSize", "normal"); st.put("fontScale", s.fontPercent);
        st.put("spacious", s.spacious); st.put("contrast", s.contrast); st.put("calm", s.calm);
        st.put("steps", s.steps); st.put("letters", s.letters); st.put("confirm", s.confirm);
        st.put("autoNext", s.autoNext); st.put("retry", s.retry); st.put("hints", s.hints);
        st.put("hintDefault", s.hintDefault); st.put("translation", s.translation); st.put("autoExplain", s.autoExplain);
        st.put("timer", s.timer); st.put("quizOnly", s.quizOnly); st.put("countdown", s.countdown); st.put("minutes", s.minutes);
        st.put("sounds", s.sounds); st.put("fx", s.fx); st.put("volume", s.volume);
        char[] picked = new char[N]; java.util.Arrays.fill(picked, '.');
        for (int i = 0; i < s.ids.length; i++) if (s.picked[i] >= 0 && s.ids[i] < N) picked[s.ids[i]] = (char) ('0' + s.picked[i]);
        char[] fav = new char[N]; java.util.Arrays.fill(fav, '0');
        for (int g : s.favorites) if (g >= 0 && g < N) fav[g] = '1';
        JSONObject setup = new JSONObject();
        Bank.BankMeta sb = a.bank.byId(s.setupSection);
        setup.put("section", sb.id); setup.put("count", sb.ids.length); setup.put("mode", s.setupMode);
        JSONObject o = new JSONObject();
        o.put("run", s.run); o.put("bank", s.run.equals("all") ? s.bankKey : s.run);
        o.put("settings", st); o.put("favorites", new String(fav));
        o.put("ids", new JSONArray(toList(s.ids))); o.put("picked", new String(picked));
        o.put("index", s.ids.length > 0 ? s.ids[Math.min(s.index, s.ids.length - 1)] : 0);
        o.put("elapsed", s.elapsed); o.put("paused", s.paused); o.put("started", s.started); o.put("submitted", s.submitted);
        o.put("resultElapsed", s.resultElapsed < 0 ? JSONObject.NULL : (Object) s.resultElapsed);
        o.put("mode", s.mode); o.put("tab", s.tab); o.put("setup", setup);
        JSONObject x = new JSONObject(); x.put("w", packBits(s.wrong)); x.put("h", s.history);
        JSONObject root = new JSONObject(); root.put("v", 1); root.put("s", o); root.put("x", x);
        byte[] in = root.toString().getBytes(U8);
        java.util.zip.Deflater df = new java.util.zip.Deflater(java.util.zip.Deflater.DEFAULT_COMPRESSION, true);
        df.setInput(in); df.finish();
        ByteArrayOutputStream bo = new ByteArrayOutputStream(); byte[] buf = new byte[8192];
        while (!df.finished()) { int n = df.deflate(buf); bo.write(buf, 0, n); }
        df.end();
        return "QH2." + Base64.encodeToString(bo.toByteArray(), Base64.NO_WRAP);
    }
    static java.util.List<Integer> toList(int[] a) { java.util.ArrayList<Integer> l = new java.util.ArrayList<>(); for (int v : a) l.add(v); return l; }

    public static String restore(MainActivity a, String codeIn) {
        try {
            String code = codeIn.replaceAll("\\s+", "");
            byte[] raw;
            if (code.startsWith("QH2.")) {
                byte[] z = Base64.decode(code.substring(4), Base64.DEFAULT);
                Inflater inf = new Inflater(true); inf.setInput(z);
                ByteArrayOutputStream bo = new ByteArrayOutputStream(); byte[] buf = new byte[8192];
                while (!inf.finished()) { int n = inf.inflate(buf); if (n == 0 && (inf.needsInput() || inf.needsDictionary())) break; bo.write(buf, 0, n); }
                raw = bo.toByteArray();
            } else if (code.startsWith("QH1.")) raw = Base64.decode(code.substring(4), Base64.DEFAULT);
            else return "الكود مش صحيح.";
            JSONObject obj = new JSONObject(new String(raw, U8));
            JSONObject st = obj.optJSONObject("s"), ex = obj.optJSONObject("x");
            if (obj.optInt("v") != 1 || st == null || ex == null) return "الكود مش صحيح.";
            String pk = st.optString("picked", ""), fv = st.optString("favorites", "");
            JSONArray idsA = st.optJSONArray("ids"); JSONObject se = st.optJSONObject("settings");
            // Code from the older 3,122-question build is refused: the old id order is not available, so it cannot be mapped safely. Current state is not touched.
            if (pk.length() == 3122 || fv.length() == 3122) return "الكود ده من نسخة قديمة (٣١٢٢ سؤال) ومش مدعوم هنا. تقدمك الحالي ما اتغيّرش.";
            if (pk.length() != N || fv.length() != N || idsA == null || se == null || ex.optJSONArray("h") == null || !ex.has("w"))
                return "الكود مش صحيح.";
            int[] ids = new int[idsA.length()];
            for (int i = 0; i < ids.length; i++) { ids[i] = idsA.getInt(i); if (ids[i] < 0 || ids[i] >= N) return "الكود مش من نفس النسخة."; }
            Session.keepBeforeRestore(a);
            Session s = a.s;
            s.ids = ids; s.picked = new int[ids.length];
            for (int i = 0; i < ids.length; i++) { char ch = pk.charAt(ids[i]); s.picked[i] = ch == '.' ? -1 : ch - '0'; }
            int gi = st.optInt("index", ids.length > 0 ? ids[0] : 0); s.index = 0;
            for (int i = 0; i < ids.length; i++) if (ids[i] == gi) s.index = i;
            s.favorites.clear(); for (int i = 0; i < N; i++) if (fv.charAt(i) == '1') s.favorites.add(i);
            s.wrong = unpackBits(ex.optString("w", ""));
            JSONArray h = ex.getJSONArray("h"); JSONArray hh = new JSONArray();
            for (int i = Math.max(0, h.length() - 60); i < h.length(); i++) hh.put(h.get(i));
            s.history = hh;
            s.run = st.optString("run", "all"); if (!s.run.equals("notebook") && !s.run.equals("flagged")) s.run = "all";
            s.bankKey = st.optString("bank", "6");
            s.dark = se.optBoolean("dark"); s.spacious = se.optBoolean("spacious"); s.contrast = se.optBoolean("contrast");
            s.calm = se.optBoolean("calm"); s.steps = se.optBoolean("steps", true); s.letters = se.optBoolean("letters", true);
            s.confirm = se.optBoolean("confirm"); s.autoNext = se.optBoolean("autoNext"); s.retry = se.optBoolean("retry");
            s.hints = se.optBoolean("hints", true); s.hintDefault = se.optBoolean("hintDefault");
            s.translation = se.optBoolean("translation"); s.autoExplain = se.optBoolean("autoExplain");
            s.timer = se.optBoolean("timer", true); s.quizOnly = se.optBoolean("quizOnly"); s.countdown = se.optBoolean("countdown");
            s.sounds = se.optBoolean("sounds", true); s.fx = se.optBoolean("fx", true); s.volume = Math.max(0, Math.min(100, se.optInt("volume", 80)));
            String ts = se.optString("textSize", "normal");
            s.fontPercent = se.has("fontScale") ? se.optInt("fontScale", 100) : ts.equals("small") ? 88 : ts.equals("large") ? 118 : 100;
            s.minutes = se.optInt("minutes", 120);
            s.elapsed = st.optInt("elapsed"); s.paused = st.optBoolean("paused"); s.started = st.optBoolean("started");
            s.submitted = st.optBoolean("submitted"); s.resultElapsed = st.isNull("resultElapsed") ? -1 : st.optInt("resultElapsed", -1);
            s.mode = st.optString("mode", "study").equals("exam") ? "exam" : "study";
            s.tab = "home";
            JSONObject su = st.optJSONObject("setup");
            if (su != null) { s.setupSection = su.optString("section", "6"); s.setupMode = su.optString("mode", "study"); }
            a.qs = null; a.theme.dark = s.dark; a.theme.scale = s.fontPercent / 100f;
            a.save(); a.buildScreen(); a.renderAll();
            return "اتسترجع التقدم.";
        } catch (Exception e) { return "الكود مش صحيح أو ناقص."; }
    }
}
