#!/bin/bash
# Builds the unsigned, aligned APK. Needs: JDK 11+ (javac), zip, Android SDK build-tools (aapt2, d8, zipalign) and platform android.jar.
# Usage: ANDROID_BUILD_TOOLS=/path/to/build-tools/34.0.0 ANDROID_JAR=/path/to/platforms/android-34/android.jar tools/build.sh
set -eu
for t in zip javac; do command -v "$t" >/dev/null || { echo "missing prerequisite: $t" >&2; exit 1; }; done
: "${ANDROID_BUILD_TOOLS:?set ANDROID_BUILD_TOOLS}"; : "${ANDROID_JAR:?set ANDROID_JAR}"
for t in aapt2 d8 zipalign; do [ -x "$ANDROID_BUILD_TOOLS/$t" ] || { echo "missing tool: $ANDROID_BUILD_TOOLS/$t (check ANDROID_BUILD_TOOLS)" >&2; exit 1; }; done
P="$(cd "$(dirname "$0")/.." && pwd)"; BT="$ANDROID_BUILD_TOOLS"; AJ="$ANDROID_JAR"; O="$P/build"
rm -rf "$O"; mkdir -p "$O/gen" "$O/classes" "$O/dex"
"$BT/aapt2" compile --dir "$P/res" -o "$O/res.zip"
"$BT/aapt2" link -o "$O/unsigned.apk" --manifest "$P/AndroidManifest.xml" -I "$AJ" --java "$O/gen" --min-sdk-version 24 --target-sdk-version 35 "$O/res.zip"
find "$P/src" "$O/gen" -name '*.java' > "$O/s.txt"
javac -encoding UTF-8 --release 8 -nowarn -cp "$AJ" -d "$O/classes" @"$O/s.txt"
find "$O/classes" -name '*.class' > "$O/c.txt"
"$BT/d8" --min-api 24 --output "$O/dex" @"$O/c.txt"
cp "$O/unsigned.apk" "$O/al.apk"
(cd "$O" && zip -q -0 al.apk -j dex/classes.dex)
# non-audio assets compressed; audio AND sfx STORED (MediaPlayer can only open a clip
# straight from the APK when it is uncompressed — compressed sfx used to make
# openFd() throw, so the correct/wrong sounds silently never played)
(cd "$P" && for d in assets/*; do [ "$d" = "assets/audio" ] && continue; [ "$d" = "assets/sfx" ] && continue; zip -q -r -9 "$O/al.apk" "$d"; done)
(cd "$P" && zip -q -r -0 "$O/al.apk" assets/audio assets/sfx)
"$BT/zipalign" -f 4 "$O/al.apk" "$O/QuizHub-unsigned.apk"
echo "Built $O/QuizHub-unsigned.apk. Sign it with your own key: apksigner sign --ks YOUR.p12 --out QuizHub.apk $O/QuizHub-unsigned.apk"
