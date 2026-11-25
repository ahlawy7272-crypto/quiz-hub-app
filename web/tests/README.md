# Web checks

`python3 tests/check_audio.py` (needs node and ffmpeg): asserts 1,186 questions, ids 0..1185 once each, an audio key for every question, every file present and decodable, generated clips match the generated set. Exit code 1 on failure.

`node tests/backup_roundtrip.js` (Node builtins only): round-trips a 1,186-question QH2 backup code (and the QH1 fallback) through the shared web/Android backup format, proves an Android-shaped code is accepted by the web validation rules, decodes the real web fixture `../android/tests/fixtures/web_qh2_code.txt`, and checks that a 3,122-question code and garbage are refused. Exit code 1 on failure.

What `backup_roundtrip.js` does and does not prove: it re-implements the validation rules of App.tsx `restoreBackup` and Android `Backup.restore` (same version, length, range and legacy checks) in plain Node — it does not run either restore function itself. The Android side of the exchange is proven in-repo by `android/tests/Verify1.java` (`webBackupRestoresOnAndroid`, `androidBackupRoundTripAndLegacyRefusal`). It does not prove playback, UI behaviour, or a restore on a phone.

Browser flows (exam, results, notebook, flag, reload, backup restore, old 3,122-question state) were run in headless Chrome during the repair round; those scripts are not in this repo.
