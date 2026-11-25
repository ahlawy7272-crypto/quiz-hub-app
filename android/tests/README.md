# Android tests

`Verify1.java` has real assertions (the run fails if one breaks): saved-state cleanup (ids from other builds, unknown bank), restore of a code made by the web build (`fixtures/web_qh2_code.txt`), Android backup round trip plus refusal of 3,122-question codes, and a clip for every one of the 1,186 questions.

It runs in the Robolectric harness (JDK 17, robolectric 4.10.3, android-all 13). Not a phone test. Run from `android/`, with the built classes, the full unsigned APK as `android_resource_apk`, and `-Drobolectric.offline=true`.

The other files here (`Shot*.java`, `Persist1`, `Audit*`) print values and write screenshots; they do not assert.

Web: `python3 web/tests/check_audio.py` (needs node, ffmpeg) asserts 1,186 questions, an audio key for every question, every file present and decodable.

## Reproducing the assertion tests
```
cd android
ANDROID_BUILD_TOOLS=... ANDROID_JAR=... tools/build.sh            # leaves build/classes and build/QuizHub-unsigned.apk
RLIBS=/folder/with/robolectric-jars ANDROID_JAR=... tests/run_verify.sh   # exit code 0 = all pass
```
`RLIBS` must hold the Robolectric 4.10.3 jars (robolectric, its junit/sandbox/shadows/resources/utils/plugins modules, junit 4.13.2, nativeruntime, android-all-instrumented-13-robolectric-9030017-i4, asm, guava and their dependencies). Tests: saved-state cleanup with the old state kept, web QH2 code restore, Android code round trip, old 3,122-question code conversion, font and question-file failure paths, a clip for every question. This is a harness, not a phone.
