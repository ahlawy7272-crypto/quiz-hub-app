package com.quizhub.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/** Session state mirroring the web Saved model (gen18), persisted to SharedPreferences. */
public final class Session {
    // settings
    public boolean dark=false, spacious=false, contrast=false, calm=false, steps=true, letters=true;
    public boolean confirm=false, autoNext=false, retry=false, hints=true, hintDefault=false;
    public boolean translation=false, autoExplain=false, timer=true, quizOnly=false, countdown=false;
    public int fontPercent=100, minutes=120;
    public boolean sounds=true, fx=true; public int volume=80;
    // session
    public int[] ids = new int[0];
    public int[] picked = new int[0]; // -1 = null, parallel to ids
    public int index = 0;             // position within ids (NOT global id; converted where needed)
    public int elapsed = 0;
    public boolean paused=false, started=false, submitted=false;
    public int resultElapsed = -1;
    public String mode = "study";     // study | exam
    public String tab = "home";       // home | quiz | settings
    public String setupSection = "6";
    public String setupMode = "study";
    public String run = "all";        // all | notebook | flagged
    public String bankKey = "6";      // bank id of the live session
    public java.util.TreeSet<Integer> favorites = new java.util.TreeSet<>();
    public java.util.TreeSet<Integer> wrong = new java.util.TreeSet<>();
    public JSONArray history = new JSONArray(); // [{t,b,m,r,n,s}], newest last, max 60

    private static final String PREF = "quizhub_native_v1";

    public static Session load(Context c, Bank bank) {
        Session s = new Session();
        s.minutes = bank.banks.get(0).minutes;
        s.ids = bank.banks.get(0).ids.clone();
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
            s.elapsed=j.optInt("elapsed"); s.paused=j.optBoolean("paused"); s.started=j.optBoolean("started");
            s.submitted=j.optBoolean("submitted"); s.resultElapsed=j.optInt("resultElapsed",-1);
            s.mode=j.optString("mode","study"); s.tab=j.optString("tab","home");
            s.setupSection=j.optString("setupSection","6"); s.setupMode=j.optString("setupMode","study");
            s.run=j.optString("run","all"); s.bankKey=j.optString("bankKey","6");
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
        } catch (Exception ignore) {}
        return s;
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
        } catch (Exception ignore) {}
    }

    // ---- derived helpers (port of App.tsx logic) ----
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
