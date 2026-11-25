import React, { useState, useEffect, useRef } from 'react';
export const FileRouter = ({children}:any)=>children;
export const FileCard = ({children}:any)=><div dir="rtl">{children}</div>;
export const Routes = ({children}:any)=>children;
export const Route = ({element}:any)=>element;
export const TextLink = ({href,children}:any)=><a href={href}>{children}</a>;
export const CloseButton = ({label,onClick}:any)=><button type="button" aria-label={label} title={label} onClick={onClick} style={{width:36,height:36,minHeight:36,borderRadius:999,border:'1.5px solid rgba(11,79,74,0.3)',background:'rgba(255,255,255,0.55)',color:'#0b4f4a',fontSize:16,lineHeight:1,cursor:'pointer',flex:'none'}}>✕</button>;
export const CopyButton = ({text,label}:any)=><button type="button" onClick={()=>navigator.clipboard?.writeText(text)}>{label}</button>;
export function useLocalState<T>(key:string, init:T):[T,(v:any)=>void,{status:string;ready:boolean}] {
  const [v,setV]=useState<T>(()=>{try{const r=localStorage.getItem(key);return r?JSON.parse(r):init;}catch{return init;}});
  const set=(f:any)=>setV(prev=>{const n=typeof f==='function'?f(prev):f;try{localStorage.setItem(key,JSON.stringify(n));}catch{} return n;});
  return [v,set,{status:'saved',ready:true}];
}
