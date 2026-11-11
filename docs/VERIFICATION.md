# Modernization verification

Date: November 11, 2026.

## Environment

| Item | Value |
|---|---|
| Host | Omarchy Linux x86_64, 16 CPUs, 31 GB RAM |
| JDK | Temurin 21.0.13 |
| Gradle | 8.13 (wrapper) |
| Android Gradle Plugin | 8.13.2 |
| Kotlin | 2.2.21 |
| compileSdk / targetSdk | 36 |
| Android SDK Build-Tools | 36.0.0 |
| Emulator | Android 28 x86, `-accel off` (no `/dev/kvm`) |

The toolchain was installed in user space under `.toolchain/` because the container does not allow `sudo` and `$HOME` is read-only. The project uses `local.properties` to point at the local SDK; that file is ignored by Git.

## Automated results

| Check | Command | Result |
|---|---|---|
| Debug build | `./gradlew assembleDebug` | ✅ |
| Unit tests | `./gradlew testDebugUnitTest` | ✅ 34 tests, 0 failures |
| Lint | `./gradlew lintDebug` | ✅ `No issues found` (0 errors, 0 warnings) |
| Release + R8 | `./gradlew assembleRelease` | ✅ 1.8 MB APK |
| Instrumented test compilation | `./gradlew assembleDebugAndroidTest` | ✅ |
| Instrumented tests | `./gradlew connectedDebugAndroidTest` | ✅ 3/3 on API 28 emulator |
| GitHub Actions | `master` and `modernize/kotlin-compose` | ✅ build, unit tests and lint |

Breakdown of the 34 unit tests:

- 6 for legacy migration, including corrupt rows, duplicates and extreme values.
- 5 for the versioned JSON codec and recovery from invalid data.
- 5 for workout program validation and total-time calculation.
- 4 for time formatting and countdown rounding.
- 6 for timeline construction and interval lookup.
- 7 for the engine: counting, pause, resume, skip, stop, finish and 3-2-1 cues.
- 1 Robolectric smoke test that creates the Activity and composes the UI on the JVM.

The instrumented tests cover:

1. The setup screen appears, a workout starts and resets.
2. A new program is created from the dialog.
3. A 5-second program runs through the complete session and reaches the completion screen.

## Manual verification on the emulator

Flow executed on the debug APK:

1. Cold start with the default program (5 s prepare, 30 s work, 10 s rest, 8 rounds, 60 s cool down; total 06:15).
2. Workout start and persistent notification:
   - Title: `Work · Round 1/8`.
   - Text: `00:20 · 06:00 left`.
   - Actions: `Pause`, `Skip`, `Stop`.
3. Pause:
   - The UI shows `Paused`, the time freezes and the button changes to `Resume`.
   - The notification's primary action changes to `Resume`.
4. Skip while paused:
   - Moves from `Rest · Round 1/8` to `Work · Round 2/8`.
   - The state remains paused.
5. Resume:
   - The notification shows `Pause` again and time advances.
6. Stop:
   - The confirmation dialog opens; confirming returns to setup.
   - The foreground service stops and the notification is removed.
7. Real screenshots:
   - `docs/setup.png`: compact program header, workout settings section and fixed start button.
   - `docs/timer.png`: work interval.
   - `docs/timer-paused.png`: paused work interval with the active notification.

## Emulator environment notes

The host has no `/dev/kvm`, so the x86 emulator ran with software CPU emulation (`-accel off`). A cold boot takes around 10 minutes. During testing, an intermediate run of the instrumented suite aborted with `INSTRUMENTATION_ABORTED: System has crashed` because the emulator's `system_server` became unstable under load; the emulator was restarted and the final run of all 3 instrumented tests finished with 0 failures.

That failure belongs to the emulator's Android system, not the app: unit tests, lint, debug and release builds ran repeatedly without errors, and the final instrumented suite passed completely. On a host with KVM or a physical device, execution is much faster and more stable.

## Reproduce

```bash
./gradlew assembleDebug testDebugUnitTest lintDebug assembleRelease
# with a connected emulator or device:
./gradlew connectedDebugAndroidTest
```
