package com.quizhub.app;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/** Question bank loaded from assets/bank (byte-preserved from gen18 bank-data.ts).
 *  Global question ids are explicit per bank (same ids arrays the web app used). */
public final class Bank {

    public static final class Question {
        public String q, tr, hint, ex, why, src, url, st;
        public String[] o;
        public int c;
        public int gid; // global id (index into the 3122-question array)
    }

    public static final class BankMeta {
        public String id, name, title, group;
        public int minutes, count;
        public int[] ids;          // global ids, in session order
        public List<Question> questions; // lazy, parallel to ids
    }

    public final List<BankMeta> banks = new ArrayList<>();
    public final HashSet<Integer> missingImageIds = new HashSet<>();
    private final Context ctx;

    public Bank(Context c) throws Exception {
        this.ctx = c.getApplicationContext();
        JSONObject idx = new JSONObject(readAsset("bank/index.json"));
        JSONArray arr = idx.getJSONArray("banks");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject b = arr.getJSONObject(i);
            BankMeta m = new BankMeta();
            m.id = b.getString("id");
            m.name = b.getString("name");
            m.title = b.optString("title", "");
            m.group = b.getString("group");
            m.minutes = b.optInt("minutes", 0);
            m.count = b.optInt("count", 0);
            JSONArray idsA = b.getJSONArray("ids");
            m.ids = new int[idsA.length()];
            for (int k = 0; k < idsA.length(); k++) m.ids[k] = idsA.getInt(k);
            banks.add(m);
        }
        JSONArray miss = idx.optJSONArray("missingImage");
        if (miss != null) for (int i = 0; i < miss.length(); i++) missingImageIds.add(miss.getInt(i));
    }

    public BankMeta byId(String id) {
        for (BankMeta m : banks) if (m.id.equals(id)) return m;
        return banks.get(0);
    }

    /** Load (and cache) the full question list of one bank, in session order. */
    public synchronized List<Question> questions(BankMeta m) throws Exception {
        if (m.questions != null) return m.questions;
        JSONObject f = new JSONObject(readAsset("bank/bank-" + m.id + ".json"));
        JSONArray arr = f.getJSONArray("questions");
        List<Question> qs = new ArrayList<>(arr.length());
        for (int i = 0; i < arr.length(); i++) {
            JSONObject j = arr.getJSONObject(i);
            Question q = new Question();
            q.gid = m.ids[i];
            q.q = j.getString("q");
            JSONArray os = j.getJSONArray("o");
            q.o = new String[os.length()];
            for (int k = 0; k < os.length(); k++) q.o[k] = os.getString(k);
            q.c = j.getInt("c");
            q.tr = opt(j, "tr"); q.hint = opt(j, "hint"); q.ex = opt(j, "ex");
            q.why = opt(j, "why"); q.src = opt(j, "src"); q.url = opt(j, "url");
            q.st = j.optString("st", "");
            qs.add(q);
        }
        m.questions = qs;
        return qs;
    }

    private static String opt(JSONObject j, String k) {
        String v = j.optString(k, null);
        return (v == null || v.isEmpty() || "null".equals(v)) ? null : v;
    }

    /** Questions for global ids (tests banks only), in the order given. */
    public synchronized List<Question> forIds(int[] gids) {
        java.util.HashMap<Integer,Question> map = new java.util.HashMap<>();
        try { for (BankMeta m : banks) for (Question q : questions(m)) map.put(q.gid, q); }
        catch (Exception e) { throw new RuntimeException(e); }
        List<Question> out = new ArrayList<>();
        for (int g : gids) out.add(map.get(g));
        return out;
    }

    public boolean missingImage(int gid) { return missingImageIds.contains(gid); }

    private String readAsset(String path) throws Exception {
        InputStream in = ctx.getAssets().open(path);
        BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) sb.append(line).append('\n');
        r.close();
        return sb.toString();
    }
}
