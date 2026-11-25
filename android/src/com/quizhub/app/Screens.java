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
            add(c, detail(a, a.runLabel(s.run.equals("all") ? s.bankKey : s.run)), 6);
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
        LinearLayout list = new LinearLayout(a);
        list.setOrientation(LinearLayout.VERTICAL);
        search.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence ch, int st, int co, int af) {}
            @Override public void onTextChanged(CharSequence ch, int st, int be, int co) {}
            @Override public void afterTextChanged(android.text.Editable e) { a.bankQuery = e.toString(); fillExamCards(a, list); }
        });
        fillExamCards(a, list);
        add(c, list, 4);
    }

    static void fillExamCards(MainActivity a, LinearLayout list) {
        Session s = a.s; Theme t = a.theme;
        list.removeAllViews();
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
            for (int i = 0; i < e.getValue().size(); i += 2) {
                any = true;
                LinearLayout row = new LinearLayout(a);
                // RTL row: first card right (start), second card left (end).
                for (int j = i; j < Math.min(i + 2, e.getValue().size()); j++) {
                    LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, -2, 1f);
                    if (j > i) cp.setMarginStart(dp(a, 10));
                    row.addView(examCard(a, e.getValue().get(j)), cp);
                }
                if (e.getValue().size() - i == 1) row.addView(new View(a), new LinearLayout.LayoutParams(0, -2, 1f));
                add(list, row, 6);
            }
        }
        if (!any) add(list, detail(a, "مفيش نتائج للبحث."), 10);
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
        TextView name = Ui.tv(a, m.name, 14.5f, t.dispB(), live ? t.onAccent() : t.head());
        name.setMaxLines(2);
        card.addView(name);
        TextView count = Ui.tv(a, Ui.toAr(String.valueOf(m.ids.length)) + " سؤال · " + Ui.toAr(String.valueOf(m.minutes)) + " د", 12, t.body(), live ? t.onAccent() : t.muted());
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2); cp.topMargin = dp(a, 4);
        card.addView(count, cp);
        int last = lastResult(a, m);
        TextView st = Ui.tv(a, cardStatus(a, m, last), 12, t.dispB(), live ? t.onAccent() : (last >= 0 ? t.ink() : t.muted()));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2); sp.topMargin = dp(a, 4);
        card.addView(st, sp);
        card.setOnClickListener(v -> a.onExamCard(m));
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
        if (a.celebrate) { TextView br = Ui.tv(a, "برافو! نتيجة عالية 🎉", 16, t.dispB(), t.head()); br.setGravity(Gravity.CENTER); add(c, br, 10); }
        TextView score = Ui.tv(a, Ui.lre(Ui.toAr(String.valueOf(Math.round(right * 100f / s.ids.length))) + "٪"), 48, t.dispB(), t.ink());
        Fx.pop(a, score, 550);
        if (a.celebrate && !a.confettiDone) { a.confettiDone = true; score.post(() -> Fx.confetti(a, true)); }
        score.setTextDirection(View.TEXT_DIRECTION_LTR); // web renders the score in a <bdi> (LTR): digits then ٪
        score.setGravity(Gravity.CENTER);
        add(c, score, 16);
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

        // progress row (RTL: the bar fills right to left, like the web version)
        if (s.steps) {
            LinearLayout row = new LinearLayout(a);
            row.setGravity(Gravity.CENTER_VERTICAL);
            ProgressView pv = new ProgressView(a, t);
            pv.set(fpos, view.size(), t);
            row.addView(pv, new LinearLayout.LayoutParams(0, dp(a,28), 1f));
            TextView total = Ui.tv(a, Ui.lre("/ " + view.size()), 11, t.dispB(), t.muted());
            total.setTextDirection(View.TEXT_DIRECTION_LTR); // "/ 147" must keep LTR order inside the RTL row
            total.setPadding(dp(a,8),0,dp(a,8),0);
            row.addView(total);
            android.widget.ImageView map = iconChip(a, false, R.drawable.ic_map, "خريطة الأسئلة");
            map.setOnClickListener(v -> showMap(a, view));
            row.addView(map, new LinearLayout.LayoutParams(dp(a,44), dp(a,44)));
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, dp(a,44));
            c.addView(row, rp);
        }

        // question (LTR)
        TextView qv = Ui.tv(a, q.q, t.qSize(), t.bodyB(), t.head());
        qv.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        qv.setTextIsSelectable(true);
        qv.setLineSpacing(0, 1.45f);
        add(c, qv, 6);
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
        if (clipFile != null && feedback && (answered || s.submitted)) {
            final String cf = clipFile;
            boolean on0 = ClipPlayer.isPlaying(cf);
            android.widget.ImageView sp = iconChip(a, on0, on0 ? R.drawable.ic_stop : R.drawable.ic_audio, on0 ? "إيقاف الشرح الصوتي" : "اسمع الشرح");
            sp.setOnClickListener(v -> {
                // Round 4: flip the icon in place — the old code ran renderAll()
                // twice (once here, once via the toggle callback) on every audio tap.
                boolean on = ClipPlayer.toggle(a, cf, s.volume, () -> setChip(a, sp, false, R.drawable.ic_audio));
                setChip(a, sp, on, on ? R.drawable.ic_stop : R.drawable.ic_audio);
                sp.setContentDescription(on ? "إيقاف الشرح الصوتي" : "اسمع الشرح");
            });
            LinearLayout.LayoutParams spp = new LinearLayout.LayoutParams(dp(a,44), dp(a,44));
            spp.setMarginStart(dp(a,6));
            tools.addView(sp, spp);
        }
        // Round 4: stop the clip of another question only when one is actually
        // playing — the old code abandoned audio focus on every quiz render.
        if (ClipPlayer.isAnyPlaying() && !ClipPlayer.isPlaying("r" + q.gid + ".mp3") && !ClipPlayer.isPlaying("g" + q.gid + ".mp3")) ClipPlayer.stop();
        if (feedback && (answered || s.submitted)) {
            Button ex = toolBtn(a, "الشرح");
            ex.setTextSize(11f); ex.setPadding(dp(a,12),dp(a,6),dp(a,12),dp(a,6)); // user: smaller, like the chips next to it
            LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(-2, dp(a,44));
            ep.setMarginStart(dp(a,6));
            ex.setOnClickListener(v -> showExplain(a));
            tools.addView(ex, ep);
        }
        if (s.mode.equals("study") && !s.submitted && !answered && s.hints) {
            android.widget.ImageView hint = iconChip(a, a.hintOpen, R.drawable.ic_bulb, "التلميح");
            hint.setOnClickListener(v -> { a.hintOpen = !a.hintOpen; a.renderAll(); });
            LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(dp(a,44), dp(a,44));
            hp.setMarginStart(dp(a,6));
            tools.addView(hint, hp);
        }
        add(c, tools, 8);

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
            else if (isPending) { bg = t.pendingBg(); border = t.dark ? Theme.D_TEXT : Theme.L_TEAL; }
            if (s.contrast && !showCorrect && !showWrong) { bg = t.dark ? Theme.D_CARD : 0xffffffff; border = t.dark ? Theme.D_TEXT : 0xff4a7a70; }
            opt.setBackground(Ui.roundStroke(bg, dp(a,999), border, dp(a,1.5f)));
            int textCol = showCorrect ? Theme.L_BG : (showWrong ? (t.dark?Theme.D_MISSED:0xffb3242b) : t.optText());
            if (s.letters) {
                TextView letter = Ui.tv(a, String.valueOf(L[i]), 12.5f, t.disp(), showCorrect ? Theme.L_TEAL : (showWrong ? 0xffffffff : t.muted()));
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
            if (!locked) opt.setOnClickListener(v -> { if (s.confirm) { a.pending = fi; a.pendingByQuestion.put(s.index, fi); a.renderAll(); } else a.commit(fi); });
            LinearLayout.LayoutParams olp = new LinearLayout.LayoutParams(-1,-2);
            olp.topMargin = dp(a, s.spacious ? 14 : 7);
            opts.addView(opt, olp);
        }
        a.popOnce = false;
        add(c, opts, 12);

        if (s.expired() && !s.submitted)
            add(c, hintBox(a, "الوقت خلص. الإجابات اتقفلت، سلّم التجربة عشان تشوف النتيجة، أو صفّر العداد من الإعدادات."), 12);

        if (s.confirm && a.pending != null && !s.expired() && !s.submitted) {
            Button cb = btn(a, "تأكيد الإجابة", true);
            cb.setOnClickListener(v -> a.commit(a.pending));
            add(c, cb, 12);
        }

        // nav: RTL row — Previous on the right (start), Next on the left (end), matching reading direction.
        LinearLayout nav = new LinearLayout(a);
        Button prev = btn(a, "السابق", false);
        prev.setEnabled(fpos > 0); prev.setAlpha(fpos > 0 ? 1f : .35f);
        prev.setOnClickListener(v -> { s.index = view.get(fpos - 1); a.trReset(); a.save(); a.renderAll(); });
        Button next = btn(a, "التالي", true);
        boolean hasNext = fpos < view.size() - 1;
        next.setEnabled(hasNext); next.setAlpha(hasNext ? 1f : .35f);
        next.setOnClickListener(v -> { s.index = view.get(fpos + 1); a.trReset(); a.save(); a.renderAll(); });
        nav.addView(prev, new LinearLayout.LayoutParams(0,-2,1f));
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(0,-2,1f);
        np.setMarginStart(dp(a,10));
        nav.addView(next, np);
        add(c, nav, 12);

        Button cta = btn(a, s.submitted ? "الرجوع للرئيسية" : "إنهاء وعرض النتيجة", true);
        cta.setOnClickListener(v -> { if (s.submitted) a.goHome(); else a.finishQuiz(); });
        add(c, cta, 12);
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
        String ttl = chosen < 0 ? "سؤال من غير إجابة" : (chosen == q.c ? "إجابة صحيحة" : "الإجابة الصحيحة");
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
            d.getWindow().setBackgroundDrawable(Ui.round(t.sheet(), dp(a,22)));
            // S4-14: same height cap as the question map.
            d.getWindow().setLayout(-1, (int) (a.getResources().getDisplayMetrics().heightPixels * 0.85));
        }
        d.show();
    }

    private static Button activeAudioBtn; private static String activeAudioCap;

    static void addAudio(MainActivity a, LinearLayout box, Bank.Question q, String file, String caption) {
        if (!ClipPlayer.exists(a, file)) return;
        final Button b = btn(a, "▶  " + caption, false);
        b.setOnClickListener(v -> {
            // starting this clip stops the other one, so reset the other button's label too
            if (activeAudioBtn != null && activeAudioBtn != b) activeAudioBtn.setText("▶  " + activeAudioCap);
            boolean on = ClipPlayer.toggle(a, file, a.s.volume, () -> b.setText("▶  " + caption));
            b.setText((on ? "■  " : "▶  ") + caption);
            activeAudioBtn = on ? b : null; activeAudioCap = caption;
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin = dp(a,14);
        box.addView(b, lp);
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
