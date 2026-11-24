import React, { useState, useEffect, useRef } from 'react';
export const FileRouter = ({children}:any)=>children;
export const FileCard = ({children}:any)=><div dir="rtl">{children}</div>;
export const Routes = ({children}:any)=>children;
export const Route = ({element}:any)=>element;
export const TextLink = ({href,children}:any)=><a href={href}>{children}</a>;
export const CloseButton = ({label,onClick}:any)=><button type="button" aria-label={label} onClick={onClick}>x</button>;
export const CopyButton = ({text,label}:any)=><button type="button" onClick={()=>navigator.clipboard?.writeText(text)}>{label}</button>;
export function useLocalState<T>(key:string, init:T):[T,(v:any)=>void,{status:string;ready:boolean}] {
  const [v,setV]=useState<T>(()=>{try{const r=localStorage.getItem(key);return r?JSON.parse(r):init;}catch{return init;}});
  const set=(f:any)=>setV(prev=>{const n=typeof f==='function'?f(prev):f;try{localStorage.setItem(key,JSON.stringify(n));}catch{} return n;});
  return [v,set,{status:'saved',ready:true}];
}
