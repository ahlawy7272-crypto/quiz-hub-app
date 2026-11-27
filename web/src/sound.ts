// Correct / wrong answer blips, played through WebAudio so they never wait
// on an <audio> element. The two clips are base64 data in sounds-data.ts.
import { b64_correct, b64_wrong } from './sounds-data';

export type SoundKind = 'correct' | 'wrong';

const soundData: Record<SoundKind, string> = { correct: b64_correct, wrong: b64_wrong };
let masterLevel = 0.8;
let audioCtx: AudioContext | null = null;
const bufferCache = new Map<string, Promise<AudioBuffer>>();
const liveGains = new Set<GainNode>();

const getCtx = (): AudioContext | null => {
    try {
        if (!audioCtx) {
            const AC = window.AudioContext || (window as unknown as { webkitAudioContext?: typeof AudioContext }).webkitAudioContext;
            if (!AC) return null;
            audioCtx = new AC();
        }
        if (audioCtx.state === 'suspended') void audioCtx.resume();
        return audioCtx;
    } catch { return null; }
};

const loadBuffer = (kind: SoundKind, ctx: AudioContext): Promise<AudioBuffer> => {
    const key = soundData[kind];
    let p = bufferCache.get(key);
    if (!p) {
        const bin = atob(key);
        const bytes = new Uint8Array(bin.length);
        for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);
        p = new Promise<AudioBuffer>((res, rej) => { ctx.decodeAudioData(bytes.buffer.slice(0), res, rej); });
        bufferCache.set(key, p);
    }
    return p;
};

export const setMasterVolume = (pct: number) => {
    masterLevel = Math.min(1, Math.max(0, pct / 100));
    liveGains.forEach(g => { g.gain.value = masterLevel; });
};

/** Fire-and-forget blip; never throws, never blocks the UI. */
export function playSound(kind: SoundKind) {
    try {
        const ctx = getCtx();
        if (!ctx) return;
        loadBuffer(kind, ctx).then(buf => {
            const src = ctx.createBufferSource();
            src.buffer = buf;
            const g = ctx.createGain();
            g.gain.value = masterLevel;
            src.connect(g);
            g.connect(ctx.destination);
            liveGains.add(g);
            src.onended = () => { liveGains.delete(g); };
            src.start(0);
        }).catch(() => { /* decode failed */ });
    } catch { /* audio unavailable */ }
}

// Warm the decoder on the first user gesture so the first blip is instant.
if (typeof window !== 'undefined') {
    const unlock = () => {
        const c = getCtx();
        if (c) { (['correct', 'wrong'] as const).forEach(n => { void loadBuffer(n, c).catch(() => {}); }); }
    };
    window.addEventListener('pointerdown', unlock, { once: true, capture: true });
    window.addEventListener('keydown', unlock, { once: true, capture: true });
}
