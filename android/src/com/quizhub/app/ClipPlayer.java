package com.quizhub.app;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

/** Plays one bundled audio clip at a time from assets/audio. */
public final class ClipPlayer {
    private static MediaPlayer mp;
    private static String playing;

    private static java.util.Map<String, String> alias;

    /** Identical clips are stored once; clip_map.json maps a duplicate file name to the stored one. */
    private static String resolve(Context c, String name) {
        if (alias == null) {
            java.util.HashMap<String, String> m = new java.util.HashMap<>();
            try {
                java.io.InputStream in = c.getAssets().open("clip_map.json");
                java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
                byte[] buf = new byte[4096]; int n;
                while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
                in.close();
                org.json.JSONObject o = new org.json.JSONObject(bo.toString("UTF-8"));
                java.util.Iterator<String> it = o.keys();
                while (it.hasNext()) { String k = it.next(); m.put(k, o.getString(k)); }
            } catch (Exception e) { /* no alias file: names map to themselves */ }
            alias = m;
        }
        String r = alias.get(name);
        return r != null ? r : name;
    }

    /** Test hook only: lets a harness without real asset file descriptors show the audio rows. */
    static boolean existsOverride = false;
    /** Test hook only: pretend playback started (no MediaPlayer) so the harness can check button states. */
    static boolean fakePlay = false;

    private static java.util.Set<String> listed;
    public static String lastError = "";
    public static int fdFallbacks = 0;

    /** Shows a plain-Arabic failure toast (the technical detail goes to logcat only, S4-9). */
    static void fail(Context c, String why) {
        lastError = why;
        android.util.Log.e("QuizHub", "audio failed: " + why + " fd-fallbacks=" + fdFallbacks);
        try {
            android.widget.Toast.makeText(c.getApplicationContext(), "تعذّر تشغيل الصوت، جرّب تاني.",
                android.widget.Toast.LENGTH_LONG).show();
        } catch (Exception ignore) {}
    }

    private static android.media.AudioManager.OnAudioFocusChangeListener focusListener = focusChange -> {
        // a call or notification takes focus: stop the explanation instead of talking over it
        if (focusChange == android.media.AudioManager.AUDIOFOCUS_LOSS
                || focusChange == android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) stop();
    };
    private static android.media.AudioManager lastAm;
    private static boolean requestFocus(Context c) {
        try {
            android.media.AudioManager am = (android.media.AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
            if (am == null) return true;
            lastAm = am;
            int r = am.requestAudioFocus(focusListener, android.media.AudioManager.STREAM_MUSIC,
                    android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
            return r == android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        } catch (Exception e) { return true; }
    }

    public static boolean exists(Context c, String name) {
        if (existsOverride) return true;
        String r = resolve(c, name);
        if (listed == null) {
            java.util.HashSet<String> h = new java.util.HashSet<>();
            try { String[] l = c.getAssets().list("audio"); if (l != null) for (String x : l) h.add(x); } catch (Exception ignore) {}
            listed = h;
        }
        if (listed.contains(r)) return true;
        try { AssetFileDescriptor f = c.getAssets().openFd("audio/" + r); f.close(); return true; }
        catch (Exception e) { return false; }
    }

    /** Opens the clip as a data source: direct asset descriptor first, else a cached copy (works even if the asset were compressed). */
    private static void load(Context c, MediaPlayer m, String name) throws Exception {
        String r = resolve(c, name);
        try {
            AssetFileDescriptor f = c.getAssets().openFd("audio/" + r);
            m.setDataSource(f.getFileDescriptor(), f.getStartOffset(), f.getLength());
            f.close();
            return;
        } catch (Exception first) { fdFallbacks++; }
        java.io.File out = new java.io.File(c.getCacheDir(), r);
        if (!out.exists() || out.length() == 0) {
            java.io.InputStream in = c.getAssets().open("audio/" + r);
            java.io.FileOutputStream fo = new java.io.FileOutputStream(out);
            byte[] buf = new byte[16384]; int n;
            while ((n = in.read(buf)) > 0) fo.write(buf, 0, n);
            fo.close(); in.close();
        }
        m.setDataSource(out.getAbsolutePath());
    }

    public static boolean isPlaying(String name) {
        return (mp != null || fakePlay) && name.equals(playing);
    }

    /** Toggle: starts the clip, or stops it if it is already playing. Returns true if now playing.
     *  volume is 0-100 and now drives the explanation clip too (S2-2). Preparation is async
     *  (S2-4) so a slow clip never freezes the UI thread. */
    public static boolean toggle(Context c, String name, int volume, Runnable onDone) {
        lastError = "";
        if (isPlaying(name)) { stop(); return false; }
        stop();
        if (fakePlay) { playing = name; return true; }
        if (!requestFocus(c)) { fail(c, "audio focus denied"); return false; }
        try {
            mp = new MediaPlayer();
            mp.setAudioAttributes(new android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA).build());
            float v = Math.max(0f, Math.min(1f, volume / 100f));
            mp.setVolume(v, v);
            load(c, mp, name);
            mp.setOnCompletionListener(m -> { stop(); if (onDone != null) onDone.run(); });
            mp.setOnErrorListener((m, w, x) -> { stop(); fail(c, "MediaPlayer error what=" + w + " extra=" + x + " [" + name + "]"); if (onDone != null) onDone.run(); return true; });
            mp.setOnPreparedListener(m -> m.start());
            mp.prepareAsync();
            playing = name;
            return true;
        } catch (Exception e) { stop(); fail(c, e.getClass().getSimpleName() + ": " + e.getMessage() + " [" + name + "]"); return false; }
    }

    public static void stop() {
        if (mp != null) {
            try { mp.stop(); } catch (Exception ignore) {}
            try { mp.release(); } catch (Exception ignore) {}
        }
        mp = null; playing = null;
        if (lastAm != null) {
            try { lastAm.abandonAudioFocus(focusListener); } catch (Exception ignore) {}
        }
    }

    /** Short feedback sound (study mode only; callers enforce). volume 0-100. */
    public static void sfx(Context c, boolean correct, int volume) {
        if (volume <= 0) return;
        try {
            AssetFileDescriptor f = c.getAssets().openFd("sfx/" + (correct ? "correct" : "wrong") + ".mp3");
            final MediaPlayer m = new MediaPlayer();
            m.setDataSource(f.getFileDescriptor(), f.getStartOffset(), f.getLength());
            f.close();
            float v = Math.max(0f, Math.min(1f, volume / 100f));
            m.setVolume(v, v);
            m.setOnCompletionListener(x -> x.release());
            m.setOnErrorListener((x, w, e) -> { x.release(); return true; });
            m.setOnPreparedListener(x -> x.start());
            m.prepareAsync(); // S2-4: never prepare on the main thread
        } catch (Exception e) { lastError = "sfx: " + e.getMessage(); }
    }
}
