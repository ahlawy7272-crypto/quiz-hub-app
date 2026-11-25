import quizHubMark from './branding/quiz-hub-mark.png';
import quizHubMarkLime from './branding/quiz-hub-mark-lime.png';
import { sourceData } from './bank-data';
import { audioLoaders, generatedAudio } from './audio-map';
import React, { useEffect, useState, useRef } from 'react';
import { FileCard, Route, Routes, TextLink, CloseButton, CopyButton, useLocalState } from '@instinct/files';

type QuizQuestion = {
    q: string;
    o: string[];
    c: number;
    tr: string;
    hint: string;
    ex: string;
    why: string;
    src?: string;
    url?: string;
};

const questions = sourceData.questions as QuizQuestion[];
const banks = [...sourceData.banks].sort((a,b)=>(a.id==='6'?-1:0)-(b.id==='6'?-1:0));
const audioFor = (id:number):boolean => !!audioLoaders[id];
const OLD_COUNT = 3122;
const removedSet = new Set<number>([...Array(4).keys()].map(i=>1370+i).concat([...Array(16).keys()].map(i=>1384+i)));
const oldToNew = (old:number) => { let shift=0; for(let i=1370;i<old;i++){ if(removedSet.has(i)) shift++; } return old-shift; };
const letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');
const arDigits = ['٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'];
const toAr = (s: string) => s.replace(/\d/g, (d) => arDigits[Number(d)]);
const formatTime = (total: number) => {
    const m = Math.floor(total / 60);
    const s = total % 60;
    return toAr(`${m}:${String(s).padStart(2, '0')}`);
};

function QuizBrand({compact=false}:{compact?:boolean}) {
    return <span className={'qz-brand'+(compact?' qz-brand-compact':'')} dir="rtl"><img className="qz-brand-mark qz-brand-mark-light" src={quizHubMark} alt="QH مع كتاب"/><img className="qz-brand-mark qz-brand-mark-dark" src={quizHubMarkLime} alt="QH مع كتاب"/><bdi className="qz-brand-name" dir="ltr">Quiz Hub</bdi>{!compact&&<span className="qz-brand-purpose" dir="rtl">أسئلة مزاولة المهنة</span>}</span>;
}

function ClockIcon() {
    return (
        <svg className="qz-clock" viewBox="0 0 24 24" width="15" height="15" aria-hidden="true">
            <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" strokeWidth="2" />
            <line x1="12" y1="12" x2="12" y2="7.2" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
            <line className="qz-clock-hand" x1="12" y1="12" x2="16.2" y2="12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
        </svg>
    );
}

function TranslateIcon() {
    return <svg viewBox="0 0 24 24" width="21" height="21" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M3 5h11M8 3v2M5 5c.5 4 3.5 7.5 7 9M12 5c-.5 4-4 8-9 10M13 21l4-11 4 11M14.5 17h5"/></svg>;
}

function HomeIcon() {
    return (
        <svg viewBox="0 0 24 24" width="21" height="21" aria-hidden="true">
            <path d="M4 11.2 12 4l8 7.2" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
            <path d="M6.5 9.8V20h11V9.8" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
    );
}

function FlagIcon({filled}:{filled:boolean}) {
    return <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true"><path d="M6 3v18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/><path d="M6 4h11l-2.5 4L17 12H6z" fill={filled?'currentColor':'none'} stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/></svg>;
}

// S4-7: real SVG icons instead of the 🔊/⏹/💡 emoji the tools row used before.
function SpeakerIcon() {
    return <svg viewBox="0 0 24 24" width="15" height="15" aria-hidden="true"><path d="M3 9v6h4l5 5V4L7 9H3z" fill="currentColor"/><path d="M16 8.5a5 5 0 0 1 0 7" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/><path d="M18.5 6a8.5 8.5 0 0 1 0 12" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/></svg>;
}
function StopIcon() {
    return <svg viewBox="0 0 24 24" width="14" height="14" aria-hidden="true"><rect x="6" y="6" width="12" height="12" rx="2" fill="currentColor"/></svg>;
}
function BulbIcon() {
    return <svg viewBox="0 0 24 24" width="15" height="15" aria-hidden="true"><path d="M12 2a7 7 0 0 0-4 12.7c.6.5 1 1.4 1 2.3h6c0-.9.4-1.8 1-2.3A7 7 0 0 0 12 2z" fill="none" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/><path d="M9.5 19h5M10.5 21.5h3" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/></svg>;
}

function ListIcon() {
    return (
        <svg viewBox="0 0 24 24" width="21" height="21" aria-hidden="true">
            <circle cx="5" cy="6" r="1.6" fill="currentColor" />
            <circle cx="5" cy="12" r="1.6" fill="currentColor" />
            <circle cx="5" cy="18" r="1.6" fill="currentColor" />
            <path d="M10 6h9M10 12h9M10 18h9" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
        </svg>
    );
}

function GearIcon() {
    return (
        <svg viewBox="0 0 24 24" width="21" height="21" aria-hidden="true">
            <circle cx="12" cy="12" r="3.2" fill="none" stroke="currentColor" strokeWidth="2" />
            <path
                d="M12 2.8v2.6M12 18.6v2.6M2.8 12h2.6M18.6 12h2.6M5.5 5.5l1.8 1.8M16.7 16.7l1.8 1.8M18.5 5.5l-1.8 1.8M7.3 16.7l-1.8 1.8"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
            />
        </svg>
    );
}

function MenuIcon() {
    return <svg viewBox="0 0 24 24" width="21" height="21" aria-hidden="true"><path d="M4 6h16M4 12h16M4 18h16" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/></svg>;
}

/** Side navigation drawer — replaces the old "› القائمة" header button.
 *  Data-driven: items are {id, label, badge?, disabled?, action} so more can
 *  be added later without touching the drawer chrome. Slides in from the
 *  start (right) side in RTL; closes via X, scrim tap, Escape, or a swipe
 *  toward the start edge. */
function Drawer({ open, onClose, items, dark, onDark, fontPct, onFontScale }: {
    open: boolean; onClose: () => void;
    items: { id: string; label: string; badge?: string; disabled?: boolean; action: () => void }[];
    dark: boolean; onDark: (v: boolean) => void;
    fontPct: number; onFontScale: (v: number) => void;
}) {
    const touchStart = useRef<{ x: number } | null>(null);
    useEffect(() => {
        if (!open) return;
        const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
        window.addEventListener('keydown', onKey);
        return () => window.removeEventListener('keydown', onKey);
    }, [open, onClose]);
    return <div className={'qz-drawer-wrap' + (open ? ' is-open' : '')} aria-hidden={!open}>
        <div className="qz-drawer-scrim" onClick={onClose} />
        <aside className="qz-drawer" dir="rtl" role="dialog" aria-label="القائمة"
            onTouchStart={e => { touchStart.current = { x: e.changedTouches[0].clientX }; }}
            onTouchEnd={e => { const s = touchStart.current; touchStart.current = null; if (!s) return; const dx = e.changedTouches[0].clientX - s.x; if (dx > 60) onClose(); }}>
            <div className="qz-drawer-head"><span className="qz-drawer-title">القائمة</span><button type="button" className="qz-drawer-x" aria-label="إغلاق القائمة" onClick={onClose}>✕</button></div>
            <nav className="qz-drawer-nav" aria-label="التنقل">
                {items.map(it => <button type="button" key={it.id} className="qz-drawer-item" disabled={it.disabled} onClick={() => { onClose(); it.action(); }}>{it.label}{it.badge && <span className="qz-drawer-badge"><bdi>{it.badge}</bdi></span>}</button>)}
            </nav>
            <div className="qz-drawer-theme"><div className="qz-font-row" dir="rtl"><span className="qz-font-row-label">حجم الخط</span><button type="button" className="qz-font-step" dir="ltr" aria-label="تصغير الخط" onClick={()=>onFontScale(Math.max(70,fontPct-10))}>A−</button><bdi className="qz-font-row-pct">{fontPct}%</bdi><button type="button" className="qz-font-step" dir="ltr" aria-label="تكبير الخط" onClick={()=>onFontScale(Math.min(125,fontPct+10))}>A+</button></div><div className="qz-set-row"><div><span className="qz-set-label" id="set-drawer-dark">الوضع الليلي</span></div><button type="button" className={'qz-switch'+(dark?' is-on':'')} role="switch" aria-labelledby="set-drawer-dark" aria-checked={dark} onClick={()=>onDark(!dark)}><span className="qz-switch-knob"/></button></div></div>
        </aside>
    </div>;
}

type Settings = {
    dark: boolean; textSize: string; fontScale?: number; spacious: boolean; contrast: boolean; calm: boolean; steps: boolean; letters: boolean;
    confirm: boolean; autoNext: boolean; retry: boolean; hints: boolean; hintDefault: boolean;
    translation: boolean; autoExplain: boolean; timer: boolean; quizOnly: boolean; countdown: boolean; minutes: number;
    sounds: boolean; fx: boolean; volume?: number;
    /** Suggestion 36: bank ids whose questions run in a random order when a
     *  fresh exam starts. Default off (absent/empty). */
    shuffleBanks?: string[];
};
const fontPercent = (s: Settings) => s.fontScale ?? (s.textSize==='small'?88:s.textSize==='large'?118:100);
const defaults: Settings = { dark: false, textSize: 'normal', spacious: false, contrast: false, calm: false, steps: true, letters: true, confirm: false, autoNext: false, retry: false, hints: true, hintDefault: false, translation: false, autoExplain: false, timer: true, quizOnly: false, countdown: false, minutes: 120, sounds: true, fx: true, volume: 80, shuffleBanks: [] };

import { b64_correct, b64_wrong } from './sounds-data';
import { shareQuestionCard } from './share-card';

type SoundKind = 'correct' | 'wrong';
const soundData: Record<SoundKind, string> = { correct: b64_correct, wrong: b64_wrong };
let masterLevel = 0.8;
let audioCtx: AudioContext | null = null;
const bufferCache = new Map<string, Promise<AudioBuffer>>();
const liveGains = new Set<GainNode>();
const getCtx = (): AudioContext | null => {
    try {
        if (!audioCtx) { const AC = window.AudioContext || (window as any).webkitAudioContext; if (!AC) return null; audioCtx = new AC(); }
        if (audioCtx.state === 'suspended') void audioCtx.resume();
        return audioCtx;
    } catch { return null; }
};
const loadBuffer = (kind: SoundKind, ctx: AudioContext): Promise<AudioBuffer> => {
    const key = soundData[kind];
    let p = bufferCache.get(key);
    if (!p) {
        const bin = atob(key); const bytes = new Uint8Array(bin.length);
        for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);
        p = new Promise<AudioBuffer>((res, rej) => { ctx.decodeAudioData(bytes.buffer.slice(0), res, rej); });
        bufferCache.set(key, p);
    }
    return p;
};
const setMasterVolume = (pct: number) => {
    masterLevel = Math.min(1, Math.max(0, pct / 100));
    liveGains.forEach(g => { g.gain.value = masterLevel; });
};
function play(kind: SoundKind) {
    try {
        const ctx = getCtx();
        if (!ctx) return;
        loadBuffer(kind, ctx).then(buf => {
            const src = ctx.createBufferSource(); src.buffer = buf;
            const g = ctx.createGain(); g.gain.value = masterLevel;
            src.connect(g); g.connect(ctx.destination);
            liveGains.add(g);
            src.onended = () => { liveGains.delete(g); };
            src.start(0);
        }).catch(() => { /* decode failed */ });
    } catch { /* audio unavailable */ }
}
if (typeof window !== 'undefined') {
    const unlock = () => { const c = getCtx(); if (c) { (['correct','wrong'] as const).forEach(n => { void loadBuffer(n, c).catch(()=>{}); }); } };
    window.addEventListener('pointerdown', unlock, { once: true, capture: true });
    window.addEventListener('keydown', unlock, { once: true, capture: true });
}
/** Round 5 (feedback): the timer is a designed pill — a small clock icon plus
 *  tabular numbers in gold, calm red in the last 5 minutes of a countdown
 *  (`.qz-timer-low`). The value stays bidi-isolated in a <bdi>. */
function TimerChip({ elapsed, countdown = false, low = false }: { elapsed: number; countdown?: boolean; low?: boolean }) {
    return (
        <span className={'qz-timer'+(low?' qz-timer-low':'')} role="timer" aria-label={`${countdown ? "الوقت المتبقي" : "الوقت من بداية التجربة"} ${formatTime(elapsed)}`}>
            <ClockIcon />
            <span className="qz-timer-label">{countdown ? "باقي" : "الوقت"}</span>
            <bdi className="qz-timer-value">{formatTime(elapsed)}</bdi>
        </span>
    );
}

type Mode = 'study' | 'exam';
type Setup = { section: string; mode: Mode };
type Run = { kind: 'notebook'|'flagged'; ids: number[] };
type Attempt = { t: number; b: string; m: Mode; r: number; n: number; s: number };
type Extra = { w: string; h: Attempt[] };
const emptyExtra: Extra = { w: '', h: [] };
const packBits = (set: Set<number>): string => { const bytes = new Uint8Array(Math.ceil(questions.length/8)); set.forEach(i => { if (i>=0 && i<questions.length) bytes[i>>3] |= 1<<(i&7); }); let last = bytes.length; while (last>0 && bytes[last-1]===0) last--; let bin=''; for (let i=0;i<last;i++) bin+=String.fromCharCode(bytes[i]); return btoa(bin); };
const unpackBits = (str: string): Set<number> => { const out = new Set<number>(); if (!str) return out; try { const bin = atob(str); for (let i=0;i<bin.length;i++){ const b = bin.charCodeAt(i); for (let k=0;k<8;k++) if (b&(1<<k)) { const id=i*8+k; if (id<questions.length) out.add(id); } } } catch { /* ignore bad data */ } return out; };
const HISTORY_MAX = 60;
const runLabel = (b: string) => b==='notebook' ? 'دفتر الغلطات' : b==='flagged' ? 'الأسئلة المعلّمة' : (banks.find(x=>x.id===b)?.name ?? b);
// Round 5 (#7/#16): exam type + year derived from the name (mirrors Android).
const examType = (name:string)=> name.includes('فعلي')?2:name.includes('محاكاة')?1:0;
const examTypeLabel = (t:number)=> t===2?'فعلي':t===1?'محاكاة':'تجريبي';
const examYear = (name:string)=> name.includes('2026')?2026:name.includes('2025')?2025:name.includes('2024')?2024:0;
const toB64 = (u8: Uint8Array) => { let bin=''; for (let i=0;i<u8.length;i++) bin+=String.fromCharCode(u8[i]); return btoa(bin); };
const fromB64 = (str: string) => { const bin=atob(str); const u8=new Uint8Array(bin.length); for (let i=0;i<bin.length;i++) u8[i]=bin.charCodeAt(i); return u8; };
const streamBytes = async (data: Uint8Array, T: 'CompressionStream'|'DecompressionStream'): Promise<Uint8Array> => { const C = (window as any)[T]; const st = new Blob([data as unknown as BlobPart]).stream().pipeThrough(new C('deflate-raw')); return new Uint8Array(await new Response(st).arrayBuffer()); };
type Saved = { run?: string; bank?: string; settings: Settings; favorites: number[]; ids: number[]; picked: (number|null)[]; index: number; elapsed: number; paused: boolean; started: boolean; submitted: boolean; resultElapsed: number|null; mode: Mode; tab: 'home'|'quiz'|'settings'; setup: Setup };
const initial: Saved = { settings: {...defaults}, favorites: [], ids: banks[0].ids, picked: questions.map(()=>null), index: 0, elapsed: 0, paused: false, started: false, submitted: false, resultElapsed: null, mode: 'study', tab: 'home', setup: {section:banks[0].id,mode:'study'} };

type Stored = Omit<Saved,'picked'|'favorites'> & { picked: string; favorites: string };
const encode = (s:Saved):Stored => ({...s,picked:s.picked.map(v=>v===null?'.':String(v)).join(''),favorites:questions.map((_,i)=>s.favorites.includes(i)?'1':'0').join('')});
const migrate = (s:Stored):Stored => {
    if(typeof s.picked!=='string'||s.picked.length!==OLD_COUNT||questions.length===OLD_COUNT)return s;
    const keep:number[]=[];
    for(let i=0;i<OLD_COUNT;i++)if(!removedSet.has(i))keep.push(i);
    const picked=keep.map(i=>s.picked[i]).join('');
    const favorites=typeof s.favorites==='string'&&s.favorites.length===OLD_COUNT?keep.map(i=>s.favorites[i]).join(''):s.favorites;
    let ids=s.ids;
    let index=typeof s.index==='number'?s.index:0;
    // A session whose id list exactly matches a current bank was started with already-compacted ids: keep ids and index as they are.
    const joined=Array.isArray(s.ids)?s.ids.join(','):'';
    const compactedSession=joined.length>0&&banks.some(b=>b.ids.join(',')===joined);
    if(compactedSession){ ids=s.ids; }
    else {
        ids=Array.isArray(s.ids)?s.ids.filter(i=>!removedSet.has(i)).map(oldToNew):s.ids;
        if(removedSet.has(index)){ let n=index; while(n<OLD_COUNT&&removedSet.has(n))n++; index=n<OLD_COUNT?oldToNew(n):Math.max(0,keep.length-1); } else index=oldToNew(index);
    }
    return {...s,picked,favorites,ids,index};
};
const freshStored = ():Stored => encode({...initial,settings:{...defaults,minutes:banks[0].minutes}});
// Saved state is only trusted when it matches this build (1,186 questions).
const validStored = (s:any):boolean => !!s && typeof s.picked==='string' && s.picked.length===questions.length && typeof s.favorites==='string' && s.favorites.length===questions.length && Array.isArray(s.ids) && s.ids.length>0 && s.ids.every((n:unknown)=>typeof n==='number'&&Number.isInteger(n)&&n>=0&&n<questions.length) && /^[.0-4]*$/.test(s.picked) && typeof s.index==='number';
// State saved by the older 3,122-question build is NOT converted: the old question order is not available here, so answers, flags and the notebook cannot be mapped safely. The raw old state and the raw old notebook are kept byte for byte under LEGACY_KEY / LEGACY_EXTRA_KEY (never overwritten) and can be exported from Settings > backup. This build starts fresh and says so.
const LEGACY_KEY = 'quiz-banks.v2.legacy-3122';
const LEGACY_EXTRA_KEY = 'qh-extra.v1.legacy-3122';
const legacyInfo = { active: false };
// The exact localStorage strings are captured before anything parses or rewrites them, so the originals survive byte for byte (whitespace, key order, even malformed JSON).
const legacyStr = (k:string):string|null => { try { return localStorage.getItem(k); } catch { return null; } };
const quarantineLegacy = () => {
    try {
        if (typeof localStorage==='undefined') return;
        const st = localStorage.getItem('quiz-banks.v2'), ex = localStorage.getItem('qh-extra.v1');
        if (st===null && ex===null) return;
        let kind = '';
        let stateOk = false;
        if (st!==null) { try { const o = JSON.parse(st); if (o && typeof o.picked==='string') { if (o.picked.length===OLD_COUNT) kind='old'; else if (o.picked.length===questions.length) stateOk=true; else kind='other'; } else kind='other'; } catch { kind='malformed'; } }
        // A notebook (a bit string with no version or length) is only trusted next to a valid current state. Without one it cannot be read safely, so it is quarantined.
        const quarantineState = st!==null && !stateOk;
        const quarantineExtra = ex!==null && !stateOk;
        if (!quarantineState && !quarantineExtra) return;
        if (quarantineState && localStorage.getItem(LEGACY_KEY)===null) localStorage.setItem(LEGACY_KEY, st as string);
        if (quarantineExtra && localStorage.getItem(LEGACY_EXTRA_KEY)===null) localStorage.setItem(LEGACY_EXTRA_KEY, ex as string);
        if (quarantineExtra) localStorage.removeItem('qh-extra.v1');
        if (quarantineState) localStorage.removeItem('quiz-banks.v2');
        legacyInfo.active = true;
    } catch { /* storage unavailable */ }
};
quarantineLegacy();
if (!legacyInfo.active && legacyStr(LEGACY_KEY)!==null) legacyInfo.active = true;
const legacyExportText = ():string => { const st=legacyStr(LEGACY_KEY), ex=legacyStr(LEGACY_EXTRA_KEY); if(st===null&&ex===null) return ''; return JSON.stringify({kind:'quizhub-legacy-raw-strings',state:st,notebook:ex}); };
const decode = (raw:Stored):Saved => { let s:any=raw; if(raw&&typeof raw.picked==='string'&&raw.picked.length===OLD_COUNT){ s={...freshStored(),settings:(raw as any).settings&&typeof (raw as any).settings==='object'?(raw as any).settings:{}}; } if(!validStored(s)) s={...freshStored(),settings:(raw as any)?.settings&&typeof (raw as any).settings==='object'?(raw as any).settings:{}}; s=migrate(s); return {...s,settings:{...defaults,...s.settings},picked:Array.from(s.picked as string,v=>v==='.'?null:Number(v)),favorites:Array.from(s.favorites as string).flatMap((v,i)=>v==='1'?[i]:[])}; };
function QuizScreen({ state, update, viewIds, jump, pick, expired, finish, review, setReview, goHome, attemptKey, focus, setFocus, mapSignal, onMapOpened }: {state: Saved; update: (v: Partial<Saved>) => void; viewIds: number[]; jump:(id:number)=>void; pick:(v:number)=>void; expired:boolean; finish:()=>void; review:string; setReview:(v:string)=>void; goHome:()=>void; attemptKey:number; focus:boolean; setFocus:(v:boolean)=>void; mapSignal:number; onMapOpened:()=>void}) {
    const {index,picked,settings,mode,submitted} = state;
    const [trOpen,setTrOpen] = useState(settings.translation);
    const [hintOpen,setHintOpen] = useState(settings.hintDefault);
    const [exOpen,setExOpen] = useState(false);
    const audioRef = useRef<HTMLAudioElement|null>(null);
    const [playing,setPlaying] = useState(false);
    const [audioSrc,setAudioSrc] = useState<string|undefined>(undefined);
    const [wantPlay,setWantPlay] = useState(false);
    const [audioError,setAudioError] = useState(false); // S3-4: an audio failure says so instead of staying silent
    const audioReq = useRef(0);
    useEffect(()=>{ const a=audioRef.current; if(a){a.pause();a.currentTime=0;} audioReq.current++; setWantPlay(false); setPlaying(false); setAudioSrc(undefined); setAudioError(false); },[index]);
    // Start playback only after React has applied the new src to the <audio> element (no timers, so a slow load cannot race it).
    // S2-2: the volume slider now drives the explanation audio too, not just the correct/wrong blips.
    useEffect(()=>{ const a=audioRef.current; if(!wantPlay||!a||!audioSrc) return; let dead=false; a.volume=(settings.volume??80)/100; a.play().then(()=>{ if(!dead) setPlaying(true); }).catch(()=>{ if(!dead){ setPlaying(false); setWantPlay(false); setAudioError(true); } }); return ()=>{ dead=true; }; },[wantPlay,audioSrc]);
    useEffect(()=>{ const a=audioRef.current; if(a) a.volume=(settings.volume??80)/100; },[settings.volume]);
    useEffect(()=>()=>{ audioRef.current?.pause(); },[]);
    const [pending,setPendingState] = useState<number|null>(null);
    const pendingById = useRef<Record<number, number>>({});
    // S2-1: a pending (unconfirmed) choice is kept per question instead of being thrown away on every navigation.
    const setPending = (v:number|null) => {
        if (v===null) delete pendingById.current[index]; else pendingById.current[index]=v;
        setPendingState(v);
    };
    const sheet = useRef<HTMLDialogElement>(null);
    const mapSheet=useRef<HTMLDialogElement>(null);
    // Drawer "كل الأسئلة" item opens the question map (jump list). The signal
    // is reset after opening so remounting the quiz (e.g. hero resume) does
    // not pop the map open again on its own.
    useEffect(()=>{ if(mapSignal>0 && mapSheet.current && !mapSheet.current.open) { mapSheet.current.showModal(); onMapOpened(); } },[mapSignal]);
    // Round 5 (#9): one audio toggle shared by the tools-row chip and the big
    // button inside the explanation card.
    const toggleAudio = () => { const a=audioRef.current; if(!a) return; if(playing||wantPlay){a.pause();a.currentTime=0;audioReq.current++;setPlaying(false);setWantPlay(false);return;} const id=++audioReq.current; const ld=audioLoaders[index]; (audioSrc?Promise.resolve(audioSrc):ld().then(m=>m.default)).then(u=>{if(id!==audioReq.current)return;if(audioSrc!==u)setAudioSrc(u);setWantPlay(true);}).catch(()=>{if(id===audioReq.current){setWantPlay(false);setAudioError(true);}}); };
    const q = questions[index]; const chosen = picked[index]; const answered = chosen !== null;
    const feedback = submitted || mode === 'study';
    // Suggestion 31: exam name (+ year, which the exam names already carry)
    // as a small chip at the top of the quiz screen.
    const examName = state.run==='notebook'||state.run==='flagged' ? runLabel(state.run) : (banks.find(b=>b.id===(state.bank??state.setup.section))?.name ?? '');
    const [shareMsg,setShareMsg]=useState('');
    // S3-9 decision (Mohamed): in study mode the audio / explanation /
    // translation icons sit next to the question and work BEFORE answering;
    // in exam mode they stay after-answer only, as today.
    const preAnswer = mode==='study' && !submitted;
    // Round 5 (#20): swipe between questions — the touch
    // start is remembered here and resolved on touch end, below.
    const touchStart = useRef<{x:number;y:number}|null>(null);
    // Round 5 (#18): gentle haptic on select/confirm.
    const buzz = () => { if (settings.fx && !settings.calm) { try { navigator.vibrate?.(18); } catch { /* no vibrator */ } } };
    const pos = viewIds.indexOf(index);
    const progress = viewIds.length > 1 ? Math.max(0,pos)/(viewIds.length-1)*100 : 100;
    useEffect(()=>{setTrOpen(settings.translation);setHintOpen(settings.hintDefault);setExOpen(false);setPendingState(pendingById.current[index] ?? null);},[index,review]);
    useEffect(()=>{ pendingById.current={}; setPendingState(null); },[attemptKey]);
    useEffect(()=>{if(exOpen)sheet.current?.showModal();else sheet.current?.close();},[exOpen]);
    const commit = (v:number) => { pick(v);setPending(null);if(settings.autoExplain && mode==='study')setExOpen(true); };
    if(!viewIds.length) return <div className="qz-home"><h2>مفيش أسئلة هنا</h2><p>كل الإجابات صح، مفيش غلط للمراجعة.</p><button type="button" className="qz-btn primary" onClick={goHome}>ارجع للرئيسية</button></div>;
    return <>
        {focus&&<button type="button" className="qz-focus-exit" onClick={()=>setFocus(false)}>إنهاء التركيز</button>}
        <div className="qz-exam-chip"><span>{examName}</span></div>
        {(state.run==='notebook'||state.run==='flagged') && <p className="qz-review-banner">{runLabel(state.run)}</p>}
        {review!=='all' && <p className="qz-review-banner">مراجعة الغلط فقط · مراجعة من غير تغيير النتيجة <button type="button" className="qz-tool-btn" onClick={()=>setReview('all')}>كل أسئلة التجربة</button></p>}
        {settings.steps && <div className="qz-progress-row">
            <div className="qz-linear-progress" role="progressbar" aria-label="السؤال الحالي" aria-valuemin={1} aria-valuemax={viewIds.length} aria-valuenow={pos+1} aria-valuetext={`Question ${pos+1} of ${viewIds.length}`}>
                <div className="qz-progress-rail"><span className="qz-progress-fill" style={{width:`${progress}%`}}/><span className="qz-progress-marker" style={{right:`clamp(14px, ${progress}%, calc(100% - 14px))`}}>{toAr(String(pos+1))}</span></div>
            </div>
            <button type="button" className="qz-tool-btn qz-icon-btn qz-map-trigger" aria-label="خريطة الأسئلة" title="خريطة الأسئلة" onClick={()=>mapSheet.current?.showModal()}><ListIcon/></button>
        </div>}
        <dialog ref={mapSheet} className="qz-ex-dialog" dir="rtl"><CloseButton label="قفل الخريطة" onClick={()=>mapSheet.current?.close()}/><p className="qz-explain-title">خريطة الأسئلة</p><div className="qz-question-map">{viewIds.map((id,p)=><button type="button" className={'qz-dot'+(id===index?' is-current':'')+(feedback&&picked[id]!==null?(picked[id]===questions[id].c?' is-right':' is-missed'):'')} key={id} onClick={()=>{jump(id);mapSheet.current?.close();}} aria-label={'السؤال '+toAr(String(p+1))}>{toAr(String(p+1))}{picked[id]!==null?' •':''}</button>)}</div></dialog>
        <div className="qz-stage" key={index} onTouchStart={e=>{const t=e.changedTouches[0];touchStart.current={x:t.clientX,y:t.clientY};}} onTouchEnd={e=>{const s=touchStart.current;touchStart.current=null;if(!s)return;const t=e.changedTouches[0];const dx=t.clientX-s.x,dy=t.clientY-s.y;if(Math.abs(dx)>60&&Math.abs(dx)>Math.abs(dy)*1.5){if(dx<0){if(pos<viewIds.length-1)jump(viewIds[pos+1]);}else if(pos>0)jump(viewIds[pos-1]);}}}><div className="qz-question"><p className="qz-q-en" dir="ltr">{q.q}</p></div>
        <div className="qz-tools">
        <button type="button" className={'qz-tool-btn qz-icon-btn'+(trOpen?' is-open':'')} onClick={()=>setTrOpen(!trOpen)} aria-expanded={trOpen} aria-label="الترجمة" title="الترجمة"><span className="qz-icon-chip"><TranslateIcon/></span></button>

        <button type="button" className={'qz-tool-btn qz-icon-btn qz-flag-btn'+(state.favorites.includes(index)?' is-open':'')} onClick={()=>update({favorites:state.favorites.includes(index)?state.favorites.filter(i=>i!==index):[...state.favorites,index]})} aria-pressed={state.favorites.includes(index)} aria-label="راجعني" title="راجعني"><span className="qz-icon-chip"><FlagIcon filled={state.favorites.includes(index)}/></span></button>
        {audioFor(index)&&((feedback&&answered)||submitted||preAnswer)&&<><button type="button" className={'qz-tool-btn qz-icon-btn qz-audio-btn'+(playing?' is-open':'')} onClick={toggleAudio} aria-pressed={playing} aria-label={playing?'إيقاف الشرح الصوتي':(generatedAudio.has(index)?'اسمع الشرح (صوت مولّد)':'اسمع الشرح')} title={playing?'إيقاف الشرح الصوتي':(generatedAudio.has(index)?'اسمع الشرح (صوت مولّد، مش تسجيل الدكتور)':'اسمع الشرح')}><span className="qz-icon-chip"><span aria-hidden="true">{playing?<StopIcon/>:<SpeakerIcon/>}</span></span></button><span className={'qz-wave'+(playing?' is-playing':'')} aria-hidden="true"><i/><i/><i/><i/></span><audio ref={audioRef} key={index} src={audioSrc} preload="none" onEnded={()=>{setPlaying(false);setWantPlay(false);}} onError={()=>setAudioError(true)}/></>}
        {feedback && (answered||submitted||preAnswer) && <button type="button" className="qz-tool-btn" onClick={()=>setExOpen(true)}>الشرح</button>}
        {feedback && (answered||submitted||preAnswer) && <button type="button" className="qz-tool-btn" onClick={()=>{shareQuestionCard(q,examName).then(r=>setShareMsg(r==='failed'?'تعذّر تجهيز صورة السؤال.':''));}}>مشاركة</button>}
        {mode==='study'&&!submitted&&!answered&&settings.hints&&<button type="button" className={'qz-tool-btn qz-icon-btn'+(hintOpen?' is-open':'')} onClick={()=>setHintOpen(!hintOpen)} aria-expanded={hintOpen} aria-label="التلميح" title="التلميح"><span className="qz-icon-chip"><span aria-hidden="true"><BulbIcon/></span></span></button>}
        <button type="button" className="qz-tool-btn" onClick={()=>setFocus(true)}>تركيز</button>
        </div>
        {shareMsg&&<p className="qz-hint" role="status">{shareMsg}</p>}
        {audioError&&<p className="qz-hint" role="status">تعذّر تشغيل الصوت، جرّب تاني.</p>}
        {trOpen&&<p className="qz-q-ar" dir="rtl">{q.tr}</p>}
        {mode==='study'&&!submitted&&!answered&&settings.hints&&hintOpen&&<p className="qz-hint" dir="rtl">{q.hint}</p>}
        <div className="qz-options" role="listbox" aria-label="الاختيارات">{q.o.map((opt,i)=>{
            const correct=i===q.c;const selected=i===chosen;
            const locked=submitted||expired||(mode==='study'&&answered&&!(settings.retry&&chosen!==q.c));
            let cls='qz-opt';if(feedback&&(answered||submitted)&&correct)cls+=' is-correct';if(feedback&&selected&&!correct)cls+=' is-wrong';if((pending===i)||(!feedback&&selected))cls+=' is-pending';
            return <button type="button" key={i} role="option" className={cls} dir="ltr" disabled={locked} aria-selected={selected||pending===i} onClick={()=>{if(settings.confirm){buzz();setPending(i);}else commit(i);}} onDoubleClick={()=>{if(settings.confirm&&pending===i)commit(i);}}>{settings.letters&&<span className="qz-letter" dir="ltr">{letters[i]}</span>}<span className="qz-opt-text" dir="ltr">{opt}</span>{feedback&&(answered||submitted)&&correct&&<span className="qz-mark">✓</span>}{feedback&&selected&&!correct&&<span className="qz-mark">✕</span>}</button>;
        })}</div></div>
        {expired&&!submitted&&<p className="qz-hint" role="status">الوقت خلص. الإجابات اتقفلت، سلّم التجربة عشان تشوف النتيجة، أو صفّر العداد من الإعدادات.</p>}
        {settings.confirm&&pending!==null&&!expired&&!submitted&&<button type="button" className={'qz-btn primary qz-confirm qz-gold'} onClick={()=>commit(pending)}>تأكيد الإجابة</button>}
        {/* One-page layout: Previous/Next/list moved to side rails on the screen edges — the bottom nav is gone, so the question + options fit on one page. */}
        <button type="button" className="qz-btn primary qz-home-cta" onClick={submitted?goHome:finish}>{submitted?'الرجوع للرئيسية':'إنهاء وعرض النتيجة'}</button>
        <dialog ref={sheet} className={'qz-ex-dialog qz-ex-gold'} onCancel={()=>setExOpen(false)} onClose={()=>setExOpen(false)} dir="rtl"><CloseButton label="قفل الشرح" onClick={()=>setExOpen(false)}/><p className="qz-explain-title">الشرح</p><p className="qz-explain-body" dir="rtl">{q.ex}</p><p className="qz-why" dir="rtl">{q.why}</p>{generatedAudio.has(index)&&<p className="qz-src" dir="rtl">الشرح الصوتي للسؤال ده مولّد، مش تسجيل الدكتور.</p>}{q.url&&<p className="qz-src" dir="rtl">المصدر: <TextLink href={q.url}>{q.src||q.url}</TextLink></p>}{audioFor(index)&&((feedback&&answered)||submitted||preAnswer)&&<button type="button" className="qz-btn qz-ex-audio" onClick={toggleAudio}>{playing?'■  إيقاف الشرح':'▶  استمع للشرح'}{playing&&<span className="qz-wave is-playing" aria-hidden="true"><i/><i/><i/><i/></span>}</button>}</dialog>
    </>;
}

function HomeScreen({state,update,open,finish,storage,celebrate,wrongSet,history,startRun,onCard}: {state:Saved;update:(v:Partial<Saved>)=>void;open:(filter:string)=>void;finish:()=>void;storage:string;celebrate:boolean;wrongSet:Set<number>;history:Attempt[];startRun:(r:Run)=>void;onCard:(b:typeof banks[number])=>void}) {
 const {picked,ids,submitted,started,setup,elapsed} = state;
 const right=ids.filter(id=>picked[id]===questions[id].c).length;const wrong=ids.filter(id=>picked[id]!==null&&picked[id]!==questions[id].c).length;const blank=ids.filter(id=>picked[id]===null).length;
 const bank=banks.find(b=>b.id===setup.section)||banks[0];
 const [bankQuery,setBankQuery]=useState('');
 const filtered=bankQuery?banks.filter(b=>b.name.includes(bankQuery)):banks;
 const confetti=React.useMemo(()=>Array.from({length:12},(_,i)=>({left:(i*37+13)%100,delay:(i%9)*0.09,duration:1.4+(i%5)*0.2,color:['#0b4f4a','#e4f5a4','#7fd08a','#ffffff','#c9ef6b'][i%5],size:6+(i%3)*3})),[]);
 const flagIds=[...state.favorites].sort((a,b)=>a-b);
 const wrongIds=[...wrongSet].sort((a,b)=>a-b);
 // Round 4: per-exam status for the cards (mirrors Android examCard).
 const isLive=(b:typeof banks[number])=>started&&!submitted&&state.run==='all'&&(state.bank??setup.section)===b.id;
 const cardStatus=(b:typeof banks[number])=>{
   if(isLive(b)){const answered=ids.filter(id=>picked[id]!==null).length;return `جارٍ — سؤال ${toAr(String(answered+1))} من ${toAr(String(ids.length))}`;}
   const last=[...history].reverse().find(h=>h.b===b.id);
   if(last)return `آخر نتيجة ${toAr(String(last.n?Math.round(last.r/last.n*100):0))}٪`;
   return 'لم يبدأ';
 };
 const lastFor=(b:typeof banks[number])=>{const l=[...history].reverse().find(h=>h.b===b.id);return l?Math.round(l.n?l.r/l.n*100:0):-1;};
 const pct = ids.length?Math.round(right/ids.length*100):0;
 const sessionMap = <details className="qz-session-map"><summary>خريطة الأسئلة</summary><div className="qz-home-cards">{ids.map(id=><button type="button" className="qz-home-card" key={id} onClick={()=>{open('all');update({index:id});}}><span className="qz-home-card-num">سؤال {toAr(String(ids.indexOf(id)+1))}</span><span className="qz-home-card-state">{picked[id]===null?'لسه':state.mode==='exam'?'تم الاختيار':picked[id]===questions[id].c?'صح ✓':'غلط ✕'}</span></button>)}</div></details>;
 // Round 5 (#15): long-press (right-click / mobile long-press) details popup.
 const [detailsBank,setDetailsBank]=useState<typeof banks[number]|null>(null);
 const detailsRef=useRef<HTMLDialogElement>(null);
 useEffect(()=>{ if(detailsBank) detailsRef.current?.showModal(); else detailsRef.current?.close(); },[detailsBank]);
 // Suggestion 32: the gold score ring fills gradually up to the score.
 const [ringOn,setRingOn]=useState(false);
 useEffect(()=>{ if(!submitted){setRingOn(false);return;} const t=window.setTimeout(()=>setRingOn(true),60); return ()=>window.clearTimeout(t); },[submitted,pct]);
 const cardFor=(b:typeof banks[number])=>{const live=isLive(b);const answered=live?ids.filter(id=>picked[id]!==null).length:0;
   return <button type="button" key={b.id} role="option" aria-selected={setup.section===b.id} className={'qz-exam-card'+(live?' is-live':'')+(setup.section===b.id&&!live?' is-on':'')} onClick={()=>onCard(b)} onContextMenu={e=>{e.preventDefault();setDetailsBank(b);}}><span className="qz-exam-card-top"><span className="qz-exam-card-name">{b.name}</span><span className={'qz-exam-badge qz-badge-'+examType(b.name)}>{examTypeLabel(examType(b.name))}</span></span>{live&&<span className="qz-exam-bar" aria-hidden="true"><span style={{width:`${ids.length?Math.round(answered/ids.length*100):0}%`}}/></span>}<span className="qz-exam-card-meta">{toAr(String(b.ids.length))} سؤال · {toAr(String(b.minutes))} د</span><span className="qz-exam-card-status">{cardStatus(b)}</span></button>;};
 // Round 5 (#16): exams grouped by year (newest first) in the new design.
 const yearGroups: [number, typeof banks][] = (()=>{ const m = new Map<number, typeof banks>(); filtered.forEach(b=>{ const y=examYear(b.name); if(!m.has(y)) m.set(y,[]); m.get(y)!.push(b); }); return [...m.entries()].sort((a,b)=>b[0]-a[0]); })();
 return <div className="qz-home">
 {celebrate&&state.settings.fx&&!state.settings.calm&&<div className="qz-confetti" aria-hidden="true">{confetti.map((p,i)=><i key={i} style={{left:`${p.left}%`,width:p.size,height:p.size*1.4,background:p.color,animationDelay:`${p.delay}s`,animationDuration:`${p.duration}s`}}/>)}</div>}
 <p className={"qz-home-title"+(!submitted&&!started?" qz-home-quiz":"")}>{submitted?'نتيجة التجربة':started?'كمّل من مكانك':bank.name}</p>
 <p className="qz-save-status" role="status">{storage}</p>{started&&submitted&&<p className="qz-set-detail">{state.run==='notebook'||state.run==='flagged'?runLabel(state.run):(banks.find(b=>b.ids.includes(ids[0]))||bank).name}</p>}
 {submitted?<><div className="qz-score-ring-wrap"><div className="qz-score-ring"><svg viewBox="0 0 120 120" aria-hidden="true"><circle className="qz-ring-rail" cx="60" cy="60" r="52"/><circle className="qz-ring-arc" cx="60" cy="60" r="52" strokeDasharray={2*Math.PI*52} strokeDashoffset={(state.settings.fx&&!state.settings.calm)?(ringOn?2*Math.PI*52*(1-pct/100):2*Math.PI*52):2*Math.PI*52*(1-pct/100)}/></svg><span className="qz-ring-pct"><bdi>{toAr(String(pct))}٪</bdi></span></div><span className="qz-ring-sub">{toAr(String(right))} صح من {toAr(String(ids.length))}</span></div><div className="qz-result-grid">{[[right,'صح'],[wrong,'غلط'],[blank,'بدون إجابة']].map(([n,l])=><div key={l}><b>{toAr(String(n))}</b><span>{l}</span></div>)}</div><p className="qz-home-time"><ClockIcon/> وقت الحل: <bdi>{formatTime(state.resultElapsed??elapsed)}</bdi></p><p className="qz-set-detail">الأسئلة المتروكة محسوبة في الدرجة.</p><div className="qz-action-stack"><button type="button" className="qz-btn primary" onClick={()=>document.getElementById('qz-exam-list')?.scrollIntoView({behavior:'smooth'})}>الرجوع للرئيسية</button><button type="button" className="qz-btn primary" onClick={()=>open('all')}>مراجعة كل الإجابات</button><button type="button" className="qz-btn ghost" disabled={wrong===0} onClick={()=>open('wrong')}>مراجعة الغلط فقط ({toAr(String(wrong))})</button></div></>:started?(<><div className="qz-hero-card"><p className="qz-hero-name">{state.run==='notebook'||state.run==='flagged'?runLabel(state.run):(banks.find(b=>b.ids.includes(ids[0]))||bank).name}</p><p className="qz-hero-sub">سؤال {toAr(String(Math.min(ids.length-blank+1,ids.length)))} من {toAr(String(ids.length))} · {state.mode==='exam'?'امتحان':'مذاكرة'}</p><div className="qz-hero-bar" role="progressbar" aria-valuenow={ids.length-blank} aria-valuemin={0} aria-valuemax={ids.length} aria-label="التقدم في التجربة"><span style={{width:`${ids.length?Math.round((ids.length-blank)/ids.length*100):0}%`}}/></div><p className="qz-home-time"><ClockIcon/><bdi>{formatTime(elapsed)}</bdi></p><div className="qz-action-stack"><button type="button" className="qz-btn qz-gold" onClick={()=>open('all')}>كمّل</button><button type="button" className="qz-btn ghost" onClick={finish}>إنهاء وعرض النتيجة</button></div></div>{sessionMap}</>):null}
 {/* Round 4: home is cards only — no setup <details>, no instruction panels. */}
 <div className="qz-setup" id="qz-exam-list">
 <span className="qz-set-label">طريقة التجربة</span><div className="qz-set-seg">{[['study','مذاكرة'],['exam','امتحان']].map(([v,l])=><button type="button" className={'qz-set-seg-btn'+(setup.mode===v?' is-on':'')} key={v} aria-pressed={setup.mode===v} onClick={()=>update({setup:{...setup,mode:v as Mode}})}>{l}</button>)}</div>
 {(wrongSet.size>0||flagIds.length>0)&&<div className="qz-learn-row">{wrongSet.size>0&&<button type="button" className="qz-btn primary" onClick={()=>startRun({kind:'notebook',ids:wrongIds})}>دفتر الغلطات ({toAr(String(wrongIds.length))})</button>}{flagIds.length>0&&<button type="button" className="qz-btn primary" onClick={()=>startRun({kind:'flagged',ids:flagIds})}>راجعني ({toAr(String(flagIds.length))})</button>}</div>}
 <label className="qz-set-label" htmlFor="bank-search">الاختبار أو البنك</label><input id="bank-search" className="qz-bank-input qz-bank-search" type="search" placeholder="ابحث عن بنك…" aria-label="ابحث عن بنك" value={bankQuery} onChange={e=>setBankQuery(e.target.value)}/>
 <div className="qz-bank-list" role="listbox" aria-label="الاختبار أو البنك">{yearGroups.map(([y,gb])=>{const totalQ=gb.reduce((a,b)=>a+b.ids.length,0);return <div key={y} className="qz-bank-group"><p className="qz-bank-group-label qz-year-label">{y===0?'الاختبارات':toAr(String(y))} · {toAr(String(totalQ))} سؤال</p><div className="qz-exam-grid">{gb.map(cardFor)}</div></div>;})}{filtered.length===0&&<p className="qz-set-detail">مفيش نتائج للبحث.</p>}</div>
 {detailsBank&&<dialog ref={detailsRef} className="qz-ex-dialog" dir="rtl" onClose={()=>setDetailsBank(null)} onCancel={()=>setDetailsBank(null)}><CloseButton label="قفل" onClick={()=>setDetailsBank(null)}/><p className="qz-explain-title">{detailsBank.name}</p><p className="qz-explain-body" dir="rtl">{toAr(String(detailsBank.ids.length))} سؤال · {toAr(String(detailsBank.minutes))} د{lastFor(detailsBank)>=0?` · آخر نتيجة ${toAr(String(lastFor(detailsBank)))}٪`:' · لم يبدأ بعد'}</p><div className="qz-set-row"><div><span className="qz-set-label" id="set-shuffle">خلط ترتيب الأسئلة</span></div><button type="button" className={'qz-switch'+((state.settings.shuffleBanks??[]).includes(detailsBank.id)?' is-on':'')} role="switch" aria-labelledby="set-shuffle" aria-checked={(state.settings.shuffleBanks??[]).includes(detailsBank.id)} onClick={()=>{const cur=state.settings.shuffleBanks??[];const on=cur.includes(detailsBank.id);update({settings:{...state.settings,shuffleBanks:on?cur.filter(x=>x!==detailsBank.id):[...cur,detailsBank.id]}});}}><span className="qz-switch-knob"/></button></div><div className="qz-set-actions"><button type="button" className="qz-btn primary" onClick={()=>setDetailsBank(null)}>تمام</button></div></dialog>}
 </div>
 </div>;
}


function BackupPanel({make,restore}:{make:()=>Promise<string>;restore:(code:string)=>Promise<string>}) {
    const [code,setCode] = useState('');
    const [input,setInput] = useState('');
    const [msg,setMsg] = useState('');
    const [confirm,setConfirm] = useState(false);
    const [busy,setBusy] = useState(false);
    return <details className="qz-set-group"><summary>نسخة احتياطية <span>تصدير واسترجاع</span></summary>
        <div className="qz-set-row qz-stacked">
            <span className="qz-set-label">تصدير</span>
            <p className="qz-set-detail">بيطلع كود فيه إجاباتك ودفتر الغلطات والعلامات والنتايج والإعدادات. انسخه واحفظه في مكان، وارجع الصقه في أي متصفح.</p>
            <button type="button" className="qz-btn primary" disabled={busy} onClick={async()=>{setBusy(true);try{setCode(await make());setMsg('');}catch{setMsg('معرفتش أجهّز النسخة.');}setBusy(false);}}>جهّز النسخة</button>
            {code&&<><textarea className="qz-code" readOnly dir="ltr" rows={4} value={code} onFocus={e=>e.currentTarget.select()} aria-label="كود النسخة الاحتياطية"/><div className="qz-set-actions"><CopyButton text={code} label="انسخ الكود" compact/></div></>}
        </div>
        {legacyExportText()&&<div className="qz-set-row qz-stacked">
            <span className="qz-set-label">البيانات القديمة (٣١٢٢ سؤال)</span>
            <p className="qz-set-detail">تقدمك ودفتر الغلطات من النسخة القديمة محفوظين زي ما كانوا ومش متحوّلين. انسخ النص ده واحفظه.</p>
            <textarea className="qz-code" readOnly dir="ltr" rows={4} value={legacyExportText()} onFocus={e=>e.currentTarget.select()} aria-label="البيانات القديمة الأصلية"/>
            <div className="qz-set-actions"><CopyButton text={legacyExportText()} label="انسخ البيانات القديمة"/></div>
        </div>}
        <div className="qz-set-row qz-stacked">
            <span className="qz-set-label">استرجاع</span>
            <textarea className="qz-code" dir="ltr" rows={4} value={input} onChange={e=>{setInput(e.target.value);setConfirm(false);setMsg('');}} placeholder="الصق الكود هنا" aria-label="الصق كود النسخة"/>
            {!confirm?<button type="button" className="qz-btn ghost" disabled={!input.trim()||busy} onClick={()=>setConfirm(true)}>استرجاع</button>:<div className="qz-hint"><p>ده هيستبدل التقدم الحالي على الجهاز ده بالنسخة اللي لصقتها.</p><div className="qz-set-actions"><button type="button" className="qz-btn primary" disabled={busy} onClick={async()=>{setBusy(true);const r=await restore(input);setMsg(r);setConfirm(false);setBusy(false);}}>استبدل</button><button type="button" className="qz-btn ghost" onClick={()=>setConfirm(false)}>رجوع</button></div></div>}
            {msg&&<p className="qz-set-detail" role="status">{msg}</p>}
        </div>
    </details>;
}

function SettingsScreen({ settings, setSettings, onReset, paused, setPaused, resetClock, backup }: {
    backup:{make:()=>Promise<string>;restore:(code:string)=>Promise<string>}; settings: Settings; setSettings: (s: Settings) => void; onReset: () => void; paused: boolean; setPaused: (v: boolean) => void; resetClock: () => void;
}) {
    const [confirmReset, setConfirmReset] = useState(false);
    const toggle = (key: keyof Settings, label: string, detail: string) => <div className="qz-set-row" key={key}>
        <div><span className="qz-set-label" id={'set-'+key}>{label}</span><p className="qz-set-detail">{detail}</p></div>
        <button type="button" className={'qz-switch'+(settings[key] ? ' is-on' : '')} role="switch" aria-labelledby={'set-'+key} aria-checked={Boolean(settings[key])} onClick={() => setSettings({...settings, [key]: !settings[key]})}><span className="qz-switch-knob" /></button>
    </div>;
    return <div className="qz-settings">
        <h2 className="qz-settings-title">على مزاجك</h2><p className="qz-set-intro">تحكم في تجربتك. التغييرات بتتطبق فورًا.</p>
        <details className="qz-set-group" open><summary>العرض والقراءة <span>٧ اختيارات</span></summary>
            {toggle('dark','الوضع الليلي','خلفيات غامقة مع نفس الأخضر والليموني.')}
            <div className="qz-set-row qz-stacked"><label className="qz-set-label" htmlFor="font-scale">حجم الخط · <bdi>{fontPercent(settings)}%</bdi></label><input id="font-scale" className="qz-font-slider" type="range" min="70" max="125" step="1" value={fontPercent(settings)} aria-label="حجم الخط" aria-valuetext={`${fontPercent(settings)}%`} onChange={e=>setSettings({...settings,fontScale:Number(e.target.value)})}/><div className="qz-scale-labels"><span>صغير جدًا</span><span>كبير</span></div><div className="qz-font-preview"><p className="qz-q-en" dir="ltr">The nurse is assessing a child. Which symptom is most common?</p><p className="qz-opt-text" dir="ltr">A · Ear pain</p><p className="qz-q-ar" dir="rtl">الممرض بيفحص طفل. إيه أكتر عرض شائع؟</p><p className="qz-explain-body" dir="rtl">ده شكل الشرح بنفس حجم الخط اللي اخترته.</p></div></div>
            {toggle('spacious','مسافات أوسع','مساحة أكبر بين الاختيارات للقراءة واللمس.')}
            {toggle('contrast','تباين أعلى','خلفيات أوضح وحدود أقوى للاختيارات.')}
            {toggle('calm','تقليل الحركة','من غير حركات انتقال أو نبضات.')}
            {toggle('steps','شريط التقدم','إظهار رقم السؤال على الخط وزر خريطة الأسئلة.')}
            {toggle('letters','حروف الاختيارات','إظهار A وB وباقي الحروف جنب الاختيارات.')}
        </details>
        <details className="qz-set-group"><summary>طريقة الحل <span>٣ اختيارات</span></summary>
            {toggle('confirm','تأكيد الاختيار','اختار إجابة، وبعدها اضغط تأكيد الإجابة. في الامتحان التصحيح بيتأجل للنهاية.')}
            {toggle('autoNext','انتقال تلقائي','بعد الإجابة بثانيتين، افتح السؤال اللي بعده. يتوقف مع الشرح التلقائي.')}
            {toggle('retry','إعادة محاولة الغلط','يسمح بتغيير الإجابة الغلط. النتيجة بتتحدّث في وضع المذاكرة فقط.')}
        </details>
        <details className="qz-set-group"><summary>مساعدات المذاكرة <span>٤ اختيارات</span></summary>
            {toggle('hints','إتاحة التلميح','إظهار أو إخفاء زر التلميح قبل الإجابة في وضع المذاكرة.')}
            {toggle('hintDefault','تلميح مع كل سؤال','يفتح تلقائيًا عند دخول السؤال لو التلميح متاح.')}
            {toggle('translation','ترجمة مع كل سؤال','تظهر عند دخول السؤال. تقدر تقفلها من زر الترجمة.')}
            {toggle('autoExplain','شرح بعد الإجابة','في المذاكرة فقط: يفتح شرح الإجابة تلقائيًا، من غير انتقال تلقائي.')}
        </details>
        <details className="qz-set-group"><summary>الصوت والمؤثرات <span>٣ اختيارات</span></summary>
            {toggle('sounds','المؤثرات الصوتية','صوت للإجابة الصح وصوت للإجابة الغلط في المذاكرة فقط. في الامتحان مفيش أي صوت، عشان ما يكشفش الإجابة.')}
            {toggle('fx','حركة الاحتفال والاختيار','نبضة خفيفة عند اختيار إجابة واحتفال صغير مع النتيجة العالية. بيتعطل تلقائيًا مع تقليل الحركة.')}
            <div className="qz-set-row qz-stacked"><label className="qz-set-label" htmlFor="vol-level">مستوى الصوت · <bdi>{settings.volume??80}%</bdi></label><input id="vol-level" className="qz-font-slider" type="range" min="0" max="100" step="5" value={settings.volume??80} aria-label="مستوى الصوت" aria-valuetext={`${settings.volume??80}%`} onChange={e=>{const v=Number(e.target.value);setMasterVolume(v);setSettings({...settings,volume:v});}} onPointerUp={()=>{if(settings.sounds)play('correct');}} onKeyUp={()=>{if(settings.sounds)play('correct');}}/><div className="qz-scale-labels"><span>هادي</span><span>عالي</span></div><div className="qz-set-actions"><button type="button" className="qz-btn ghost" disabled={!settings.sounds} onClick={()=>play('correct')}>جرّب الصوت</button></div></div>
        </details>
        <details className="qz-set-group"><summary>الوقت <span>تحكم كامل</span></summary>
            {toggle('timer','إظهار العداد','إخفاؤه مش بيوقف حساب الوقت.')}
            {toggle('quizOnly','وقت الحل بس','العداد يقف في الرئيسية والإعدادات.')}
            {toggle('countdown','عد تنازلي','بدل الوقت المنقضي. عند الصفر يقفل الإجابات وتفضل المراجعة متاحة.')}
            <div className="qz-set-row qz-stacked"><span className="qz-set-label">مدة التجربة</span><div className="qz-set-seg" role="group" aria-label="مدة التجربة">{[5,30,60,120,180].map(v => <button type="button" className={'qz-set-seg-btn'+(settings.minutes===v?' is-on':'')} key={v} aria-pressed={settings.minutes===v} onClick={() => setSettings({...settings,minutes:v})}>{toAr(String(v))} د</button>)}</div><p className="qz-set-detail">المدة محسوبة من بداية العداد. صفّره لو عايز تبدأ مدة كاملة.</p></div>
            <div className="qz-set-actions"><button type="button" className="qz-btn ghost" onClick={() => setPaused(!paused)}>{paused?'تشغيل العداد':'إيقاف مؤقت'}</button><button type="button" className="qz-btn ghost" onClick={resetClock}>تصفير العداد</button></div>
        </details>
        <BackupPanel make={backup.make} restore={backup.restore}/>
        <details className="qz-set-group"><summary>إعادة الضبط</summary>
            <button type="button" className="qz-btn ghost qz-reset" onClick={() => setSettings({...defaults})}>استعادة الإعدادات الافتراضية</button>
            {!confirmReset ? <button type="button" className="qz-btn primary qz-reset" onClick={() => setConfirmReset(true)}>إعادة التجربة من الأول</button> : <div className="qz-hint"><p>هتمسح إجابات التجربة وتصفر الوقت. الإعدادات هتفضل زي ما هي.</p><div className="qz-set-actions"><button type="button" className="qz-btn primary" onClick={onReset}>امسح وابدأ</button><button type="button" className="qz-btn ghost" onClick={() => setConfirmReset(false)}>رجوع</button></div></div>}
        </details>
    </div>;
}

function MockApp() {
    const [stored,setStored,saved] = useLocalState<Stored>('quiz-banks.v2',encode({...initial,settings:{...defaults,minutes:banks[0].minutes}}));
    const [extra,setExtra,savedX] = useLocalState<Extra>('qh-extra.v1',emptyExtra);
    const ready = saved.ready && savedX.ready;
    const wrongSet = React.useMemo(()=>unpackBits(extra.w),[extra.w]);
    const history = Array.isArray(extra.h)?extra.h:[];
    const state=decode(stored);
    const setState=(v:Saved|((prev:Saved)=>Saved))=>setStored(prev=>encode(typeof v==='function'?v(decode(prev)):v));
    const update = (v:Partial<Saved>) => { if(ready)setState(prev=>({...prev,...v})); };
    const {tab,index,picked,elapsed,settings,paused,started,submitted,ids} = state;
    const flagIds=[...state.favorites].sort((a,b)=>a-b);
    // Side drawer items (data-driven, so more can be added later). "الرجوع
    // للقائمة" only shows while an exam is live, like the old button did.
    const drawerItems: {id:string;label:string;badge?:string;disabled?:boolean;action:()=>void}[] = [
        { id:'home', label:'الرئيسية', action:()=>goHome() },
        { id:'search', label:'بحث', action:()=>{ goHome(); setTimeout(()=>{ const el=document.getElementById('bank-search'); el?.scrollIntoView({behavior:'smooth',block:'center'}); el?.focus(); }, 80); } },
        ...(tab==='quiz'&&started ? [{ id:'map', label:'كل الأسئلة', action:()=>setMapSignal(s=>s+1) }] : []),
        { id:'flagged', label:'المذكرة', badge: flagIds.length?toAr(String(flagIds.length)):undefined, disabled: flagIds.length===0, action:()=>{ const r={kind:'flagged' as const,ids:flagIds}; if(started){setPendingRun(r);setConfirmAction('start');} else begin(r); } },
        { id:'settings', label:'الإعدادات', action:()=>update({tab:'settings'}) },
        ...(started&&!submitted ? [{ id:'list', label:'الرجوع للقائمة', action:()=>goHome() }] : []),
    ];
    const [opened,setOpened] = useState(true);
    const [review,setReview] = useState('all');
    const [confirmAction,setConfirmAction] = useState<'finish'|'start'|null>(null);
    const [pendingRun,setPendingRun] = useState<Run|null>(null);
    const [autoPending,setAutoPending] = useState(false);
    // Suggestion 34: focus mode — hides the timer, badges and chrome on the
    // quiz screen, leaving only the question + options. Transient (not saved).
    const [focus,setFocus]=useState(false);
    // Side navigation drawer (replaces the old header "› القائمة" button).
    const [drawerOpen,setDrawerOpen]=useState(false);
    // Signal for the drawer "كل الأسئلة" item to open the question map.
    const [mapSignal,setMapSignal]=useState(0);
    const [celebrate,setCelebrate] = useState(false);
    const [burst,setBurst] = useState(0);
    const burstPieces=React.useMemo(()=>Array.from({length:12},(_,i)=>({left:(i*37+13)%100,delay:(i%9)*0.04,duration:1.0+(i%5)*0.15,color:['#0b4f4a','#e4f5a4','#7fd08a','#ffffff','#c9ef6b'][i%5],size:6+(i%3)*3})),[]);
    const panel=useRef<HTMLDialogElement>(null);const scroller=useRef<HTMLDivElement>(null);
    const [attemptKey,setAttemptKey]=useState(0);
    // S2-5: the clock ticks every second on screen, but localStorage is written at most every 10s (pause/answer/submit flush it).
    const elapsedRef=useRef(elapsed);
    const [liveElapsed,setLiveElapsed]=useState(elapsed);
    useEffect(()=>{ elapsedRef.current=elapsed; setLiveElapsed(elapsed); },[elapsed]);
    const remaining=Math.max(0,settings.minutes*60-liveElapsed);const expired=settings.countdown&&remaining===0;
    const allAnswered=ids.every(id=>picked[id]!==null);
    const viewIds=review==='wrong'?ids.filter(id=>picked[id]!==null&&picked[id]!==questions[id].c):ids;
    const safeIndex=viewIds.includes(index)?index:(viewIds[0]??index);
    useEffect(()=>{setMasterVolume(settings.volume??80);},[settings.volume]);
    useEffect(()=>{if(opened)panel.current?.showModal();else panel.current?.close();},[opened]);
    useEffect(()=>{scroller.current?.scrollTo({top:0});setAutoPending(false);},[tab,index,review,confirmAction]);
    const ticking=ready&&started&&!submitted&&!(allAnswered&&state.mode==='study')&&!paused&&!expired&&opened&&!(settings.quizOnly&&tab!=='quiz');
    useEffect(()=>{
        if(!ticking)return;
        const t=window.setInterval(()=>{
            const next=elapsedRef.current+1;
            elapsedRef.current=next;
            setLiveElapsed(next);
            if(next%10===0)setState(prev=>({...prev,elapsed:next}));
        },1000);
        return()=>window.clearInterval(t);
    },[ticking]);
    const addWrong=(add:number[],drop:number[])=>{ if(!ready||(!add.length&&!drop.length))return; setExtra(prev=>{const set=unpackBits(prev.w);add.forEach(i=>set.add(i));drop.forEach(i=>set.delete(i));return {...prev,w:packBits(set),h:Array.isArray(prev.h)?prev.h:[]};}); };
    const pick=(v:number)=>{
        if(expired||submitted||!started||(state.mode==='study'&&picked[index]!==null&&!(settings.retry&&picked[index]!==questions[index].c)))return;
        update({picked:picked.map((p,id)=>id===index?v:p)});setAutoPending(true);
        // Round 5 (#18): gentle haptic on confirming an answer.
        if(settings.fx&&!settings.calm){try{navigator.vibrate?.(18);}catch{/* no vibrator */}}
        if(state.mode==='study'){ if(v===questions[index].c){ if(state.run==='notebook')addWrong([],[index]); } else addWrong([index],[]); }
        if(state.mode==='study'&&v===questions[index].c&&settings.fx&&!settings.calm)setBurst(b=>b+1);
        // Answer sounds: the correct/wrong blip plays whenever an answer is
        // committed, in BOTH modes (it used to be study-only). Vibration
        // mirrors it — short buzz for correct, double-buzz for wrong — in
        // both themes, gated on the fx/calm haptics setting. Both are
        // fire-and-forget and never delay the explanation card.
        if(settings.sounds)play(v===questions[index].c?'correct':'wrong');
        if(settings.fx&&!settings.calm){try{navigator.vibrate?.(v===questions[index].c?60:[60,80,60]);}catch{/* no vibrator */}}
    };
    useEffect(()=>{
        const pos=ids.indexOf(index);
        if(!autoPending||!settings.autoNext||(state.mode==='study'&&settings.autoExplain)||submitted||tab!=='quiz'||picked[index]===null||pos>=ids.length-1)return;
        const t=window.setTimeout(()=>{setAutoPending(false);update({index:ids[pos+1]});},2000);return()=>window.clearTimeout(t);
    },[picked,index,tab,autoPending,settings.autoNext,settings.autoExplain,submitted]);
    const doSubmit=()=>{
        const right=ids.filter(id=>picked[id]===questions[id].c).length;
        const pct=ids.length?right/ids.length:0;
        const high=pct>=0.8;
        setCelebrate(high);
        if(ready){
            const wr=ids.filter(id=>picked[id]!==null&&picked[id]!==questions[id].c);
            const ok=state.run==='notebook'?ids.filter(id=>picked[id]===questions[id].c):[];
            const entry:Attempt={t:Date.now(),b:state.run==='notebook'||state.run==='flagged'?state.run:(state.bank??state.setup.section),m:state.mode,r:right,n:ids.length,s:liveElapsed};
            setExtra(prev=>{const set=unpackBits(prev.w);wr.forEach(i=>set.add(i));ok.forEach(i=>set.delete(i));const h=[...(Array.isArray(prev.h)?prev.h:[]),entry].slice(-HISTORY_MAX);return {w:packBits(set),h};});
        }
        update({submitted:true,resultElapsed:liveElapsed,elapsed:liveElapsed,tab:'home'});setReview('all');
    };
    const begin=(run?:Run,section?:string)=>{const setup=state.setup;const sec=section??setup.section;let nextIds=run?run.ids:(banks.find(b=>b.id===sec)||banks[0]).ids;if(!nextIds.length)return;
    // Suggestion 36: shuffle the order for a fresh exam when the bank has
    // shuffle on. The shuffled order is saved with the run (ids), so resume,
    // the progress bar and the result all follow it. Notebook/flagged runs
    // keep their own order.
    if(!run&&(settings.shuffleBanks??[]).includes(sec)){nextIds=[...nextIds];for(let i=nextIds.length-1;i>0;i--){const j=Math.floor(Math.random()*(i+1));const t=nextIds[i];nextIds[i]=nextIds[j];nextIds[j]=t;}}
    setReview('all');setCelebrate(false);setAttemptKey(k=>k+1);update({run:run?run.kind:'all',bank:run?run.kind:sec,ids:nextIds,picked:questions.map(()=>null),index:nextIds[0],elapsed:0,paused:false,started:true,submitted:false,resultElapsed:null,mode:setup.mode,tab:'quiz'});setConfirmAction(null);setFocus(false);};
    // Round 4: tapping an exam card starts that exam in the chosen mode — or
    // resumes it when it is the live session (mirrors Android onExamCard).
    const onCard=(b:typeof banks[number])=>{
        if(started&&!submitted&&state.run==='all'&&(state.bank??state.setup.section)===b.id){open('all');return;}
        update({setup:{section:b.id,mode:state.setup.mode},settings:{...state.settings,minutes:b.minutes}});
        if(started){setPendingRun(null);setConfirmAction('start');}
        else begin(undefined,b.id);
    };
    // Phase 1: always ask before finishing — even with no blank questions — so a stray tap cannot end the attempt.
    const finish=()=>{setConfirmAction('finish');};
    const reset=()=>{setReview('all');setCelebrate(false);setAttemptKey(k=>k+1);update({started:false,submitted:false,resultElapsed:null,picked:questions.map(()=>null),elapsed:0,index:0,paused:false,tab:'home'});};
    const warnFired=useRef<{m5:boolean;m1:boolean}>({m5:false,m1:false});
    useEffect(()=>{if(elapsed===0)warnFired.current={m5:false,m1:false};},[elapsed]);
    useEffect(()=>{
        if(!settings.sounds||!settings.countdown||!started||submitted||paused)return;
        if(remaining===60&&!warnFired.current.m1){warnFired.current={...warnFired.current,m1:true};}
        if(remaining===300&&settings.minutes*60>300&&!warnFired.current.m5){warnFired.current={...warnFired.current,m5:true};}
    },[remaining,settings.sounds,settings.countdown,settings.minutes,started,submitted,paused]);
    // S3-1: leaving the result screen must not wipe it; a new attempt only starts when the user explicitly begins one.
    // Round 4: going home from a live quiz pauses it — the clock used to keep
    // ticking while the user thought he had left (same fix as Android goToList).
    const goHome=()=>{setConfirmAction(null);setFocus(false);update({tab:'home',paused:started&&!submitted?true:paused});};
    const open=(filter:string)=>{setReview(filter);const list=filter==='wrong'?ids.filter(id=>picked[id]!==null&&picked[id]!==questions[id].c):ids;update({tab:'quiz',paused:started&&!submitted?false:paused,index:list.includes(index)?index:(list[0]??index)});};
        const makeBackup = async (): Promise<string> => {
        const json = JSON.stringify({v:1,s:stored,x:extra});
        const raw = new TextEncoder().encode(json);
        try { if ((window as any).CompressionStream) return 'QH2.'+toB64(await streamBytes(raw,'CompressionStream')); } catch { /* fall back */ }
        return 'QH1.'+toB64(raw);
    };
    const restoreBackup = async (codeIn: string): Promise<string> => {
        try {
            const code = codeIn.replace(/\s+/g,'');
            let raw: Uint8Array;
            if (code.startsWith('QH2.')) raw = await streamBytes(fromB64(code.slice(4)),'DecompressionStream');
            else if (code.startsWith('QH1.')) raw = fromB64(code.slice(4));
            else return 'الكود مش صحيح.';
            const obj = JSON.parse(new TextDecoder().decode(raw));
            const st = obj?.s; const ex = obj?.x;
            const okState = obj?.v===1 && st && typeof st.picked==='string' && st.picked.length===questions.length && typeof st.favorites==='string' && Array.isArray(st.ids) && st.ids.every((n:unknown)=>typeof n==='number') && st.settings && typeof st.settings==='object';
            if (obj?.v===1 && st && typeof st.picked==='string' && st.picked.length===OLD_COUNT) return 'الكود ده من نسخة قديمة (٣١٢٢ سؤال) ومش مدعوم هنا. تقدمك الحالي ما اتغيّرش.';
            const okExtra = ex && typeof ex.w==='string' && Array.isArray(ex.h);
                        if (!okState || !okExtra) return 'الكود مش صحيح.';
            const next = encode(decode(st as Stored));
            if (next.ids.some((n:number)=>n<0||n>=questions.length)) return 'الكود مش من نفس النسخة.';
            setStored(()=>next);
            setExtra(()=>({w:ex.w,h:(ex.h as Attempt[]).slice(-HISTORY_MAX)}));
            return 'اتسترجع التقدم.';
        } catch { return 'الكود مش صحيح أو ناقص.'; }
    };
    // S2-6: the reassurance only shows when both stores actually report saved; a write failure shows an honest warning.
    const storage = saved.status==='error'||savedX.status==='error' ? 'تنبيه: الحفظ مش شغال في المتصفح ده — تقدمك هيضيع لو قفلت الصفحة. جرّب متصفح تاني أو افضي مساحة.' : 'الحفظ مفعّل على هذا المتصفح.';
    const storageFull = storage+(legacyInfo.active?' لقينا تقدم محفوظ من نسخة قديمة (٣١٢٢ سؤال). مش بنحوّله لأن ترتيب الأسئلة القديم مش متاح هنا، فالتجربة بدأت من جديد. بياناتك القديمة محفوظة زي ما هي، وتقدر تصدّرها من الإعدادات ثم نسخة احتياطية.':'');
    const screenCls = `qz-screen qz-size-${settings.textSize}${settings.dark?' qz-dark':''}${settings.textSize==='large'?' qz-large':''}${settings.calm?' qz-calm':''}${settings.fx?' qz-fx':''}${settings.spacious?' qz-spacious':''}${settings.contrast?' qz-contrast':''} qz-design-new${focus?' qz-focus':''}`;

    return (
        <>
        <div className={'qz-launch'+' qz-design-new'} hidden={opened} dir="rtl"><h2><QuizBrand/></h2><p>اختار الاختبار وكمّل من مكانك.</p><button type="button" className="qz-btn primary" onClick={() => setOpened(true)}>افتح التجربة</button></div>
        <dialog ref={panel} className="qz-app-dialog" onCancel={(e) => { if (e.target === e.currentTarget) { if (tab!=='home') { e.preventDefault(); goHome(); } else setOpened(false); } }} onClose={(e) => { if (e.target === e.currentTarget) setOpened(false); }} dir="rtl">
        <div className={screenCls+(tab==='quiz'&&started?' qz-is-solving':'')} style={{'--qz-scale':fontPercent(settings)/100} as React.CSSProperties} aria-label="Quiz Hub، أسئلة مزاولة المهنة">
            <header className="qz-app-head">{!focus&&<button type="button" className="qz-btn ghost qz-menu-btn" aria-label="القائمة" onClick={()=>setDrawerOpen(true)}><MenuIcon/></button>}<QuizBrand compact/>{tab==='quiz'&&started&&!focus&&settings.timer&&<TimerChip elapsed={settings.countdown?remaining:liveElapsed} countdown={settings.countdown} low={settings.countdown&&remaining<=300}/>}{!(tab==='quiz'&&started)&&<CloseButton label="قفل التجربة" onClick={() => { if (tab!=='home') goHome(); else setOpened(false); }} />}</header>
            {burst>0&&<div className="qz-confetti" key={burst} aria-hidden="true" data-testid="burst">{burstPieces.map((p,i)=><i key={i} style={{left:`${p.left}%`,width:p.size,height:p.size*1.4,background:p.color,animationDelay:`${p.delay}s`,animationDuration:`${p.duration}s`}}/>)}</div>}
            <div className="qz-scroll" ref={scroller}>
            {!(tab==='quiz'&&started)&&<div className="qz-topbar">
                <span className="qz-mode">{!started?'إعداد التجربة':state.mode==='study'?'وضع المذاكرة':'وضع الامتحان'}</span>
                
            </div>}

            {<>
            {(paused||(allAnswered&&state.mode==='study')||expired||submitted)&&started&&<p className="qz-clock-status">{expired?'الوقت خلص':submitted?'تم التسليم، العداد متوقف':allAnswered?'اكتملت الاختيارات، العداد متوقف':'العداد متوقف مؤقتًا'}</p>}
            {confirmAction&&<div className="qz-hint qz-confirm-box" role="alert"><p>{confirmAction==='finish'?(ids.some(id=>picked[id]===null)?'في أسئلة لسه من غير إجابة. هتتحسب ضمن الدرجة، تسلّم دلوقتي؟':'كل الأسئلة مجاوبة. تسلّم دلوقتي وتعرض النتيجة؟'):'هتبدأ تجربة جديدة وتمسح الإجابات والنتيجة والوقت الحاليين. الإعدادات هتفضل زي ما هي.'}</p><div className="qz-set-actions"><button type="button" className="qz-btn primary" onClick={()=>{if(confirmAction==='start')begin(pendingRun??undefined);else{doSubmit();setConfirmAction(null);}}}>{confirmAction==='start'?'ابدأ من جديد':'سلّم التجربة'}</button><button type="button" className="qz-btn ghost" onClick={()=>setConfirmAction(null)}>رجوع</button></div></div>}
            {tab==='home'&&<HomeScreen state={{...state,elapsed:liveElapsed}} update={update} onCard={onCard} open={open} finish={finish} storage={storageFull} celebrate={celebrate} wrongSet={wrongSet} history={history} startRun={r=>{if(started){setPendingRun(r);setConfirmAction('start');}else begin(r);}}/>}
            {tab==='quiz'&&started&&<QuizScreen state={{...state,index:safeIndex}} update={update} viewIds={viewIds} jump={id=>update({index:id})} pick={pick} expired={expired} finish={finish} review={review} setReview={open} goHome={goHome} attemptKey={attemptKey} focus={focus} setFocus={setFocus} mapSignal={mapSignal} onMapOpened={()=>setMapSignal(0)}/>}
            {tab==='quiz'&&!started&&<div className="qz-home"><p>اختار طريقة التجربة من الرئيسية الأول.</p><button type="button" className="qz-btn primary" onClick={()=>update({tab:'home'})}>إعداد التجربة</button></div>}
            {tab==='settings'&&<SettingsScreen backup={{make:makeBackup,restore:restoreBackup}} settings={settings} setSettings={v=>update({settings:v})} onReset={reset} paused={paused} setPaused={v=>update({paused:v})} resetClock={()=>update({elapsed:0})}/>}
            </>}

            </div>
            <nav
                className="qz-tabbar"
                aria-label="التنقل الرئيسي"
                style={{ '--qz-notch-x': tab === 'home' ? '83.33%' : tab === 'quiz' ? '50%' : '16.66%' } as React.CSSProperties}
            >
                <span className="qz-tabbar-bg" aria-hidden="true" />
                <button type="button" className={`qz-tab${tab === 'home' ? ' is-active' : ''}`} disabled={!ready} onClick={goHome} aria-current={tab === 'home' ? 'page' : undefined}>
                    <span className="qz-tab-icon"><HomeIcon /></span>
                    <span className="qz-tab-label">الرئيسية</span>
                </button>
                <button type="button" className={`qz-tab${tab === 'quiz' ? ' is-active' : ''}`} disabled={!ready} onClick={() => {setConfirmAction(null);open('all');}} aria-current={tab === 'quiz' ? 'page' : undefined}>
                    <span className="qz-tab-icon"><ListIcon /></span>
                    <span className="qz-tab-label">الأسئلة</span>
                </button>
                <button type="button" className={`qz-tab${tab === 'settings' ? ' is-active' : ''}`} disabled={!ready} onClick={() => {setConfirmAction(null);update({tab:'settings'});}} aria-current={tab === 'settings' ? 'page' : undefined}>
                    <span className="qz-tab-icon"><GearIcon /></span>
                    <span className="qz-tab-label">الإعدادات</span>
                </button>
            </nav>
            <Drawer open={drawerOpen} onClose={()=>setDrawerOpen(false)} items={drawerItems} dark={settings.dark} onDark={v=>update({settings:{...settings,dark:v}})} fontPct={fontPercent(settings)} onFontScale={v=>update({settings:{...settings,fontScale:v}})} />
        </div>
        </dialog>
        </>
    );
}

export function App() {
    return (
        <FileCard lang="ar" dir="rtl">
            <Routes>
                <Route path="/" element={<MockApp />} />
            </Routes>
        </FileCard>
    );
}
