// Round-trips a 1,186-question QH2 backup code through the same format the
// web app (App.tsx makeBackup/restoreBackup) and the Android app
// (Backup.java make/restore) share, and proves the real web fixture restores.
//
// Run from web/:  node tests/backup_roundtrip.js
// Exit code 1 on any failure. Node builtins only (zlib for deflate-raw).
//
// What it proves:
//   - A QH2 (and QH1) code built from a 1,186-question state decodes to the
//     exact same picked answers, favorites, ids, index and notebook.
//   - A code shaped exactly like Android's Backup.make() (settings with
//     fontScale, setup {section,count,mode}, index as a global id) is
//     accepted by the web validation rules, and vice versa: the shared
//     fields (v, picked/favorites strings of length 1,186, ids, x.w/x.h)
//     are identical on both sides.
//   - The real web fixture android/tests/fixtures/web_qh2_code.txt decodes
//     and validates (1,186 picked/favorites, 147 ids starting at 598, bank 6).
//   - A 3,122-question (older build) code is refused, and garbage is refused.
//
// What it does NOT prove:
//   - It does not run App.tsx's restoreBackup or Android's Backup.restore
//     themselves; it re-implements their validation rules (same length,
//     range and version checks) in plain Node. The Android side of the
//     exchange is proven in-repo by android/tests/Verify1.java
//     (webBackupRestoresOnAndroid, androidBackupRoundTripAndLegacyRefusal).
//   - It does not prove playback, UI behaviour, or a restore on a phone.
'use strict';
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

const N = 1186; // questions in the current build, both web and Android
const OLD_COUNT = 3122;
const fail = [];
function check(c, msg) { if (!c) fail.push(msg); }
function eq(a, b, msg) { check(JSON.stringify(a) === JSON.stringify(b), msg + ' (got ' + JSON.stringify(a) + ', want ' + JSON.stringify(b) + ')'); }

// --- the shared format (mirrors App.tsx + Backup.java) ---
function packBits(set) {
  const bytes = Buffer.alloc(Math.ceil(N / 8));
  for (const i of set) if (i >= 0 && i < N) bytes[i >> 3] |= 1 << (i & 7);
  let last = bytes.length;
  while (last > 0 && bytes[last - 1] === 0) last--;
  return bytes.subarray(0, last).toString('base64');
}
function unpackBits(str) {
  const out = new Set();
  if (!str) return out;
  const b = Buffer.from(str, 'base64');
  for (let i = 0; i < b.length; i++) for (let k = 0; k < 8; k++)
    if (b[i] & (1 << k)) { const id = i * 8 + k; if (id < N) out.add(id); }
  return out;
}
// encode() in App.tsx: picked as a 1,186-char string ('.' or '0'..'4'),
// favorites as a 1,186-char '0'/'1' string, ids as an array of global ids.
function encodeState(s) {
  const picked = Array.from({ length: N }, (_, i) => (s.picked[i] == null ? '.' : String(s.picked[i]))).join('');
  const favorites = Array.from({ length: N }, (_, i) => (s.favorites.has(i) ? '1' : '0')).join('');
  return { ...s, picked, favorites };
}
function makeCode(stored, extra, useQh2) {
  const json = JSON.stringify({ v: 1, s: stored, x: extra });
  const raw = Buffer.from(json, 'utf8');
  if (useQh2) return 'QH2.' + zlib.deflateRawSync(raw).toString('base64');
  return 'QH1.' + raw.toString('base64');
}
// restore validation, mirroring App.tsx restoreBackup + Backup.java restore:
// v===1, picked/favorites strings of length N, ids an array of ints in range,
// settings an object, extra {w: string, h: array}; 3,122 codes refused.
function restore(code) {
  const c = code.replace(/\s+/g, '');
  let raw;
  try {
    if (c.startsWith('QH2.')) raw = zlib.inflateRawSync(Buffer.from(c.slice(4), 'base64'));
    else if (c.startsWith('QH1.')) raw = Buffer.from(c.slice(4), 'base64');
    else return { ok: false, msg: 'الكود مش صحيح.' };
  } catch { return { ok: false, msg: 'الكود مش صحيح.' }; }
  let obj;
  try { obj = JSON.parse(raw.toString('utf8')); } catch { return { ok: false, msg: 'الكود مش صحيح.' }; }
  const st = obj && obj.s, ex = obj && obj.x;
  if (obj && obj.v === 1 && st && typeof st.picked === 'string' && st.picked.length === OLD_COUNT)
    return { ok: false, msg: 'legacy-3122-refused' };
  const okState = obj && obj.v === 1 && st && typeof st.picked === 'string' && st.picked.length === N
    && typeof st.favorites === 'string' && st.favorites.length === N
    && Array.isArray(st.ids) && st.ids.every(n => Number.isInteger(n))
    && st.settings && typeof st.settings === 'object';
  const okExtra = ex && typeof ex.w === 'string' && Array.isArray(ex.h);
  if (!okState || !okExtra) return { ok: false, msg: 'الكود مش صحيح.' };
  if (st.ids.some(n => n < 0 || n >= N)) return { ok: false, msg: 'الكود مش من نفس النسخة.' };
  const picked = Array.from(st.picked, v => (v === '.' ? null : Number(v)));
  const favorites = Array.from(st.favorites).flatMap((v, i) => (v === '1' ? [i] : []));
  return { ok: true, state: { ...st, picked, favorites }, extra: { w: ex.w, h: ex.h.slice(-60) } };
}

// --- a synthetic 1,186-question state (bank 6: ids 598..744, as in the fixture) ---
const bank6Ids = Array.from({ length: 147 }, (_, i) => 598 + i);
const pickedMap = {};
pickedMap[598] = 1; pickedMap[600] = 3; pickedMap[700] = 0; pickedMap[744] = 2;
const base = {
  settings: { dark: false, minutes: 120, volume: 80 },
  favorites: new Set([598, 610]),
  ids: bank6Ids,
  picked: pickedMap,
  index: 598,
  elapsed: 120, paused: false, started: true, submitted: false,
  resultElapsed: null, mode: 'study', tab: 'home',
  setup: { section: '6', count: 147, mode: 'study' },
  run: 'all', bank: '6',
};
const extra = { w: packBits(new Set([598, 599])), h: [{ t: 1, b: '6', m: 'study', r: 0, n: 147, s: 2 }] };

// 1. QH2 round-trip
{
  const code = makeCode(encodeState(base), extra, true);
  check(code.startsWith('QH2.'), 'QH2 code must start with QH2.');
  const r = restore(code);
  check(r.ok, 'QH2 round-trip must restore');
  if (r.ok) {
    eq(r.state.picked[598], 1, 'picked[598] survives QH2');
    eq(r.state.picked[600], 3, 'picked[600] survives QH2');
    eq(r.state.picked[599], null, 'unanswered stays unanswered');
    eq(r.state.favorites, [598, 610], 'favorites survive QH2');
    eq(r.state.ids, bank6Ids, 'ids survive QH2');
    eq(r.state.index, 598, 'index survives QH2');
    eq([...unpackBits(r.extra.w)].sort((a, b) => a - b), [598, 599], 'notebook bits survive QH2');
    eq(r.extra.h.length, 1, 'history survives QH2');
  }
}
// 2. QH1 (uncompressed fallback) round-trip
{
  const r = restore(makeCode(encodeState(base), extra, false));
  check(r.ok, 'QH1 round-trip must restore');
  if (r.ok) eq(r.state.favorites, [598, 610], 'favorites survive QH1');
}
// 3. Android-shaped code is accepted by the web rules (and vice versa:
//    Android's Backup.make emits exactly these fields)
{
  const androidShaped = encodeState({
    ...base,
    settings: { dark: false, textSize: 'normal', fontScale: 100, minutes: 120, volume: 80 },
    index: 610, // Android writes the index as a global id
  });
  androidShaped.setup = { section: '6', count: 147, mode: 'study' };
  const r = restore(makeCode(androidShaped, extra, true));
  check(r.ok, 'Android-shaped QH2 code must be accepted by the web rules');
  if (r.ok) eq(r.state.index, 610, 'Android global-id index survives');
  check(r.ok && r.state.setup.section === '6', 'Android setup section survives');
}
// 4. The real web fixture restores
{
  const fixturePath = path.join(__dirname, '..', '..', 'android', 'tests', 'fixtures', 'web_qh2_code.txt');
  const code = fs.readFileSync(fixturePath, 'utf8').trim();
  const r = restore(code);
  check(r.ok, 'real web fixture must restore');
  if (r.ok) {
    eq(r.state.ids.length, 147, 'fixture ids length is 147');
    eq(r.state.ids[0], 598, 'fixture ids start at 598');
    eq(r.state.ids[146], 744, 'fixture ids end at 744');
    eq(r.state.favorites, [598], 'fixture favorites are {598}');
    eq([...unpackBits(r.extra.w)].sort((a, b) => a - b), [598, 599], 'fixture notebook is {598, 599}');
    eq(r.state.bank, '6', 'fixture bank is 6');
  }
}
// 5. A 3,122-question code is refused, not converted
{
  const legacy = makeCode({
    ...encodeState(base),
    picked: '.'.repeat(OLD_COUNT),
    favorites: '0'.repeat(OLD_COUNT),
  }, extra, false);
  const r = restore(legacy);
  check(!r.ok && r.msg === 'legacy-3122-refused', '3,122 code must be refused as legacy, got: ' + r.msg);
}
// 6. Garbage is refused
{
  const r = restore('QH1.AAAA');
  check(!r.ok, 'garbage code must be refused');
  const r2 = restore('not-a-code');
  check(!r2.ok, 'non-QH code must be refused');
}

if (fail.length) { console.log('FAIL:\n- ' + fail.join('\n- ')); process.exit(1); }
console.log('OK: QH2 + QH1 round-trip, Android-shaped code, real web fixture, legacy refusal, garbage refusal');
