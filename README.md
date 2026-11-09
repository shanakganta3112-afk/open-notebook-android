# Open Notebook for Android

An Android client for [Open Notebook](https://github.com/lfnovo/open-notebook), the open-source
NotebookLM alternative.

**This is a client, not a port of the server.** Open Notebook is a server application: a FastAPI
backend, a background worker, a SurrealDB database and a Next.js frontend, all shipped as Docker
images for Linux, macOS and Windows. None of that can run inside an Android app — there is no
SurrealDB build for Android and no Android wheels for its Python native dependencies. So the
server stays on your computer or a VPS, and this app is the phone in front of it.

## What the app does

- Connect to a server by URL, with the optional `OPEN_NOTEBOOK_PASSWORD` (sent as `Bearer`).
- Browse notebooks, create a notebook.
- Read a notebook's sources and notes; add a note from the phone.
- Full-text search scoped to the notebook.

Ask/chat is not wired up yet: `POST /api/search/ask/simple` requires three model IDs configured on
the server, so it needs a model picker first.

## Running the server

On your computer:

```bash
curl -o docker-compose.yml https://raw.githubusercontent.com/lfnovo/open-notebook/main/docker-compose.yml
# set OPEN_NOTEBOOK_ENCRYPTION_KEY in docker-compose.yml to a long random string
docker compose up -d
```

The REST API listens on port **5055**. The phone must reach it over your LAN, so use your
computer's LAN address (for example `http://192.168.1.10:5055`), not `localhost`. From the Android
emulator, `10.0.2.2` is the host machine.

## Building

Requires JDK 17 and the Android SDK (compileSdk 35).

```bash
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/app-debug.apk`. CI does the same on every push and
uploads the APK as a build artifact, then installs and launches it on an emulator.

## Install on a phone

1. Download `open-notebook-android-debug-apk` from the latest green GitHub Actions run.
2. Unzip, copy `app-debug.apk` to the phone, and allow installing from unknown sources.
3. Open the app, enter your server URL and password, tap **Save and test connection**.

The debug APK is signed with the standard debug key — fine for sideloading, not for Play Store
distribution.

## Layout

```
app/src/main/java/ai/opennote/android/
  MainActivity.kt
  data/     API interface, DTOs, Retrofit factory, settings store, repository
  ui/       Compose screens: connect, notebooks, notebook detail (sources/notes/search)
app/src/test/java/ai/opennote/android/data/
  NotebookRepositoryTest.kt   MockWebServer tests for every API call
```

## Licence

MIT, matching upstream Open Notebook.
