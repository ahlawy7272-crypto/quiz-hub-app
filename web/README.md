# Quiz Hub, web version (HTML)

The browser version of Quiz Hub: 10 tests, 1,186 questions, 1,185 audio explanation files (`src/audio`), same design as the Android app.

## Build
```
npm install
npm run build        # writes dist/ (index.html, app.js, app.css, a/ assets)
```
Serve `dist/` with any static web server (for example `npx serve dist` or `python3 -m http.server -d dist`). Audio is loaded on demand when you tap the speaker button after answering.

## Notes
- `src/files-stub.tsx` stands in for the hosting platform's `@instinct/files` package, so progress is saved in the browser's localStorage here. On the original hosted page the platform's own storage is used.
- Question data is in `src/bank-data.ts`, audio mapping in `src/audio-map.ts`.
- This folder is the source that was prepared for the web version. It was built successfully with esbuild when this repo was assembled. The exact hosted page on files.instinct.com was not re-opened to compare it side by side.
