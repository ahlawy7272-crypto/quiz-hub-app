# Quiz Hub

Nursing licensing exam practice, 1,186 questions in 10 tests, with a generated Egyptian-Arabic audio explanation for every question.

Two versions, kept separate:
- `android/`: the native Android app (offline, all audio inside). Package `com.quizhub.app`. Signed APKs are on the Releases page.
- `web/`: the browser (HTML) version.

See `android/README.md` and `web/README.md` for build steps. No signing keys or passwords are in this repository, only the public certificate in `android/signing/`.

## Backup and old progress compatibility
Backup codes transfer between the current web build and the current Android build (1,186 questions). There is no automatic migration of progress or notebook from the older 3,122-question build: the old question order is not available, so nothing is mapped. Old data is kept as raw text and can be exported from Settings, Backup. A 3,122-question code is refused and the current progress is left unchanged.
