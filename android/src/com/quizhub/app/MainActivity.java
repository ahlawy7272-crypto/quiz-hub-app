package com.quizhub.app;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;

public class MainActivity extends Activity {
    Theme theme = new Theme();
    Bank bank;
    Session s;

    FrameLayout root;
    LinearLayout screen;      // the rounded card column: header / scroll / tabbar
    LinearLayout header;
    ScrollView scroll;
    LinearLayout content;     // inside scroll
    FrameLayout tabbarWrap;
    TabBarBgView tabbarBg;
    View[] tabBtns = new View[3];
    ImageView[] tabIcons = new ImageView[3];
    TextView[] tabLabels = new TextView[3];
    LinearLayout timerChip;
    TextView timerChipLabel;
    TextView timerChipTime;

    Handler handler = new Handler();
    Runnable ticker;
    Runnable autoNextRun;
    boolean tickerScheduled = false;
    boolean bankLoadFailed = false;

    /** Clear Arabic error view for unreadable question files; marks the failure so no quiz is rendered over it. */
    void showBankError(Exception e) {
        bankLoadFailed = true;
        android.util.Log.e("QuizHub", "bank load failed", e);
        android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText("ملفات الأسئلة جوه التطبيق مش سليمة، فمقدرش أفتحها. نزّل نسخة التطبيق تاني وثبّتها فوق الموجودة. تقدمك محفوظ.");
        tv.setPadding(48, 96, 48, 48); tv.setTextSize(16); tv.setTextDirection(View.TEXT_DIRECTION_RTL);
        setContentView(tv);
    }

    String review = "all";        // all | wrong
    String confirmAction = null;  // finish | start | null
    Boolean setupOpen = null;     // null = auto (!started), like web <details open={!started}>
    boolean trOpen=false, hintOpen=false;
    Integer pending = null;
    /** S2-1: a pending (unconfirmed) choice kept per question position, so navigating away and back does not lose it. */
    java.util.HashMap<Integer,Integer> pendingByQuestion = new java.util.HashMap<>();
    /** S3-2: only scroll to top when the view identity actually changed (tab/question/review/confirm), not on every settings toggle. */
    String lastScrollSig = "";
    /** S3-10: info panels collapsed state (exam panel open by default). */
    boolean infoExamOpen = true;
    /** S4-5: current search text in the in-page bank picker. */
    String bankQuery = "";

    List<Bank.Question> qs;       // current session bank questions (session order)
    Bank.BankMeta currentBank;

    static final String[] TAB_NAMES = {"الرئيسية","الأسئلة","الإعدادات"};
    static final int[] TAB_ICONS = {R.drawable.ic_home, R.drawable.ic_list, R.drawable.ic_gear};
    static final float[] NOTCH = {0.8333f, 0.5f, 0.1666f}; // home, quiz, settings (RTL layout)

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        theme.loadFonts(this);
        try { bank = new Bank(this); } catch (Exception e) {
            showBankError(e);
            return;
        }
        s = Session.load(this, bank);
        restoreTransient(b);
        theme.dark = s.dark;
        theme.scale = s.fontPercent / 100f;

        applyFrame();

        root = new FrameLayout(this);
        root.setBackgroundColor(theme.dark ? Theme.D_BG : 0xffffffff);
        setContentView(root);
        buildScreen();
        renderAll();
        startTicker();
        if (s.notice != null) { android.widget.Toast.makeText(this, s.notice, android.widget.Toast.LENGTH_LONG).show(); s.notice = null; save(); }
    }

    /** S3-3: rotation / process recreation keeps transient UI state that is not in Session (pending answers, open panels/dialogs). */
    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putInt("pending", pending == null ? -1 : pending);
        int n = pendingByQuestion.size();
        int[] pk = new int[n], pv = new int[n]; int i = 0;
        for (java.util.Map.Entry<Integer,Integer> e : pendingByQuestion.entrySet()) { pk[i] = e.getKey(); pv[i] = e.getValue(); i++; }
        out.putIntArray("pendingKeys", pk);
        out.putIntArray("pendingVals", pv);
        out.putBoolean("trOpen", trOpen);
        out.putBoolean("hintOpen", hintOpen);
        out.putString("review", review);
        out.putString("confirmAction", confirmAction);
        out.putBoolean("setupOpenSet", setupOpen != null);
        out.putBoolean("setupOpen", setupOpen != null && setupOpen);
        out.putBoolean("infoExamOpen", infoExamOpen);
        out.putString("pendingRunKind", pendingRunKind);
        out.putIntArray("pendingRunIds", pendingRunIds);
        out.putString("bankQuery", bankQuery);
    }

    void restoreTransient(Bundle b) {
        if (b == null) return;
        int p = b.getInt("pending", -1);
        pending = p < 0 ? null : p;
        int[] pk = b.getIntArray("pendingKeys"), pv = b.getIntArray("pendingVals");
        if (pk != null && pv != null && pk.length == pv.length)
            for (int i = 0; i < pk.length; i++) pendingByQuestion.put(pk[i], pv[i]);
        trOpen = b.getBoolean("trOpen", trOpen);
        hintOpen = b.getBoolean("hintOpen", hintOpen);
        review = b.getString("review", review);
        confirmAction = b.getString("confirmAction");
        if (b.getBoolean("setupOpenSet", false)) setupOpen = b.getBoolean("setupOpen", true);
        infoExamOpen = b.getBoolean("infoExamOpen", true);
        pendingRunKind = b.getString("pendingRunKind");
        pendingRunIds = b.getIntArray("pendingRunIds");
        bankQuery = b.getString("bankQuery", "");
    }
    void applyFrame() {
        if (theme.dark) {
            getWindow().setStatusBarColor(0xff092f2c);
            getWindow().setNavigationBarColor(0xff092f2c);
            getWindow().getDecorView().setSystemUiVisibility(0);
        } else {
            getWindow().setStatusBarColor(0xffffffff);
            getWindow().setNavigationBarColor(0xffffffff);
            int f = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (android.os.Build.VERSION.SDK_INT >= 26) f |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            getWindow().getDecorView().setSystemUiVisibility(f);
        }
    }

    // ---------------- shell ----------------
    void buildScreen() {
        root.removeAllViews();
        screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        int m = Ui.dp(this, 8);
        FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(-1, -1);
        sp.setMargins(m, m, m, m);
        screen.setLayoutParams(sp);
        screen.setBackground(Ui.round(theme.bg(), Ui.dp(this, 28)));
        root.addView(screen);

        // header (48dp compact)
        header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(Ui.dp(this,16), 0, Ui.dp(this,16), 0);
        screen.addView(header, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));

        // scroll area
        scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(Ui.dp(this,14), Ui.dp(this,4), Ui.dp(this,14), Ui.dp(this,28));
        scroll.addView(content);
        LinearLayout.LayoutParams scr = new LinearLayout.LayoutParams(-1, 0, 1f);
        screen.addView(scroll, scr);

        // tabbar 60dp with raised icons
        tabbarWrap = new FrameLayout(this);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 76));
        tp.setMargins(Ui.dp(this,14), Ui.dp(this,2), Ui.dp(this,14), Ui.dp(this,8));
        screen.addView(tabbarWrap, tp);
        tabbarWrap.setClipChildren(false);
        tabbarWrap.setClipToPadding(false);
        tabbarBg = new TabBarBgView(this, theme);
        tabbarWrap.addView(tabbarBg, new FrameLayout.LayoutParams(-1, Ui.dp(this,60), Gravity.BOTTOM));
        LinearLayout tabs = new LinearLayout(this);
        tabs.setClipChildren(false);
        tabs.setClipToPadding(false);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(-1, Ui.dp(this,60), Gravity.BOTTOM);
        tabbarWrap.addView(tabs, tlp);
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            FrameLayout cell = new FrameLayout(this);
            cell.setClipChildren(false);
            cell.setClipToPadding(false);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, -1, 1f);
            cell.setLayoutParams(cp);
            ImageView icon = new ImageView(this);
            icon.setImageResource(TAB_ICONS[i]);
            FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams(Ui.dp(this,40), Ui.dp(this,40), Gravity.TOP|Gravity.CENTER_HORIZONTAL);
            ip.topMargin = Ui.dp(this,10);
            icon.setLayoutParams(ip);
            icon.setPadding(Ui.dp(this,9),Ui.dp(this,9),Ui.dp(this,9),Ui.dp(this,9));
            TextView label = Ui.tv(this, TAB_NAMES[i], 12, theme.disp(), theme.tabText());
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
            lp.bottomMargin = Ui.dp(this,7);
            label.setLayoutParams(lp);
            cell.addView(icon); cell.addView(label);
            cell.setOnClickListener(v -> onTab(idx));
            tabs.addView(cell);
            tabBtns[i]=cell; tabIcons[i]=icon; tabLabels[i]=label;
        }
    }

    void onTab(int i) {
        confirmAction = null;
        if (i == 0) { goToList(); }
        else if (i == 1) { openReview("all"); }
        else { s.tab = "settings"; save(); renderAll(); }
    }

    void refreshTabbar() {
        int active = s.tab.equals("home") ? 0 : s.tab.equals("quiz") ? 1 : 2;
        tabbarBg.setNotch(NOTCH[active]);
        for (int i = 0; i < 3; i++) {
            boolean on = i == active;
            tabIcons[i].setTranslationY(on ? -Ui.dp(this, 26) : 0);
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            if (on) {
                circle.setColor(theme.dark ? Theme.D_SOFT : Theme.L_TEAL);
                circle.setStroke(Ui.dp(this,4), theme.bg());
            } else {
                circle.setColor(Color.TRANSPARENT);
            }
            tabIcons[i].setBackground(circle);
            tabIcons[i].setColorFilter(on ? (theme.dark ? Theme.D_BG : Theme.L_BG) : theme.tabText());
            tabLabels[i].setTextColor(on ? theme.ink() : theme.tabText());
        }
    }

    void refreshHeader() {
        header.removeAllViews();
        boolean solving = s.tab.equals("quiz") && s.started;
        // Round 4: an explicit "‹ القائمة" back control on the quiz screen — the
        // only way back used to be the small bottom tab or the system Back key.
        if (solving) {
            android.widget.Button back = new android.widget.Button(this);
            back.setText("‹ القائمة");
            back.setAllCaps(false);
            back.setTextSize(13);
            back.setTypeface(theme.disp());
            back.setMinHeight(0); back.setMinimumHeight(0);
            back.setPadding(Ui.dp(this, 12), Ui.dp(this, 6), Ui.dp(this, 12), Ui.dp(this, 6));
            back.setStateListAnimator(null);
            back.setBackground(Ui.roundStroke(Color.TRANSPARENT, Ui.dp(this, 999), theme.border3(), Ui.dp(this, 1.5f)));
            back.setTextColor(theme.ink());
            back.setContentDescription("رجوع لقائمة الامتحانات");
            back.setOnClickListener(v -> goToList());
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-2, -2);
            bp.setMarginEnd(Ui.dp(this, 8));
            header.addView(back, bp);
        }
        // brand: mark + name (+ purpose when not compact), LTR
        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.HORIZONTAL);
        brand.setGravity(Gravity.CENTER_VERTICAL);
        brand.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        ImageView mark = new ImageView(this);
        try {
            mark.setImageBitmap(android.graphics.BitmapFactory.decodeStream(getAssets().open(
                theme.dark ? "branding/quiz-hub-mark-lime.png" : "branding/quiz-hub-mark.png")));
        } catch (Exception ignore) {}
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
            solving ? Ui.dp(this,27) : Ui.dp(this,34), solving ? Ui.dp(this,24) : Ui.dp(this,28));
        brand.addView(mark, mp);
        TextView name = Ui.tv(this, "Quiz Hub", 18, theme.dispB(), theme.ink());
        name.setPadding(Ui.dp(this,7),0,0,0);
        brand.addView(name);
        if (!solving) {
            TextView purpose = Ui.tv(this, "أسئلة مزاولة المهنة", 12, theme.bodyB(), theme.ink());
            purpose.setGravity(Gravity.CENTER);
            purpose.setPadding(Ui.dp(this,8),0,0,0);
            LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(Ui.dp(this,110), -1);
            pp.setMargins(Ui.dp(this,8),0,0,0);
            purpose.setLayoutParams(pp);
            brand.addView(purpose);
        }
        if (solving) brand.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        header.addView(brand, new LinearLayout.LayoutParams(0, -1, 1f));
        if (solving && s.timer) {
            View tc = makeTimerChip();
            if (timerChipLabel != null) timerChipLabel.setVisibility(View.GONE); // web: .qz-is-solving .qz-timer-label {display:none}
            LinearLayout.LayoutParams tcp = new LinearLayout.LayoutParams(-2, -2);
            tcp.setMarginEnd(Ui.dp(this, 8));
            header.addView(tc, tcp);
        }
    }

    View makeTimerChip() {
        boolean cd = s.countdown;
        int val = cd ? s.remaining() : s.elapsed;
        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setGravity(Gravity.CENTER);
        chip.setBackground(Ui.roundStroke(theme.card55(), Ui.dp(this,999), theme.border3(), Ui.dp(this,1.5f)));
        chip.setPadding(Ui.dp(this,10), Ui.dp(this,2), Ui.dp(this,10), Ui.dp(this,2));
        timerChipLabel = Ui.tv(this, cd?"باقي":"الوقت", 11, theme.disp(), theme.ink());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMargins(Ui.dp(this,5), 0, 0, 0); // gap between label and time
        chip.addView(timerChipLabel, lp); // RTL row: label rightmost, time to its left
        timerChipTime = Ui.timeView(this, val, 11, theme.disp(), theme.ink());
        chip.addView(timerChipTime);
        timerChip = chip;
        return chip;
    }

    // ---------------- timer ----------------
    // The ticker only runs while a quiz is being solved. When it is not running
    // it stops re-posting (no idle ticking); startTicker() restarts it on resume/begin.
    void startTicker() {
        if (tickerScheduled || s == null) return;
        tickerScheduled = true;
        ticker = new Runnable() {
            @Override public void run() {
                if (bankLoadFailed) { tickerScheduled = false; return; }
                boolean running = s.started && !s.submitted && !(s.allAnswered() && s.mode.equals("study"))
                        && !s.paused && !s.expired() && !(s.quizOnly && !s.tab.equals("quiz"));
                if (!running) { tickerScheduled = false; return; }
                s.elapsed++;
                if (s.elapsed % 10 == 0) save(); // onPause saves too; was every second
                if (timerChip != null && s.timer) {
                    int val = s.countdown ? s.remaining() : s.elapsed;
                    timerChipLabel.setText(s.countdown?"باقي":"الوقت");
                    timerChipTime.setText(Ui.lre(Ui.formatTime(val)));
                }
                if (s.expired()) renderAll(); // lock answers the moment it hits zero
                handler.postDelayed(ticker, 1000);
            }
        };
        handler.postDelayed(ticker, 1000);
    }

    // ---------------- actions (ports of App.tsx) ----------------
    boolean celebrate = false;
    String pendingRunKind = null; int[] pendingRunIds = null;
    void ensureQs() {
        if (qs != null || bankLoadFailed) return;
        // S1-2: a corrupt bank file must show the error view, not crash out of the render.
        if (!s.run.equals("all")) { try { qs = bank.forIds(s.ids); } catch (Exception e) { showBankError(e); } }
        else loadBank(sessionBank());
    }
    void startRun(String kind, int[] ids) {
        if (ids.length == 0) return;
        pendingRunKind = kind; pendingRunIds = ids;
        if (s.started) { confirmAction = "start"; renderAll(); } else begin();
    }
    String runLabel(String key) {
        if (key.equals("notebook")) return "دفتر الغلطات";
        if (key.equals("flagged")) return "الأسئلة المعلّمة";
        Bank.BankMeta b = bank.byId(key);
        return b != null ? b.name : "بنك غير معروف (" + key + ")";
    }
    void recordSubmit() {
        int right = 0; java.util.ArrayList<Integer> wr = new java.util.ArrayList<>(), ok = new java.util.ArrayList<>();
        for (int i = 0; i < s.ids.length; i++) {
            if (s.picked[i] < 0) continue;
            if (s.picked[i] == qs.get(i).c) { right++; ok.add(s.ids[i]); } else wr.add(s.ids[i]);
        }
        celebrate = s.ids.length > 0 && right / (float) s.ids.length >= 0.8f; confettiDone = false;
        s.wrong.addAll(wr);
        if (s.run.equals("notebook")) s.wrong.removeAll(ok);
        try {
            org.json.JSONObject e = new org.json.JSONObject();
            e.put("t", System.currentTimeMillis());
            e.put("b", s.run.equals("all") ? s.bankKey : s.run);
            e.put("m", s.mode); e.put("r", right); e.put("n", s.ids.length); e.put("s", s.elapsed);
            s.history.put(e);
            while (s.history.length() > 60) s.history.remove(0);
        } catch (Exception ignore) {}
    }
    void begin() {
        Bank.BankMeta b = bank.byId(s.setupSection);
        if (b == null) {
            android.widget.Toast.makeText(this, "البنك المحفوظ مش موجود في النسخة دي، فبدأنا من أول بنك.", android.widget.Toast.LENGTH_LONG).show();
            b = bank.banks.get(0);
            s.setupSection = b.id;
        }
        if (pendingRunKind != null) {
            // S1-1: bankKey stays a real bank id (the setup bank); the run kind lives in run only.
            // Storing "notebook"/"flagged" in bankKey used to make sanitize() wipe the session on next launch.
            s.run = pendingRunKind; s.bankKey = b.id; s.ids = pendingRunIds.clone();
            pendingRunKind = null; pendingRunIds = null;
            try { qs = bank.forIds(s.ids); } catch (Exception e) { showBankError(e); return; }
        } else { s.run = "all"; s.bankKey = b.id; s.ids = b.ids.clone(); qs = null; }
        s.picked = new int[s.ids.length];
        java.util.Arrays.fill(s.picked, -1);
        s.index = 0; s.elapsed = 0; s.paused = false; s.started = true; s.submitted = false;
        s.resultElapsed = -1; s.mode = s.setupMode; s.tab = "quiz";
        review = "all"; celebrate = false; confirmAction = null; setupOpen = null;
        pending = null; pendingByQuestion.clear();
        if (qs == null) loadBank(b);
        save(); renderAll(); startTicker();
    }
    /** Phase 1: always ask before finishing — even with no blank questions — so a stray tap cannot end the attempt. */
    void finishQuiz() {
        confirmAction = "finish"; renderAll();
    }
    void doFinishConfirmed() {
        recordSubmit(); s.submitted = true; s.resultElapsed = s.elapsed; s.tab = "home"; review = "all"; confirmAction = null;
        save(); renderAll();
    }
    void reset() {
        review = "all"; celebrate = false;
        pending = null; pendingByQuestion.clear();
        s.started = false; s.submitted = false; s.resultElapsed = -1;
        s.picked = new int[s.ids.length]; java.util.Arrays.fill(s.picked, -1);
        s.elapsed = 0; s.index = 0; s.paused = false; s.tab = "home";
        setupOpen = null;
        save(); renderAll();
    }
    /** S3-1: leaving the result screen must not wipe it; a new attempt only starts when the user explicitly begins one (setup "ابدأ تجربة" -> begin()). */
    void goHome() {
        confirmAction = null;
        s.tab = "home"; save(); renderAll();
    }
    /** Round 4: tapping an exam card starts that exam in the chosen mode — or
     *  resumes it when it is the live session. A live session for another exam
     *  asks for confirmation first (confirmBox -> begin()). */
    void onExamCard(Bank.BankMeta m) {
        if (s.started && !s.submitted && s.run.equals("all") && m.id.equals(s.bankKey)) { openReview("all"); return; }
        s.setupSection = m.id; s.minutes = m.minutes; save();
        if (s.started) { confirmAction = "start"; renderAll(); } else begin();
    }
    /** Round 4: back to the exam list from a quiz — pauses the exam so the clock
     *  stops (going home used to leave it ticking), and opens the exam list
     *  immediately instead of leaving it collapsed behind "تجربة جديدة". */
    void goToList() {
        if (s.started && !s.submitted) s.paused = true;
        setupOpen = true;
        goHome();
    }    /** S3-6: restore the progress that was live before the last backup restore. */
    void undoLastRestore() {
        if (!Session.undoRestore(this)) { android.widget.Toast.makeText(this, "مفيش استرجاع سابق للتراجع عنه.", android.widget.Toast.LENGTH_SHORT).show(); return; }
        s = Session.load(this, bank);
        theme.dark = s.dark; theme.scale = s.fontPercent / 100f;
        qs = null; review = "all"; confirmAction = null; pending = null; pendingByQuestion.clear();
        buildScreen(); renderAll();
        android.widget.Toast.makeText(this, "رجعنا التقدم اللي قبل الاسترجاع.", android.widget.Toast.LENGTH_LONG).show();
    }
    void openReview(String filter) {
        review = filter;
        // Round 4: "كمّل الحل" means resume — unpause a live session, or the quiz
        // would reopen with a stopped clock and no visible way to restart it.
        if (s.started && !s.submitted) s.paused = false;
        if (filter.equals("wrong")) {
            // ensure index points at a wrong answer
            if (!(s.picked[s.index] >= 0 && s.picked[s.index] != qs.get(s.index).c)) {
                for (int i = 0; i < s.ids.length; i++)
                    if (s.picked[i] >= 0 && s.picked[i] != qs.get(i).c) { s.index = i; break; }
            }
        }
        s.tab = "quiz"; save(); renderAll();
    }
    void pick(int v) {
        if (s.expired() || s.submitted || !s.started) return;
        Bank.Question q = qs.get(s.index);
        int chosen = s.picked[s.index];
        if (s.mode.equals("study") && chosen >= 0 && !(s.retry && chosen != q.c)) return;
        s.picked[s.index] = v;
        // web parity: in study mode the mistakes notebook updates right at the answer, not only at submit
        if (s.mode.equals("study")) { if (v == q.c) { if (s.run.equals("notebook")) s.wrong.remove(q.gid); } else s.wrong.add(q.gid); }
        save();
        if (s.sounds && s.mode.equals("study")) ClipPlayer.sfx(this, v == q.c, s.volume);
        // auto-advance after 2s
        if (autoNextRun != null) handler.removeCallbacks(autoNextRun);
        if (s.autoNext && !(s.mode.equals("study") && s.autoExplain) && !s.submitted
                && s.tab.equals("quiz") && s.index < s.ids.length - 1) {
            autoNextRun = () -> { s.index++; trReset(); save(); renderAll(); };
            handler.postDelayed(autoNextRun, 2000);
        }
        popOnce = true;
        renderAll();
        if (s.mode.equals("study") && v == q.c) Fx.confetti(this, false);
    }
    boolean popOnce = false, confettiDone = false; String lastSig = "";
    /** commit = pick + optional auto-explain (port of web commit) */
    void commit(int v) {
        pick(v);
        pending = null;
        pendingByQuestion.remove(s.index);
        if (s.autoExplain && s.mode.equals("study") && !s.submitted) Screens.showExplain(this);
    }

    void trReset() {
        trOpen = s.translation; hintOpen = s.hintDefault;
        // S2-1: a pending (unconfirmed) choice is kept per question instead of being thrown away on every navigation.
        pending = pendingByQuestion.get(s.index);
    }

    void loadBank(Bank.BankMeta b) {
        currentBank = b;
        try { qs = bank.questions(b); } catch (Exception e) { showBankError(e); }
    }
    Bank.BankMeta sessionBank() {
        // find the bank whose ids match the session's first id
        if (s.ids.length > 0) {
            for (Bank.BankMeta m : bank.banks)
                if (m.ids.length == s.ids.length && m.ids[0] == s.ids[0]) return m;
        }
        Bank.BankMeta b = bank.byId(s.setupSection);
        return b != null ? b : bank.banks.get(0); // explicit default: first bank in index order
    }

    void save() { s.save(this); }

    void renderAll() {
        if (bankLoadFailed) return;
        theme.dark = s.dark; theme.scale = s.fontPercent / 100f;
        root.setBackgroundColor(theme.dark ? Theme.D_BG : 0xffffffff);
        screen.setBackground(Ui.round(theme.bg(), Ui.dp(this, 28)));
        applyFrame();
        tabbarBg.applyTheme();
        refreshHeader();
        refreshTabbar();
        // S3-2: a settings toggle re-renders the same view, so it must not jump to the top.
        String scrollSig = s.tab + "|" + s.index + "|" + review + "|" + confirmAction;
        if (!scrollSig.equals(lastScrollSig)) { lastScrollSig = scrollSig; scroll.scrollTo(0, 0); }
        content.removeAllViews();
        if (s.started) ensureQs();
        if (bankLoadFailed) return;
        if (s.tab.equals("home")) Screens.home(this, content);
        else if (s.tab.equals("quiz")) {
            if (s.started) Screens.quiz(this, content);
            else Screens.quizEmpty(this, content);
        }
        else Screens.settings(this, content);
        String sig = s.tab + "|" + s.index + "|" + s.submitted + "|" + (s.started ? 1 : 0) + "|" + review + "|" + confirmAction;
        if (!sig.equals(lastSig)) { boolean first = lastSig.isEmpty(); lastSig = sig; if (!first) Fx.rise(this, content); }
    }

    @Override public void onBackPressed() {
        // Round 4: system Back from a quiz also pauses the exam (same as "‹ القائمة").
        if (!s.tab.equals("home")) { goToList(); }
        else super.onBackPressed();
    }

    @Override protected void onDestroy() { super.onDestroy(); tickerScheduled = false; if (handler != null) { if (ticker != null) handler.removeCallbacks(ticker); if (autoNextRun != null) handler.removeCallbacks(autoNextRun); } }

    @Override protected void onPause() { super.onPause(); ClipPlayer.stop(); tickerScheduled = false; if (handler != null && ticker != null) handler.removeCallbacks(ticker); if (s != null) save(); }

    @Override protected void onResume() { super.onResume(); startTicker(); }
}
