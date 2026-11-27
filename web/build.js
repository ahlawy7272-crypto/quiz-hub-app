// Bundles the clean web app into dist/ (index.html + app.js + app.css + a/
// with fonts, images and audio). Standalone build: progress lives in the
// browser's localStorage, no hosting-platform kit involved.
const fs = require('fs');
require('esbuild').build({
  entryPoints: ['src/main.tsx'], bundle: true, minify: true, outfile: 'dist/app.js',
  loader: { '.mp3': 'file', '.png': 'file', '.woff2': 'file', '.ttf': 'file', '.css': 'css' },
  assetNames: 'a/[name]-[hash]', define: { 'process.env.NODE_ENV': '"production"' }, jsx: 'automatic', logLevel: 'error'
}).then(() => { fs.copyFileSync('index.html', 'dist/index.html'); console.log('built dist/'); })
  .catch(e => { console.error(e.message); process.exit(1); });
