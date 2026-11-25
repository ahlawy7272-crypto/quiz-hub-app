// Bundles the web app into dist/ (index.html + app.js + app.css + a/ with fonts, images and audio).
// '@instinct/files' is the hosting platform's UI/storage kit; src/files-stub.tsx replaces it for a standalone build
// (progress is then kept in the browser's localStorage).
const fs = require('fs');
require('esbuild').build({
  entryPoints: ['src/main.tsx'], bundle: true, outfile: 'dist/app.js',
  alias: { '@instinct/files': './src/files-stub.tsx' },
  loader: { '.mp3': 'file', '.png': 'file', '.woff2': 'file', '.ttf': 'file', '.css': 'css' },
  assetNames: 'a/[name]-[hash]', define: { 'process.env.NODE_ENV': '"production"' }, jsx: 'automatic', logLevel: 'error'
}).then(() => { fs.copyFileSync('index.html', 'dist/index.html'); console.log('built dist/'); })
  .catch(e => { console.error(e.message); process.exit(1); });
