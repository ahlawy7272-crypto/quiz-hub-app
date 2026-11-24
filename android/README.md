# Quiz Hub, native Android app

Offline nursing-exam question bank. 1,186 questions in 10 tests, each with a lecturer clip or a generated Egyptian-Arabic audio explanation (generated clips are labeled as generated, not the lecturer). Package `com.quizhub.app`, minSdk 24. Plain Java, no Gradle, no libraries.

Signed APKs are on the Releases page of this repo. Run the commands below from this `android/` folder.

## Layout
- `src/`, `res/`, `AndroidManifest.xml`: app code and resources.
- `assets/bank/`: question banks (bank-0..9.json, index.json). `assets/audio/`: clips (g* generated, r* lecturer; `clip_map.json` maps duplicate names to one stored file). `assets/fonts`, `assets/branding`, `assets/sfx`.
- `tools/build.sh`: builds an unsigned, aligned APK.
- `tests/`: Robolectric UI harness sources used for screenshots and state checks (a harness, not a device test).
- `signing/quizhub-release-cert.pem`: PUBLIC certificate only. SHA-256 of the release signer: 19feecae1df35141ba896216bc7837080bfd78bc6e645b3929243058787ce203. The private key is not in this repo.

## Build
Needs JDK 11+, Android SDK build-tools 34 (aapt2, d8, zipalign) and platform android-34.
```
ANDROID_BUILD_TOOLS=$ANDROID_HOME/build-tools/34.0.0 ANDROID_JAR=$ANDROID_HOME/platforms/android-34/android.jar tools/build.sh
apksigner sign --ks my.p12 --out QuizHub.apk build/QuizHub-unsigned.apk
```
An APK signed with another key cannot update over the release APK (uninstall first, after backing up progress in Settings).

## Notes
- Audio files must stay stored (uncompressed) in the APK. `tools/build.sh` does this.
- Progress backup format is the web-compatible `QH2` code (Settings).
- Tested in a Robolectric harness and with apksigner/zipalign. Real-phone playback was not confirmed at the time of this commit.
