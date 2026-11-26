// Saved state: same localStorage keys and same wire format as the current
// web build, so an in-progress attempt survives the rebuild untouched.
//   quiz-banks.v2 — the attempt (answers, flags, exam, settings, clock)
//   qh-extra.v1  — the wrong-questions notebook (bit-packed) + attempt history
// State saved by the older 3,122-question build is NOT converted: the old
// question order is not available here, so answers, flags and the notebook
// cannot be mapped safely. Its raw strings are kept byte for byte under the
// LEGACY keys (never overwritten) and can be exported from Settings > backup.
import React, { useState } from 'react';
import { sourceData } from './bank-data';

export type QuizQuestion = {
    q: string; o: string[]; c: number; tr: string; hint: string; ex: string;
    why: string; st?: string; src?: string; url?: string;
};

export const questions = sourceData.questions as QuizQuestion[];
export const banks = [...sourceData.banks].sort((a, b) => (a.id === '0' ? -1 : 0) - (b.id === '0' ? -1 : 0));

export type Mode = 'study' | 'exam';
export type Setup = { section: string; mode: Mode };
export type RunKind = 'notebook' | 'flagged';
export type Run = { kind: RunKind; ids: number[] };
export type Attempt = { t: number; b: string; m: Mode; r: number; n: number; s: number };
export type Extra = { w: string; h: Attempt[] };

export type Settings = {
    dark: boolean; textSize: string; fontScale?: number; spacious: boolean; contrast: boolean; calm: boolean; steps: boolean; letters: boolean;
    confirm: boolean; autoNext: boolean; retry: boolean; hints: boolean; hintDefault: boolean;
    translation: boolean; autoExplain: boolean; timer: boolean; quizOnly: boolean; countdown: boolean; minutes: number;
    sounds: boolean; fx: boolean; volume?: number;
    /** Bank ids whose questions run in a random order on a fresh start. Default off. */
    shuffleBanks?: string[];
};

export const fontPercent = (s: Settings) => s.fontScale ?? (s.textSize === 'small' ? 88 : s.textSize === 'large' ? 118 : 100);

export const defaults: Settings = {
    dark: false, textSize: 'normal', spacious: false, contrast: false, calm: false, steps: true, letters: true,
    confirm: false, autoNext: false, retry: false, hints: true, hintDefault: false, translation: false, autoExplain: false,
    timer: true, quizOnly: false, countdown: false, minutes: 120, sounds: true, fx: true, volume: 80, shuffleBanks: [],
};

export type Saved = {
    run?: string; bank?: string; settings: Settings; favorites: number[]; ids: number[];
    picked: (number | null)[]; index: number; elapsed: number; paused: boolean; started: boolean;
    submitted: boolean; resultElapsed: number | null; mode: Mode; tab: 'home' | 'quiz' | 'settings'; setup: Setup;
};

const initial: Saved = {
    settings: { ...defaults }, favorites: [], ids: banks[0].ids, picked: questions.map(() => null),
    index: 0, elapsed: 0, paused: false, started: false, submitted: false, resultElapsed: null,
    mode: 'study', tab: 'home', setup: { section: banks[0].id, mode: 'study' },
};

export const emptyExtra: Extra = { w: '', h: [] };
export const HISTORY_MAX = 60;

const OLD_COUNT = 3122;
const removedSet = new Set<number>([...Array(4).keys()].map(i => 1370 + i).concat([...Array(16).keys()].map(i => 1384 + i)));
const oldToNew = (old: number) => {
    let shift = 0;
    for (let i = 1370; i < old; i++) { if (removedSet.has(i)) shift++; }
    return old - shift;
};

export const packBits = (set: Set<number>): string => {
    const bytes = new Uint8Array(Math.ceil(questions.length / 8));
    set.forEach(i => { if (i >= 0 && i < questions.length) bytes[i >> 3] |= 1 << (i & 7); });
    let last = bytes.length;
    while (last > 0 && bytes[last - 1] === 0) last--;
    let bin = '';
    for (let i = 0; i < last; i++) bin += String.fromCharCode(bytes[i]);
    return btoa(bin);
};

export const unpackBits = (str: string): Set<number> => {
    const out = new Set<number>();
    if (!str) return out;
    try {
        const bin = atob(str);
        for (let i = 0; i < bin.length; i++) {
            const b = bin.charCodeAt(i);
            for (let k = 0; k < 8; k++) if (b & (1 << k)) { const id = i * 8 + k; if (id < questions.length) out.add(id); }
        }
    } catch { /* ignore bad data */ }
    return out;
};

export const toB64 = (u8: Uint8Array) => { let bin = ''; for (let i = 0; i < u8.length; i++) bin += String.fromCharCode(u8[i]); return btoa(bin); };
export const fromB64 = (str: string) => { const bin = atob(str); const u8 = new Uint8Array(bin.length); for (let i = 0; i < bin.length; i++) u8[i] = bin.charCodeAt(i); return u8; };
export const streamBytes = async (data: Uint8Array, T: 'CompressionStream' | 'DecompressionStream'): Promise<Uint8Array> => {
    const C = (window as unknown as Record<string, new (f: string) => TransformStream>)[T];
    const st = new Blob([data as unknown as BlobPart]).stream().pipeThrough(new C('deflate-raw'));
    return new Uint8Array(await new Response(st).arrayBuffer());
};

type Stored = Omit<Saved, 'picked' | 'favorites'> & { picked: string; favorites: string };

export const encode = (s: Saved): Stored => ({
    ...s,
    picked: s.picked.map(v => v === null ? '.' : String(v)).join(''),
    favorites: questions.map((_, i) => s.favorites.includes(i) ? '1' : '0').join(''),
});

const migrate = (s: Stored): Stored => {
    if (typeof s.picked !== 'string' || s.picked.length !== OLD_COUNT || questions.length === OLD_COUNT) return s;
    const keep: number[] = [];
    for (let i = 0; i < OLD_COUNT; i++) if (!removedSet.has(i)) keep.push(i);
    const picked = keep.map(i => s.picked[i]).join('');
    const favorites = typeof s.favorites === 'string' && s.favorites.length === OLD_COUNT ? keep.map(i => s.favorites[i]).join('') : s.favorites;
    let ids = s.ids;
    let index = typeof s.index === 'number' ? s.index : 0;
    // A session whose id list exactly matches a current bank was started with already-compacted ids: keep ids and index as they are.
    const joined = Array.isArray(s.ids) ? s.ids.join(',') : '';
    const compactedSession = joined.length > 0 && banks.some(b => b.ids.join(',') === joined);
    if (compactedSession) { ids = s.ids; } else {
        ids = Array.isArray(s.ids) ? s.ids.filter(i => !removedSet.has(i)).map(oldToNew) : s.ids;
        if (removedSet.has(index)) { let n = index; while (n < OLD_COUNT && removedSet.has(n)) n++; index = n < OLD_COUNT ? oldToNew(n) : Math.max(0, keep.length - 1); } else index = oldToNew(index);
    }
    return { ...s, picked, favorites, ids, index };
};

const freshStored = (): Stored => encode({ ...initial, settings: { ...defaults, minutes: banks[0].minutes } });

// Saved state is only trusted when it matches this build (1,186 questions).
const validStored = (s: unknown): boolean => {
    const o = s as Stored;
    return !!s && typeof o.picked === 'string' && o.picked.length === questions.length
        && typeof o.favorites === 'string' && o.favorites.length === questions.length
        && Array.isArray(o.ids) && o.ids.length > 0
        && o.ids.every((n: unknown) => typeof n === 'number' && Number.isInteger(n) && n >= 0 && n < questions.length)
        && /^[.0-4]*$/.test(o.picked) && typeof o.index === 'number';
};

const LEGACY_KEY = 'quiz-banks.v2.legacy-3122';
const LEGACY_EXTRA_KEY = 'qh-extra.v1.legacy-3122';
export const legacyInfo = { active: false };

const legacyStr = (k: string): string | null => { try { return localStorage.getItem(k); } catch { return null; } };

const quarantineLegacy = () => {
    try {
        if (typeof localStorage === 'undefined') return;
        const st = localStorage.getItem('quiz-banks.v2'), ex = localStorage.getItem('qh-extra.v1');
        if (st === null && ex === null) return;
        let stateOk = false;
        if (st !== null) {
            try {
                const o = JSON.parse(st);
                if (o && typeof o.picked === 'string') { if (o.picked.length === questions.length) stateOk = true; }
            } catch { /* malformed — quarantined below */ }
        }
        // A notebook (a bit string with no version or length) is only trusted next to a valid current state.
        const quarantineState = st !== null && !stateOk;
        const quarantineExtra = ex !== null && !stateOk;
        if (!quarantineState && !quarantineExtra) return;
        if (quarantineState && localStorage.getItem(LEGACY_KEY) === null) localStorage.setItem(LEGACY_KEY, st as string);
        if (quarantineExtra && localStorage.getItem(LEGACY_EXTRA_KEY) === null) localStorage.setItem(LEGACY_EXTRA_KEY, ex as string);
        if (quarantineExtra) localStorage.removeItem('qh-extra.v1');
        if (quarantineState) localStorage.removeItem('quiz-banks.v2');
        legacyInfo.active = true;
    } catch { /* storage unavailable */ }
};
quarantineLegacy();
if (!legacyInfo.active && legacyStr(LEGACY_KEY) !== null) legacyInfo.active = true;

export const legacyExportText = (): string => {
    const st = legacyStr(LEGACY_KEY), ex = legacyStr(LEGACY_EXTRA_KEY);
    if (st === null && ex === null) return '';
    return JSON.stringify({ kind: 'quizhub-legacy-raw-strings', state: st, notebook: ex });
};

export const decode = (raw: Stored): Saved => {
    let s: Stored = raw;
    if (raw && typeof raw.picked === 'string' && raw.picked.length === OLD_COUNT) {
        s = { ...freshStored(), settings: (raw as unknown as { settings?: Settings }).settings && typeof (raw as unknown as { settings?: Settings }).settings === 'object' ? (raw as unknown as { settings: Settings }).settings : {} as Settings };
    }
    if (!validStored(s)) {
        s = { ...freshStored(), settings: (raw as unknown as { settings?: Settings })?.settings && typeof (raw as unknown as { settings?: Settings }).settings === 'object' ? (raw as unknown as { settings: Settings }).settings : {} as Settings };
    }
    s = migrate(s);
    return {
        ...s, settings: { ...defaults, ...s.settings },
        picked: Array.from(s.picked as string, v => v === '.' ? null : Number(v)),
        favorites: Array.from(s.favorites as string).flatMap((v, i) => v === '1' ? [i] : []),
    };
};

export const freshSaved = (): Saved => decode(freshStored());

// localStorage-backed state with a real save status: a failed write reports
// 'error' instead of always claiming 'saved'.
const storageOk = (): boolean => {
    try { localStorage.setItem('__qh_probe', '1'); localStorage.removeItem('__qh_probe'); return true; } catch { return false; }
};

export function useStoredState<T>(key: string, init: T): [T, (v: T | ((prev: T) => T)) => void, { status: string; ready: boolean }] {
    const [status, setStatus] = useState<string>(() => storageOk() ? 'saved' : 'error');
    const [v, setV] = useState<T>(() => {
        try { const r = localStorage.getItem(key); return r ? JSON.parse(r) as T : init; } catch { return init; }
    });
    const set = (f: T | ((prev: T) => T)) => setV(prev => {
        const n = typeof f === 'function' ? (f as (prev: T) => T)(prev) : f;
        try { localStorage.setItem(key, JSON.stringify(n)); setStatus('saved'); } catch { setStatus('error'); }
        return n;
    });
    return [v, set, { status, ready: true }];
}

export const freshStoredFor = freshStored;
export type { Stored };
