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

/** Screen builders: home / quiz / settings, ports of gen18 App.tsx screens. */
final class Screens {

    static int dp(MainActivity a, float v) { return Ui.dp(a, v); }

    static TextView label(MainActivity a, String t) {
        TextView v = Ui.tv(a, t, 13, a.theme.disp(), a.theme.head());
        return v;
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
        else b.setBackground(Ui.roundStroke(Color.TRANSPARENT, dp(a,999), t.dark?0x77cfe98a:Theme.L_TEAL, dp(a,1.5f)));
        b.setTextColor(primary ? t.onAccent() : (t.dark ? Theme.D_SOFT : Theme.L_TEAL));
    }
    /** Round 5: gold button — the most prominent action (confirm, hero continue). */
    static Button goldBtn(MainActivity a, String t) {
        Button b = btn(a, t, true);
        b.setBackground(Ui.round(a.theme.goldFill(), dp(a,999)));
        b.setTextColor(a.theme.onGold());
        return b;
    }
    /** Round 5: thin gold progress line for an exam card / hero card.
     *  The fill starts at the inline-start side (right in RTL). */
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
    /** Round 5: exam type derived from the name — 0 تجريبي (teal), 1 محاكاة (gold), 2 فعلي (calm green). */
    static int examType(String name) {
        if (name.contains("فعلي")) return 2;
        if (name.contains("محاكاة")) return 1;
        return 0;
    }
    static String examTypeLabel(int type) {
        return type == 2 ? "فعلي" : type == 1 ? "محاكاة" : "تجريبي";
    }
    /** Round 5: year derived from the name (2024 / 2025 / 2026), 0 when unknown. */
    static int examYear(String name) {
        if (name.contains("2026")) return 2026;
        if (name.contains("2025")) return 2025;
        if (name.contains("2024")) return 2024;
        return 0;
    }
    /** Round 5: the new design is the only design now (the "current" theme
     *  was removed 2026-11-25). nd() stays as the single gate so the round-5
     *  styling branches read as before; it always returns true. */
    static boolean nd(MainActivity a) { return true; }

    /** Round 5: long-press details popup for an exam card — question count,
     *  duration, last score. Suggestion 36 adds the shuffle toggle here:
     *  when on, a fresh start of this exam runs its questions in a random
     *  order. The toggle only sets the preference; a live run is untouched. */
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
        // Shuffle toggle (label only — no explanatory copy).
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
        // topbar: mode + timer
        LinearLayout top = new LinearLayout(a);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        // S4-12: the home title names the bank being set up, not a generic label.
        Bank.BankMeta homeBank = bank.byId(s.setupSection);
        TextView mode = Ui.tv(a, s.submitted ? "نتيجة التجربة" : s.started ? "كمّل من مكانك" : (homeBank != null ? homeBank.name : "امتحان تجريبي"), 20, t.dispB(), t.head());
        top.addView(mode, new LinearLayout.LayoutParams(0,-2,1f));
        if (s.timer && s.started) { LinearLayout.LayoutParams chp = new LinearLayout.LayoutParams(-2, -2); chp.setMarginStart(dp(a, 12)); top.addView(a.makeTimerChip(), chp); }
        c.addView(top);

        if (s.started && (s.paused || (s.allAnswered() && s.mode.equals("study")) || s.expired() || s.submitted)) {
            String msg = s.expired() ? "الوقت خلص" : s.submitted ? "تم التسليم، العداد متوقف"
                    : s.allAnswered() ? "اكتملت الاختيارات، العداد متوقف" : "العداد متوقف مؤقتًا";
            add(c, Ui.tv(a, msg, 12, t.dispB(), t.ink()), 8);
        }
        if (a.confirmAction != null) { confirmBox(a, c); }

        // S2-6: never claim "الحفظ مفعّل" without checking; the last save() records whether it actually wrote.
        TextView save = Ui.tv(a, s.saveFailed ? "تنبيه: آخر حفظ فشل — جرب تقفل التطبيق وتفتحه تاني، أو افضي مساحة." : "الحفظ مفعّل على هذا الجهاز.", 11.5f, t.dispB(), s.saveFailed ? (t.dark ? Theme.D_MISSED : 0xffb3242b) : t.muted());
        add(c, save, 10);

        if (s.started) {
            a.ensureQs();
            // Round 5: in the new design the hero card carries the exam name,
            // so the separate run-label line above it is dropped (not in the
            // submitted state, where the result card needs the context).
            if (!nd(a) || s.submitted) add(c, detail(a, a.runLabel(s.run.equals("all") ? s.bankKey : s.run)), 6);
            if (s.submitted) { resultCard(a, c); }
            else inProgressCard(a, c);
        }

        // Round 4: home is cards only — no collapsible setup row, no instruction
        // panels. Mode first (it applies to whichever card you tap), then
        // search, then the exam cards. Notebook / flagged live as compact
        // action cards above the search (see examCards()).
        modeSeg(a, c);
        examCards(a, c);
    }

    static String pctTxt(int p) { return Ui.toAr(String.valueOf(p)) + "٪"; }
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
        LinearLayout box = new LinearLayout(a);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(Ui.roundStroke(t.card55(), dp(a,14), t.dark?Theme.D_BORDER4:Color.TRANSPARENT, dp(a,1)));
        box.setPadding(dp(a,14), dp(a,10), dp(a,14), dp(a,10));
        if (!t.dark) { // gold side bar feel via hint style
            box.setBackground(Ui.roundStroke(t.card55(), dp(a,14), Theme.L_GOLD, dp(a,1)));
        }
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
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(0,-2,1f);
        row.addView(ok, new LinearLayout.LayoutParams(0,-2,1f));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0,-2,1f);
        bp.setMarginStart(dp(a,10));
        row.addView(back, bp);
        LinearLayout.LayoutParams rp2 = new LinearLayout.LayoutParams(-1,-2);
        rp2.topMargin = dp(a,10);
        box.addView(row, rp2);
        add(c, box, 10);
    }

    /** Round 4: the exam list is a grid of cards (2 per row) — one card per exam
     *  with the name, question count and a small status. Tapping a card starts
     *  that exam in the chosen mode (or resumes it when it is the live session);
     *  a live session for another exam asks for confirmation first.
     *  The search filters the grid in place (no full re-render) so the keyboard
     *  and focus survive typing. Group labels stay as small section headers. */
    static void examCards(MainActivity a, LinearLayout c) {
        Session s = a.s; Theme t = a.theme;
        // compact notebook / flagged actions — the only way to start those runs
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

    /** Round 5: year headers of the current exam list, for the sticky header. */
    static java.util.ArrayList<TextView> yearHeaders = new java.util.ArrayList<TextView>();

    /** Round 5: the sticky year header — a fixed overlay above the scroll area
     *  (MainActivity.stickyYear) mirroring the topmost year header that has
     *  scrolled under the viewport top. Hidden while the first header is
     *  still fully visible, and outside the home tab. */
    static void updateStickyYear(MainActivity a) {
        if (a.stickyYear == null) return;
        if (!nd(a) || !a.s.tab.equals("home") || yearHeaders.isEmpty()) { a.stickyYear.setVisibility(View.GONE); return; }
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

    /** Two exam cards per row (RTL: first card right/start). Shared by both designs. */
    static void addCardRows(MainActivity a, LinearLayout list, java.util.List<Bank.BankMeta> banks) {
        for (int i = 0; i < banks.size(); i += 2) {
            LinearLayout row = new LinearLayout(a);
            // RTL row: first card right (start), second card left (end).
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
        if (nd(a)) {
            // Round 5: exams grouped by year (newest first) with section headers.
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
            return;
        }
        java.util.LinkedHashMap<String, java.util.List<Bank.BankMeta>> groups = new java.util.LinkedHashMap<>();
        for (Bank.BankMeta m : a.bank.banks) {
            if (!a.bankQuery.isEmpty() && !m.name.contains(a.bankQuery)) continue;
            if (!groups.containsKey(m.group)) groups.put(m.group, new java.util.ArrayList<Bank.BankMeta>());
            groups.get(m.group).add(m);
        }
        boolean any = false;
        for (java.util.Map.Entry<String, java.util.List<Bank.BankMeta>> e : groups.entrySet()) {
            int totalQ = 0; for (Bank.BankMeta m : e.getValue()) totalQ += m.ids.length;
            add(list, Ui.tv(a, (e.getKey().equals("tests") ? "الاختبارات" : e.getKey()) + " · " + Ui.toAr(String.valueOf(totalQ)) + " سؤال", 12, t.dispB(), t.muted()), 10);
            any = true;
            addCardRows(a, list, e.getValue());
        }
        if (!any) add(list, detail(a, "مفيش نتائج للبحث."), 10);
    }

    /** Round 5: small tinted exam-type badge — تجريبي teal / محاكاة gold / فعلي calm green. */
    static TextView examBadge(MainActivity a, int type, boolean live) {
        Theme t = a.theme;
        TextView b = Ui.tv(a, examTypeLabel(type), 11, t.dispB(), t.ink());
        int bg, fg;
        if (live) { bg = 0x33ffffff; fg = t.onAccent(); }
        else if (type == 2) { bg = t.dark ? 0x337fd08a : 0x1f2e7d5b; fg = t.dark ? Theme.D_GREEN : Theme.L_GREEN; }
        else if (type == 1) { bg = t.dark ? 0x33d4a24c : 0x1fb98a2f; fg = t.dark ? Theme.D_GOLD : Theme.L_GOLD; }
        else { bg = t.dark ? 0x33cfe98a : 0x1f0b4f4a; fg = t.dark ? Theme.D_SOFT : Theme.L_TEAL; }
        b.setTextColor(fg);
        b.setBackground(Ui.round(bg, dp(a,999)));
        b.setPadding(dp(a,8), dp(a,3), dp(a,8), dp(a,3));
        return b;
    }

    /** One exam card: name, question count + minutes, and a small status line. */
    static LinearLayout examCard(MainActivity a, Bank.BankMeta m) {
        Session s = a.s; Theme t = a.theme;
        boolean live = s.started && !s.submitted && s.run.equals("all") && m.id.equals(s.bankKey);
        boolean selected = m.id.equals(s.setupSection);
        LinearLayout card = new LinearLayout(a);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(Ui.roundStroke(live ? t.primary() : (selected ? t.card55() : t.card()), dp(a, 16), live ? t.primary() : (selected ? t.primary() : t.border()), dp(a, 1.5f)));
        card.setPadding(dp(a, 14), dp(a, 12), dp(a, 14), dp(a, 12));
        card.setMinimumHeight(dp(a, 96));
        if (nd(a)) {
            // Round 5: name + type badge on one row (RTL: name right/start, badge left/end).
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
            // Round 5: thin gold progress line under the name for in-progress exams.
            if (live) {
                LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1, dp(a, 4));
                pp.topMargin = dp(a, 8);
                card.addView(cardProgressBar(a, (s.ids.length - s.blank()) / (float) s.ids.length), pp);
            }
        } else {
            TextView name = Ui.tv(a, m.name, 14.5f, t.dispB(), live ? t.onAccent() : t.head());
            name.setMaxLines(2);
            card.addView(name);
        }
        TextView count = Ui.tv(a, Ui.toAr(String.valueOf(m.ids.length)) + " سؤال · " + Ui.toAr(String.valueOf(m.minutes)) + " د", 12, t.body(), live ? t.onAccent() : t.muted());
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2); cp.topMargin = dp(a, 4);
        card.addView(count, cp);
        int last = lastResult(a, m);
        TextView st = Ui.tv(a, cardStatus(a, m, last), 12, t.dispB(), live ? t.onAccent() : (last >= 0 ? t.ink() : t.muted()));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2); sp.topMargin = dp(a, 4);
        card.addView(st, sp);
        card.setOnClickListener(v -> a.onExamCard(m));
        if (nd(a)) card.setOnLongClickListener(v -> { showExamDetails(a, m); return true; }); // Round 5: long-press details popup
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
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2); // S4-6: full width, like the minutes segment
        lp.topMargin = dp(a,6);
        c.addView(seg, lp);
    }

    // ---------- in-progress session card ----------
    static void inProgressCard(MainActivity a, LinearLayout c) {
        Session s = a.s; Theme t = a.theme;
        if (nd(a)) {
            // Round 5 (#1): "continue where you left off" hero card — the most
            // recently played exam (the live session), with its name, current
            // question, a gold progress line and a gold "كمّل" button.
            String name = a.runLabel(s.run.equals("all") ? s.bankKey : s.run);
            int answered = s.ids.length - s.blank();
            LinearLayout card = new LinearLayout(a);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(Ui.roundStroke(t.card55(), dp(a,18), t.dark ? Theme.D_GOLD : Theme.L_GOLD, dp(a,1.5f)));
            card.setPadding(dp(a,16), dp(a,14), dp(a,16), dp(a,14));
            card.addView(Ui.tv(a, name, 16, t.dispB(), t.head()));
            // One RTL run, Arabic-Indic digits — never a mixed bidi fragment.
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
            return;
        }
        add(c, Ui.tv(a, "جاوبت " + Ui.toAr(String.valueOf(s.ids.length - s.blank())) + " من " + Ui.toAr(String.valueOf(s.ids.length))
                + " · " + (s.mode.equals("exam")?"امتحان":"مذاكرة"), 12.5f, t.dispB(), t.muted()), 10);
        if (s.timer) {
            LinearLayout tr = new LinearLayout(a);
            tr.setOrientation(LinearLayout.HORIZONTAL);
            tr.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            android.widget.ImageView ic = new android.widget.ImageView(a);
            ic.setImageResource(R.drawable.ic_clock);
            ic.setColorFilter(t.ink());
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(a,16), dp(a,16));
            ip.setMarginEnd(dp(a, 4)); // gap on the side facing the time view
            tr.addView(ic, ip); // RTL row: first child sits rightmost
            tr.addView(Ui.timeView(a, s.elapsed, 13, t.disp(), t.ink()));
            add(c, tr, 10);
        }
        Button cont = btn(a, "كمّل الحل", true);
        cont.setOnClickListener(v -> a.openReview("all"));
        add(c, cont, 12);
        Button fin = btn(a, "إنهاء وعرض النتيجة", false);
        fin.setOnClickListener(v -> a.finishQuiz());
        add(c, fin, 8);
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
        if (nd(a)) {
            // Round 5 (#4): gold score ring as the hero element instead of the plain percentage text.
            // Suggestion 32: it fills gradually up to the score (no animation when fx is off / calm).
            ScoreRingView ring = new ScoreRingView(a, t);
            ring.animateTo(pct, t, Fx.on(a));
            Fx.pop(a, ring, 550);
            if (a.celebrate && !a.confettiDone) { a.confettiDone = true; ring.post(() -> Fx.confetti(a, true)); }
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(dp(a,150), dp(a,150));
            rp.gravity = Gravity.CENTER_HORIZONTAL; rp.topMargin = dp(a,16);
            c.addView(ring, rp);
        } else {
            TextView score = Ui.tv(a, Ui.lre(Ui.toAr(String.valueOf(pct)) + "٪"), 48, t.dispB(), t.ink());
            Fx.pop(a, score, 550);
            if (a.celebrate && !a.confettiDone) { a.confettiDone = true; score.post(() -> Fx.confetti(a, true)); }
            score.setTextDirection(View.TEXT_DIRECTION_LTR); // web renders the score in a <bdi> (LTR): digits then ٪
            score.setGravity(Gravity.CENTER);
            add(c, score, 16);
        }
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
        add(c, detail(a, "الأسئلة المتروكة محسوبة في الدرجة."), 6);
        Button all = btn(a, "مراجعة كل الإجابات", true);
        all.setOnClickListener(v -> a.openReview("all"));
        add(c, all, 12);
        Button wr = btn(a, "مراجعة الغلط فقط (" + Ui.toAr(String.valueOf(wrong)) + ")", false);
        // S4-2: with no wrong answers the button must not open an empty review screen.
        wr.setEnabled(wrong > 0);
        wr.setAlpha(wrong > 0 ? 1f : .35f);
        wr.setOnClickListener(v -> a.openReview("wrong"));
        add(c, wr, 8);
        Button home = btn(a, "الرجوع للرئيسية", false);
        home.setOnClickListener(v -> a.scrollHomeToList());
        add(c, home, 8);
        // Round 4: a clear way back to the exam cards from the result screen.
        Button list = btn(a, "القائمة", false);
        list.setOnClickListener(v -> a.goToList());
        add(c, list, 8);
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
        // viewIds positions: all, or wrong-only
        final java.util.List<Integer> view = new java.util.ArrayList<>();
        for (int i = 0; i < s.ids.length; i++)
            if (a.review.equals("all") || (s.picked[i] >= 0 && s.picked[i] != qs.get(i).c)) view.add(i);
        if (view.isEmpty()) {
            add(c, homeTitle(a, "مفيش أسئلة هنا"), 16);
            add(c, detail(a, "كل الإجابات صح، مفيش غلط للمراجعة."), 8);
            Button back = btn(a, "ارجع للرئيسية", true);
            back.setOnClickListener(v -> a.goHome());
            add(c, back, 12);
            return;
        }
        int pos = view.indexOf(s.index);
        if (pos < 0) { pos = 0; s.index = view.get(0); }
        final int fpos = pos;
        a.quizView = view; a.quizPos = fpos; // Round 5 (#20): swipe navigation reads these
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

        // Suggestion 34: focus mode — a small exit control at the top; the
        // timer, chip, progress row, tools and font row are all hidden below.
        if (a.focus) {
            Button exitFocus = btn(a, "إنهاء التركيز", false);
            exitFocus.setOnClickListener(v -> { a.focus = false; a.renderAll(); });
            add(c, exitFocus, 0);
        }

        // Suggestion 31: exam name (+ year, which the exam names already
        // carry) as a small chip at the top of the quiz screen.
        if (!a.focus) {
            String examName = s.run.equals("all") ? (a.sessionBank() != null ? a.sessionBank().name : "") : a.runLabel(s.run);
            TextView chip = Ui.tv(a, examName, 12.5f, t.disp(), t.muted());
            chip.setBackground(Ui.roundStroke(t.card55(), dp(a,999), t.border3(), dp(a,1)));
            chip.setPadding(dp(a,14), dp(a,5), dp(a,14), dp(a,5));
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-2, -2);
            cp.gravity = Gravity.CENTER_HORIZONTAL;
            c.addView(chip, cp);
        }

        // progress row (RTL: the bar fills right to left, like the web version)
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

        // question (LTR)
        TextView qv = Ui.tv(a, q.q, t.qSize(), t.bodyB(), t.head());
        qv.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        qv.setTextIsSelectable(true);
        qv.setLineSpacing(0, nd(a) ? 1.7f : 1.45f); // Round 5 (#5): more line spacing in the new design
        add(c, qv, 4); // one-page layout: tighter gaps around the question
        if (a.bank.missingImage(q.gid))
            add(c, detail(a, "الصورة المشار إليها غير موجودة في المصدر."), 8);

        // tools row: translate chip, explain, hint chip — all real vector icons (S4-7), no emoji/text glyphs
        LinearLayout tools = new LinearLayout(a);
        tools.setGravity(Gravity.CENTER_VERTICAL);
        android.widget.ImageView tr = iconChip(a, a.trOpen, R.drawable.ic_translate, "الترجمة");
        tr.setOnClickListener(v -> { a.trOpen = !a.trOpen; a.renderAll(); });
        tools.addView(tr, new LinearLayout.LayoutParams(dp(a,44), dp(a,44)));
        {
            android.widget.ImageView fl = iconChip(a, s.favorites.contains(q.gid), R.drawable.ic_flag, "راجعني");
            fl.setOnClickListener(v -> { if (!s.favorites.remove(q.gid)) s.favorites.add(q.gid); a.save(); a.renderAll(); });
            LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(dp(a,44), dp(a,44));
            fp.setMarginStart(dp(a,6));
            tools.addView(fl, fp);
        }
        String clipFile = ClipPlayer.exists(a, "r" + q.gid + ".mp3") ? "r" + q.gid + ".mp3"
            : ClipPlayer.exists(a, "g" + q.gid + ".mp3") ? "g" + q.gid + ".mp3" : null;
        // S3-9 decision (Mohamed): in study mode the audio / explanation /
        // translation icons sit next to the question and are usable BEFORE
        // answering; in exam mode they stay after-answer only, as today.
        boolean preAnswer = s.mode.equals("study") && !s.submitted;
        if (clipFile != null && feedback && (answered || s.submitted || preAnswer)) {
            final String cf = clipFile;
            boolean on0 = ClipPlayer.isPlaying(cf);
            android.widget.ImageView sp = iconChip(a, on0, on0 ? R.drawable.ic_stop : R.drawable.ic_audio, on0 ? "إيقاف الشرح الصوتي" : "اسمع الشرح");
            // Round 5 (#13): bigger audio button with an animated waveform while
            // playing (new design only).
            final WaveView wv = nd(a) ? new WaveView(a, t.dark ? Theme.D_GOLD : Theme.L_GOLD) : null;
            if (wv != null && on0) wv.start();
            sp.setOnClickListener(v -> {
                // Round 4: flip the icon in place — the old code ran renderAll()
                // twice (once here, once via the toggle callback) on every audio tap.
                boolean on = ClipPlayer.toggle(a, cf, s.volume, () -> { setChip(a, sp, false, R.drawable.ic_audio); if (wv != null) wv.stop(); });
                setChip(a, sp, on, on ? R.drawable.ic_stop : R.drawable.ic_audio);
                sp.setContentDescription(on ? "إيقاف الشرح الصوتي" : "اسمع الشرح");
                if (wv != null) { if (on) wv.start(); else wv.stop(); }
            });
            int chipSize = nd(a) ? 52 : 44;
            LinearLayout.LayoutParams spp = new LinearLayout.LayoutParams(dp(a,chipSize), dp(a,chipSize));
            spp.setMarginStart(dp(a,6));
            tools.addView(sp, spp);
            if (wv != null) {
                LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(-2, dp(a,28));
                wp.setMarginStart(dp(a,6));
                wp.gravity = Gravity.CENTER_VERTICAL;
                tools.addView(wv, wp);
            }
        }
        // Round 4: stop the clip of another question only when one is actually
        // playing — the old code abandoned audio focus on every quiz render.
        if (ClipPlayer.isAnyPlaying() && !ClipPlayer.isPlaying("r" + q.gid + ".mp3") && !ClipPlayer.isPlaying("g" + q.gid + ".mp3")) ClipPlayer.stop();
        if (feedback && (answered || s.submitted || preAnswer)) {
            Button ex = toolBtn(a, "الشرح");
            ex.setTextSize(11f); ex.setPadding(dp(a,12),dp(a,6),dp(a,12),dp(a,6)); // user: smaller, like the chips next to it
            LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(-2, dp(a,44));
            ep.setMarginStart(dp(a,6));
            ex.setOnClickListener(v -> showExplain(a));
            tools.addView(ex, ep);
            // Suggestion 35: share the question as a designed image (same gate
            // as the explanation — the image carries the answer).
            Button share = toolBtn(a, "مشاركة");
            share.setTextSize(11f); share.setPadding(dp(a,12),dp(a,6),dp(a,12),dp(a,6));
            LinearLayout.LayoutParams shp = new LinearLayout.LayoutParams(-2, dp(a,44));
            shp.setMarginStart(dp(a,6));
            share.setOnClickListener(v -> a.shareQuestion());
            tools.addView(share, shp);
        }
        if (s.mode.equals("study") && !s.submitted && !answered && s.hints) {
            android.widget.ImageView hint = iconChip(a, a.hintOpen, R.drawable.ic_bulb, "التلميح");
            hint.setOnClickListener(v -> { a.hintOpen = !a.hintOpen; a.renderAll(); });
            LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(dp(a,44), dp(a,44));
            hp.setMarginStart(dp(a,6));
            tools.addView(hint, hp);
        }
        {   // Suggestion 34: focus mode toggle.
            Button foc = toolBtn(a, "تركيز");
            foc.setTextSize(11f); foc.setPadding(dp(a,12),dp(a,6),dp(a,12),dp(a,6));
            LinearLayout.LayoutParams fop = new LinearLayout.LayoutParams(-2, dp(a,44));
            fop.setMarginStart(dp(a,6));
            foc.setOnClickListener(v -> { a.focus = true; a.renderAll(); });
            tools.addView(foc, fop);
        }
        if (!a.focus) add(c, tools, 6); // one-page layout: tighter gaps; hidden in focus mode


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

        // options (LTR)
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
            if (showCorrect) { bg = Theme.L_TEAL; border = Theme.L_TEAL; if (t.dark) { bg = Theme.L_TEAL; } }
            else if (showWrong) { bg = t.dark ? Theme.D_CARD : Theme.L_REDSOFT; border = t.dark ? Theme.D_MISSED : Theme.L_RED; }
            else if (isPending) { bg = t.pendingBg(); border = nd(a) ? (t.dark ? Theme.D_GOLD : Theme.L_GOLD) : (t.dark ? Theme.D_TEXT : Theme.L_TEAL); } // Round 5 (#5): gold border on the selected/pending answer in the new design
            if (s.contrast && !showCorrect && !showWrong) { bg = t.dark ? Theme.D_CARD : 0xffffffff; border = t.dark ? Theme.D_TEXT : 0xff4a7a70; }
            opt.setBackground(Ui.roundStroke(bg, dp(a,999), border, dp(a,1.5f)));
            int textCol = showCorrect ? Theme.L_BG : (showWrong ? (t.dark?Theme.D_MISSED:0xffb3242b) : t.optText());
            if (s.letters) {
                TextView letter = Ui.tv(a, String.valueOf(L[i]), 12.5f * t.scale, t.disp(), showCorrect ? Theme.L_TEAL : (showWrong ? 0xffffffff : t.muted())); // Round 5 (#14): letters scale with the font toggle too
                GradientDrawable lc = new GradientDrawable();
                lc.setShape(GradientDrawable.OVAL);
                lc.setColor(showCorrect ? Theme.L_BG : (showWrong ? (t.dark?Theme.D_MISSED:Theme.L_RED) : (t.dark ? 0x22cfe98a : 0x1a0b4f4a)));
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
                // Suggestion 33: double-tap an option confirms it instantly
                // (confirm mode only — otherwise a tap already commits). The
                // tap is tracked on MainActivity so it survives the re-render.
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
            olp.topMargin = dp(a, s.spacious ? 12 : 6); // one-page layout: tighter option gaps
            opts.addView(opt, olp);
        }
        a.popOnce = false;
        add(c, opts, 8);

        if (s.expired() && !s.submitted)
            add(c, hintBox(a, "الوقت خلص. الإجابات اتقفلت، سلّم التجربة عشان تشوف النتيجة، أو صفّر العداد من الإعدادات."), 12);

        if (s.confirm && a.pending != null && !s.expired() && !s.submitted) {
            Button cb = nd(a) ? goldBtn(a, "تأكيد الإجابة") : btn(a, "تأكيد الإجابة", true); // Round 5 (#8): full-width gold confirm in the new design
            cb.setOnClickListener(v -> a.commit(a.pending));
            add(c, cb, 8);
        }

        // One-page layout: the Previous/Next/list controls moved from the bottom
        // to side rails on the screen edges (MainActivity side rails), so the
        // question + options fit on one viewport. The confirm button and the
        // finish CTA stay at the bottom.
        Button cta = btn(a, s.submitted ? "الرجوع للرئيسية" : "إنهاء وعرض النتيجة", true);
        cta.setOnClickListener(v -> { if (s.submitted) a.goHome(); else a.finishQuiz(); });
        add(c, cta, 8);
    }

    /** S4-7: circular icon chip with a real vector icon + contentDescription (TalkBack), matching web's SVG chips.
     *  The gold flag in dark mode keeps the brand accent (S4-8). */
    static android.widget.ImageView iconChip(MainActivity a, boolean open, int res, String desc) {
        android.widget.ImageView iv = new android.widget.ImageView(a);
        iv.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        setChip(a, iv, open, res);
        iv.setPadding(dp(a,11), dp(a,11), dp(a,11), dp(a,11));
        iv.setContentDescription(desc);
        return iv;
    }
    /** Round 4: flip an icon chip's visual state in place (no full renderAll). */
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
                if (s.picked[qi] == a.qs.get(qi).c) { bgc = Theme.L_TEAL; tc = Theme.L_BG; bc = Theme.L_TEAL; }
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
            // S4-14: cap the height at 85% of the screen so the close button stays reachable on small phones.
            d.getWindow().setLayout(-1, (int) (a.getResources().getDisplayMetrics().heightPixels * 0.85));
        }
        d.show();
    }

    // ================= explain dialog =================
    static void showExplain(MainActivity a) {
        Session s = a.s; Theme t = a.theme;
        Bank.Question q = a.qs.get(s.index);
        int chosen = s.picked[s.index];
        Dialog d = new Dialog(a);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ScrollView sv = new ScrollView(a);
        LinearLayout box = new LinearLayout(a);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(a,20), dp(a,24), dp(a,20), dp(a,20));
        sv.addView(box);
        // No praise/feedback words after answering (Mohamed): the green/red
        // colours on the options are the entire feedback. The dialog title
        // stays a neutral "الشرح".
        String ttl = "الشرح";
        box.addView(Ui.tv(a, ttl, t.smallSize() * 1.125f, t.disp(), t.dark ? Theme.D_SOFT : Theme.L_TEAL));
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
        addAudio(a, box, q, "r" + q.gid + ".mp3", "تسجيل الدكتور");
        addAudio(a, box, q, "g" + q.gid + ".mp3", "شرح صوتي إضافي مولّد (مش تسجيل الدكتور)");
        d.setOnDismissListener(x -> ClipPlayer.stop());
        Button close = btn(a, "قفل الشرح", false);
        close.setOnClickListener(v -> d.dismiss());
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1,-2);
        cp.topMargin = dp(a,16);
        box.addView(close, cp);
        d.setContentView(sv);
        if (d.getWindow() != null) {
            // Round 5 (#9): the explanation is a separate card with a light gold
            // border in the new design. The written explanation, why, source and
            // audio buttons inside it are exactly as before.
            d.getWindow().setBackgroundDrawable(nd(a)
                ? Ui.roundStroke(t.sheet(), dp(a,22), t.dark ? Theme.D_GOLD : Theme.L_GOLD, dp(a,1.5f))
                : Ui.round(t.sheet(), dp(a,22)));
            // S4-14: same height cap as the question map.
            d.getWindow().setLayout(-1, (int) (a.getResources().getDisplayMetrics().heightPixels * 0.85));
        }
        d.show();
    }

    private static Button activeAudioBtn; private static String activeAudioCap;
    private static WaveView activeAudioWave; // Round 5: the wave of the currently playing explain-dialog clip

    static void addAudio(MainActivity a, LinearLayout box, Bank.Question q, String file, String caption) {
        if (!ClipPlayer.exists(a, file)) return;
        // Round 5 (#9): in the new design the explanation audio is a large
        // prominent gold button with a waveform while playing. The written
        // explanation, why and source above it are untouched — the audio
        // button is an addition, never a replacement.
        final Button b = nd(a) ? goldBtn(a, "▶  " + caption) : btn(a, "▶  " + caption, false);
        if (nd(a)) { b.setTextSize(15f); b.setMinHeight(dp(a,52)); b.setMinimumHeight(dp(a,52)); }
        final WaveView wv = nd(a) ? new WaveView(a, a.theme.dark ? Theme.D_GOLD : Theme.L_GOLD) : null;
        b.setOnClickListener(v -> {
            // starting this clip stops the other one, so reset the other button's label too
            if (activeAudioBtn != null && activeAudioBtn != b) { activeAudioBtn.setText("▶  " + activeAudioCap); if (activeAudioWave != null) activeAudioWave.stop(); }
            boolean on = ClipPlayer.toggle(a, file, a.s.volume, () -> { b.setText("▶  " + caption); if (wv != null) wv.stop(); });
            b.setText((on ? "■  " : "▶  ") + caption);
            if (wv != null) { if (on) wv.start(); else wv.stop(); }
            activeAudioBtn = on ? b : null; activeAudioCap = caption; activeAudioWave = on ? wv : null;
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin = dp(a,14);
        if (wv == null) { box.addView(b, lp); return; }
        LinearLayout row = new LinearLayout(a);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(b, new LinearLayout.LayoutParams(0, -2, 1f));
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(-2, dp(a,28));
        wp.setMarginStart(dp(a,8));
        row.addView(wv, wp);
        box.addView(row, lp);
    }

    // ================= SETTINGS =================
    interface Toggle { void run(boolean on); }

    static void settings(MainActivity a, LinearLayout c) {
        Session s = a.s; Theme t = a.theme;
        add(c, Ui.tv(a, "على مزاجك", 23, t.dispB(), t.head()), 16);
        add(c, Ui.tv(a, "تحكم في تجربتك. التغييرات بتتطبق فورًا.", 13, t.body(), t.ink()), 4);

        groupTitle(a, c, "العرض والقراءة");
        toggleRow(a, c, "الوضع الليلي", "خلفيات غامقة مع نفس الأخضر والليموني.", s.dark, on -> { s.dark = on; a.theme.dark = on; });
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

        groupTitle(a, c, "الصوت والمؤثرات"); // S4-10: same group and order as web settings
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
                    // S3-6: second step — confirm the replacement before anything is touched.
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
        s.fontPercent=100; s.sounds=true; s.fx=true; s.volume=80; s.minutes=120; // S3-7: minutes reset too
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
        // percent in its own LTR view: platform bidi flings ٪ to the line start inside RTL text (bidi6 row 7)
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
        // RTL row: small (right/start), big (left/end) — matches the mirrored SeekBar.
        TextView big = Ui.tv(a, "كبير", 11, t.body(), t.muted());
        TextView small = Ui.tv(a, "صغير جدًا", 11, t.body(), t.muted());
        // RTL row, both spans rtl: small right, big left.
        labels.addView(small, new LinearLayout.LayoutParams(0,-2,1f));
        big.setGravity(Gravity.END); // left edge = END side in RTL
        labels.addView(big, new LinearLayout.LayoutParams(0,-2,1f));
        box.addView(labels);
        // preview
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
