const p=require('/tmp/rw/node_modules/puppeteer-core');
(async()=>{const b=await p.launch({executablePath:'/usr/bin/google-chrome',args:['--no-sandbox'],headless:'new'});
const w=ms=>new Promise(r=>setTimeout(r,ms));const errs=[];
async function run(name,st,ex,shot){const ctx=await b.createBrowserContext();const pg=await ctx.newPage();await pg.setViewport({width:360,height:800});pg.on('pageerror',e=>errs.push(e.message));
await pg.goto('http://localhost:8793/index.html');await w(400);
await pg.evaluate((s,x)=>{localStorage.clear();if(s!==null)localStorage.setItem('quiz-banks.v2',s);if(x!==null)localStorage.setItem('qh-extra.v1',x)},st,ex);
await pg.reload();await w(1200);
const r=await pg.evaluate(()=>({s:localStorage.getItem('quiz-banks.v2'),x:localStorage.getItem('qh-extra.v1'),ls:localStorage.getItem('quiz-banks.v2.legacy-3122'),lx:localStorage.getItem('qh-extra.v1.legacy-3122')}));
await pg.evaluate(()=>{[...document.querySelectorAll('button')].find(e=>(e.innerText||'').trim().startsWith('الإعدادات'))?.click()});await w(500);
const ex2=await pg.evaluate(()=>{const d=[...document.querySelectorAll('details')].find(e=>/نسخة احتياطية/.test(e.innerText));d&&(d.open=true);const t=[...document.querySelectorAll('textarea')].find(t=>/البيانات القديمة/.test(t.getAttribute('aria-label')||''));if(t&&d)t.scrollIntoView();return t?JSON.parse(t.value):null});
if(shot){await w(300);await pg.screenshot({path:shot})}
const okS=st===null?true:(ex2&&ex2.state===st);const okX=ex===null?true:(ex2&&ex2.notebook===ex);
console.log(name,'| quarantined state',r.ls===st,'| quarantined nb',r.lx===ex,'| export roundtrip',okS&&okX,'| current state kept',r.s===st,'| current nb kept',r.x===ex);await ctx.close();}
const N=3122,pk=Array(N).fill('.').join(''),fv=Array(N).fill('0').join('');
const old=' {"index":0 ,\n  "picked":"'+pk+'","favorites":"'+fv+'","ids":[1,2],"settings":{}}\n';
await run('old3122 whitespace/order',old,'{ "h":[], "w":"AQ==" }\n','/downloads/web-14-legacy-notice-v3.png');
await run('malformed state',"{not json 😀 ",'garbage[',null);
await run('orphan notebook only',null,'{"w":"AQ==","h":[]}',null);
const n=1186;const cur=JSON.stringify({settings:{},favorites:Array(n).fill('0').join(''),ids:[1],picked:Array(n).fill('.').join(''),index:0,elapsed:0,paused:false,started:false,submitted:false,resultElapsed:null,mode:'study',tab:'home',setup:{section:'6',count:3,mode:'study'}});
await run('valid 1186 state+notebook (must persist)',cur,'{"w":"AQ==","h":[]}',null);
console.log('ERRS',JSON.stringify(errs.slice(0,3)));await b.close()})();
