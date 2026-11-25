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

type Settings = {
    dark: boolean; textSize: string; fontScale?: number; spacious: boolean; contrast: boolean; calm: boolean; steps: boolean; letters: boolean;
    confirm: boolean; autoNext: boolean; retry: boolean; hints: boolean; hintDefault: boolean;
    translation: boolean; autoExplain: boolean; timer: boolean; quizOnly: boolean; countdown: boolean; minutes: number;
    sounds: boolean; fx: boolean; volume?: number;
};
const fontPercent = (s: Settings) => s.fontScale ?? (s.textSize==='small'?88:s.textSize==='large'?118:100);
const defaults: Settings = { dark: false, textSize: 'normal', spacious: false, contrast: false, calm: false, steps: true, letters: true, confirm: false, autoNext: false, retry: false, hints: true, hintDefault: false, translation: false, autoExplain: false, timer: true, quizOnly: false, countdown: false, minutes: 120, sounds: true, fx: true, volume: 80 };

import { b64_correct, b64_wrong } from './sounds-data';

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
function TimerChip({ elapsed, countdown = false }: { elapsed: number; countdown?: boolean }) {
    return (
        <span className="qz-timer" role="timer" aria-label={`${countdown ? "الوقت المتبقي" : "الوقت من بداية التجربة"} ${formatTime(elapsed)}`}>
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
function QuizScreen({ state, update, viewIds, jump, pick, expired, finish, review, setReview, goHome, attemptKey }: {state: Saved; update: (v: Partial<Saved>) => void; viewIds: number[]; jump:(id:number)=>void; pick:(v:number)=>void; expired:boolean; finish:()=>void; review:string; setReview:(v:string)=>void; goHome:()=>void; attemptKey:number}) {
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
    const q = questions[index]; const chosen = picked[index]; const answered = chosen !== null;
    const feedback = submitted || mode === 'study';
    const pos = viewIds.indexOf(index);
    const progress = viewIds.length > 1 ? Math.max(0,pos)/(viewIds.length-1)*100 : 100;
    useEffect(()=>{setTrOpen(settings.translation);setHintOpen(settings.hintDefault);setExOpen(false);setPendingState(pendingById.current[index] ?? null);},[index,review]);
    useEffect(()=>{ pendingById.current={}; setPendingState(null); },[attemptKey]);
    useEffect(()=>{if(exOpen)sheet.current?.showModal();else sheet.current?.close();},[exOpen]);
    const commit = (v:number) => { pick(v);setPending(null);if(settings.autoExplain && mode==='study')setExOpen(true); };
    if(!viewIds.length) return <div className="qz-home"><h2>مفيش أسئلة هنا</h2><p>كل الإجابات صح، مفيش غلط للمراجعة.</p><button type="button" className="qz-btn primary" onClick={goHome}>ارجع للرئيسية</button></div>;
    return <>
        {(state.run==='notebook'||state.run==='flagged') && <p className="qz-review-banner">{runLabel(state.run)}</p>}
        {review!=='all' && <p className="qz-review-banner">مراجعة الغلط فقط · مراجعة من غير تغيير النتيجة <button type="button" className="qz-tool-btn" onClick={()=>setReview('all')}>كل أسئلة التجربة</button></p>}
        {settings.steps && <div className="qz-progress-row" dir="ltr">
            <div className="qz-linear-progress" role="progressbar" aria-label="السؤال الحالي" aria-valuemin={1} aria-valuemax={viewIds.length} aria-valuenow={pos+1} aria-valuetext={`Question ${pos+1} of ${viewIds.length}`}>
                <div className="qz-progress-rail"><span className="qz-progress-fill" style={{width:`${progress}%`}}/><span className="qz-progress-marker" style={{left:`${progress}%`}}>{pos+1}</span></div>
            </div>
            <span className="qz-progress-total" aria-hidden="true">/ {viewIds.length}</span>
            <button type="button" className="qz-tool-btn qz-icon-btn qz-map-trigger" aria-label="خريطة الأسئلة" title="خريطة الأسئلة" onClick={()=>mapSheet.current?.showModal()}><ListIcon/></button>
        </div>}
        <dialog ref={mapSheet} className="qz-ex-dialog" dir="rtl"><CloseButton label="قفل الخريطة" onClick={()=>mapSheet.current?.close()}/><p className="qz-explain-title">خريطة الأسئلة</p><div className="qz-question-map">{viewIds.map((id,p)=><button type="button" className={'qz-dot'+(id===index?' is-current':'')+(feedback&&picked[id]!==null?(picked[id]===questions[id].c?' is-right':' is-missed'):'')} key={id} onClick={()=>{jump(id);mapSheet.current?.close();}} aria-label={'السؤال '+toAr(String(p+1))}>{toAr(String(p+1))}{picked[id]!==null?' •':''}</button>)}</div></dialog>
        <div className="qz-stage" key={index}><div className="qz-question"><p className="qz-q-en" dir="ltr">{q.q}</p></div>
        <div className="qz-tools">
        <button type="button" className={'qz-tool-btn qz-icon-btn'+(trOpen?' is-open':'')} onClick={()=>setTrOpen(!trOpen)} aria-expanded={trOpen} aria-label="الترجمة" title="الترجمة"><span className="qz-icon-chip"><TranslateIcon/></span></button>

        <button type="button" className={'qz-tool-btn qz-icon-btn qz-flag-btn'+(state.favorites.includes(index)?' is-open':'')} onClick={()=>update({favorites:state.favorites.includes(index)?state.favorites.filter(i=>i!==index):[...state.favorites,index]})} aria-pressed={state.favorites.includes(index)} aria-label="راجعني" title="راجعني"><span className="qz-icon-chip"><FlagIcon filled={state.favorites.includes(index)}/></span></button>
        {audioFor(index)&&((feedback&&answered)||submitted)&&<><button type="button" className={'qz-tool-btn qz-icon-btn'+(playing?' is-open':'')} onClick={()=>{const a=audioRef.current;if(!a)return;if(playing||wantPlay){a.pause();a.currentTime=0;audioReq.current++;setPlaying(false);setWantPlay(false);return;}const id=++audioReq.current;const ld=audioLoaders[index];(audioSrc?Promise.resolve(audioSrc):ld().then(m=>m.default)).then(u=>{if(id!==audioReq.current)return;if(audioSrc!==u)setAudioSrc(u);setWantPlay(true);}).catch(()=>{if(id===audioReq.current){setWantPlay(false);setAudioError(true);}});}} aria-pressed={playing} aria-label={playing?'إيقاف الشرح الصوتي':(generatedAudio.has(index)?'اسمع الشرح (صوت مولّد)':'اسمع الشرح')} title={playing?'إيقاف الشرح الصوتي':(generatedAudio.has(index)?'اسمع الشرح (صوت مولّد، مش تسجيل الدكتور)':'اسمع الشرح')}><span className="qz-icon-chip"><span aria-hidden="true">{playing?<StopIcon/>:<SpeakerIcon/>}</span></span></button><audio ref={audioRef} key={index} src={audioSrc} preload="none" onEnded={()=>{setPlaying(false);setWantPlay(false);}} onError={()=>setAudioError(true)}/></>}
        {feedback && (answered||submitted) && <button type="button" className="qz-tool-btn" onClick={()=>setExOpen(true)}>الشرح</button>}
        {mode==='study'&&!submitted&&!answered&&settings.hints&&<button type="button" className={'qz-tool-btn qz-icon-btn'+(hintOpen?' is-open':'')} onClick={()=>setHintOpen(!hintOpen)} aria-expanded={hintOpen} aria-label="التلميح" title="التلميح"><span className="qz-icon-chip"><span aria-hidden="true"><BulbIcon/></span></span></button>}
        </div>
        {audioError&&<p className="qz-hint" role="status">تعذّر تشغيل الصوت، جرّب تاني.</p>}
        {trOpen&&<p className="qz-q-ar" dir="rtl">{q.tr}</p>}
        {mode==='study'&&!submitted&&!answered&&settings.hints&&hintOpen&&<p className="qz-hint" dir="rtl">{q.hint}</p>}
        <div className="qz-options" role="listbox" aria-label="الاختيارات">{q.o.map((opt,i)=>{
            const correct=i===q.c;const selected=i===chosen;
            const locked=submitted||expired||(mode==='study'&&answered&&!(settings.retry&&chosen!==q.c));
            let cls='qz-opt';if(feedback&&(answered||submitted)&&correct)cls+=' is-correct';if(feedback&&selected&&!correct)cls+=' is-wrong';if((pending===i)||(!feedback&&selected))cls+=' is-pending';
            return <button type="button" key={i} role="option" className={cls} dir="ltr" disabled={locked} aria-selected={selected||pending===i} onClick={()=>settings.confirm?setPending(i):commit(i)}>{settings.letters&&<span className="qz-letter" dir="ltr">{letters[i]}</span>}<span className="qz-opt-text" dir="ltr">{opt}</span>{feedback&&(answered||submitted)&&correct&&<span className="qz-mark">✓</span>}{feedback&&selected&&!correct&&<span className="qz-mark">✕</span>}</button>;
        })}</div></div>
        {expired&&!submitted&&<p className="qz-hint" role="status">الوقت خلص. الإجابات اتقفلت، سلّم التجربة عشان تشوف النتيجة، أو صفّر العداد من الإعدادات.</p>}
        {settings.confirm&&pending!==null&&!expired&&!submitted&&<button type="button" className="qz-btn primary qz-confirm" onClick={()=>commit(pending)}>تأكيد الإجابة</button>}
        <div className="qz-nav"><button type="button" className="qz-btn ghost" disabled={pos<=0} onClick={()=>jump(viewIds[pos-1])}>السابق</button><button type="button" className="qz-btn primary" disabled={pos>=viewIds.length-1} onClick={()=>jump(viewIds[pos+1])}>التالي</button></div>
        <button type="button" className="qz-btn primary qz-home-cta" onClick={submitted?goHome:finish}>{submitted?'الرجوع للرئيسية':'إنهاء وعرض النتيجة'}</button>
        <dialog ref={sheet} className="qz-ex-dialog" onCancel={()=>setExOpen(false)} onClose={()=>setExOpen(false)} dir="rtl"><CloseButton label="قفل الشرح" onClick={()=>setExOpen(false)}/><p className="qz-explain-title">{chosen===q.c?'إجابة صحيحة':chosen===null?'سؤال من غير إجابة':'الإجابة الصحيحة'}</p><p className="qz-explain-body" dir="rtl">{q.ex}</p><p className="qz-why" dir="rtl">{q.why}</p>{generatedAudio.has(index)&&<p className="qz-src" dir="rtl">الشرح الصوتي للسؤال ده مولّد، مش تسجيل الدكتور.</p>}{q.url&&<p className="qz-src" dir="rtl">المصدر: <TextLink href={q.url}>{q.src||q.url}</TextLink></p>}</dialog>
    </>;
}

function HomeScreen({state,update,start,open,finish,storage,celebrate,wrongSet,history,startRun}: {state:Saved;update:(v:Partial<Saved>)=>void;start:()=>void;open:(filter:string)=>void;finish:()=>void;storage:string;celebrate:boolean;wrongSet:Set<number>;history:Attempt[];startRun:(r:Run)=>void}) {
 const {picked,ids,submitted,started,setup,elapsed} = state;
 const right=ids.filter(id=>picked[id]===questions[id].c).length;const wrong=ids.filter(id=>picked[id]!==null&&picked[id]!==questions[id].c).length;const blank=ids.filter(id=>picked[id]===null).length;
 const bank=banks.find(b=>b.id===setup.section)||banks[0];
 const [bankQuery,setBankQuery]=useState('');
 const filtered=bankQuery?banks.filter(b=>b.name.includes(bankQuery)):banks;
 const groups=Array.from(new Set(banks.map(b=>b.group)));
 const confetti=React.useMemo(()=>Array.from({length:26},(_,i)=>({left:(i*37+13)%100,delay:(i%9)*0.14,duration:2.2+(i%5)*0.35,color:['#0b4f4a','#e4f5a4','#7fd08a','#ffffff','#c9ef6b'][i%5],size:6+(i%3)*3})),[]);
 return <div className="qz-home">
 {celebrate&&state.settings.fx&&!state.settings.calm&&<div className="qz-confetti" aria-hidden="true">{confetti.map((p,i)=><i key={i} style={{left:`${p.left}%`,width:p.size,height:p.size*1.4,background:p.color,animationDelay:`${p.delay}s`,animationDuration:`${p.duration}s`}}/>)}</div>}
 <p className={"qz-home-title"+(!submitted&&!started?" qz-home-quiz":"")}>{submitted?'نتيجة التجربة':started?'كمّل من مكانك':bank.name}</p>
 <p className="qz-save-status" role="status">{storage}</p>{started&&<p className="qz-set-detail">{state.run==='notebook'||state.run==='flagged'?runLabel(state.run):(banks.find(b=>b.ids.includes(ids[0]))||bank).name}</p>}
 {submitted?<>{celebrate&&<p className="qz-bravo" role="status">برافو! نتيجة عالية 🎉</p>}<div className="qz-score"><strong><bdi>{toAr(String(Math.round(right/ids.length*100)))}٪</bdi></strong><span>{toAr(String(right))} صح من {toAr(String(ids.length))}</span></div><div className="qz-result-grid">{[[right,'صح'],[wrong,'غلط'],[blank,'بدون إجابة']].map(([n,l])=><div key={l}><b>{toAr(String(n))}</b><span>{l}</span></div>)}</div><p className="qz-home-time"><ClockIcon/> وقت الحل: <bdi>{formatTime(state.resultElapsed??elapsed)}</bdi></p><p className="qz-set-detail">الأسئلة المتروكة محسوبة في الدرجة.</p><div className="qz-action-stack"><button type="button" className="qz-btn primary" onClick={()=>open('all')}>مراجعة كل الإجابات</button><button type="button" className="qz-btn ghost" disabled={wrong===0} onClick={()=>open('wrong')}>مراجعة الغلط فقط ({toAr(String(wrong))})</button></div></>:started?<><p className="qz-home-sub">جاوبت {toAr(String(ids.length-blank))} من {toAr(String(ids.length))} · {state.mode==='exam'?'امتحان':'مذاكرة'}</p><details className="qz-session-map"><summary>خريطة الأسئلة</summary><div className="qz-home-cards">{ids.map(id=><button type="button" className="qz-home-card" key={id} onClick={()=>{open('all');update({index:id});}}><span className="qz-home-card-num">سؤال {toAr(String(ids.indexOf(id)+1))}</span><span className="qz-home-card-state">{picked[id]===null?'لسه':state.mode==='exam'?'تم الاختيار':picked[id]===questions[id].c?'صح ✓':'غلط ✕'}</span></button>)}</div></details><p className="qz-home-time"><ClockIcon/><bdi>{formatTime(elapsed)}</bdi></p><div className="qz-action-stack"><button type="button" className="qz-btn primary" onClick={()=>open('all')}>كمّل الحل</button><button type="button" className="qz-btn ghost" onClick={finish}>إنهاء وعرض النتيجة</button></div></>:null}
 <details className="qz-set-group" open={!started}><summary>{started?'تجربة جديدة':'إعداد التجربة'}</summary><div className="qz-setup">
 <label className="qz-set-label" htmlFor="bank-search">الاختبار أو البنك</label><input id="bank-search" className="qz-bank-input qz-bank-search" type="search" placeholder="ابحث عن بنك…" aria-label="ابحث عن بنك" value={bankQuery} onChange={e=>setBankQuery(e.target.value)}/><div className="qz-bank-list" role="listbox" aria-label="الاختبار أو البنك">{groups.map(g=>{const gb=filtered.filter(b=>b.group===g);if(!gb.length)return null;const totalQ=gb.reduce((a,b)=>a+b.ids.length,0);return <div key={g} className="qz-bank-group"><p className="qz-bank-group-label">{g==='tests'?'الاختبارات':g} · {toAr(String(totalQ))} سؤال</p>{gb.map(b=><button type="button" key={b.id} role="option" aria-selected={setup.section===b.id} className={'qz-bank-opt'+(setup.section===b.id?' is-on':'')} onClick={()=>update({setup:{section:b.id,mode:setup.mode},settings:{...state.settings,minutes:b.minutes}})}>{b.name} · {toAr(String(b.ids.length))} سؤال</button>)}</div>;})}{filtered.length===0&&<p className="qz-set-detail">مفيش نتائج للبحث.</p>}</div>

 <span className="qz-set-label">طريقة التجربة</span><div className="qz-set-seg">{[['study','مذاكرة'],['exam','امتحان']].map(([v,l])=><button type="button" className={'qz-set-seg-btn'+(setup.mode===v?' is-on':'')} key={v} aria-pressed={setup.mode===v} onClick={()=>update({setup:{...setup,mode:v as Mode}})}>{l}</button>)}</div><p className="qz-set-detail">{setup.mode==='study'?'تصحيح فوري، مع التلميح والشرح حسب إعداداتك.':'من غير تلميحات أو تصحيح أو شرح لحد التسليم. تقدر تغيّر إجابتك قبل النهاية.'}</p><button type="button" className="qz-btn primary qz-home-cta" onClick={start}>ابدأ تجربة {setup.mode==='study'?'مذاكرة':'امتحان'}</button></div></details>
 <LearnPanels wrongSet={wrongSet} flagged={state.favorites} history={history} startRun={startRun} mode={setup.mode}/>
 <InfoPanels/>

 </div>;
}


const infoConfirmed = ['الامتحان 100 سؤال اختيار من متعدد، وزمنه لا يتجاوز 3 ساعات.','النتيجة نجاح أو رسوب (Pass or Fail) من غير درجات.','الدخول من لابتوب أو كمبيوتر فيه كاميرا (USB)، والكاميرا تشتغل طول الامتحان.','ارفع صورة شخصية بخلفية بيضاء قبل أي امتحان، وإلا ممكن يتلغي الامتحان أو يتعلّق البروفايل.','اقعد لوحدك في مكان هادي ومنعزل وبشكل لائق طول الامتحان.','ادخل الموقع قبل الميعاد بـ20 دقيقة على الأقل.','ممنوع فتح أي متصفح أو موقع تاني، أو استخدام أجهزة تانية أو سماعات خارجية، والمخالفة بتلغي الامتحان.','في كل دور 3 امتحانات فعلية، تنجح في أي واحد منهم، والمحسوب آخر امتحان تدخله. لو نجحت من أول مرة الأفضل ما تدخلش تاني.'];
// S4-3: the unconfirmed stats (60٪ pass rate, question split) and the stale July 2024 payment panel are
// removed entirely — no verification wording or unconfirmed numbers in the UI.
function InfoPanels() {
    return <>
        <details className="qz-set-group" open><summary>تعليمات الامتحان الفعلي <span>للطلاب</span></summary>
            <div className="qz-set-row qz-stacked">
                <p className="qz-set-detail">دي تعليمات امتحان المزاولة الحقيقي، مش قواعد الكويز ده.</p>
                <ul className="qz-info-list">{infoConfirmed.map(t=><li key={t}>{t}</li>)}</ul>
            </div>
        </details>
    </>;
}

function LearnPanels({wrongSet,flagged,history,startRun,mode}:{wrongSet:Set<number>;flagged:number[];history:Attempt[];startRun:(r:Run)=>void;mode:Mode}) {
    const [scope,setScope] = useState('all');
    const wrongBanks = banks.map(b=>({b,n:b.ids.filter(i=>wrongSet.has(i)).length})).filter(x=>x.n>0);
    const scopeOk = scope==='all' || wrongBanks.some(x=>x.b.id===scope);
    const sc = scopeOk?scope:'all';
    const wrongIds = (sc==='all'?[...wrongSet]:banks.find(b=>b.id===sc)!.ids.filter(i=>wrongSet.has(i))).sort((a,b)=>a-b);
    const flagIds = [...flagged].sort((a,b)=>a-b);
    const keys = Array.from(new Set(history.map(h=>h.b)));
    const rows = keys.map(k=>{
        const hs = history.filter(h=>h.b===k); const pct = (h:Attempt)=>h.n?Math.round(h.r/h.n*100):0;
        const last = hs[hs.length-1]; const prev = hs.length>1?hs[hs.length-2]:null;
        return {k,hs,last:pct(last),best:Math.max(...hs.map(pct)),delta:prev?pct(last)-pct(prev):null,recent:hs.slice(-5).map(pct)};
    }).sort((a,b)=>a.last-b.last);
    const totalR = history.reduce((a,h)=>a+h.r,0); const totalN = history.reduce((a,h)=>a+h.n,0);
    return <>
        <details className="qz-set-group"><summary>دفتر الغلطات <span>{toAr(String(wrongSet.size))} سؤال</span></summary>
            <div className="qz-set-row qz-stacked">
                {wrongSet.size===0?<p className="qz-set-detail">مفيش أسئلة غلط لسه. أي إجابة غلط هتتضاف هنا تلقائيًا.</p>:<>
                <label className="qz-set-label" htmlFor="nb-scope">من أي بنك</label>
                <select id="nb-scope" className="qz-bank-input" value={sc} onChange={e=>setScope(e.target.value)}><option value="all">كل الكويزات · {toAr(String(wrongSet.size))}</option>{wrongBanks.map(x=><option key={x.b.id} value={x.b.id}>{x.b.name} · {toAr(String(x.n))}</option>)}</select>
                <p className="qz-set-detail">الإجابة الصح هنا بتشيل السؤال من الدفتر.</p>
                <button type="button" className="qz-btn primary" onClick={()=>startRun({kind:'notebook',ids:wrongIds})}>ذاكر الغلطات ({toAr(String(wrongIds.length))})</button></>}
            </div>
        </details>
        <details className="qz-set-group"><summary>راجعني <span>{toAr(String(flagIds.length))} سؤال</span></summary>
            <div className="qz-set-row qz-stacked">
                {flagIds.length===0?<p className="qz-set-detail">علّم أي سؤال بزر العلم أثناء الحل وهيظهر هنا.</p>:<button type="button" className="qz-btn primary" onClick={()=>startRun({kind:'flagged',ids:flagIds})}>راجع الأسئلة المعلّمة ({toAr(String(flagIds.length))})</button>}
            </div>
        </details>
        <details className="qz-set-group"><summary>مستواك <span>{toAr(String(history.length))} محاولة</span></summary>
            {history.length===0?<div className="qz-set-row"><p className="qz-set-detail">سلّم أول تجربة وهتظهر نسبتك لكل بنك هنا.</p></div>:<>
            <div className="qz-set-row"><div><span className="qz-set-label">المتوسط العام</span><p className="qz-set-detail">{toAr(String(totalN?Math.round(totalR/totalN*100):0))}٪ صح في {toAr(String(history.length))} محاولة. الأضعف فوق.</p></div></div>
            {rows.map(r=><div className="qz-set-row qz-stat" key={r.k}>
                <div className="qz-stat-head"><span className="qz-set-label">{runLabel(r.k)}</span><span className="qz-stat-last"><bdi>{toAr(String(r.last))}٪</bdi></span></div>
                <p className="qz-set-detail">{toAr(String(r.hs.length))} محاولة · أحسن نتيجة <bdi>{toAr(String(r.best))}٪</bdi>{r.delta!==null&&<> · عن اللي قبلها <bdi dir="ltr">{r.delta>0?'+':''}{toAr(String(r.delta))}٪</bdi></>}</p>
                <div className="qz-spark" aria-label="آخر المحاولات">{r.recent.map((v,i)=><span key={i} className="qz-spark-bar" style={{height:`${Math.max(6,v*0.36)}px`}} title={`${v}%`}><i>{toAr(String(v))}</i></span>)}</div>
            </div>)}</>}
        </details>
    </>;
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
    const [opened,setOpened] = useState(true);
    const [review,setReview] = useState('all');
    const [confirmAction,setConfirmAction] = useState<'finish'|'start'|null>(null);
    const [pendingRun,setPendingRun] = useState<Run|null>(null);
    const [autoPending,setAutoPending] = useState(false);
    const [celebrate,setCelebrate] = useState(false);
    const [burst,setBurst] = useState(0);
    const burstPieces=React.useMemo(()=>Array.from({length:26},(_,i)=>({left:(i*37+13)%100,delay:(i%9)*0.06,duration:1.6+(i%5)*0.25,color:['#0b4f4a','#e4f5a4','#7fd08a','#ffffff','#c9ef6b'][i%5],size:6+(i%3)*3})),[]);
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
        if(state.mode==='study'){ if(v===questions[index].c){ if(state.run==='notebook')addWrong([],[index]); } else addWrong([index],[]); }
        if(state.mode==='study'&&v===questions[index].c&&settings.fx&&!settings.calm)setBurst(b=>b+1);
        if(settings.sounds&&state.mode==='study')play(v===questions[index].c?'correct':'wrong');
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
    const begin=(run?:Run)=>{const setup=state.setup;const nextIds=run?run.ids:(banks.find(b=>b.id===setup.section)||banks[0]).ids;if(!nextIds.length)return;setReview('all');setCelebrate(false);setAttemptKey(k=>k+1);update({run:run?run.kind:'all',bank:run?run.kind:setup.section,ids:nextIds,picked:questions.map(()=>null),index:nextIds[0],elapsed:0,paused:false,started:true,submitted:false,resultElapsed:null,mode:setup.mode,tab:'quiz'});setConfirmAction(null);};
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
    const goHome=()=>{setConfirmAction(null);update({tab:'home'});};
    const open=(filter:string)=>{setReview(filter);const list=filter==='wrong'?ids.filter(id=>picked[id]!==null&&picked[id]!==questions[id].c):ids;update({tab:'quiz',index:list.includes(index)?index:(list[0]??index)});};
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
    const screenCls = `qz-screen qz-size-${settings.textSize}${settings.dark?' qz-dark':''}${settings.textSize==='large'?' qz-large':''}${settings.calm?' qz-calm':''}${settings.fx?' qz-fx':''}${settings.spacious?' qz-spacious':''}${settings.contrast?' qz-contrast':''}`;

    return (
        <>
        <div className="qz-launch" hidden={opened} dir="rtl"><h2><QuizBrand/></h2><p>اختار الاختبار وكمّل من مكانك.</p><button type="button" className="qz-btn primary" onClick={() => setOpened(true)}>افتح التجربة</button></div>
        <dialog ref={panel} className="qz-app-dialog" onCancel={(e) => { if (e.target === e.currentTarget) { if (tab!=='home') { e.preventDefault(); goHome(); } else setOpened(false); } }} onClose={(e) => { if (e.target === e.currentTarget) setOpened(false); }} dir="rtl">
        <div className={screenCls+(tab==='quiz'&&started?' qz-is-solving':'')} style={{'--qz-scale':fontPercent(settings)/100} as React.CSSProperties} aria-label="Quiz Hub، أسئلة مزاولة المهنة">
            <header className="qz-app-head"><QuizBrand compact={tab==='quiz'&&started}/>{tab==='quiz'&&started&&settings.timer&&<TimerChip elapsed={settings.countdown?remaining:liveElapsed} countdown={settings.countdown}/>}<CloseButton label="قفل التجربة" onClick={() => { if (tab!=='home') goHome(); else setOpened(false); }} /></header>
            {burst>0&&<div className="qz-confetti" key={burst} aria-hidden="true" data-testid="burst">{burstPieces.map((p,i)=><i key={i} style={{left:`${p.left}%`,width:p.size,height:p.size*1.4,background:p.color,animationDelay:`${p.delay}s`,animationDuration:`${p.duration}s`}}/>)}</div>}
            <div className="qz-scroll" ref={scroller}>
            {!(tab==='quiz'&&started)&&<div className="qz-topbar">
                <span className="qz-mode">{!started?'إعداد التجربة':state.mode==='study'?'وضع المذاكرة':'وضع الامتحان'}</span>
                
            </div>}

            {<>
            {(paused||(allAnswered&&state.mode==='study')||expired||submitted)&&started&&<p className="qz-clock-status">{expired?'الوقت خلص':submitted?'تم التسليم، العداد متوقف':allAnswered?'اكتملت الاختيارات، العداد متوقف':'العداد متوقف مؤقتًا'}</p>}
            {confirmAction&&<div className="qz-hint qz-confirm-box" role="alert"><p>{confirmAction==='finish'?(ids.some(id=>picked[id]===null)?'في أسئلة لسه من غير إجابة. هتتحسب ضمن الدرجة، تسلّم دلوقتي؟':'كل الأسئلة مجاوبة. تسلّم دلوقتي وتعرض النتيجة؟'):'هتبدأ تجربة جديدة وتمسح الإجابات والنتيجة والوقت الحاليين. الإعدادات هتفضل زي ما هي.'}</p><div className="qz-set-actions"><button type="button" className="qz-btn primary" onClick={()=>{if(confirmAction==='start')begin(pendingRun??undefined);else{doSubmit();setConfirmAction(null);}}}>{confirmAction==='start'?'ابدأ من جديد':'سلّم التجربة'}</button><button type="button" className="qz-btn ghost" onClick={()=>setConfirmAction(null)}>رجوع</button></div></div>}
            {tab==='home'&&<HomeScreen state={{...state,elapsed:liveElapsed}} update={update} start={()=>{setPendingRun(null);if(started)setConfirmAction('start');else begin();}} open={open} finish={finish} storage={storageFull} celebrate={celebrate} wrongSet={wrongSet} history={history} startRun={r=>{if(started){setPendingRun(r);setConfirmAction('start');}else begin(r);}}/>}
            {tab==='quiz'&&started&&<QuizScreen state={{...state,index:safeIndex}} update={update} viewIds={viewIds} jump={id=>update({index:id})} pick={pick} expired={expired} finish={finish} review={review} setReview={open} goHome={goHome} attemptKey={attemptKey}/>}
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
