package com.quizhub.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/** Session state, persisted to SharedPreferences ("quizhub_native_v1").
 *  Mirrors the web Saved model. */
public final class Session {
    // settings
    public boolean dark=false, spacious=false, contrast=false, calm=false, steps=true, letters=true;
    public boolean confirm=false, autoNext=false, retry=false, hints=true, hintDefault=false;
    public boolean translation=false, autoExplain=false, timer=true, quizOnly=false, countdown=false;
    public int fontPercent=100, minutes=120;
    public boolean sounds=true, fx=true; public int volume=80;
    /** Bank ids whose questions run in a random order on a fresh start. Default off. */
    public java.util.TreeSet<String> shuffleBanks = new java.util.TreeSet<>();
    /** True when the live run's ids are a shuffled order (the order itself lives in ids[]). */
    public boolean shuffled = false;
    // session
    public int[] ids = new int[0];
    public int[] picked = new int[0]; // -1 = unanswered, parallel to ids
    public int index = 0;             // position within ids (NOT global id)
    public int elapsed = 0;
    public boolean paused=false, started=false, submitted=false;
    public int resultElapsed = -1;
    public String mode = "study";     // study | exam
    public String tab = "home";       // home | quiz | settings
    public String setupSection = Bank.DEFAULT_BANK_ID;
    public String setupMode = "study";
    public String run = "all";        // all | notebook | flagged
    public String bankKey = Bank.DEFAULT_BANK_ID;      // bank id of the live session
    public java.util.TreeSet<Integer> favorites = new java.util.TreeSet<>();
    public java.util.TreeSet<Integer> wrong = new java.util.TreeSet<>();
    public JSONArray history = new JSONArray(); // [{t,b,m,r,n,s}], newest last, max 60

    private static final String PREF = "quizhub_native_v1";

    /** True when the last save() actually failed to write (storage full/blocked). */
    public boolean saveFailed = false;

    /** Set when saved state pointed at a bank or question that does not exist in this build; shown once at startup. */
    public String notice = null;

    public static Session load(Context c, Bank bank) {
        Session s = new Session();
        Bank.BankMeta def = bank.defaultBank();
        s.minutes = def.minutes;
        s.ids = def.ids.clone();
        s.picked = new int[s.ids.length];
        java.util.Arrays.fill(s.picked, -1);
        SharedPreferences p = c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String raw = p.getString("state", null);
        if (raw == null) return s;
        try {
            JSONObject j = new JSONObject(raw);
            JSONObject st = j.getJSONObject("settings");
            s.dark=st.optBoolean("dark"); s.spacious=st.optBoolean("spacious"); s.contrast=st.optBoolean("contrast");
            s.calm=st.optBoolean("calm"); s.steps=st.optBoolean("steps",true); s.letters=st.optBoolean("letters",true);
            s.confirm=st.optBoolean("confirm"); s.autoNext=st.optBoolean("autoNext"); s.retry=st.optBoolean("retry");
            s.hints=st.optBoolean("hints",true); s.hintDefault=st.optBoolean("hintDefault");
            s.translation=st.optBoolean("translation"); s.autoExplain=st.optBoolean("autoExplain");
            s.timer=st.optBoolean("timer",true); s.quizOnly=st.optBoolean("quizOnly"); s.countdown=st.optBoolean("countdown");
            s.sounds=st.optBoolean("sounds",true); s.fx=st.optBoolean("fx",true); s.volume=st.optInt("volume",80);
            s.fontPercent=st.optInt("fontPercent",100); s.minutes=st.optInt("minutes",120);
            JSONArray shb=st.optJSONArray("shuffleBanks"); if(shb!=null) for(int i=0;i<shb.length();i++) s.shuffleBanks.add(shb.getString(i));
            s.shuffled=j.optBoolean("shuffled");
            s.elapsed=j.optInt("elapsed"); s.paused=j.optBoolean("paused"); s.started=j.optBoolean("started");
            s.submitted=j.optBoolean("submitted"); s.resultElapsed=j.optInt("resultElapsed",-1);
            s.mode=j.optString("mode","study"); s.tab=j.optString("tab","home");
            s.setupSection=j.optString("setupSection",Bank.DEFAULT_BANK_ID); s.setupMode=j.optString("setupMode","study");
            s.run=j.optString("run","all"); s.bankKey=j.optString("bankKey",Bank.DEFAULT_BANK_ID);
            JSONArray fv=j.optJSONArray("favorites"); if(fv!=null) for(int i=0;i<fv.length();i++) s.favorites.add(fv.getInt(i));
            JSONArray wv=j.optJSONArray("wrong"); if(wv!=null) for(int i=0;i<wv.length();i++) s.wrong.add(wv.getInt(i));
            JSONArray hv=j.optJSONArray("history"); if(hv!=null) s.history=hv;
            JSONArray idsA = j.getJSONArray("ids");
            s.ids = new int[idsA.length()];
            for (int i=0;i<idsA.length();i++) s.ids[i]=idsA.getInt(i);
            String pk = j.getString("picked");
            s.picked = new int[s.ids.length];
            for (int i=0;i<s.picked.length;i++) s.picked[i] = i<pk.length() && pk.charAt(i)!='.' ? pk.charAt(i)-'0' : -1;
            s.index = Math.min(j.optInt("index"), Math.max(0, s.ids.length-1));
            s.sanitize(bank);
            if (s.notice != null && !p.contains("state_legacy_backup")) p.edit().putString("state_legacy_backup", raw).apply();
        } catch (Exception ignore) {
            if (!p.contains("state_legacy_backup")) p.edit().putString("state_legacy_backup", raw).apply();
            s.notice = "مقدرناش نقرا التقدم المحفوظ، فبدأنا من جديد. بياناتك القديمة محفوظة زي ما هي وتقدر تصدّرها من الإعدادات.";
        }
        return s;
    }

    /** Saved state from another build (ids, favorites, notebook or bank that do not exist here)
     *  is NOT converted: the old id order is not available, so nothing can be mapped safely.
     *  The untouched old state is kept (state_legacy_backup, exportable from Settings),
     *  this build starts fresh with the readable settings, and a notice says so. */
    void sanitize(Bank bank) {
        int total = bank.total();
        // Picked values are only '.' (-1) or '0'..'4'; anything else is corrupt data — reset just those answers.
        for (int i = 0; i < picked.length; i++) if (picked[i] < -1 || picked[i] > 4) picked[i] = -1;
        if (!run.equals("all") && !run.equals("notebook") && !run.equals("flagged")) run = "all";
        // A notebook/flagged run of THIS build stores the run kind in bankKey;
        // accept it and point bankKey back at the real setup bank.
        if ((bankKey.equals("notebook") || bankKey.equals("flagged")) && bankKey.equals(run)) {
            bankKey = bank.has(setupSection) ? setupSection : bank.defaultBank().id;
        }
        boolean bad = !bank.has(bankKey) || !bank.has(setupSection);
        for (int id : ids) if (id < 0 || id >= total) bad = true;
        for (int g : favorites) if (g < 0 || g >= total) bad = true;
        for (int g : wrong) if (g < 0 || g >= total) bad = true;
        if (ids.length == 0) bad = true;
        if (!bad) { index = Math.min(Math.max(index, 0), ids.length - 1); return; }
        ids = bank.defaultBank().ids.clone(); picked = new int[ids.length]; java.util.Arrays.fill(picked, -1);
        index = 0; started = false; submitted = false; elapsed = 0; resultElapsed = -1; shuffled = false;
        favorites.clear(); wrong.clear(); history = new JSONArray();
        bankKey = bank.defaultBank().id; setupSection = bank.defaultBank().id; run = "all";
        notice = "لقينا تقدم محفوظ من نسخة تانية مش بنحوّله، لأن ترتيب الأسئلة القديم مش متاح هنا، فبدأنا من جديد. بياناتك القديمة محفوظة زي ما هي وتقدر تصدّرها من الإعدادات.";
    }

    /** The untouched saved state set aside by sanitize(), or null. */
    public static String legacyRaw(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString("state_legacy_backup", null);
    }

    /** Copy the saved state aside before a backup restore replaces it. */
    public static void keepBeforeRestore(Context c) {
        SharedPreferences p = c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String raw = p.getString("state", null);
        if (raw != null) p.edit().putString("state_before_restore", raw).apply();
    }

    public void save(Context c) {
        try {
            JSONObject j = new JSONObject();
            JSONObject st = new JSONObject();
            st.put("dark",dark); st.put("spacious",spacious); st.put("contrast",contrast); st.put("calm",calm);
            st.put("steps",steps); st.put("letters",letters); st.put("confirm",confirm); st.put("autoNext",autoNext);
            st.put("retry",retry); st.put("hints",hints); st.put("hintDefault",hintDefault); st.put("translation",translation);
            st.put("autoExplain",autoExplain); st.put("timer",timer); st.put("quizOnly",quizOnly); st.put("countdown",countdown);
            st.put("sounds",sounds); st.put("fx",fx); st.put("volume",volume); st.put("fontPercent",fontPercent); st.put("minutes",minutes);
            st.put("shuffleBanks", new JSONArray(shuffleBanks));
            j.put("shuffled",shuffled);
            j.put("settings",st);
            j.put("elapsed",elapsed); j.put("paused",paused); j.put("started",started); j.put("submitted",submitted);
            j.put("resultElapsed",resultElapsed); j.put("mode",mode); j.put("tab",tab);
            j.put("setupSection",setupSection); j.put("setupMode",setupMode);
            j.put("run",run); j.put("bankKey",bankKey);
            j.put("favorites", new JSONArray(favorites)); j.put("wrong", new JSONArray(wrong)); j.put("history", history);
            JSONArray idsA = new JSONArray();
            for (int id : ids) idsA.put(id);
            j.put("ids", idsA);
            StringBuilder pk = new StringBuilder(picked.length);
            for (int v : picked) pk.append(v<0?'.':(char)('0'+v));
            j.put("picked", pk.toString());
            j.put("index", index);
            c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString("state", j.toString()).apply();
            saveFailed = false;
        } catch (Exception ignore) {
            saveFailed = true; // remember the failure instead of claiming the save worked
        }
    }

    /** True when a pre-restore copy exists (see keepBeforeRestore). */
    public static boolean hasBeforeRestore(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE).contains("state_before_restore");
    }

    /** Restores the copy saved by keepBeforeRestore() as the live state; returns false when there is none. */
    public static boolean undoRestore(Context c) {
        SharedPreferences p = c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String raw = p.getString("state_before_restore", null);
        if (raw == null) return false;
        p.edit().putString("state", raw).remove("state_before_restore").apply();
        return true;
    }

    // ---- derived helpers ----
    public int right(Bank.BankMeta b, List<Bank.Question> qs) {
        int n=0; for (int i=0;i<ids.length;i++) if (picked[i]==qs.get(i).c) n++; return n;
    }
    public int wrong(List<Bank.Question> qs) {
        int n=0; for (int i=0;i<ids.length;i++) if (picked[i]>=0 && picked[i]!=qs.get(i).c) n++; return n;
    }
    public int blank() { int n=0; for (int v : picked) if (v<0) n++; return n; }
    public boolean allAnswered() { return blank()==0; }
    public int remaining() { return Math.max(0, minutes*60 - elapsed); }
    public boolean expired() { return countdown && remaining()==0; }
}
