# Web checks

`python3 tests/check_audio.py` (needs node and ffmpeg): asserts 1,186 questions, ids 0..1185 once each, an audio key for every question, every file present and decodable, generated clips match the generated set. Exit code 1 on failure.

Browser flows (exam, results, notebook, flag, reload, backup restore, old 3,122-question state) were run in headless Chrome during the repair round; those scripts are not in this repo.
