package com.quizhub.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;

/** Suggestion 35: render the current question as a designed brand image
 *  (question + options with the correct answer marked + explanation +
 *  translation) and share it through the system share sheet. The card is
 *  built from the same Views and fonts as the quiz screen, measured offscreen
 *  at a fixed width and drawn to a bitmap — no new fonts, no new assets. */
public final class ShareCard {
    static final String FILE_NAME = "quiz-hub-question.png";
    private static final int TEAL = 0xff0b3b37;
    private static final int GOLD = 0xffd4a24c;
    private static final int IVORY = 0xfff5f3ee;
    private static final int INK = 0xff1f2a2a;
    private static final int MUTED = 0xff5a6b68;
    private static final int SOFT = 0xffcfe8e0;
    private static final int CARD_W_DP = 411; // ~1080 px on a ~2.6x phone

    public static void share(MainActivity a, Bank.Question q, int chosen) {
        share(a, q, chosen, null);
    }

    public static void share(MainActivity a, Bank.Question q, int chosen, String examName) {
        try {
            Bitmap bmp = render(a, q, chosen, examName);
            File dir = new File(a.getCacheDir(), "share");
            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, FILE_NAME);
            try (FileOutputStream fos = new FileOutputStream(out)) {
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }
            Uri uri = ShareFileProvider.uriFor(a, out);
            Intent it = new Intent(Intent.ACTION_SEND);
            it.setType("image/png");
            it.putExtra(Intent.EXTRA_STREAM, uri);
            it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            a.startActivity(Intent.createChooser(it, "مشاركة السؤال"));
        } catch (Exception e) {
            android.widget.Toast.makeText(a, "مقدرناش نجهّز صورة السؤال.", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    static Bitmap render(MainActivity a, Bank.Question q, int chosen, String examName) {
        Theme t = a.theme;
        LinearLayout root = new LinearLayout(a);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(IVORY);
        int pad = Ui.dp(a, 28);
        root.setPadding(pad, pad, pad, pad);

        // Header band: deep teal, gold "Quiz Hub" + exam name.
        LinearLayout head = new LinearLayout(a);
        head.setOrientation(LinearLayout.VERTICAL);
        head.setBackground(Ui.round(TEAL, Ui.dp(a, 16)));
        head.setPadding(Ui.dp(a, 20), Ui.dp(a, 18), Ui.dp(a, 20), Ui.dp(a, 18));
        TextView title = Ui.tv(a, "Quiz Hub", 22, t.dispB(), GOLD);
        title.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        head.addView(title);
        TextView sub = Ui.tv(a, examName != null ? examName : "أسئلة مزاولة المهنة", 13, t.bodyM(), SOFT);
        head.addView(sub);
        root.addView(head, new LinearLayout.LayoutParams(-1, -2));

        // Question (LTR).
        TextView qv = Ui.tv(a, q.q, 19, t.bodyB(), INK);
        qv.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        qv.setLineSpacing(0, 1.4f);
        LinearLayout.LayoutParams qp = new LinearLayout.LayoutParams(-1, -2);
        qp.topMargin = Ui.dp(a, 20);
        root.addView(qv, qp);

        // Options with letter circles; the correct one is marked.
        char[] L = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        for (int i = 0; i < q.o.length; i++) {
            boolean correct = i == q.c;
            LinearLayout row = new LinearLayout(a);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(Ui.dp(a, 12), Ui.dp(a, 10), Ui.dp(a, 12), Ui.dp(a, 10));
            row.setBackground(Ui.roundStroke(correct ? TEAL : Color.WHITE, Ui.dp(a, 12), correct ? TEAL : 0xffd8ddd8, Ui.dp(a, 1)));
            TextView letter = Ui.tv(a, String.valueOf(L[i]), 13, t.disp(), correct ? TEAL : MUTED);
            GradientDrawable lc = new GradientDrawable();
            lc.setShape(GradientDrawable.OVAL);
            lc.setColor(correct ? GOLD : 0x1a0b4f4a);
            letter.setBackground(lc);
            letter.setGravity(Gravity.CENTER);
            row.addView(letter, new LinearLayout.LayoutParams(Ui.dp(a, 28), Ui.dp(a, 28)));
            TextView ot = Ui.tv(a, q.o[i], 16, t.bodyM(), correct ? Color.WHITE : INK);
            ot.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            LinearLayout.LayoutParams op = new LinearLayout.LayoutParams(0, -2, 1f);
            op.setMargins(Ui.dp(a, 12), 0, 0, 0);
            row.addView(ot, op);
            if (correct) row.addView(Ui.tv(a, "✓", 16, t.disp(), GOLD));
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, -2);
            rp.topMargin = Ui.dp(a, 8);
            root.addView(row, rp);
        }

        // Correct-answer line.
        TextView ans = Ui.tv(a, "الإجابة الصحيحة: " + Ui.lre(String.valueOf(L[q.c])), 15, t.dispB(), TEAL);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, -2);
        ap.topMargin = Ui.dp(a, 16);
        root.addView(ans, ap);

        // Divider.
        View div = new View(a);
        div.setBackgroundColor(GOLD);
        LinearLayout.LayoutParams dp2 = new LinearLayout.LayoutParams(-1, Ui.dp(a, 1));
        dp2.topMargin = Ui.dp(a, 16);
        root.addView(div, dp2);

        // Explanation (English, LTR) + Arabic translation (RTL).
        if (q.ex != null && !q.ex.isEmpty()) {
            TextView et = Ui.tv(a, "الشرح", 15, t.dispB(), TEAL);
            LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(-1, -2);
            ep.topMargin = Ui.dp(a, 16);
            root.addView(et, ep);
            TextView ex = Ui.tv(a, q.ex, 15, t.body(), INK);
            ex.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            ex.setLineSpacing(0, 1.4f);
            LinearLayout.LayoutParams xp = new LinearLayout.LayoutParams(-1, -2);
            xp.topMargin = Ui.dp(a, 6);
            root.addView(ex, xp);
        }
        if (q.tr != null && !q.tr.isEmpty()) {
            TextView tr = Ui.tv(a, q.tr, 15, t.body(), INK);
            tr.setLineSpacing(0, 1.5f);
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, -2);
            tp.topMargin = Ui.dp(a, 10);
            root.addView(tr, tp);
        }

        // Footer.
        TextView foot = Ui.tv(a, "Quiz Hub", 11, t.disp(), MUTED);
        foot.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, -2);
        fp.topMargin = Ui.dp(a, 22);
        root.addView(foot, fp);

        int w = Ui.dp(a, CARD_W_DP);
        root.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int h = root.getMeasuredHeight();
        root.layout(0, 0, w, h);
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(IVORY);
        root.draw(c);
        return bmp;
    }
}
