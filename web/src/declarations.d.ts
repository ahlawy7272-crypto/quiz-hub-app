// Type declarations for asset imports that esbuild resolves as files
// (see build.js loaders). These are type-only; they do not affect the build.
declare module '*.mp3' {
  const src: string;
  export default src;
}
declare module '*.png' {
  const src: string;
  export default src;
}
declare module '*.woff2' {
  const src: string;
  export default src;
}
declare module '*.ttf' {
  const src: string;
  export default src;
}
