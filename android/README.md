# Quiz Hub — clean rebuild (android-clean)

Fresh from-scratch Android implementation of the agreed round-5 design
(soft ivory #F5F3EE, gold accents, deep-teal dark mode), plain Java, no
Gradle/libraries, minSdk 24, package com.quizhub.app. Built from the demo
spec (fixes-round-5/FIXES_REPORT.md) — no old code was copied over; the
previous tree under build-apk/ was read only as a reference for verified
behaviour (bank loading, audio playback, save/resume, backup codes).

Data (copied as-is, never edited): assets/bank (10 banks, 1,186 questions,
incl. the 15 lecturer keys), assets/audio (1,171 mp3s incl. the 5 fixed
clips r427/r853/r1144/r1166/r1173), assets/clip_map.json, fonts, branding,
sfx, launcher icons.

Build: JDK 17 on PATH, ANDROID_BUILD_TOOLS + ANDROID_JAR set, then
`tools/build.sh`. Tests: `tests/run_verify.sh` (Verify1, 14 tests, needs
RLIBS + ANDROID_JAR). R5/R7 items pending Mohamed's decision are preserved
exactly as in the previous build (settings detail paragraphs; source line +
generated-audio note in the explanation dialog) — see the R5/R7 comments in
Screens.java.

No GitHub writes, no uploads, no real-phone verification yet.
