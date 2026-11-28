#!/usr/bin/env python3
"""Asserts web data/audio integrity. Exit code 1 on any failure. Needs node and ffmpeg.
Run from web/:  python3 tests/check_audio.py"""
import json, os, re, subprocess, sys
from concurrent.futures import ThreadPoolExecutor
os.chdir(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..'))
fail = []
def check(c, msg):
    if not c: fail.append(msg)
data = subprocess.check_output(['node','-e',"const fs=require('fs');const o=eval('('+fs.readFileSync('src/bank-data.ts','utf8').replace(/^export const sourceData =/,'').replace(/;\\s*$/,'')+')');console.log(JSON.stringify({n:o.questions.length,b:o.banks.map(b=>b.ids)}))"])
d = json.loads(data); n = d['n']
check(n == 1186, f'question count {n} != 1186')
allids = sorted(i for b in d['b'] for i in b)
check(allids == list(range(n)), 'bank ids are not exactly 0..n-1 once each')
m = open('src/audio-map.ts', encoding='utf8').read()
ent = {int(k): f for k, f in re.findall(r"^(\d+):\(\)=>import\('\./audio/([^']+)'\)", m, re.M)}
check(sorted(ent) == list(range(n)), f'audio keys are not 0..{n-1}: {len(ent)} keys, {len([k for k in ent if k>=n])} out of range')
files = sorted(set(ent.values()))
check(all(os.path.exists('src/audio/'+f) for f in files), 'audio-map file missing on disk')
extra = set(os.listdir('src/audio')) - set(files)
check(not extra, f'unreferenced audio files: {len(extra)}')
def dec(f):
    p = subprocess.run(['ffmpeg','-v','error','-i','src/audio/'+f,'-f','null','-'], capture_output=True, text=True)
    return f, p.stderr.strip(), os.path.getsize('src/audio/'+f)
with ThreadPoolExecutor(8) as ex:
    res = list(ex.map(dec, files))
bad = [r for r in res if r[1] or r[2] < 1000]
check(not bad, f'{len(bad)} audio files fail to decode or are tiny: {[b[0] for b in bad[:5]]}')
gen = set(map(int, re.findall(r'\d+', m.split('generatedAudio')[1].split('[',1)[1].split(']')[0])))
check(len(gen) == n and all(ent[g].startswith('g') for g in gen), 'generatedAudio set does not match g clips')
print('questions', n, '| audio keys', len(ent), '| files', len(files), '| generated', len(gen), '| decode failures', len(bad))
if fail:
    print('FAIL:\n- ' + '\n- '.join(fail)); sys.exit(1)
print('OK')
