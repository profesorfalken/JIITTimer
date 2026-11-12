# JIIT Timer

[![Android CI](https://github.com/profesorfalken/JIITTimer/actions/workflows/android.yml/badge.svg?branch=master)](https://github.com/profesorfalken/JIITTimer/actions/workflows/android.yml)

JIIT Timer is a simple, offline HIIT interval timer for Android. Configure a program, press start, and focus on the workout: the app keeps track of work, rest, rounds and cool down, with sound and vibration cues and a persistent notification.

It is built with Kotlin and Jetpack Compose and works without an account, an internet connection, ads or tracking.

| Setup | Workout paused |
|---|---|
| ![Setup screen](docs/setup.png) | ![Paused workout](docs/timer-paused.png) |

## Features

- Create, rename, delete and switch reusable workout programs.
- Configure prepare time, work time, rest time, rounds and cool down.
- Rest between rounds, using the actual elapsed time for accurate countdowns.
- Pause, resume, skip an interval, stop or repeat a workout.
- Sound cues with audio focus and distinct vibration patterns for work, rest and finish.
- Persistent notification with Pause, Resume, Skip and Stop actions.
- Keeps running when the screen is off or the app is in the background.
- Light and dark Material 3 themes, responsive layouts and English/Spanish UI.
- All data stays on the device.

## How it works

The UI is a single Jetpack Compose screen driven by a ViewModel. Programs and settings are stored locally with DataStore. A pure Kotlin timer engine derives the current interval and remaining time from `SystemClock.elapsedRealtime()`, so delayed ticks do not make the workout drift. When a session starts, a foreground service keeps the process alive, shows the notification, holds a partial wake lock and sends notification actions back to the timer engine.

The session lives in the application process rather than the Activity, so rotating the screen or leaving the UI does not reset the workout. The previous 2018 data format is migrated automatically on first launch.

## Install

For now, build and install the APK from source:

```bash
git clone https://github.com/profesorfalken/JIITTimer.git
cd JIITTimer
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The debug build uses the application ID `com.profesorfalken.jiittimer.debug`, so it can coexist with a release build. A Google Play release is planned.

## Build

Requirements:

- JDK 17 or 21.
- Android SDK with `platforms;android-36` and `build-tools;36.0.0`.
- `local.properties` with `sdk.dir=/path/to/Android/Sdk`, or `ANDROID_HOME` set.

```bash
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

For a release build, create `keystore.properties` from `keystore.properties.example` and point it at your upload keystore:

```properties
storeFile=/absolute/path/to/upload-keystore.jks
storePassword=change-me
keyAlias=upload
keyPassword=change-me
```

Then build an APK or an Android App Bundle:

```bash
./gradlew assembleRelease
./gradlew bundleRelease
```

Outputs:

```text
app/build/outputs/apk/release/app-release.apk
app/build/outputs/bundle/release/app-release.aab
```

`keystore.properties` and keystores are ignored by Git. Never commit them.

See [docs/PUBLISHING.md](docs/PUBLISHING.md) for the complete Google Play publishing steps.

## Tests

```bash
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest  # emulator or device required
./gradlew lintDebug
```

## Tech stack

- Kotlin 2.2.21, Jetpack Compose, Material 3
- Android Gradle Plugin 8.13.2, Gradle 8.13
- compileSdk/targetSdk 36, minSdk 24
- DataStore Preferences and kotlinx.serialization
- Coroutines and StateFlow
- JUnit4, Truth, Robolectric and Compose UI Test

## Privacy

JIIT Timer does not use the network, does not request location or storage permissions, and does not collect or share personal data. See the [privacy policy](docs/privacy-policy.html) for the full statement.

## License

No license has been chosen for this project yet. Contact the author before reusing or redistributing the code.
