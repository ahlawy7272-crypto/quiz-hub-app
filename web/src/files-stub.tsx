import React, { useState, useEffect, useRef } from 'react';
export const FileRouter = ({children}:any)=>children;
export const FileCard = ({children}:any)=><div dir="rtl">{children}</div>;
export const Routes = ({children}:any)=>children;
export const Route = ({element}:any)=>element;
export const TextLink = ({href,children}:any)=><a href={href}>{children}</a>;
export const CloseButton = ({label,onClick}:any)=><button type="button" aria-label={label} title={label} onClick={onClick} style={{width:36,height:36,minHeight:36,borderRadius:999,border:'1.5px solid rgba(11,79,74,0.3)',background:'rgba(255,255,255,0.55)',color:'#0b4f4a',fontSize:16,lineHeight:1,cursor:'pointer',flex:'none'}}>✕</button>;
// S4-1: copying shows "اتنسخ ✓" for 2s instead of staying silent.
export const CopyButton = ({text,label,compact}:any)=>{
  const [copied,setCopied]=useState(false);
  const timer=useRef<any>(null);
  useEffect(()=>()=>{ if(timer.current) clearTimeout(timer.current); },[]);
  return <button type="button" onClick={()=>{
    const done=()=>{ setCopied(true); if(timer.current) clearTimeout(timer.current); timer.current=setTimeout(()=>setCopied(false),2000); };
    try { const r=navigator.clipboard?.writeText(text) as any; if(r&&typeof r.then==='function') r.then(done).catch(()=>{}); else done(); } catch { /* clipboard unavailable */ }
  }}>{copied?'اتنسخ ✓':label}</button>;
};
// S2-6: the save status is real — a failed localStorage write reports 'error' instead of always claiming 'saved'.
const storageOk = ():boolean => { try { localStorage.setItem('__qh_probe','1'); localStorage.removeItem('__qh_probe'); return true; } catch { return false; } };
export function useLocalState<T>(key:string, init:T):[T,(v:T|((prev:T)=>T))=>void,{status:string;ready:boolean}] {
  const [status,setStatus]=useState<string>(()=>storageOk()?'saved':'error');
  const [v,setV]=useState<T>(()=>{try{const r=localStorage.getItem(key);return r?JSON.parse(r):init;}catch{return init;}});
  const set=(f:T|((prev:T)=>T))=>setV(prev=>{const n=typeof f==='function'?(f as (prev:T)=>T)(prev):f;try{localStorage.setItem(key,JSON.stringify(n));setStatus('saved');}catch{setStatus('error');} return n;});
  return [v,set,{status,ready:true}];
}
