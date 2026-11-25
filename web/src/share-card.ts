/** Suggestion 35 (web): render the current question as a designed brand image
 *  (question + options with the correct answer marked + explanation +
 *  translation) on a canvas, then share it via the system share sheet when
 *  supported, or download the PNG otherwise. Existing fonts only
 *  (IBM Plex Sans Arabic / Readex Pro), brand colours only. */

export type ShareQuestion = { q: string; o: string[]; c: number; tr?: string; ex?: string };

const TEAL = '#0b3b37';
const GOLD = '#d4a24c';
const IVORY = '#f5f3ee';
const INK = '#1f2a2a';
const MUTED = '#5a6b68';
const SOFT = '#cfe8e0';
const W = 1080;
const PAD = 72;
const LETTERS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');

function wrap(ctx: CanvasRenderingContext2D, text: string, maxW: number): string[] {
    const words = text.split(/\s+/).filter(Boolean);
    const lines: string[] = [];
    let cur = '';
    for (const w of words) {
        const t = cur ? cur + ' ' + w : w;
        if (ctx.measureText(t).width <= maxW || !cur) cur = t;
        else { lines.push(cur); cur = w; }
    }
    if (cur) lines.push(cur);
    return lines;
}

function roundRect(ctx: CanvasRenderingContext2D, x: number, y: number, w: number, h: number, r: number) {
    ctx.beginPath();
    ctx.moveTo(x + r, y);
    ctx.arcTo(x + w, y, x + w, y + h, r);
    ctx.arcTo(x + w, y + h, x, y + h, r);
    ctx.arcTo(x, y + h, x, y, r);
    ctx.arcTo(x, y, x + w, y, r);
    ctx.closePath();
}

export async function shareQuestionCard(q: ShareQuestion, examName: string): Promise<'shared' | 'downloaded' | 'failed'> {
    try {
        try {
            await Promise.all([
                document.fonts.load('600 44px "IBM Plex Sans Arabic"'),
                document.fonts.load('700 64px "Readex Pro"'),
                document.fonts.load('500 36px "IBM Plex Sans Arabic"'),
            ]);
            await document.fonts.ready;
        } catch { /* fall back to system fonts */ }
        const meas = document.createElement('canvas').getContext('2d')!;
        const contentW = W - PAD * 2;
        // ---- layout pass (measure only) ----
        meas.font = '600 44px "IBM Plex Sans Arabic"';
        const qLines = wrap(meas, q.q, contentW);
        meas.font = '500 38px "IBM Plex Sans Arabic"';
        const optLines = q.o.map(o => wrap(meas, o, contentW - 150));
        meas.font = '400 38px "IBM Plex Sans Arabic"';
        const exLines = q.ex ? wrap(meas, q.ex, contentW) : [];
        const trLines = q.tr ? wrap(meas, q.tr, contentW) : [];
        const optH = optLines.map(ls => Math.max(96, ls.length * 52 + 44));
        const H = PAD + 190 + 44 + qLines.length * 60 + 24
            + optH.reduce((a, b) => a + b + 20, 0) + 20
            + 64 + 24 + 2 + 40
            + (exLines.length ? 56 + 44 + exLines.length * 52 + 24 : 0)
            + (trLines.length ? trLines.length * 56 + 24 : 0)
            + 60 + PAD;
        const cv = document.createElement('canvas');
        cv.width = W; cv.height = H;
        const ctx = cv.getContext('2d')!;
        ctx.fillStyle = IVORY; ctx.fillRect(0, 0, W, H);
        let y = PAD;
        // header band
        ctx.fillStyle = TEAL;
        roundRect(ctx, PAD, y, contentW, 190, 36); ctx.fill();
        ctx.fillStyle = GOLD; ctx.font = '700 64px "Readex Pro"'; ctx.textAlign = 'left'; ctx.textBaseline = 'alphabetic'; ctx.direction = 'ltr';
        ctx.fillText('Quiz Hub', PAD + 48, y + 88);
        ctx.fillStyle = SOFT; ctx.font = '500 36px "IBM Plex Sans Arabic"'; ctx.direction = 'rtl'; ctx.textAlign = 'right';
        ctx.fillText(examName || 'أسئلة مزاولة المهنة', PAD + contentW - 48, y + 148);
        y += 190 + 44;
        // question (LTR)
        ctx.fillStyle = INK; ctx.font = '600 44px "IBM Plex Sans Arabic"'; ctx.direction = 'ltr'; ctx.textAlign = 'left';
        for (const ln of qLines) { ctx.fillText(ln, PAD, y + 44); y += 60; }
        y += 24;
        // options
        for (let i = 0; i < q.o.length; i++) {
            const correct = i === q.c;
            const h = optH[i];
            ctx.fillStyle = correct ? TEAL : '#ffffff';
            roundRect(ctx, PAD, y, contentW, h, 28); ctx.fill();
            if (!correct) { ctx.strokeStyle = '#d8ddd8'; ctx.lineWidth = 2; ctx.stroke(); }
            // letter circle
            const cy = y + h / 2;
            ctx.beginPath(); ctx.arc(PAD + 54, cy, 30, 0, Math.PI * 2);
            ctx.fillStyle = correct ? GOLD : 'rgba(11,79,74,0.10)'; ctx.fill();
            ctx.fillStyle = correct ? TEAL : MUTED; ctx.font = '600 30px "Readex Pro"'; ctx.textAlign = 'center'; ctx.textBaseline = 'middle';
            ctx.fillText(LETTERS[i], PAD + 54, cy + 2);
            // option text
            ctx.fillStyle = correct ? '#ffffff' : INK; ctx.font = '500 38px "IBM Plex Sans Arabic"'; ctx.textAlign = 'left'; ctx.textBaseline = 'alphabetic'; ctx.direction = 'ltr';
            const ls = optLines[i];
            let ty = y + (h - (ls.length - 1) * 52) / 2 + 13;
            for (const ln of ls) { ctx.fillText(ln, PAD + 110, ty); ty += 52; }
            if (correct) { ctx.fillStyle = GOLD; ctx.font = '600 40px "Readex Pro"'; ctx.textAlign = 'right'; ctx.fillText('✓', PAD + contentW - 36, cy + 14); }
            y += h + 20;
        }
        y += 20;
        // correct-answer line (RTL)
        ctx.fillStyle = TEAL; ctx.font = '700 40px "Readex Pro"'; ctx.direction = 'rtl'; ctx.textAlign = 'right';
        ctx.fillText('الإجابة الصحيحة: ' + LETTERS[q.c], PAD + contentW, y + 40);
        y += 64 + 24;
        // divider
        ctx.fillStyle = GOLD; ctx.fillRect(PAD, y, contentW, 2);
        y += 40;
        // explanation (LTR) + translation (RTL)
        if (exLines.length) {
            ctx.fillStyle = TEAL; ctx.font = '700 40px "Readex Pro"'; ctx.direction = 'rtl'; ctx.textAlign = 'right';
            ctx.fillText('الشرح', PAD + contentW, y + 40);
            y += 56 + 44;
            ctx.fillStyle = INK; ctx.font = '400 38px "IBM Plex Sans Arabic"'; ctx.direction = 'ltr'; ctx.textAlign = 'left';
            for (const ln of exLines) { ctx.fillText(ln, PAD, y + 38); y += 52; }
            y += 24;
        }
        if (trLines.length) {
            ctx.fillStyle = INK; ctx.font = '400 38px "IBM Plex Sans Arabic"'; ctx.direction = 'rtl'; ctx.textAlign = 'right';
            for (const ln of trLines) { ctx.fillText(ln, PAD + contentW, y + 38); y += 56; }
            y += 24;
        }
        // footer
        ctx.fillStyle = MUTED; ctx.font = '500 28px "Readex Pro"'; ctx.direction = 'ltr'; ctx.textAlign = 'center';
        ctx.fillText('Quiz Hub', W / 2, H - PAD + 20);
        // ---- share or download ----
        const blob = await new Promise<Blob | null>(res => cv.toBlob(res, 'image/png'));
        if (!blob) return 'failed';
        const file = new File([blob], 'quiz-hub-question.png', { type: 'image/png' });
        if (typeof navigator.share === 'function') {
            try {
                if (!navigator.canShare || navigator.canShare({ files: [file] })) {
                    await navigator.share({ files: [file] });
                    return 'shared';
                }
            } catch { /* user cancelled or share failed — fall through to download */ }
        }
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url; a.download = 'quiz-hub-question.png'; a.click();
        window.setTimeout(() => URL.revokeObjectURL(url), 5000);
        return 'downloaded';
    } catch {
        return 'failed';
    }
}
