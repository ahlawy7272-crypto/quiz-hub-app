package com.quizhub.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;

/**
 * Clean rebuild of the Quiz Hub shell. One design only (round 5, ivory +
 * gold + deep-teal dark mode), plain Java, no libraries. The app is Arabic
 * RTL throughout; English question/option text renders LTR; numbers and
 * times are bidi-isolated. No old bottom tab bar, no old theme switcher.
 */
public class MainActivity extends Activity {
    Theme theme = new Theme();
    Bank bank;
    Session s;

    FrameLayout root;
    LinearLayout screen;      // the rounded card column: header / scroll
    LinearLayout header;
    ScrollView scroll;
    android.widget.EditText homeSearch; // home search box, for the drawer "بحث" item to focus
    LinearLayout content;     // inside scroll
    LinearLayout timerChip;
    TextView timerChipLabel;
    TextView timerChipTime;
    ImageView timerChipIcon; // clock icon in the redesigned timer pill
    TextView stickyYear; // sticky year header overlay (home only)
    java.util.List<Integer> quizView; int quizPos; // swipe navigation reads these (set by Screens.quiz)
    boolean drawerOpen = false;
    View drawerScrim;
    LinearLayout drawerPanel;

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
    Boolean setupOpen = null;     // null = auto (!started)
    boolean trOpen=false, hintOpen=false;
    Integer pending = null;
    boolean focus = false;        // focus mode: only the question + options (transient)
    long lastOptTapAt = 0; int lastOptTapIdx = -1; // double-tap-to-confirm tracking
    java.util.HashMap<Integer,Integer> pendingByQuestion = new java.util.HashMap<>();
    String lastScrollSig = "";
    boolean infoExamOpen = true;
    String bankQuery = "";

    List<Bank.Question> qs;       // current session bank questions (session order)
    Bank.BankMeta currentBank;

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
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); // the whole app is Arabic RTL
        root.setBackgroundColor(theme.rootBg());
        setContentView(root);
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            // Edge-to-edge (target SDK 35): pad the root by the system-bar insets
            // so the margined card never hides behind the status/navigation bars.
            // The insets are passed on unconsumed; children do not re-apply them.
            try {
                root.setOnApplyWindowInsetsListener((v, insets) -> {
                    android.graphics.Insets sb = insets.getInsets(android.view.WindowInsets.Type.systemBars());
                    v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
                    return insets;
                });
            } catch (Exception ignore) {}
        }
        buildScreen();
        renderAll();
        startTicker();
        if (s.notice != null) { android.widget.Toast.makeText(this, s.notice, android.widget.Toast.LENGTH_LONG).show(); s.notice = null; save(); }
    }

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

    android.graphics.Bitmap logoCache;
    boolean logoCacheDark;
    android.graphics.Bitmap logoBitmap() {
        if (logoCache != null && logoCacheDark == theme.dark) return logoCache;
        try {
            logoCache = android.graphics.BitmapFactory.decodeStream(getAssets().open(
                theme.dark ? "branding/quiz-hub-mark-gold.png" : "branding/quiz-hub-mark.png"));
            logoCacheDark = theme.dark;
        } catch (Exception ignore) { logoCache = null; }
        return logoCache;
    }
    void applyFrame() {
        getWindow().setStatusBarColor(theme.rootBg());
        getWindow().setNavigationBarColor(theme.rootBg());
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            // Target SDK 35 enforces edge-to-edge: the window extends behind the
            // system bars, so stop the automatic inset here and let the root view
            // pad itself from the real bar insets (see onCreate). The bars keep
            // the app background color, so they still blend in.
            try {
                getWindow().setDecorFitsSystemWindows(false);
                android.view.WindowInsetsController c = getWindow().getInsetsController();
                if (c != null) {
                    int mask = android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                            | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                    c.setSystemBarsAppearance(theme.dark ? 0 : mask, mask);
                }
            } catch (Exception ignore) {}
        } else if (theme.dark) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        } else {
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

        // header (48dp compact): hamburger + brand + timer pill only
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
        // Swipe between questions on the quiz screen. The listener never
        // consumes the event, so vertical scrolling still works; only a
        // clear horizontal fling navigates.
        final android.view.GestureDetector gd = new android.view.GestureDetector(this, new android.view.GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onFling(android.view.MotionEvent e1, android.view.MotionEvent e2, float vx, float vy) {
                if (e1 == null || e2 == null) return false;
                float dx = e2.getX() - e1.getX(), dy = e2.getY() - e1.getY();
                if (Math.abs(dx) > Ui.dp(MainActivity.this, 100) && Math.abs(dx) > Math.abs(dy) * 1.5f) {
                    // RTL reading direction: swiping left moves to the next question.
                    swipeQuiz(dx < 0 ? 1 : -1);
                    return true;
                }
                return false;
            }
        });
        scroll.setOnTouchListener((v, ev) -> {
            if (s != null && s.tab.equals("quiz") && s.started) gd.onTouchEvent(ev);
            return false;
        });

        // Sticky year header — a fixed overlay at the top of the scroll area.
        stickyYear = new TextView(this);
        stickyYear.setTextSize(14);
        stickyYear.setTypeface(theme.dispB());
        stickyYear.setPadding(Ui.dp(this,6), Ui.dp(this,2), Ui.dp(this,6), Ui.dp(this,6));
        stickyYear.setVisibility(View.GONE);
        FrameLayout.LayoutParams syp = new FrameLayout.LayoutParams(-1, -2);
        syp.setMargins(Ui.dp(this,22), Ui.dp(this,56), Ui.dp(this,22), 0);
        root.addView(stickyYear, syp);

        // Side navigation drawer: a scrim (tap to close) + a slide-in panel on the start (right) side.
        drawerScrim = new View(this);
        drawerScrim.setBackgroundColor(android.graphics.Color.argb(115, 7, 46, 43));
        drawerScrim.setOnClickListener(v -> closeDrawer());
        root.addView(drawerScrim, new FrameLayout.LayoutParams(-1, -1));
        drawerScrim.setVisibility(View.GONE);
        drawerPanel = new LinearLayout(this);
        drawerPanel.setOrientation(LinearLayout.VERTICAL);
        drawerPanel.setBackgroundColor(theme.card());
        drawerPanel.setPadding(Ui.dp(this,16), Ui.dp(this,18), Ui.dp(this,16), Ui.dp(this,18));
        if (android.os.Build.VERSION.SDK_INT >= 21) drawerPanel.setElevation(Ui.dp(this,8));
        // Swipe toward the start edge (right, in RTL) closes the drawer.
        final float[] downX = {0};
        drawerPanel.setOnTouchListener((v, ev) -> {
            if (ev.getAction() == android.view.MotionEvent.ACTION_DOWN) downX[0] = ev.getX();
            else if (ev.getAction() == android.view.MotionEvent.ACTION_UP && ev.getX() - downX[0] > Ui.dp(MainActivity.this, 60)) closeDrawer();
            return false;
        });
        FrameLayout.LayoutParams dpp = new FrameLayout.LayoutParams(Ui.dp(this,280), -1, Gravity.START);
        root.addView(drawerPanel, dpp);
        drawerPanel.setVisibility(View.GONE);
    }

    void refreshHeader() {
        header.removeAllViews();
        boolean solving = s.tab.equals("quiz") && s.started;
        // Hamburger opens the side navigation drawer. Hidden in focus mode.
        if (!focus) {
            android.widget.ImageView menu = new android.widget.ImageView(this);
            menu.setImageResource(R.drawable.ic_menu);
            menu.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
            menu.setPadding(Ui.dp(this,10), Ui.dp(this,10), Ui.dp(this,10), Ui.dp(this,10));
            menu.setColorFilter(theme.ink());
            menu.setContentDescription("القائمة");
            menu.setOnClickListener(v -> openDrawer());
            LinearLayout.LayoutParams mp2 = new LinearLayout.LayoutParams(Ui.dp(this,44), Ui.dp(this,44));
            mp2.setMarginEnd(Ui.dp(this, 12));
            header.addView(menu, mp2);
        }
        // brand: mark + name, LTR
        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.HORIZONTAL);
        brand.setGravity(Gravity.CENTER_VERTICAL);
        brand.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        ImageView mark = new ImageView(this);
        android.graphics.Bitmap logo = logoBitmap();
        if (logo != null) mark.setImageBitmap(logo);
        // Logo scale + fade-in on launch (once — the header is rebuilt on every render).
        if (!logoSplashDone) { logoSplashDone = true; Fx.splash(this, mark); }
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
            solving ? Ui.dp(this,27) : Ui.dp(this,34), solving ? Ui.dp(this,24) : Ui.dp(this,28));
        brand.addView(mark, mp);
        TextView name = Ui.tv(this, "Quiz Hub", 18, theme.dispB(), theme.ink());
        name.setPadding(Ui.dp(this,7),0,0,0);
        brand.addView(name);
        if (solving) brand.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        header.addView(brand, new LinearLayout.LayoutParams(0, -1, 1f));
        if (solving && !focus && s.timer) {
            View tc = makeTimerChip();
            if (timerChipLabel != null) timerChipLabel.setVisibility(View.GONE);
            LinearLayout.LayoutParams tcp = new LinearLayout.LayoutParams(-2, -2);
            tcp.setMarginEnd(Ui.dp(this, 8));
            header.addView(tc, tcp);
        }
    }

    boolean logoSplashDone = false;

    View makeTimerChip() {
        boolean cd = s.countdown;
        int val = cd ? s.remaining() : s.elapsed;
        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setGravity(Gravity.CENTER_VERTICAL);
        timerChipLabel = Ui.tv(this, cd?"باقي":"الوقت", 11, theme.disp(), theme.ink());
        timerChipTime = Ui.timeView(this, val, 13, theme.dispB(), theme.ink());
        // Clean pill: small clock icon + tabular numbers in gold; calm red in the last 5 minutes of a countdown.
        chip.setPadding(Ui.dp(this,12), Ui.dp(this,6), Ui.dp(this,12), Ui.dp(this,6));
        timerChipIcon = new android.widget.ImageView(this);
        timerChipIcon.setImageResource(R.drawable.ic_clock);
        timerChipIcon.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        chip.addView(timerChipIcon, new LinearLayout.LayoutParams(Ui.dp(this,16), Ui.dp(this,16)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMarginStart(Ui.dp(this,6));
        chip.addView(timerChipLabel, lp);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-2, -2);
        tp.setMarginStart(Ui.dp(this,5));
        chip.addView(timerChipTime, tp);
        timerChip = chip;
        colorTimerChip();
        return chip;
    }

    /** Timer pill color: gold normally, calm red in the last 5 minutes of a countdown. */
    void colorTimerChip() {
        if (timerChip == null) return;
        boolean low = s.countdown && s.remaining() <= 300;
        int col = low ? (theme.dark ? Theme.D_RED_CALM : Theme.L_RED_CALM)
                      : (theme.dark ? Theme.D_GOLD : Theme.L_GOLD);
        if (timerChipIcon != null) timerChipIcon.setColorFilter(col);
        if (timerChipLabel != null) timerChipLabel.setTextColor(col);
        if (timerChipTime != null) timerChipTime.setTextColor(col);
        timerChip.setBackground(Ui.roundStroke(theme.card55(), Ui.dp(this,999), col, Ui.dp(this,1.5f)));
    }

    // ---------------- timer ----------------
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
                if (s.elapsed % 10 == 0) save(); // onPause saves too
                if (timerChip != null && s.timer) {
                    int val = s.countdown ? s.remaining() : s.elapsed;
                    timerChipLabel.setText(s.countdown?"باقي":"الوقت");
                    timerChipTime.setText(Ui.lre(Ui.formatTime(val)));
                    colorTimerChip();
                }
                if (s.expired()) renderAll(); // lock answers the moment it hits zero
                handler.postDelayed(ticker, 1000);
            }
        };
        handler.postDelayed(ticker, 1000);
    }

    // ---------------- actions ----------------
    boolean celebrate = false;
    String pendingRunKind = null; int[] pendingRunIds = null;
    void ensureQs() {
        if (qs != null || bankLoadFailed) return;
        if (!s.run.equals("all") || s.shuffled) { try { qs = bank.forIds(s.ids); } catch (Exception e) { showBankError(e); } }
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
            android.widget.Toast.makeText(this, "البنك المحفوظ مش موجود في النسخة دي، فبدأنا من أحدث امتحان.", android.widget.Toast.LENGTH_LONG).show();
            b = bank.defaultBank();
            s.setupSection = b.id;
        }
        if (pendingRunKind != null) {
            // bankKey stays a real bank id (the setup bank); the run kind lives in run only.
            s.run = pendingRunKind; s.bankKey = b.id; s.ids = pendingRunIds.clone();
            pendingRunKind = null; pendingRunIds = null;
            s.shuffled = false; // notebook/flagged runs keep their own order
            try { qs = bank.forIds(s.ids); } catch (Exception e) { showBankError(e); return; }
        } else {
            s.run = "all"; s.bankKey = b.id; s.ids = b.ids.clone();
            // Shuffle the order for a fresh exam when the bank has shuffle on (default off).
            // The shuffled order is saved with the run (ids[]), so resume, progress and result follow it.
            s.shuffled = s.shuffleBanks.contains(b.id);
            if (s.shuffled) {
                java.util.List<Integer> l = new java.util.ArrayList<>();
                for (int id : s.ids) l.add(id);
                java.util.Collections.shuffle(l);
                for (int i = 0; i < s.ids.length; i++) s.ids[i] = l.get(i);
                try { qs = bank.forIds(s.ids); } catch (Exception e) { showBankError(e); return; }
            } else qs = null;
        }
        s.picked = new int[s.ids.length];
        java.util.Arrays.fill(s.picked, -1);
        s.index = 0; s.elapsed = 0; s.paused = false; s.started = true; s.submitted = false;
        s.resultElapsed = -1; s.mode = s.setupMode; s.tab = "quiz";
        review = "all"; celebrate = false; confirmAction = null; setupOpen = null;
        pending = null; pendingByQuestion.clear(); focus = false;
        if (qs == null) loadBank(b);
        save(); renderAll(); startTicker();
    }
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
    /** Swipe navigation between questions. dir +1 = next (swipe left in RTL), -1 = previous. */
    void swipeQuiz(int dir) {
        if (quizView == null || quizView.isEmpty()) return;
        int np = quizPos + dir;
        if (np < 0 || np >= quizView.size()) return;
        s.index = quizView.get(np); trReset(); save(); renderAll();
    }

    /** Side navigation drawer: data-driven rows (label + action). */
    void openDrawer() {
        drawerOpen = true;
        refreshDrawer();
        drawerScrim.setVisibility(View.VISIBLE);
        drawerPanel.setVisibility(View.VISIBLE);
        if (Fx.on(this)) {
            drawerPanel.setTranslationX(Ui.dp(this, 280));
            drawerPanel.animate().translationX(0).setDuration(250).setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
            drawerScrim.setAlpha(0f);
            drawerScrim.animate().alpha(1f).setDuration(250).start();
        } else {
            drawerPanel.setTranslationX(0);
            drawerScrim.setAlpha(1f);
        }
    }
    void closeDrawer() {
        if (!drawerOpen) return;
        drawerOpen = false;
        if (Fx.on(this)) {
            drawerPanel.animate().translationX(Ui.dp(this, 280)).setDuration(220).setInterpolator(new android.view.animation.DecelerateInterpolator()).withEndAction(() -> drawerPanel.setVisibility(View.GONE)).start();
            drawerScrim.animate().alpha(0f).setDuration(220).withEndAction(() -> drawerScrim.setVisibility(View.GONE)).start();
        } else {
            drawerPanel.setVisibility(View.GONE);
            drawerScrim.setVisibility(View.GONE);
        }
    }
    void refreshDrawer() {
        if (drawerPanel == null) return;
        drawerPanel.removeAllViews();
        drawerPanel.setBackgroundColor(theme.card());
        // Header row: title + X.
        LinearLayout head = new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = Ui.tv(this, "القائمة", 17, theme.dispB(), theme.ink());
        head.addView(title, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView x = Ui.tv(this, "✕", 15, theme.disp(), theme.ink());
        x.setGravity(Gravity.CENTER);
        x.setBackground(Ui.roundStroke(android.graphics.Color.TRANSPARENT, Ui.dp(this,999), theme.border3(), Ui.dp(this,1)));
        x.setContentDescription("إغلاق القائمة");
        x.setOnClickListener(v -> closeDrawer());
        head.addView(x, new LinearLayout.LayoutParams(Ui.dp(this,36), Ui.dp(this,36)));
        drawerPanel.addView(head);
        // Items.
        drawerItem("الرئيسية", true, () -> { drawerOpen = false; goHome(); });
        drawerItem("بحث", true, () -> { drawerOpen = false; goHome(); if (homeSearch != null) homeSearch.post(() -> { homeSearch.requestFocus(); android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE); if (imm != null) imm.showSoftInput(homeSearch, 0); }); });
        if (s.tab.equals("quiz") && s.started) drawerItem("كل الأسئلة", true, () -> { drawerOpen = false; Screens.showMap(this, quizView); });
        int flagN = s.favorites.size();
        drawerItem("المذكرة" + (flagN > 0 ? " (" + Ui.toAr(String.valueOf(flagN)) + ")" : ""), flagN > 0, () -> {
            drawerOpen = false;
            int[] ids = new int[s.favorites.size()];
            int i = 0; for (int g : s.favorites) ids[i++] = g;
            startRun("flagged", ids);
        });
        drawerItem("الإعدادات", true, () -> { drawerOpen = false; s.tab = "settings"; save(); renderAll(); });
        if (s.started && !s.submitted) drawerItem("الرجوع للقائمة", true, () -> { drawerOpen = false; goToList(); });
        // Font-size toggle + dark-mode toggle pinned at the bottom.
        View spacer = new View(this);
        drawerPanel.addView(spacer, new LinearLayout.LayoutParams(-1, 0, 1f));
        Screens.groupTitle(this, drawerPanel, "المظهر");
        LinearLayout fontRow = new LinearLayout(this);
        fontRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView fl = Ui.tv(this, "حجم الخط", 15, theme.disp(), theme.ink());
        fontRow.addView(fl, new LinearLayout.LayoutParams(0, -2, 1f));
        android.widget.Button fminus = new android.widget.Button(this);
        fminus.setText("A−"); fminus.setAllCaps(false); fminus.setTextSize(13); fminus.setTypeface(theme.dispB());
        fminus.setTextDirection(View.TEXT_DIRECTION_LTR);
        fminus.setMinHeight(Ui.dp(this,36)); fminus.setMinimumHeight(Ui.dp(this,36));
        fminus.setPadding(Ui.dp(this,14),0,Ui.dp(this,14),0); fminus.setStateListAnimator(null);
        fminus.setBackground(Ui.roundStroke(theme.card55(), Ui.dp(this,999), theme.border3(), Ui.dp(this,1.5f)));
        fminus.setTextColor(theme.ink());
        fminus.setOnClickListener(v -> { s.fontPercent = Math.max(70, s.fontPercent - 10); save(); renderAll(); });
        fontRow.addView(fminus);
        TextView fpct = Ui.tv(this, Ui.lre(Ui.toAr(String.valueOf(s.fontPercent)) + "٪"), 12, theme.disp(), theme.ink());
        fpct.setTextDirection(View.TEXT_DIRECTION_LTR);
        LinearLayout.LayoutParams fpp = new LinearLayout.LayoutParams(-2, -2);
        fpp.setMarginStart(Ui.dp(this,8));
        fontRow.addView(fpct, fpp);
        android.widget.Button fplus = new android.widget.Button(this);
        fplus.setText("A+"); fplus.setAllCaps(false); fplus.setTextSize(13); fplus.setTypeface(theme.dispB());
        fplus.setTextDirection(View.TEXT_DIRECTION_LTR);
        fplus.setMinHeight(Ui.dp(this,36)); fplus.setMinimumHeight(Ui.dp(this,36));
        fplus.setPadding(Ui.dp(this,14),0,Ui.dp(this,14),0); fplus.setStateListAnimator(null);
        fplus.setBackground(Ui.roundStroke(theme.card55(), Ui.dp(this,999), theme.border3(), Ui.dp(this,1.5f)));
        fplus.setTextColor(theme.ink());
        fplus.setOnClickListener(v -> { s.fontPercent = Math.min(125, s.fontPercent + 10); save(); renderAll(); });
        LinearLayout.LayoutParams fpp2 = new LinearLayout.LayoutParams(-2, Ui.dp(this,36));
        fpp2.setMarginStart(Ui.dp(this,8));
        fontRow.addView(fplus, fpp2);
        drawerPanel.addView(fontRow);
        LinearLayout darkRow = new LinearLayout(this);
        darkRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView darkLabel = Ui.tv(this, "الوضع الليلي", 15, theme.disp(), theme.ink());
        darkRow.addView(darkLabel, new LinearLayout.LayoutParams(0, -2, 1f));
        android.widget.Switch darkSw = new android.widget.Switch(this);
        darkSw.setChecked(s.dark);
        darkSw.setOnCheckedChangeListener((v, on) -> { s.dark = on; save(); renderAll(); });
        darkRow.addView(darkSw);
        drawerPanel.addView(darkRow);
    }
    void drawerItem(String label, boolean enabled, Runnable action) {
        android.widget.Button b = new android.widget.Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setTypeface(theme.disp());
        b.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        b.setMinHeight(Ui.dp(this, 52));
        b.setMinimumHeight(Ui.dp(this, 52));
        b.setPadding(Ui.dp(this, 14), 0, Ui.dp(this, 14), 0);
        b.setStateListAnimator(null);
        b.setBackground(Ui.round(android.graphics.Color.TRANSPARENT, Ui.dp(this, 12)));
        b.setTextColor(theme.ink());
        b.setEnabled(enabled);
        b.setAlpha(enabled ? 1f : .45f);
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = Ui.dp(this, 6);
        drawerPanel.addView(b, lp);
    }

    /** Result screen "الرجوع للرئيسية": scroll the home exam list into view. */
    void scrollHomeToList() {
        if (scroll == null || content == null) return;
        View v = content.findViewWithTag("exam-list");
        if (v == null) { scroll.post(() -> scroll.scrollTo(0, 0)); return; }
        scroll.post(() -> scroll.scrollTo(0, v.getTop()));
    }

    void goHome() {
        confirmAction = null;
        focus = false; // leaving the quiz exits focus mode
        s.tab = "home"; save(); renderAll();
    }
    /** Back to home from review/result: land on the exam list. The saved
     *  result stays (scroll up to see it) — same scroll as the result
     *  screen's own button. */
    void goHomeToList() {
        goHome();
        scrollHomeToList();
    }
    /** Back to the exam list from a quiz — pauses the exam so the clock stops. */
    void goToList() {
        if (s.started && !s.submitted) s.paused = true;
        setupOpen = true;
        goHome();
    }
    /** Tapping an exam card starts that exam in the chosen mode — or resumes
     *  it when it is the live session. A live session for another exam asks
     *  for confirmation first (confirmBox -> begin()). */
    void onExamCard(Bank.BankMeta m) {
        if (s.started && !s.submitted && s.run.equals("all") && m.id.equals(s.bankKey)) { openReview("all"); return; }
        s.setupSection = m.id; s.minutes = m.minutes; save();
        if (s.started) { confirmAction = "start"; renderAll(); } else begin();
    }
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
        // "كمّل الحل" means resume — unpause a live session.
        if (s.started && !s.submitted) s.paused = false;
        if (filter.equals("wrong")) {
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
        // In study mode the mistakes notebook updates right at the answer, not only at submit.
        if (s.mode.equals("study")) { if (v == q.c) { if (s.run.equals("notebook")) s.wrong.remove(q.gid); } else s.wrong.add(q.gid); }
        save();
        // Answer sounds + result vibration are STUDY-mode only: in exam mode the
        // answer must not leak through a correct/wrong blip or a distinct buzz.
        // (The gentle select haptic below is the same for correct/wrong in both modes.)
        // Neither delays the explanation card.
        if (s.mode.equals("study")) {
            if (s.sounds) ClipPlayer.sfx(this, v == q.c, s.volume);
            Fx.resultBuzz(this, v == q.c);
        }
        Fx.buzz(this); // gentle haptic on selecting an answer
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
    /** commit = pick + optional auto-explain */
    void commit(int v) {
        pick(v);
        pending = null;
        pendingByQuestion.remove(s.index);
        if (s.autoExplain && s.mode.equals("study") && !s.submitted) Screens.showExplain(this);
    }

    /** Share the current question as a designed image via the system share sheet. */
    void shareQuestion() {
        if (qs == null || s.index < 0 || s.index >= qs.size()) return;
        String examName = s.run.equals("all") ? (sessionBank() != null ? sessionBank().name : null) : runLabel(s.run);
        ShareCard.share(this, qs.get(s.index), s.picked[s.index], examName);
    }

    void trReset() {
        trOpen = s.translation; hintOpen = s.hintDefault;
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
        return b != null ? b : bank.defaultBank(); // explicit default: the newest exam
    }

    void save() { s.save(this); }

    void renderAll() {
        if (bankLoadFailed) return;
        theme.dark = s.dark; theme.scale = s.fontPercent / 100f;
        root.setBackgroundColor(theme.rootBg());
        screen.setBackground(Ui.round(theme.bg(), Ui.dp(this, 28)));
        applyFrame();
        refreshHeader();
        if (stickyYear != null && !s.tab.equals("home")) stickyYear.setVisibility(View.GONE);
        // A settings toggle re-renders the same view, so it must not jump to the top.
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
        // Side navigation drawer — visibility follows the flag; content is refreshed while open.
        if (drawerScrim != null) drawerScrim.setVisibility(drawerOpen ? View.VISIBLE : View.GONE);
        if (drawerPanel != null) {
            drawerPanel.setVisibility(drawerOpen ? View.VISIBLE : View.GONE);
            if (drawerOpen) refreshDrawer();
        }
    }

    @Override public void onBackPressed() {
        // Side drawer closes first; otherwise system Back from a quiz pauses the exam.
        if (drawerOpen) { closeDrawer(); return; }
        if (!s.tab.equals("home")) { goToList(); }
        else super.onBackPressed();
    }

    @Override protected void onDestroy() { super.onDestroy(); tickerScheduled = false; if (handler != null) { if (ticker != null) handler.removeCallbacks(ticker); if (autoNextRun != null) handler.removeCallbacks(autoNextRun); } }

    @Override protected void onPause() { super.onPause(); ClipPlayer.stop(); tickerScheduled = false; if (handler != null && ticker != null) handler.removeCallbacks(ticker); if (s != null) save(); }

    @Override protected void onResume() { super.onResume(); startTicker(); }
}
