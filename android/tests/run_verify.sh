#!/bin/bash
# Runs tests/Verify1.java (real assertions) in the Robolectric harness. Exits non-zero if any test fails.
# Needs: JDK 17 (JAVA_HOME or on PATH), the Robolectric 4.10.3 jars in $RLIBS (robolectric, junit 4.13.2, robolectric junit/sandbox/shadows/resources/utils/plugins jars,
#        nativeruntime, android-all-instrumented-13-robolectric-9030017-i4, asm, guava ...; all *.jar in that folder are used), android.jar in $ANDROID_JAR,
#        and a built unsigned APK (run tools/build.sh first; it leaves build/classes and build/QuizHub-unsigned.apk).
# Usage (from android/):  RLIBS=/path/to/robolectric/libs ANDROID_JAR=/path/to/android-34/android.jar tests/run_verify.sh
set -e
: "${RLIBS:?set RLIBS}"; : "${ANDROID_JAR:?set ANDROID_JAR}"
P="$(cd "$(dirname "$0")/.." && pwd)"; cd "$P"
[ -d build/classes ] || { echo "run tools/build.sh first" >&2; exit 1; }
T="$(mktemp -d)"; mkdir -p "$T/cls" "$T/cp/com/android/tools"
cat > "$T/cp/com/android/tools/test_config.properties" <<EOF
android_merged_manifest=$P/AndroidManifest.xml
android_merged_resources=$P/res
android_merged_assets=$P/assets
android_resource_apk=$P/build/QuizHub-unsigned.apk
android_custom_package=com.quizhub.app
EOF
LIBS="$(ls "$RLIBS"/*.jar | tr '\n' ':')"
javac -encoding UTF-8 -nowarn -cp "$LIBS$P/build/classes:$ANDROID_JAR" -d "$T/cls" tests/Verify1.java 2>&1 | grep -v '^Note' || true
set +e
java -Drobolectric.offline=true -Drobolectric.dependency.dir="$RLIBS" -cp "$LIBS$P/build/classes:$T/cls:$T/cp" org.junit.runner.JUnitCore com.quizhub.app.Verify1 > "$T/out.txt" 2>&1
RC=$?
grep -vE '^\s+at |System.logW|SLF4J|WARNING' "$T/out.txt" | tail -15
exit $RC
