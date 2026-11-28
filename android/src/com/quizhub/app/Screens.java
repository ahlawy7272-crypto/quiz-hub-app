package com.quizhub.app;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;

/**
 * Clean-rebuild screen builders: home / quiz / settings.
 * The agreed design only — soft ivory, gold accents, deep-teal dark mode.
 * No praise words after answering (green/red colors only), no explanatory
 * captions inside screens, no verification wording or statistics in the UI.
 * R5 + R7 (source line) items pending Mohamed's decision are preserved
 * EXACTLY as in the current build: the settings detail paragraphs + intro,
 * and the source line inside the explanation dialog (see showExplain
 * below). The generated-audio note was removed 2026-11-26 per his decision
 * (R7 part 1); both clips now use the same audio button label.
 * Nothing else was kept from the old screens.
 */
final class Screens {

    static int dp(MainActivity a, float v) { return Ui.dp(a, v); }

    static TextView label(MainActivity a, String t) {
        return Ui.tv(a, t, 13, a.theme.disp(), a.theme.head());
    }
    static TextView detail(MainActivity a, String t) {
        TextView v = Ui.tv(a, t, 12, a.theme.bodyM(), a.theme.muted());
        v.setLineSpacing(0, 1.7f);
        return v;
    }
    static Button btn(MainActivity a, String t, boolean primary) {
        Button b = new Button(a);
        b.setText(t); b.setAllCaps(false);
        b.setTextSize(13); b.setTypeface(a.theme.disp());
        b.setMinHeight(dp(a,44)); b.setMinimumHeight(dp(a,44));
        b.setPadding(dp(a,28), dp(a,8), dp(a,28), dp(a,8));
        b.setStateListAnimator(null);
        applyBtn(a, b, primary);
        return b;
    }
    static void applyBtn(MainActivity a, Button b, boolean primary) {
        Theme t = a.theme;
        if (primary) b.setBackground(Ui.round(t.primary(), dp(a,999)));
        else b.setBackground(Ui.roundStroke(Color.TRANSPARENT, dp(a,999), t.dark?0x77D4A24C:Theme.L_TEAL, dp(a,1.5f)));
        b.setTextColor(primary ? t.onAccent() : (t.dark ? Theme.D_SOFT : Theme.L_TEAL));
    }
    /** Gold button — the most prominent action (confirm, hero continue). */
    static Button goldBtn(MainActivity a, String t) {
        Button b = btn(a, t, true);
        b.setBackground(Ui.round(a.theme.goldFill(), dp(a,999)));
        b.setTextColor(a.theme.onGold());
        return b;
    }
    /** Thin gold progress line for an exam card / hero card. Fill starts at the inline-start side (right in RTL). */
    static View cardProgressBar(MainActivity a, float fraction) {
        Theme t = a.theme;
        LinearLayout rail = new LinearLayout(a);
        rail.setOrientation(LinearLayout.HORIZONTAL);
        rail.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        rail.setBackground(Ui.round(t.rail(), dp(a,999)));
        rail.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(a,4)));
        float f = Math.max(0f, Math.min(1f, fraction));
        View fill = new View(a);
        fill.setBackground(Ui.round(t.dark ? Theme.D_GOLD : Theme.L_GOLD, dp(a,999)));
        fill.setLayoutParams(new LinearLayout.LayoutParams(0, -1, f));
        View gap = new View(a);
        gap.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 1f - f));
        rail.addView(fill); rail.addView(gap);
        return rail;
    }
    /** Exam type derived from the name — 0 تجريبي (teal), 1 محاكاة (gold), 2 فعلي (calm green). */
    static int examType(String name) {
        if (name.contains("فعلي")) return 2;
        if (name.contains("محاكاة")) return 1;
        return 0;
    }
    static String examTypeLabel(int type) {
        return type == 2 ? "فعلي" : type == 1 ? "محاكاة" : "تجريبي";
    }
    /** Year derived from the name (2024 / 2025 / 2026), 0 when unknown. */
    static int examYear(String name) {
        if (name.contains("2026")) return 2026;
        if (name.contains("2025")) return 2025;
        if (name.contains("2024")) return 2024;
        return 0;
    }

    /** Long-press details popup for an exam card — question count, duration,
     *  last score. Also hosts the shuffle toggle: when on, a fresh start of
     *  this exam runs its questions in a random order (default off). */
    static void showExamDetails(MainActivity a, Bank.BankMeta m) {
        Session s = a.s;
        int last = lastResult(a, m);
        LinearLayout v = new LinearLayout(a);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setPadding(dp(a, 20), dp(a, 8), dp(a, 20), dp(a, 4));
        String msg = Ui.toAr(String.valueOf(m.ids.length)) + " سؤال · " + Ui.toAr(String.valueOf(m.minutes)) + " د"
            + (last >= 0 ? "\nآخر نتيجة " + Ui.toAr(String.valueOf(last)) + "٪" : "\nلم يبدأ بعد");
        TextView info = Ui.tv(a, msg, 15, a.theme.body(), a.theme.ink());
        v.addView(info);
        LinearLayout row = new LinearLayout(a);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView lab = Ui.tv(a, "خلط ترتيب الأسئلة", 15, a.theme.disp(), a.theme.ink());
        row.addView(lab, new LinearLayout.LayoutParams(0, -2, 1f));
        android.widget.Switch sw = new android.widget.Switch(a);
        sw.setChecked(s.shuffleBanks.contains(m.id));
        sw.setOnCheckedChangeListener((b, on) -> {
            if (on) s.shuffleBanks.add(m.id); else s.shuffleBanks.remove(m.id);
            a.save();
        });
        row.addView(sw);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, -2);
        rp.topMargin = dp(a, 16);
        v.addView(row, rp);
        new android.app.AlertDialog.Builder(a).setTitle(m.name).setView(v)
            .setPositiveButton("تمام", null).show();
    }
    static View card(MainActivity a) {
        LinearLayout c = new LinearLayout(a);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(Ui.roundStroke(a.theme.card(), dp(a,16), a.theme.border(), dp(a,1.5f)));
        c.setPadding(dp(a,14), dp(a,12), dp(a,14), dp(a,12));
        return c;
    }
    static void add(LinearLayout parent, View v, int top) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp((MainActivity) parent.getContext(), top);
        parent.addView(v, p);
    }

    // ================= HOME =================
    static void home(MainActivity a, LinearLayout c) {
        Session s = a.s; Theme t = a.theme; Bank bank = a.bank;
        // Demo home top lines (web .qz-topbar + .qz-clock-status): the setup
        // mode first, then the clock status — both above the title, and no
        // timer pill on home/result (the time lives in the resume card and
        // in the result "وقت الحل" line, exactly like the demo).
        add(c, Ui.tv(a, !s.started ? "إعداد التجربة" : s.mode.equals("study") ? "وضع المذاكرة" : "وضع الامتحان", 12, t.dispB(), t.muted()), 0);
        if ((s.started || s.submitted) && (s.paused || (s.allAnswered() && s.mode.equals("study")) || s.expired() || s.submitted)) {
            String msg = s.expired() ? "الوقت خلص" : s.submitted ? "تم التسليم، العداد متوقف"
                    : s.allAnswered() ? "اكتملت الاختيارات، العداد متوقف" : "العداد متوقف مؤقتًا";
            add(c, Ui.tv(a, msg, 12, t.dispB(), t.ink()), 6);
        }
        LinearLayout top = new LinearLayout(a);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        Bank.BankMeta homeBank = bank.byId(s.setupSection);
        TextView mode = Ui.tv(a, s.submitted ? "نتيجة التجربة" : s.started ? "كمّل من مكانك" : (homeBank != null ? homeBank.name : "امتحان تجريبي"), 20, t.dispB(), t.head());
        top.addView(mode, new LinearLayout.LayoutParams(0,-2,1f));
        LinearLayout.LayoutParams tp2 = new LinearLayout.LayoutParams(-1,-2);
        tp2.topMargin = dp(a, 10);
        c.addView(top, tp2);
        // The save-status line shows ONLY when a save actually failed; otherwise nothing.
        if (s.saveFailed) {
            TextView save = Ui.tv(a, "تنبيه: آخر حفظ فشل — جرب تقفل التطبيق وتفتحه تاني، أو افضي مساحة.", 11.5f, t.dispB(), t.dark ? Theme.D_MISSED : 0xffb3242b);
            add(c, save, 10);
        }

        // A finished attempt has started: false, but its result screen and
        // its review still live on the home tab (submitted: true).
        if (s.started || s.submitted) {
            a.ensureQs();
            if (s.submitted) add(c, detail(a, a.runLabel(s.run.equals("all") ? s.bankKey : s.run)), 6);
            if (s.submitted) { resultCard(a, c); }
            else { inProgressCard(a, c); sessionMap(a, c); }
        }

        modeSeg(a, c);
        examCards(a, c);
    }

    static int[] wrongIds(MainActivity a, String scope) {
        java.util.ArrayList<Integer> out = new java.util.ArrayList<>();
        if (scope.equals("all")) out.addAll(a.s.wrong);
        else { Bank.BankMeta m = a.bank.byId(scope); if (m == null) out.addAll(a.s.wrong); else for (int g : m.ids) if (a.s.wrong.contains(g)) out.add(g); }
        java.util.Collections.sort(out);
        int[] r = new int[out.size()]; for (int i = 0; i < r.length; i++) r[i] = out.get(i); return r;
    }

    static TextView homeTitle(MainActivity a, String t) {
        return Ui.tv(a, t, 23, a.theme.dispB(), a.theme.head());
    }

    static void confirmBox(MainActivity a, LinearLayout c) {
        Session s = a.s; Theme t = a.theme;
        // Demo box: a white card with a gold accent strip on the right edge
        // (web .qz-hint: border-inline-start 4px gold). The strip is the
        // first child of the RTL row, so it sits on the right edge.
        LinearLayout wrap = new LinearLayout(a);
        wrap.setOrientation(LinearLayout.HORIZONTAL);
        wrap.setBackground(Ui.round(t.card(), dp(a,14)));
        View strip = new View(a);
        strip.setBackgroundColor(t.dark ? Theme.D_GOLD : Theme.L_GOLD);
        wrap.addView(strip, new LinearLayout.LayoutParams(dp(a,4), -1));
        LinearLayout box = new LinearLayout(a);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(a,14), dp(a,10), dp(a,14), dp(a,10));
        String msg = a.confirmAction.equals("finish")
            ? (s.blank() > 0
                ? "في أسئلة لسه من غير إجابة. هتتحسب ضمن الدرجة، تسلّم دلوقتي؟"
                : "كل الأسئلة مجاوبة. تسلّم دلوقتي وتعرض النتيجة؟")
            : "هتبدأ تجربة جديدة وتمسح الإجابات والنتيجة والوقت الحاليين. الإعدادات هتفضل زي ما هي.";
        box.addView(detail(a, msg));
        LinearLayout row = new LinearLayout(a);
        Button ok = btn(a, a.confirmAction.equals("start")?"ابدأ من جديد":"سلّم التجربة", true);
        ok.setOnClickListener(v -> { if (a.confirmAction.equals("start")) a.begin(); else a.doFinishConfirmed(); });
        Button back = btn(a, "رجوع", false);
        back.setOnClickListener(v -> { a.confirmAction = null; a.pendingRunKind = null; a.pendingRunIds = null; a.renderAll(); });
        row.addView(ok, new LinearLayout.LayoutParams(0,-2,1f));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0,-2,1f);
        bp.setMarginStart(dp(a,10));
        row.addView(back, bp);
        LinearLayout.LayoutParams rp2 = new LinearLayout.LayoutParams(-1,-2);
        rp2.topMargin = dp(a,10);
        box.addView(row, rp2);
        wrap.addView(box, new LinearLayout.LayoutParams(0, -2, 1f));
        add(c, wrap, 10);
    }

    /** The exam list: compact notebook/flagged actions, search, then cards
     *  grouped by year (newest first) with sticky section headers.
     *  The search filters in place (no full re-render) so the keyboard and
     *  focus survive typing. */
    static void examCards(MainActivity a, LinearLayout c) {
        Session s = a.s; Theme t = a.theme;
        if (!s.wrong.isEmpty() || !s.favorites.isEmpty()) {
            LinearLayout row = new LinearLayout(a);
            // RTL row: notebook first (right/start), flagged second (left/end).
            if (!s.wrong.isEmpty()) {
                Button nb = btn(a, "دفتر الغلطات (" + Ui.toAr(String.valueOf(s.wrong.size())) + ")", true);
                nb.setOnClickListener(v -> a.startRun("notebook", wrongIds(a, "all")));
                row.addView(nb, new LinearLayout.LayoutParams(0, -2, 1f));
            }
            if (!s.favorites.isEmpty()) {
                Button fb = btn(a, "راجعني (" + Ui.toAr(String.valueOf(s.favorites.size())) + ")", true);
                fb.setOnClickListener(v -> { int[] ids = new int[s.favorites.size()]; int i = 0; for (int g : s.favorites) ids[i++] = g; a.startRun("flagged", ids); });
                LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(0, -2, 1f);
                if (!s.wrong.isEmpty()) fp.setMarginStart(dp(a, 10));
                row.addView(fb, fp);
            }
            add(c, row, 14);
        }
        add(c, label(a, "الاختبار أو البنك"), 12);
        android.widget.EditText search = new android.widget.EditText(a);
        search.setHint("ابحث عن بنك…");
        search.setText(a.bankQuery);
        search.setTextSize(13);
        search.setTypeface(t.body());
        search.setTextColor(t.ink());
        search.setHintTextColor(t.muted());
        search.setBackground(Ui.roundStroke(t.card55(), dp(a,12), t.border3(), dp(a,1)));
        search.setPadding(dp(a,14), dp(a,10), dp(a,14), dp(a,10));
        search.setSingleLine(true);
        search.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
        add(c, search, 6);
        a.homeSearch = search; // drawer "بحث" focuses this box
        LinearLayout list = new LinearLayout(a);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setTag("exam-list");
        search.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence ch, int st, int co, int af) {}
            @Override public void onTextChanged(CharSequence ch, int st, int be, int co) {}
            @Override public void afterTextChanged(android.text.Editable e) { a.bankQuery = e.toString(); fillExamCards(a, list); }
        });
        fillExamCards(a, list);
        add(c, list, 4);
    }

    /** Year headers of the current exam list, for the sticky header. */
    static java.util.ArrayList<TextView> yearHeaders = new java.util.ArrayList<TextView>();

    /** The sticky year header — a fixed overlay above the scroll area
     *  (MainActivity.stickyYear) mirroring the topmost year header that has
     *  scrolled under the viewport top. Hidden while the first header is
     *  still fully visible, and outside the home tab. */
    static void updateStickyYear(MainActivity a) {
        if (a.stickyYear == null) return;
        if (!a.s.tab.equals("home") || yearHeaders.isEmpty()) { a.stickyYear.setVisibility(View.GONE); return; }
        int[] scLoc = new int[2];
        a.scroll.getLocationOnScreen(scLoc);
        int top = scLoc[1];
        TextView best = null;
        for (TextView h : yearHeaders) {
            int[] loc = new int[2];
            h.getLocationOnScreen(loc);
            if (loc[1] <= top + dp(a, 2)) best = h; else break;
        }
        if (best == null) { a.stickyYear.setVisibility(View.GONE); return; }
        a.stickyYear.setText(best.getText());
        a.stickyYear.setTextColor(a.theme.ink());
        a.stickyYear.setBackgroundColor(a.theme.bg());
        a.stickyYear.setVisibility(View.VISIBLE);
    }

    /** Two exam cards per row (RTL: first card right/start). */
    static void addCardRows(MainActivity a, LinearLayout list, java.util.List<Bank.BankMeta> banks) {
        for (int i = 0; i < banks.size(); i += 2) {
            LinearLayout row = new LinearLayout(a);
            for (int j = i; j < Math.min(i + 2, banks.size()); j++) {
                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, -2, 1f);
                if (j > i) cp.setMarginStart(dp(a, 10));
                row.addView(examCard(a, banks.get(j)), cp);
            }
            if (banks.size() - i == 1) row.addView(new View(a), new LinearLayout.LayoutParams(0, -2, 1f));
            add(list, row, 6);
        }
    }

    static void fillExamCards(MainActivity a, LinearLayout list) {
        Session s = a.s; Theme t = a.theme;
        list.removeAllViews();
        yearHeaders.clear();
        java.util.TreeMap<Integer, java.util.List<Bank.BankMeta>> byYear =
            new java.util.TreeMap<>(java.util.Collections.reverseOrder());
        for (Bank.BankMeta m : a.bank.banks) {
            if (!a.bankQuery.isEmpty() && !m.name.contains(a.bankQuery)) continue;
            int y = examYear(m.name);
            if (!byYear.containsKey(y)) byYear.put(y, new java.util.ArrayList<Bank.BankMeta>());
            byYear.get(y).add(m);
        }
        boolean any = false;
        for (java.util.Map.Entry<Integer, java.util.List<Bank.BankMeta>> e : byYear.entrySet()) {
            int totalQ = 0; for (Bank.BankMeta m : e.getValue()) totalQ += m.ids.length;
            TextView h = Ui.tv(a, (e.getKey() == 0 ? "الاختبارات" : Ui.toAr(String.valueOf(e.getKey()))) + " · " + Ui.toAr(String.valueOf(totalQ)) + " سؤال", 14, t.dispB(), t.ink());
            h.setPadding(dp(a,6), dp(a,10), dp(a,6), dp(a,4));
            h.setTag(e.getKey());
            add(list, h, 0);
            yearHeaders.add(h);
            any = true;
            addCardRows(a, list, e.getValue());
        }
        if (!any) add(list, detail(a, "مفيش نتائج للبحث."), 10);
        a.scroll.post(() -> updateStickyYear(a));
        a.scroll.setOnScrollChangeListener((v, sx, sy, ox, oy) -> updateStickyYear(a));
    }

    /** Small tinted exam-type badge — تجريبي teal / محاكاة gold / فعلي calm green. */
    static TextView examBadge(MainActivity a, int type, boolean live) {
        Theme t = a.theme;
        TextView b = Ui.tv(a, examTypeLabel(type), 11, t.dispB(), t.ink());
        int bg, fg;
        if (live) { bg = 0x33ffffff; fg = t.onAccent(); }
        else if (type == 2) { bg = t.dark ? 0x337fd08a : 0x1f2e7d5b; fg = t.dark ? Theme.D_GREEN : Theme.L_GREEN; }
        else if (type == 1) { bg = t.dark ? 0x33d4a24c : 0x1fb98a2f; fg = t.dark ? Theme.D_GOLD : Theme.L_GOLD; }
        else { bg = t.dark ? 0x338fd0c8 : 0x1f0b4f4a; fg = t.dark ? Theme.D_TEAL_LT : Theme.L_TEAL; }
        b.setTextColor(fg);
        b.setBackground(Ui.round(bg, dp(a,999)));
        b.setPadding(dp(a,8), dp(a,3), dp(a,8), dp(a,3));
        return b;
    }

    /** One exam card: name + type badge, count + minutes, status, and a gold
     *  progress line for the in-progress exam. Long-press opens details. */
    static LinearLayout examCard(MainActivity a, Bank.BankMeta m) {
        Session s = a.s; Theme t = a.theme;
        boolean live = s.started && !s.submitted && s.run.equals("all") && m.id.equals(s.bankKey);
        boolean selected = m.id.equals(s.setupSection);
        LinearLayout card = new LinearLayout(a);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(Ui.roundStroke(live ? t.primary() : (selected ? t.card55() : t.card()), dp(a, 16), live ? t.primary() : (selected ? t.primary() : t.border()), dp(a, 1.5f)));
        card.setPadding(dp(a, 14), dp(a, 12), dp(a, 14), dp(a, 12));
        card.setMinimumHeight(dp(a, 96));
        LinearLayout top = new LinearLayout(a);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView name = Ui.tv(a, m.name, 14.5f, t.dispB(), live ? t.onAccent() : t.head());
        name.setMaxLines(2);
        top.addView(name, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView badge = examBadge(a, examType(m.name), live);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-2, -2);
        bp.setMarginStart(dp(a, 6));
        top.addView(badge, bp);
        card.addView(top);
        if (live) {
            LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1, dp(a, 4));
            pp.topMargin = dp(a, 8);
            card.addView(cardProgressBar(a, (s.ids.length - s.blank()) / (float) s.ids.length), pp);
        }
        TextView count = Ui.tv(a, Ui.toAr(String.valueOf(m.ids.length)) + " سؤال · " + Ui.toAr(String.valueOf(m.minutes)) + " د", 12, t.body(), live ? t.onAccent() : t.muted());
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2); cp.topMargin = dp(a, 4);
        card.addView(count, cp);
        int last = lastResult(a, m);
        TextView st = Ui.tv(a, cardStatus(a, m, last), 12, t.dispB(), live ? t.onAccent() : (last >= 0 ? t.ink() : t.muted()));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2); sp.topMargin = dp(a, 4);
        card.addView(st, sp);
        card.setOnClickListener(v -> a.onExamCard(m));
        card.setOnLongClickListener(v -> { showExamDetails(a, m); return true; });
        return card;
    }

    /** Last recorded result for this bank, or -1 when it was never submitted. */
    static int lastResult(MainActivity a, Bank.BankMeta m) {
        for (int i = a.s.history.length() - 1; i >= 0; i--) {
            try {
                org.json.JSONObject e = a.s.history.getJSONObject(i);
                if (e.getString("b").equals(m.id)) return e.getInt("n") > 0 ? Math.round(e.getInt("r") * 100f / e.getInt("n")) : 0;
            } catch (Exception ignore) {}
        }
        return -1;
    }

    static String cardStatus(MainActivity a, Bank.BankMeta m, int last) {
        Session s = a.s;
        if (s.started && !s.submitted && s.run.equals("all") && m.id.equals(s.bankKey)) {
            int answered = s.ids.length - s.blank();
            return "جارٍ — سؤال " + Ui.toAr(String.valueOf(answered + 1)) + " من " + Ui.toAr(String.valueOf(s.ids.length));
        }
        if (last >= 0) return "آخر نتيجة " + Ui.toAr(String.valueOf(last)) + "٪";
        return "لم يبدأ";
    }

    static void modeSeg(MainActivity a, LinearLayout c) {
        Session s = a.s; Theme t = a.theme;
        add(c, label(a, "طريقة التجربة"), 12);
        LinearLayout seg = new LinearLayout(a);
        seg.setBackground(Ui.round(t.dark?0x14ffffff:0x140b4f4a, dp(a,999)));
        seg.setPadding(dp(a,3),dp(a,3),dp(a,3),dp(a,3));
        String[][] opts = {{"study","مذاكرة"},{"exam","امتحان"}};
        for (String[] o : opts) {
            Button b = new Button(a);
            b.setText(o[1]); b.setAllCaps(false); b.setTextSize(12.5f);
            b.setTypeface(t.disp()); b.setMinHeight(dp(a,44)); b.setMinimumHeight(dp(a,44));
            b.setStateListAnimator(null);
            boolean on = s.setupMode.equals(o[0]);
            b.setBackground(Ui.round(on ? t.primary() : Color.TRANSPARENT, dp(a,999)));
            b.setTextColor(on ? t.onAccent() : t.muted());
            b.setOnClickListener(v -> { s.setupMode = o[0]; a.save(); a.renderAll(); });
            seg.addView(b, new LinearLayout.LayoutParams(0,-2,1f));
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin = dp(a,6);
        c.addView(seg, lp);
    }

    // ---------- in-progress session card ----------
    static void inProgressCard(MainActivity a, LinearLayout c) {
        Session s = a.s; Theme t = a.theme;
        // "Continue where you left off" hero card: exam name, current question,
        // a gold progress line and a gold "كمّل" button.
        String name = a.runLabel(s.run.equals("all") ? s.bankKey : s.run);
        int answered = s.ids.length - s.blank();
        LinearLayout card = new LinearLayout(a);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(Ui.roundStroke(t.card55(), dp(a,18), t.dark ? Theme.D_GOLD : Theme.L_GOLD, dp(a,1.5f)));
        card.setPadding(dp(a,16), dp(a,14), dp(a,16), dp(a,14));
        card.addView(Ui.tv(a, name, 16, t.dispB(), t.head()));
        TextView prog = Ui.tv(a, "سؤال " + Ui.toAr(String.valueOf(Math.min(answered + 1, s.ids.length))) + " من " + Ui.toAr(String.valueOf(s.ids.length))
                + " · " + (s.mode.equals("exam")?"امتحان":"مذاكرة"), 13, t.dispB(), t.muted());
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1,-2); pp.topMargin = dp(a,6);
        card.addView(prog, pp);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, dp(a,4)); bp.topMargin = dp(a,10);
        card.addView(cardProgressBar(a, answered / (float) s.ids.length), bp);
        if (s.timer) {
            LinearLayout tr = new LinearLayout(a);
            tr.setOrientation(LinearLayout.HORIZONTAL);
            tr.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            android.widget.ImageView ic = new android.widget.ImageView(a);
            ic.setImageResource(R.drawable.ic_clock);
            ic.setColorFilter(t.ink());
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(a,16), dp(a,16));
            ip.setMarginEnd(dp(a, 4));
            tr.addView(ic, ip);
            tr.addView(Ui.timeView(a, s.elapsed, 13, t.disp(), t.ink()));
            LinearLayout.LayoutParams tp2 = new LinearLayout.LayoutParams(-1,-2); tp2.topMargin = dp(a,10);
            card.addView(tr, tp2);
        }
        Button cont = goldBtn(a, "كمّل");
        cont.setOnClickListener(v -> a.openReview("all"));
        LinearLayout.LayoutParams cp2 = new LinearLayout.LayoutParams(-1,-2); cp2.topMargin = dp(a,12);
        card.addView(cont, cp2);
        Button fin = btn(a, "إنهاء وعرض النتيجة", false);
        fin.setOnClickListener(v -> a.finishQuiz());
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1,-2); fp.topMargin = dp(a,8);
        card.addView(fin, fp);
        add(c, card, 12);
    }

    // ---------- session map on home (demo: "خريطة الأسئلة ▸" under the
    // resume card, expanding inline to one card per question, each with its
    // state — لسه / تم الاختيار / صح ✓ / غلط ✕ — tapping one jumps to it). ----------
    static void sessionMap(MainActivity a, LinearLayout c) {
        Session s = a.s; Theme t = a.theme;
        TextView link = Ui.tv(a, a.sessionMapOpen ? "خريطة الأسئلة ▾" : "خريطة الأسئلة ▸", 13, t.dispB(), t.ink());
        link.setOnClickListener(v -> { a.sessionMapOpen = !a.sessionMapOpen; a.renderAll(); });
        add(c, link, 12);
        if (!a.sessionMapOpen) return;
        LinearLayout grid = new LinearLayout(a);
        grid.setOrientation(LinearLayout.VERTICAL);
        int cols = Math.max(3, (a.getResources().getDisplayMetrics().widthPixels - dp(a, 32) + dp(a, 8)) / dp(a, 98));
        LinearLayout row = null;
        for (int p = 0; p < s.ids.length; p++) {
            final int qi = p;
            if (p % cols == 0) {
                row = new LinearLayout(a);
                LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(-1, -2);
                if (p > 0) rlp.topMargin = dp(a, 8);
                grid.addView(row, rlp);
            }
            LinearLayout card = new LinearLayout(a);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(Ui.roundStroke(t.card(), dp(a,16), t.border(), dp(a,1.5f)));
            card.setPadding(dp(a,14), dp(a,12), dp(a,14), dp(a,12));
            card.addView(Ui.tv(a, "سؤال " + Ui.toAr(String.valueOf(p + 1)), 13, t.dispB(), t.head()));
            String st = s.picked[qi] < 0 ? "لسه" : s.mode.equals("exam") ? "تم الاختيار"
                    : s.picked[qi] == a.qs.get(qi).c ? "صح ✓" : "غلط ✕";
            int stc = s.picked[qi] < 0 || s.mode.equals("exam") ? t.muted()
                    : s.picked[qi] == a.qs.get(qi).c ? (t.dark ? 0xff7fd08a : Theme.L_TEAL) : (t.dark ? 0xffffb2aa : 0xffb3242b);
            TextView stv = Ui.tv(a, st, 12, t.dispB(), stc);
            LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-2, -2);
            slp.topMargin = dp(a, 8);
            card.addView(stv, slp);
            card.setOnClickListener(v -> { s.index = qi; a.openReview("all"); });
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0, -2, 1f);
            if (p % cols > 0) clp.setMarginStart(dp(a, 8));
            row.addView(card, clp);
        }
        add(c, grid, 8);
    }

    // ---------- result card ----------
    static void resultCard(MainActivity a, LinearLayout c) {
        Session s = a.s; Theme t = a.theme;
        int right = 0, wrong = 0;
        for (int i = 0; i < s.ids.length; i++) {
            if (s.picked[i] == a.qs.get(i).c) right++;
            else if (s.picked[i] >= 0) wrong++;
        }
        int blank = s.ids.length - right - wrong;
        int pct = Math.round(right * 100f / s.ids.length);
        // Gold score ring as the hero element; it fills gradually up to the score.
        ScoreRingView ring = new ScoreRingView(a, t);
        ring.animateTo(pct, t, Fx.on(a));
        Fx.pop(a, ring, 550);
        if (a.celebrate && !a.confettiDone) { a.confettiDone = true; ring.post(() -> Fx.confetti(a, true)); }
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(dp(a,150), dp(a,150));
        rp.gravity = Gravity.CENTER_HORIZONTAL; rp.topMargin = dp(a,16);
        c.addView(ring, rp);
        TextView sub = Ui.tv(a, Ui.toAr(String.valueOf(right)) + " صح من " + Ui.toAr(String.valueOf(s.ids.length)), 14, t.disp(), t.muted());
        sub.setGravity(Gravity.CENTER);
        c.addView(sub);
        LinearLayout grid = new LinearLayout(a);
        int[][] cells = {{right, 0}, {wrong, 1}, {blank, 2}};
        String[] names = {"صح","غلط","بدون إجابة"};
        for (int i = 0; i < 3; i++) {
            LinearLayout cell = new LinearLayout(a);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);
            cell.setBackground(Ui.round(t.dark ? Theme.D_CARD : 0x66ffffff, dp(a,14)));
            cell.setPadding(dp(a,4), dp(a,12), dp(a,4), dp(a,12));
            TextView n = Ui.tv(a, Ui.toAr(String.valueOf(cells[i][0])), 24, t.dispB(), t.ink());
            TextView l = Ui.tv(a, names[i], 12, t.disp(), t.muted());
            cell.addView(n); cell.addView(l);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0,-2,1f);
            if (i > 0) cp.setMarginStart(dp(a,8));
            grid.addView(cell, cp);
        }
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(-1,-2);
        gp.topMargin = dp(a,12);
        c.addView(grid, gp);
        {
            LinearLayout tr = new LinearLayout(a);
            tr.setOrientation(LinearLayout.HORIZONTAL);
            tr.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            android.widget.ImageView ic = new android.widget.ImageView(a);
            ic.setImageResource(R.drawable.ic_clock);
            ic.setColorFilter(t.ink());
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(a,16), dp(a,16));
            ip.setMarginEnd(dp(a, 4));
            tr.addView(ic, ip);
            TextView lb = Ui.tv(a, "وقت الحل:", 13, t.disp(), t.ink());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
            lp.setMarginEnd(dp(a, 4));
            tr.addView(lb, lp);
            tr.addView(Ui.timeView(a, s.resultElapsed >= 0 ? s.resultElapsed : s.elapsed, 13, t.disp(), t.ink()));
            add(c, tr, 12);
        }
        // Demo order: الرجوع للرئيسية (primary) first, then مراجعة كل
        // الإجابات (primary), then مراجعة الغلط فقط (ghost).
        Button home = btn(a, "الرجوع للرئيسية", true);
        home.setOnClickListener(v -> a.goHomeToList());
        add(c, home, 12);
        Button all = btn(a, "مراجعة كل الإجابات", true);
        all.setOnClickListener(v -> a.openReview("all"));
        add(c, all, 8);
        Button wr = btn(a, "مراجعة الغلط فقط (" + Ui.toAr(String.valueOf(wrong)) + ")", false);
        wr.setEnabled(wrong > 0);
        wr.setAlpha(wrong > 0 ? 1f : .35f);
        wr.setOnClickListener(v -> a.openReview("wrong"));
        add(c, wr, 8);
    }

    static void quizEmpty(MainActivity a, LinearLayout c) {
        add(c, Ui.tv(a, "اختار طريقة التجربة من الرئيسية الأول.", 15, a.theme.body(), a.theme.ink()), 18);
        Button b = btn(a, "إعداد التجربة", true);
        b.setOnClickListener(v -> a.goHome());
        add(c, b, 14);
    }

    // ================= QUIZ =================
    static void quiz(MainActivity a, LinearLayout c) {
        Session s = a.s; Theme t = a.theme;
        a.ensureQs();
        final List<Bank.Question> qs = a.qs;
        // view positions: all, or wrong-only
        final java.util.List<Integer> view = new java.util.ArrayList<>();
        for (int i = 0; i < s.ids.length; i++)
            if (a.review.equals("all") || (s.picked[i] >= 0 && s.picked[i] != qs.get(i).c)) view.add(i);
        if (view.isEmpty()) {
            add(c, homeTitle(a, "مفيش أسئلة هنا"), 16);
            add(c, detail(a, "كل الإجابات صح، مفيش غلط للمراجعة."), 8);
            Button back = btn(a, "ارجع للرئيسية", true);
            back.setOnClickListener(v -> a.goHomeToList());
            add(c, back, 12);
            return;
        }
        int pos = view.indexOf(s.index);
        if (pos < 0) { pos = 0; s.index = view.get(0); }
        final int fpos = pos;
        a.quizView = view; a.quizPos = fpos; // swipe navigation reads these
        final int qi = s.index; // position in ids/qs
        Bank.Question q = qs.get(qi);
        final int chosen = s.picked[qi];
        final boolean answered = chosen >= 0;
        final boolean feedback = s.submitted || s.mode.equals("study");

        if (!a.review.equals("all")) {
            LinearLayout banner = new LinearLayout(a);
            banner.setGravity(Gravity.CENTER_VERTICAL);
            TextView bt = detail(a, "مراجعة الغلط فقط · مراجعة من غير تغيير النتيجة ");
            banner.addView(bt, new LinearLayout.LayoutParams(0,-2,1f));
            Button all = btn(a, "كل أسئلة التجربة", false);
            all.setMinHeight(dp(a,36)); all.setPadding(dp(a,14),dp(a,4),dp(a,14),dp(a,4));
            all.setOnClickListener(v -> a.openReview("all"));
            banner.addView(all);
            c.addView(banner);
        }

        // Focus mode: a small exit control at the top; the timer, chip,
        // progress row, tools and font row are all hidden below.
        if (a.focus) {
            Button exitFocus = btn(a, "إنهاء التركيز", false);
            exitFocus.setOnClickListener(v -> { a.focus = false; a.renderAll(); });
            add(c, exitFocus, 0);
        }

        // Exam name chip at the top of the quiz screen.
        if (!a.focus) {
            String examName = s.run.equals("all") ? (a.sessionBank() != null ? a.sessionBank().name : "") : a.runLabel(s.run);
            TextView chip = Ui.tv(a, examName, 12.5f, t.disp(), t.muted());
            chip.setBackground(Ui.roundStroke(t.card55(), dp(a,999), t.border3(), dp(a,1)));
            chip.setPadding(dp(a,14), dp(a,5), dp(a,14), dp(a,5));
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-2, -2);
            cp.gravity = Gravity.CENTER_HORIZONTAL;
            c.addView(chip, cp);
        }

        // Progress row: the bar with its travelling number circle + map button.
        if (s.steps && !a.focus) {
            LinearLayout row = new LinearLayout(a);
            row.setGravity(Gravity.CENTER_VERTICAL);
            ProgressView pv = new ProgressView(a, t);
            pv.set(fpos, view.size(), t);
            row.addView(pv, new LinearLayout.LayoutParams(0, dp(a,28), 1f));
            android.widget.ImageView map = iconChip(a, false, R.drawable.ic_map, "خريطة الأسئلة");
            map.setOnClickListener(v -> showMap(a, view));
            row.addView(map, new LinearLayout.LayoutParams(dp(a,40), dp(a,40)));
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, dp(a,40));
            c.addView(row, rp);
        }

        // Question (LTR, bigger font + more line spacing).
        TextView qv = Ui.tv(a, q.q, t.qSize(), t.bodyB(), t.head());
        qv.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        qv.setTextIsSelectable(true);
        qv.setLineSpacing(0, 1.7f);
        add(c, qv, 4);
        if (a.bank.missingImage(q.gid))
            add(c, detail(a, "الصورة المشار إليها غير موجودة في المصدر."), 8);

        // Tools row: translate, flag, audio (with waveform), explain. In
        // study mode the audio / explanation / translation icons work BEFORE
        // answering; in exam mode they stay after-answer only.
        // The web demo wraps its one tools row (flex-wrap) into two visual
        // rows — a single Android row overflows the screen and pushes the
        // last tools off the left edge, which is why the phone showed only 6
        // tools with no hint button and no تركيز. Two rows here, same order:
        // row 1 translate / flag / audio / explain, row 2 hint / focus /
        // share. (Share used to sit on row 1; the row's fixed-size children
        // already fill a 360dp phone, so the share button was squeezed to a
        // ~22dp sliver — invisible on a 320dp phone.)
        LinearLayout tools = new LinearLayout(a);
        tools.setGravity(Gravity.CENTER_VERTICAL);
        android.widget.ImageView tr = iconChip(a, a.trOpen, R.drawable.ic_translate, "الترجمة");
        tr.setOnClickListener(v -> { a.trOpen = !a.trOpen; a.renderAll(); });
        tools.addView(tr, new LinearLayout.LayoutParams(dp(a,44), dp(a,44)));
        {
            boolean flagged = s.favorites.contains(q.gid);
            android.widget.ImageView fl = iconChip(a, flagged, R.drawable.ic_flag, "راجعني");
            // Demo flag: a gold border always, gold fill when flagged (web
            // .qz-flag-btn) — the generic chip's grey border is not the demo.
            android.graphics.drawable.GradientDrawable fg = new android.graphics.drawable.GradientDrawable();
            fg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            if (flagged) { fg.setColor(t.dark ? Theme.D_GOLD : Theme.L_GOLD); }
            else { fg.setColor(t.dark ? Theme.D_CARD : 0x8cffffff); fg.setStroke(dp(a,1.5f), t.dark ? Theme.D_GOLD : Theme.L_GOLD); }
            fl.setBackground(fg);
            fl.setColorFilter(flagged ? 0xffffffff : t.ink());
            fl.setOnClickListener(v -> { if (!s.favorites.remove(q.gid)) s.favorites.add(q.gid); a.save(); a.renderAll(); });
            LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(dp(a,44), dp(a,44));
            fp.setMarginStart(dp(a,6));
            tools.addView(fl, fp);
        }
        String clipFile = ClipPlayer.exists(a, "g" + q.gid + ".mp3") ? "g" + q.gid + ".mp3" : null;
        boolean preAnswer = s.mode.equals("study") && !s.submitted;
        if (clipFile != null && feedback && (answered || s.submitted || preAnswer)) {
            final String cf = clipFile;
            boolean on0 = ClipPlayer.isPlaying(cf);
            android.widget.ImageView sp = iconChip(a, on0, on0 ? R.drawable.ic_stop : R.drawable.ic_audio, on0 ? "إيقاف الشرح الصوتي" : "اسمع الشرح");
            final WaveView wv = new WaveView(a, t.dark ? Theme.D_GOLD : Theme.L_GOLD);
            if (on0) wv.start();
            sp.setOnClickListener(v -> {
                boolean on = ClipPlayer.toggle(a, cf, s.volume, () -> { setChip(a, sp, false, R.drawable.ic_audio); wv.stop(); });
                setChip(a, sp, on, on ? R.drawable.ic_stop : R.drawable.ic_audio);
                sp.setContentDescription(on ? "إيقاف الشرح الصوتي" : "اسمع الشرح");
                if (on) wv.start(); else wv.stop();
            });
            LinearLayout.LayoutParams spp = new LinearLayout.LayoutParams(dp(a,52), dp(a,52));
            spp.setMarginStart(dp(a,6));
            tools.addView(sp, spp);
            LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(-2, dp(a,28));
            wp.setMarginStart(dp(a,6));
            wp.gravity = Gravity.CENTER_VERTICAL;
            tools.addView(wv, wp);
        }
        // Stop the clip of another question only when one is actually playing.
        if (ClipPlayer.isAnyPlaying() && !ClipPlayer.isPlaying("g" + q.gid + ".mp3")) ClipPlayer.stop();
        if (feedback && (answered || s.submitted || preAnswer)) {
            Button ex = toolBtn(a, "الشرح");
            ex.setTextSize(11f); ex.setPadding(dp(a,12),dp(a,6),dp(a,12),dp(a,6));
            LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(-2, dp(a,44));
            ep.setMarginStart(dp(a,6));
            ex.setOnClickListener(v -> showExplain(a));
            tools.addView(ex, ep);
        }
        LinearLayout tools2 = new LinearLayout(a);
        tools2.setGravity(Gravity.CENTER_VERTICAL);
        if (s.mode.equals("study") && !s.submitted && !answered && s.hints) {
            android.widget.ImageView hint = iconChip(a, a.hintOpen, R.drawable.ic_bulb, "التلميح");
            hint.setOnClickListener(v -> { a.hintOpen = !a.hintOpen; a.renderAll(); });
            LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(dp(a,44), dp(a,44));
            tools2.addView(hint, hp);
        }
        {
            Button foc = toolBtn(a, "تركيز");
            foc.setTextSize(11f); foc.setPadding(dp(a,12),dp(a,6),dp(a,12),dp(a,6));
            LinearLayout.LayoutParams fop = new LinearLayout.LayoutParams(-2, dp(a,44));
            fop.setMarginStart(dp(a,6));
            foc.setOnClickListener(v -> { a.focus = true; a.renderAll(); });
            tools2.addView(foc, fop);
        }
        if (feedback && (answered || s.submitted || preAnswer)) {
            // Share the question as a designed image (same gate as the
            // explanation — the image carries the answer). Lives on the
            // second tools row: row 1 is already full on a 360dp phone.
            Button share = toolBtn(a, "مشاركة");
            share.setTextSize(11f); share.setPadding(dp(a,12),dp(a,6),dp(a,12),dp(a,6));
            LinearLayout.LayoutParams shp = new LinearLayout.LayoutParams(-2, dp(a,44));
            shp.setMarginStart(dp(a,6));
            share.setOnClickListener(v -> a.shareQuestion());
            tools2.addView(share, shp);
        }
        if (!a.focus) { add(c, tools, 4); add(c, tools2, 4); }

        if (a.trOpen && q.tr != null) {
            TextView trv = Ui.tv(a, q.tr, t.bodySize(), t.body(), t.bodyColor());
            styleBubble(a, trv, false);
            add(c, trv, 10);
        }
        if (s.mode.equals("study") && !s.submitted && !answered && s.hints && a.hintOpen && q.hint != null) {
            TextView hv = Ui.tv(a, q.hint, t.bodySize(), t.bodyB(), t.bodyColor());
            styleBubble(a, hv, true);
            add(c, hv, 12);
        }

        // Options (LTR), with A/B/C/D letters. Green = correct, red = wrong —
        // that is the entire feedback; no praise words anywhere.
        LinearLayout opts = new LinearLayout(a);
        opts.setOrientation(LinearLayout.VERTICAL);
        final char[] L = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        for (int i = 0; i < q.o.length; i++) {
            final int fi = i;
            boolean correct = i == q.c;
            boolean selected = i == chosen;
            boolean locked = s.submitted || s.expired() || (s.mode.equals("study") && answered && !(s.retry && chosen != q.c));
            LinearLayout opt = new LinearLayout(a);
            opt.setOrientation(LinearLayout.HORIZONTAL);
            opt.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            opt.setGravity(Gravity.CENTER_VERTICAL);
            opt.setPadding(dp(a,12), dp(a,8), dp(a,12), dp(a,8));
            opt.setMinimumHeight(dp(a, s.spacious ? 60 : 48));
            boolean showCorrect = feedback && (answered || s.submitted) && correct;
            boolean showWrong = feedback && selected && !correct;
            boolean isPending = (a.pending != null && a.pending == i) || (!feedback && selected);
            int bg = t.card75(), border = Color.TRANSPARENT;
            if (showCorrect) { bg = Theme.L_CORRECT; border = Theme.L_CORRECT; }
            else if (showWrong) { bg = t.dark ? Theme.D_CARD : Theme.L_REDSOFT; border = t.dark ? Theme.D_MISSED : Theme.L_RED; }
            else if (isPending) { bg = t.pendingBg(); border = t.dark ? Theme.D_GOLD : Theme.L_GOLD; } // gold border on the selected/pending answer
            if (s.contrast && !showCorrect && !showWrong) { bg = t.dark ? Theme.D_CARD : 0xffffffff; border = t.dark ? Theme.D_TEXT : 0xff4a7a70; }
            opt.setBackground(Ui.roundStroke(bg, dp(a,999), border, dp(a,1.5f)));
            if (s.letters) {
                TextView letter = Ui.tv(a, String.valueOf(L[i]), 12.5f * t.scale, t.disp(), showCorrect ? Theme.L_CORRECT : (showWrong ? 0xffffffff : t.muted()));
                GradientDrawable lc = new GradientDrawable();
                lc.setShape(GradientDrawable.OVAL);
                lc.setColor(showCorrect ? Theme.L_BG : (showWrong ? (t.dark?Theme.D_MISSED:Theme.L_RED) : (t.dark ? 0x22D4A24C : 0x1a0b4f4a)));
                letter.setBackground(lc);
                letter.setGravity(Gravity.CENTER);
                opt.addView(letter, new LinearLayout.LayoutParams(dp(a,28), dp(a,28)));
            }
            TextView ot = Ui.tv(a, q.o[i], t.bodySize(), t.bodyM(), showCorrect ? Theme.L_BG : t.optText());
            ot.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            LinearLayout.LayoutParams op = new LinearLayout.LayoutParams(0,-2,1f);
            op.setMargins(dp(a,12),0,0,0);
            opt.addView(ot, op);
            if (showCorrect) { TextView mk = Ui.tv(a, "✓", 16, t.disp(), Theme.L_BG); opt.addView(mk); }
            else if (showWrong) { TextView mk = Ui.tv(a, "✕", 16, t.disp(), t.dark?Theme.D_MISSED:0xffb3242b); opt.addView(mk); }
            if (a.pending != null && a.pending == i) Fx.pop(a, opt, 120);
            else if (a.popOnce && selected && (showCorrect || showWrong)) Fx.pop(a, opt, 120);
            else if (a.popOnce && showCorrect && correct && feedback) Fx.pop(a, opt, 120);
            if (!locked) opt.setOnClickListener(v -> {
                // Double-tap an option confirms it instantly (confirm mode only).
                long now = System.currentTimeMillis();
                if (s.confirm && a.pending != null && a.pending == fi && a.lastOptTapIdx == fi && now - a.lastOptTapAt < 350) {
                    a.lastOptTapIdx = -1;
                    a.commit(fi);
                    return;
                }
                a.lastOptTapIdx = fi; a.lastOptTapAt = now;
                if (s.confirm) { a.pending = fi; a.pendingByQuestion.put(s.index, fi); a.renderAll(); } else a.commit(fi);
            });
            LinearLayout.LayoutParams olp = new LinearLayout.LayoutParams(-1,-2);
            olp.topMargin = dp(a, s.spacious ? 12 : 4);
            opts.addView(opt, olp);
        }
        a.popOnce = false;
        add(c, opts, 8);

        if (s.expired() && !s.submitted)
            add(c, hintBox(a, "الوقت خلص. الإجابات اتقفلت، سلّم التجربة عشان تشوف النتيجة، أو صفّر العداد من الإعدادات."), 12);

        if (s.confirm && a.pending != null && !s.expired() && !s.submitted) {
            Button cb = goldBtn(a, "تأكيد الإجابة"); // full-width gold confirm
            cb.setOnClickListener(v -> a.commit(a.pending));
            add(c, cb, 6);
        }

        // Visible السابق/التالي buttons — swipe stays as an extra, not the
        // only way. Disabled (not hidden) at the first/last question.
        // Same navigation as swipeQuiz, so no new navigation logic.
        {
            LinearLayout nav = new LinearLayout(a);
            // RTL row: السابق first (right/start), التالي second (left/end).
            Button prev = btn(a, "السابق", false);
            prev.setEnabled(fpos > 0);
            prev.setAlpha(fpos > 0 ? 1f : .35f);
            prev.setOnClickListener(v -> a.swipeQuiz(-1));
            Button next = btn(a, "التالي", false);
            next.setEnabled(fpos < view.size() - 1);
            next.setAlpha(fpos < view.size() - 1 ? 1f : .35f);
            next.setOnClickListener(v -> a.swipeQuiz(+1));
            nav.addView(prev, new LinearLayout.LayoutParams(0, -2, 1f));
            LinearLayout.LayoutParams nn = new LinearLayout.LayoutParams(0, -2, 1f);
            nn.setMarginStart(dp(a, 10));
            nav.addView(next, nn);
            add(c, nav, 6);
        }
        Button cta = btn(a, s.submitted ? "الرجوع للرئيسية" : "إنهاء وعرض النتيجة", true);
        cta.setOnClickListener(v -> { if (s.submitted) a.goHomeToList(); else a.finishQuiz(); });
        add(c, cta, 6);
    }

    /** Circular icon chip with a real vector icon + contentDescription (TalkBack). */
    static android.widget.ImageView iconChip(MainActivity a, boolean open, int res, String desc) {
        android.widget.ImageView iv = new android.widget.ImageView(a);
        iv.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        setChip(a, iv, open, res);
        iv.setPadding(dp(a,11), dp(a,11), dp(a,11), dp(a,11));
        iv.setContentDescription(desc);
        return iv;
    }
    /** Flip an icon chip's visual state in place (no full renderAll). */
    static void setChip(MainActivity a, android.widget.ImageView iv, boolean open, int res) {
        Theme t = a.theme;
        iv.setImageResource(res);
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        if (open) { g.setColor(t.dark ? Theme.D_TEXT : Theme.L_TEAL); }
        else { g.setColor(t.dark ? Theme.D_CARD : 0x8cffffff); g.setStroke(dp(a,1.5f), t.dark ? Theme.D_BORDER4 : 0x4d0b4f4a); }
        iv.setBackground(g);
        iv.setColorFilter(open ? (t.dark ? Theme.L_TEAL : Theme.L_BG) : t.ink());
    }
    static Button toolBtn(MainActivity a, String txt) {
        Theme t = a.theme;
        Button b = new Button(a);
        b.setText(txt); b.setAllCaps(false); b.setTextSize(12.5f); b.setTypeface(t.disp());
        b.setTextColor(t.ink());
        b.setBackground(Ui.roundStroke(t.card55(), dp(a,999), t.border3(), dp(a,1.5f)));
        b.setMinHeight(dp(a,44)); b.setStateListAnimator(null);
        b.setPadding(dp(a,14),dp(a,6),dp(a,14),dp(a,6));
        return b;
    }
    static void styleBubble(MainActivity a, TextView v, boolean goldBar) {
        Theme t = a.theme;
        v.setBackground(Ui.round(t.dark ? Theme.D_CARD : 0x99ffffff, dp(a,14)));
        v.setPadding(dp(a,14), dp(a,10), dp(a,14), dp(a,10));
        v.setLineSpacing(0, 1.75f);
    }
    static TextView hintBox(MainActivity a, String msg) {
        TextView v = Ui.tv(a, msg, 12.5f, a.theme.bodyB(), a.theme.bodyColor());
        styleBubble(a, v, true);
        return v;
    }

    // ================= question map dialog =================
    static void showMap(MainActivity a, java.util.List<Integer> view) {
        Session s = a.s; Theme t = a.theme;
        Dialog d = new Dialog(a);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ScrollView sv = new ScrollView(a);
        LinearLayout box = new LinearLayout(a);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(a,20), dp(a,20), dp(a,20), dp(a,20));
        sv.addView(box);
        TextView title = Ui.tv(a, "خريطة الأسئلة", 13.5f, t.disp(), t.ink());
        box.addView(title);
        int cols = Math.max(4, (a.getResources().getDisplayMetrics().widthPixels - dp(a, 80)) / dp(a, 54));
        boolean feedback = s.submitted || s.mode.equals("study");
        LinearLayout row = null;
        for (int p = 0; p < view.size(); p++) {
            final int qi = view.get(p);
            if (p % cols == 0) {
                row = new LinearLayout(a);
                box.addView(row, new LinearLayout.LayoutParams(-1,-2));
            }
            TextView dot = new TextView(a);
            String num = Ui.toAr(String.valueOf(p + 1));
            dot.setText(num + (s.picked[qi] >= 0 ? " •" : ""));
            dot.setTextSize(12); dot.setTypeface(t.disp()); dot.setGravity(Gravity.CENTER);
            boolean cur = qi == s.index;
            int bgc = t.dark ? Theme.D_CARD : 0xb3ffffff, tc = t.ink(), bc = t.border3();
            if (feedback && s.picked[qi] >= 0) {
                if (s.picked[qi] == a.qs.get(qi).c) { bgc = Theme.L_CORRECT; tc = Theme.L_BG; bc = Theme.L_CORRECT; }
                else { bgc = t.dark ? Theme.D_CARD : Theme.L_REDSOFT; tc = t.dark ? t.missed() : 0xffb3242b; bc = t.missed(); }
            }
            dot.setBackground(Ui.roundStroke(bgc, dp(a,999), cur ? t.ink() : bc, dp(a, cur ? 2 : 1.5f)));
            dot.setTextColor(tc);
            dot.setOnClickListener(v -> { s.index = qi; a.trReset(); a.save(); d.dismiss(); a.renderAll(); });
            LinearLayout.LayoutParams dp2 = new LinearLayout.LayoutParams(0, dp(a,44), 1f);
            dp2.setMargins(dp(a,4), dp(a,10), dp(a,4), 0);
            row.addView(dot, dp2);
        }
        Button close = btn(a, "قفل الخريطة", false);
        close.setOnClickListener(v -> d.dismiss());
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1,-2);
        cp.topMargin = dp(a,16);
        box.addView(close, cp);
        d.setContentView(sv);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawable(Ui.round(t.sheet(), dp(a,22)));
            d.getWindow().setLayout(-1, (int) (a.getResources().getDisplayMetrics().heightPixels * 0.85));
        }
        d.show();
    }

    // ================= explain dialog =================
    // R7 — part 1 DECIDED 2026-11-26 by Mohamed: the generated-audio note
    // is removed; both clips use the same audio button label. The source
    // line below ("المصدر: …") is preserved — R7's other half, still
    // pending his decision.
    static void showExplain(MainActivity a) {
        Session s = a.s; Theme t = a.theme;
        Bank.Question q = a.qs.get(s.index);
        Dialog d = new Dialog(a);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ScrollView sv = new ScrollView(a);
        LinearLayout box = new LinearLayout(a);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(a,20), dp(a,24), dp(a,20), dp(a,20));
        sv.addView(box);
        // No praise/feedback words after answering: green/red colours on the
        // options are the entire feedback. The dialog title stays neutral.
        box.addView(Ui.tv(a, "الشرح", t.smallSize() * 1.125f, t.disp(), t.dark ? Theme.D_SOFT : Theme.L_TEAL));
        if (q.ex != null) {
            TextView ex = Ui.tv(a, q.ex, t.bodySize(), t.body(), t.bodyColor());
            ex.setLineSpacing(0, 1.75f);
            LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(-1,-2);
            ep.topMargin = dp(a,10);
            box.addView(ex, ep);
        }
        if (q.why != null) {
            TextView why = Ui.tv(a, q.why, t.smallSize(), t.bodyB(), t.muted());
            why.setLineSpacing(0, 1.85f);
            LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(-1,-2);
            wp.topMargin = dp(a,12);
            box.addView(why, wp);
        }
        if (q.url != null && !q.url.isEmpty()) {
            TextView src = Ui.tv(a, "المصدر: " + (q.src != null ? q.src : q.url), t.smallSize(), t.bodyB(), t.dark ? Theme.D_SOFT : t.muted());
            LinearLayout.LayoutParams sp2 = new LinearLayout.LayoutParams(-1,-2);
            sp2.topMargin = dp(a,12);
            box.addView(src, sp2);
            src.setOnClickListener(v -> {
                try { a.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(q.url))); } catch (Exception ignore) {}
            });
        }
        addAudio(a, box, q, "g" + q.gid + ".mp3", "استمع للشرح");
        d.setOnDismissListener(x -> ClipPlayer.stop());
        Button close = btn(a, "قفل الشرح", false);
        close.setOnClickListener(v -> d.dismiss());
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1,-2);
        cp.topMargin = dp(a,16);
        box.addView(close, cp);
        d.setContentView(sv);
        if (d.getWindow() != null) {
            // The explanation is a separate card with a light gold border.
            // The written explanation, why, source and audio buttons inside
            // it are exactly as before.
            d.getWindow().setBackgroundDrawable(Ui.roundStroke(t.sheet(), dp(a,22), t.dark ? Theme.D_GOLD : Theme.L_GOLD, dp(a,1.5f)));
            d.getWindow().setLayout(-1, (int) (a.getResources().getDisplayMetrics().heightPixels * 0.85));
        }
        d.show();
    }

    private static Button activeAudioBtn; private static String activeAudioCap;
    private static WaveView activeAudioWave;

    static void addAudio(MainActivity a, LinearLayout box, Bank.Question q, String file, String caption) {
        if (!ClipPlayer.exists(a, file)) return;
        // The explanation audio is a large prominent gold button with a
        // waveform while playing. The written explanation, why and source
        // above it are untouched — the audio button is an addition, never a
        // replacement.
        final Button b = goldBtn(a, "▶  " + caption);
        b.setTextSize(15f); b.setMinHeight(dp(a,52)); b.setMinimumHeight(dp(a,52));
        final WaveView wv = new WaveView(a, a.theme.dark ? Theme.D_GOLD : Theme.L_GOLD);
        b.setOnClickListener(v -> {
            // starting this clip stops the other one, so reset the other button's label too
            if (activeAudioBtn != null && activeAudioBtn != b) { activeAudioBtn.setText("▶  " + activeAudioCap); if (activeAudioWave != null) activeAudioWave.stop(); }
            boolean on = ClipPlayer.toggle(a, file, a.s.volume, () -> { b.setText("▶  " + caption); wv.stop(); });
            b.setText((on ? "■  " : "▶  ") + caption);
            if (on) wv.start(); else wv.stop();
            activeAudioBtn = on ? b : null; activeAudioCap = caption; activeAudioWave = on ? wv : null;
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin = dp(a,14);
        LinearLayout row = new LinearLayout(a);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(b, new LinearLayout.LayoutParams(0, -2, 1f));
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(-2, dp(a,28));
        wp.setMarginStart(dp(a,8));
        row.addView(wv, wp);
        box.addView(row, lp);
    }

    // ================= SETTINGS =================
    // R5 — PENDING MOHAMED'S DECISION, preserved exactly as in the current
    // build: the settings intro line and every toggle's detail paragraph
    // below. Do not remove, do not add more, until he decides.
    interface Toggle { void run(boolean on); }

    static void settings(MainActivity a, LinearLayout c) {
        Session s = a.s; Theme t = a.theme;
        add(c, Ui.tv(a, "على مزاجك", 23, t.dispB(), t.head()), 16);
        add(c, Ui.tv(a, "تحكم في تجربتك. التغييرات بتتطبق فورًا.", 13, t.body(), t.ink()), 4);

        groupTitle(a, c, "العرض والقراءة");
        toggleRow(a, c, "الوضع الليلي", "خلفيات غامقة مع الذهبي والعاجي.", s.dark, on -> { s.dark = on; a.theme.dark = on; });
        fontSlider(a, c);
        toggleRow(a, c, "مسافات أوسع", "مساحة أكبر بين الاختيارات للقراءة واللمس.", s.spacious, on -> s.spacious = on);
        toggleRow(a, c, "تباين أعلى", "خلفيات أوضح وحدود أقوى للاختيارات.", s.contrast, on -> s.contrast = on);
        toggleRow(a, c, "تقليل الحركة", "من غير حركات انتقال أو نبضات.", s.calm, on -> s.calm = on);
        toggleRow(a, c, "شريط التقدم", "إظهار رقم السؤال على الخط وزر خريطة الأسئلة.", s.steps, on -> s.steps = on);
        toggleRow(a, c, "حروف الاختيارات", "إظهار A وB وباقي الحروف جنب الاختيارات.", s.letters, on -> s.letters = on);

        groupTitle(a, c, "طريقة الحل");
        toggleRow(a, c, "تأكيد الاختيار", "اختار إجابة، وبعدها اضغط تأكيد الإجابة. في الامتحان التصحيح بيتأجل للنهاية.", s.confirm, on -> s.confirm = on);
        toggleRow(a, c, "انتقال تلقائي", "بعد الإجابة بثانيتين، افتح السؤال اللي بعده. يتوقف مع الشرح التلقائي.", s.autoNext, on -> s.autoNext = on);
        toggleRow(a, c, "إعادة محاولة الغلط", "يسمح بتغيير الإجابة الغلط. النتيجة بتتحدّث في وضع المذاكرة فقط.", s.retry, on -> s.retry = on);

        groupTitle(a, c, "مساعدات المذاكرة");
        toggleRow(a, c, "إتاحة التلميح", "إظهار أو إخفاء زر التلميح قبل الإجابة في وضع المذاكرة.", s.hints, on -> s.hints = on);
        toggleRow(a, c, "تلميح مع كل سؤال", "يفتح تلقائيًا عند دخول السؤال لو التلميح متاح.", s.hintDefault, on -> s.hintDefault = on);
        toggleRow(a, c, "ترجمة مع كل سؤال", "تظهر عند دخول السؤال. تقدر تقفلها من زر الترجمة.", s.translation, on -> { s.translation = on; a.trOpen = on; });
        toggleRow(a, c, "شرح بعد الإجابة", "في المذاكرة فقط: يفتح شرح الإجابة تلقائيًا، من غير انتقال تلقائي.", s.autoExplain, on -> s.autoExplain = on);

        groupTitle(a, c, "الوقت");
        toggleRow(a, c, "إظهار العداد", "إخفاؤه مش بيوقف حساب الوقت.", s.timer, on -> s.timer = on);
        toggleRow(a, c, "وقت الحل بس", "العداد يقف في الرئيسية والإعدادات.", s.quizOnly, on -> s.quizOnly = on);
        toggleRow(a, c, "عد تنازلي", "بدل الوقت المنقضي. عند الصفر يقفل الإجابات وتفضل المراجعة متاحة.", s.countdown, on -> s.countdown = on);
        minutesSeg(a, c);
        add(c, detail(a, "المدة محسوبة من بداية العداد. صفّره لو عايز تبدأ مدة كاملة."), 6);
        LinearLayout clockRow = new LinearLayout(a);
        Button pause = btn(a, s.paused ? "تشغيل العداد" : "إيقاف مؤقت", false);
        pause.setOnClickListener(v -> { s.paused = !s.paused; a.save(); a.renderAll(); });
        Button zero = btn(a, "تصفير العداد", false);
        zero.setOnClickListener(v -> { s.elapsed = 0; a.save(); a.renderAll(); });
        clockRow.addView(pause, new LinearLayout.LayoutParams(0,-2,1f));
        LinearLayout.LayoutParams zp = new LinearLayout.LayoutParams(0,-2,1f);
        zp.setMarginStart(dp(a,10));
        clockRow.addView(zero, zp);
        add(c, clockRow, 10);

        groupTitle(a, c, "الصوت والمؤثرات");
        toggleRow(a, c, "المؤثرات الصوتية", "صوت للإجابة الصح وصوت للإجابة الغلط في المذاكرة فقط. في الامتحان مفيش أي صوت، عشان ما يكشفش الإجابة.", s.sounds, on -> s.sounds = on);
        toggleRow(a, c, "حركة الاحتفال والاختيار", "نبضة خفيفة عند اختيار إجابة واحتفال صغير مع النتيجة العالية. بيتعطل تلقائيًا مع تقليل الحركة.", s.fx, on -> s.fx = on);
        volumeRow(a, c);

        groupTitle(a, c, "نسخة احتياطية");
        add(c, detail(a, "بيطلع كود فيه إجاباتك ودفتر الغلطات والعلامات والنتايج والإعدادات. انسخه واحفظه في مكان، وارجع الصقه هنا أو في النسخة الأونلاين."), 4);
        Button mk = btn(a, "جهّز النسخة", true);
        mk.setOnClickListener(v -> {
            try {
                final String code = Backup.make(a);
                android.widget.EditText et = new android.widget.EditText(a);
                et.setText(code); et.setTextSize(11); et.setLayoutDirection(View.LAYOUT_DIRECTION_LTR); et.setKeyListener(null);
                et.setMaxLines(6);
                new android.app.AlertDialog.Builder(a).setTitle("كود النسخة الاحتياطية").setView(et)
                    .setPositiveButton("انسخ الكود", (dd, w) -> {
                        android.content.ClipboardManager cm = (android.content.ClipboardManager) a.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                        cm.setPrimaryClip(android.content.ClipData.newPlainText("QH", code));
                        android.widget.Toast.makeText(a, "اتنسخ الكود.", android.widget.Toast.LENGTH_SHORT).show();
                    }).setNegativeButton("قفل", null).show();
            } catch (Exception e) { android.widget.Toast.makeText(a, "معرفتش أجهّز النسخة.", android.widget.Toast.LENGTH_SHORT).show(); }
        });
        add(c, mk, 8);
        Button rs = btn(a, "استرجاع من كود", false);
        rs.setOnClickListener(v -> {
            android.widget.EditText et = new android.widget.EditText(a);
            et.setHint("الصق الكود هنا"); et.setTextSize(11); et.setLayoutDirection(View.LAYOUT_DIRECTION_LTR); et.setMaxLines(6);
            new android.app.AlertDialog.Builder(a).setTitle("استرجاع").setMessage("ده هيستبدل التقدم الحالي على الجهاز ده بالنسخة اللي لصقتها.").setView(et)
                .setPositiveButton("استرجاع", (dd, w) -> {
                    final String code = et.getText().toString();
                    new android.app.AlertDialog.Builder(a).setTitle("تأكيد الاسترجاع")
                        .setMessage("متأكد؟ التقدم الحالي هيتبدل بالنسخة اللي لصقتها.")
                        .setPositiveButton("استبدل", (dd2, w2) -> {
                            String m = Backup.restore(a, code);
                            android.widget.Toast.makeText(a, m, android.widget.Toast.LENGTH_LONG).show();
                        }).setNegativeButton("رجوع", null).show();
                }).setNegativeButton("رجوع", null).show();
        });
        add(c, rs, 8);
        if (Session.hasBeforeRestore(a)) {
            Button undo = btn(a, "تراجع عن آخر استرجاع", false);
            undo.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(a).setMessage("هيرجع التقدم اللي كان موجود قبل آخر استرجاع.")
                    .setPositiveButton("ارجع", (dd, w) -> a.undoLastRestore())
                    .setNegativeButton("رجوع", null).show();
            });
            add(c, undo, 8);
        }
        final String legacy = Session.legacyRaw(a);
        if (legacy != null) {
            add(c, detail(a, "فيه تقدم محفوظ من نسخة تانية (مش متحوّل). تقدر تصدّره وتحفظه."), 6);
            Button lg = btn(a, "تصدير البيانات القديمة", false);
            lg.setOnClickListener(v -> {
                android.widget.EditText et = new android.widget.EditText(a);
                et.setText(legacy); et.setTextSize(11); et.setLayoutDirection(View.LAYOUT_DIRECTION_LTR); et.setKeyListener(null); et.setMaxLines(6);
                new android.app.AlertDialog.Builder(a).setTitle("البيانات القديمة الأصلية").setView(et)
                    .setPositiveButton("انسخ", (dd, w) -> {
                        android.content.ClipboardManager cm = (android.content.ClipboardManager) a.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                        cm.setPrimaryClip(android.content.ClipData.newPlainText("QH-legacy", legacy));
                        android.widget.Toast.makeText(a, "اتنسخت البيانات القديمة.", android.widget.Toast.LENGTH_SHORT).show();
                    }).setNegativeButton("قفل", null).show();
            });
            add(c, lg, 8);
        }

        groupTitle(a, c, "إعادة الضبط");
        Button dflt = btn(a, "استعادة الإعدادات الافتراضية", false);
        dflt.setOnClickListener(v -> { resetSettings(a); });
        add(c, dflt, 6);
        Button reset = btn(a, "إعادة التجربة من الأول", true);
        reset.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(a)
                .setMessage("هتمسح إجابات التجربة وتصفر الوقت. الإعدادات هتفضل زي ما هي.")
                .setPositiveButton("امسح وابدأ", (dd, w) -> a.reset())
                .setNegativeButton("رجوع", null)
                .show();
        });
        add(c, reset, 8);
        add(c, new View(a), 24);
    }

    static void resetSettings(MainActivity a) {
        Session s = a.s;
        s.dark=false; s.spacious=false; s.contrast=false; s.calm=false; s.steps=true; s.letters=true;
        s.confirm=false; s.autoNext=false; s.retry=false; s.hints=true; s.hintDefault=false;
        s.translation=false; s.autoExplain=false; s.timer=true; s.quizOnly=false; s.countdown=false;
        s.fontPercent=100; s.sounds=true; s.fx=true; s.volume=80; s.minutes=120;
        a.save(); a.renderAll();
    }

    static void groupTitle(MainActivity a, LinearLayout c, String t) {
        add(c, Ui.tv(a, t, 15, a.theme.disp(), a.theme.head()), 18);
    }

    static void toggleRow(MainActivity a, LinearLayout c, String labelTxt, String detailTxt, boolean on, Toggle tg) {
        Theme t = a.theme;
        LinearLayout row = new LinearLayout(a);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(Ui.roundStroke(t.card(), dp(a,16), t.border(), dp(a,1.5f)));
        row.setPadding(dp(a,14), dp(a,10), dp(a,14), dp(a,10));
        LinearLayout texts = new LinearLayout(a);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.addView(Ui.tv(a, labelTxt, 13, t.disp(), t.head()));
        TextView d = detail(a, detailTxt);
        texts.addView(d);
        row.addView(texts, new LinearLayout.LayoutParams(0,-2,1f));
        android.widget.Switch sw = new android.widget.Switch(a);
        sw.setChecked(on);
        sw.setOnCheckedChangeListener((v, isOn) -> { tg.run(isOn); a.save(); a.renderAll(); });
        row.addView(sw);
        add(c, row, 10);
    }

    static void volumeRow(MainActivity a, LinearLayout c) {
        Session s = a.s; Theme t = a.theme;
        LinearLayout box = (LinearLayout) card(a);
        TextView pctTv = Ui.tv(a, "مستوى الصوت · " + Ui.lre(Ui.toAr(String.valueOf(s.volume)) + "٪"), 15, t.disp(), t.head());
        box.addView(pctTv);
        android.widget.SeekBar seek = new android.widget.SeekBar(a);
        seek.setMax(20); seek.setProgress(s.volume / 5);
        LinearLayout.LayoutParams skp = new LinearLayout.LayoutParams(-1,-2);
        skp.topMargin = dp(a,8);
        box.addView(seek, skp);
        Button test = btn(a, "جرّب الصوت", false);
        test.setEnabled(s.sounds);
        test.setOnClickListener(v -> ClipPlayer.sfx(a, true, s.volume));
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1,-2);
        tp.topMargin = dp(a,10);
        box.addView(test, tp);
        seek.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(android.widget.SeekBar sb, int prog, boolean fromUser) {
                s.volume = prog * 5;
                pctTv.setText("مستوى الصوت · " + Ui.lre(Ui.toAr(String.valueOf(s.volume)) + "٪"));
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar sb) {}
            @Override public void onStopTrackingTouch(android.widget.SeekBar sb) { a.save(); if (s.sounds) ClipPlayer.sfx(a, true, s.volume); }
        });
        add(c, box, 10);
    }

    static void fontSlider(MainActivity a, LinearLayout c) {
        Session s = a.s; Theme t = a.theme;
        LinearLayout box = (LinearLayout) card(a);
        LinearLayout lrow = new LinearLayout(a);
        lrow.setOrientation(LinearLayout.HORIZONTAL);
        lrow.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        lrow.addView(label(a, "حجم الخط"));
        TextView dotTv = Ui.tv(a, "·", 13, t.disp(), t.head());
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(-2, -2); dlp.setMarginStart(dp(a, 6)); dlp.setMarginEnd(dp(a, 6));
        lrow.addView(dotTv, dlp);
        TextView pctTv = Ui.tv(a, Ui.lre(Ui.toAr(String.valueOf(s.fontPercent)) + "٪"), 13, t.disp(), t.head());
        pctTv.setTextDirection(View.TEXT_DIRECTION_LTR);
        lrow.addView(pctTv);
        box.addView(lrow);
        android.widget.SeekBar seek = new android.widget.SeekBar(a);
        seek.setMax(55); // 70..125
        seek.setProgress(s.fontPercent - 70);
        LinearLayout.LayoutParams skp = new LinearLayout.LayoutParams(-1,-2);
        skp.topMargin = dp(a,8);
        box.addView(seek, skp);
        LinearLayout labels = new LinearLayout(a);
        TextView big = Ui.tv(a, "كبير", 11, t.body(), t.muted());
        TextView small = Ui.tv(a, "صغير جدًا", 11, t.body(), t.muted());
        labels.addView(small, new LinearLayout.LayoutParams(0,-2,1f));
        big.setGravity(Gravity.END);
        labels.addView(big, new LinearLayout.LayoutParams(0,-2,1f));
        box.addView(labels);
        // preview: question + option + translation + explanation, all scaling together
        LinearLayout prev = new LinearLayout(a);
        prev.setOrientation(LinearLayout.VERTICAL);
        prev.setBackground(Ui.round(t.dark ? Theme.D_CARD : 0x66ffffff, dp(a,14)));
        prev.setPadding(dp(a,12),dp(a,12),dp(a,12),dp(a,12));
        float sc = s.fontPercent / 100f;
        TextView p1 = Ui.tv(a, "The nurse is assessing a child. Which symptom is most common?", 15*sc, t.bodyB(), t.head());
        p1.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        TextView p2 = Ui.tv(a, "A · Ear pain", 15*sc, t.bodyM(), t.optText());
        p2.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        TextView p3 = Ui.tv(a, "الممرض بيفحص طفل. إيه أكتر عرض شائع؟", 15*sc, t.body(), t.bodyColor());
        TextView p4 = Ui.tv(a, "ده شكل الشرح بنفس حجم الخط اللي اخترته.", 15*sc, t.body(), t.bodyColor());
        prev.addView(p1); prev.addView(p2); prev.addView(p3); prev.addView(p4);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1,-2);
        pp.topMargin = dp(a,10);
        box.addView(prev, pp);
        seek.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(android.widget.SeekBar sb, int prog, boolean fromUser) {
                int pct = prog + 70;
                s.fontPercent = pct;
                float sc2 = pct / 100f;
                pctTv.setText(Ui.lre(Ui.toAr(String.valueOf(pct)) + "٪"));
                p1.setTextSize(15*sc2); p2.setTextSize(15*sc2); p3.setTextSize(15*sc2); p4.setTextSize(15*sc2);
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar sb) {}
            @Override public void onStopTrackingTouch(android.widget.SeekBar sb) { a.save(); a.renderAll(); }
        });
        add(c, box, 10);
    }

    static void minutesSeg(MainActivity a, LinearLayout c) {
        Session s = a.s; Theme t = a.theme;
        add(c, label(a, "مدة التجربة"), 10);
        LinearLayout seg = new LinearLayout(a);
        seg.setBackground(Ui.round(t.dark?0x14ffffff:0x140b4f4a, dp(a,999)));
        seg.setPadding(dp(a,3),dp(a,3),dp(a,3),dp(a,3));
        int[] vals = {5,30,60,120,180};
        for (int v2 : vals) {
            Button b = new Button(a);
            b.setText(Ui.toAr(String.valueOf(v2)) + " د"); b.setAllCaps(false); b.setTextSize(12.5f);
            b.setTypeface(t.disp()); b.setMinHeight(dp(a,44)); b.setMinimumHeight(dp(a,44));
            b.setStateListAnimator(null);
            boolean on = s.minutes == v2;
            b.setBackground(Ui.round(on ? t.primary() : Color.TRANSPARENT, dp(a,999)));
            b.setTextColor(on ? t.onAccent() : t.muted());
            b.setOnClickListener(v -> { s.minutes = v2; a.save(); a.renderAll(); });
            seg.addView(b, new LinearLayout.LayoutParams(0,-2,1f));
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin = dp(a,6);
        c.addView(seg, lp);
    }
}
