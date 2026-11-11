# JIIT Timer

Temporizador HIIT para Android, reescrito desde el proyecto Java de 2018 a un stack moderno de Kotlin y Jetpack Compose.

La app permite configurar programas de entrenamiento (preparación, trabajo, descanso, rondas y enfriamiento), guardarlos, iniciar sesiones con pausa y salto de intervalo, y mantener el temporizador vivo en segundo plano con notificación y controles.

| Configuración | Sesión en pausa |
|---|---|
| ![Pantalla de configuración](docs/setup.png) | ![Temporizador en pausa](docs/timer-paused.png) |

## Funcionalidad

- Programas reutilizables con nombre único: crear, renombrar, eliminar y cambiar de programa.
- Tiempos configurables con límites seguros: preparación, trabajo, descanso, rondas y enfriamiento.
- Descanso solo entre rondas, nunca después de la última.
- Total de sesión calculado en tiempo real.
- Motor de tiempo basado en `SystemClock.elapsedRealtime()`: sin deriva por ticks acumulados y con recuperación si el hilo principal se retrasa.
- Pausa, reanudar, saltar intervalo, detener y repetir el entrenamiento.
- Pantalla de finalización con duración total real.
- Servicio en primer plano con notificación persistente y acciones (pausa, continuar, saltar, detener).
- Wake lock parcial durante la sesión y `FLAG_KEEP_SCREEN_ON` mientras la UI está visible.
- Avisos sonoros con `SoundPool`, foco de audio temporal y patrones de vibración distintos para trabajo, descanso y final.
- Ajustes de sonido y vibración persistidos.
- Tema claro/oscuro de Material 3, diseño adaptativo para vertical, horizontal y pantallas anchas.
- Textos en inglés y español, contenido accesible y etiquetas de test.
- Migración automática de los programas del formato antiguo `SharedPreferences`/JSON.
- Persistencia con DataStore y JSON versionado; los datos corruptos se recuperan con valores por defecto en lugar de romper el arranque.

## Stack

| Componente | Versión |
|---|---|
| Kotlin | 2.2.21 |
| Android Gradle Plugin | 8.13.2 |
| Gradle | 8.13 |
| JDK | 17 o 21 (probado con 21) |
| compileSdk / targetSdk | 36 |
| minSdk | 24 |
| Jetpack Compose | BOM 2025.10.01, Material 3 |
| Persistencia | DataStore Preferences + kotlinx.serialization |
| Concurrencia | Coroutines + StateFlow |
| Tests | JUnit4, Truth, kotlinx-coroutines-test, Compose UI Test |
| CI | GitHub Actions |

## Arquitectura

```text
MainActivity
  └── JiitTimerAppScreen (Compose)
        └── MainViewModel
              ├── ProgramRepository ── DataStore (programas + ajustes)
              └── TimerEngine ── StateFlow<TimerState>
                    ├── WorkoutTimeline (prepare → work → rest → … → cooldown)
                    ├── WorkoutCuePlayer (SoundPool + vibración)
                    └── WorkoutTimerService (foreground service + notificación + wake lock)
```

Puntos clave:

- `TimerEngine` es código Kotlin puro, sin dependencias de Android, y se prueba con un reloj virtual. Deriva todo del tiempo transcurrido absoluto, no de sumar ticks.
- La sesión vive en el proceso de la aplicación, no en la Activity. Girar la pantalla o cerrar la UI no detiene el entrenamiento.
- `WorkoutTimerService` solo promueve el proceso a primer plano, publica la notificación y mantiene la CPU despierta. Las acciones de la notificación envían comandos al motor.
- `ProgramRepository` es la única fuente de verdad de los programas. Cada cambio se refleja de inmediato en memoria y se serializa con un escritor único.
- `ProgramsCodec` valida y normaliza todo lo leído. Si el JSON está corrupto, el repositorio intenta el formato antiguo y, si falla, arranca con el programa por defecto. La app ya no puede quedar inutilizable por un campo vacío.

### Migración desde 1.0

Al primer arranque, si DataStore está vacío, se lee el archivo antiguo:

```text
SharedPreferences: com.profesorfalken.jiittimer.JiitWorkoutData
Clave: WorkoutsData
Formato: [{"Title":"Default","Data":"30|10|60|3"}]
```

Cada fila se valida por separado. Las filas malformadas se ignoran, los nombres duplicados se renombran y los valores fuera de rango se ajustan. Los programas migrados reciben 5 segundos de preparación, que el modelo antiguo no tenía.

## Compilar

Requisitos:

- JDK 17 o 21.
- Android SDK con `platforms;android-36` y `build-tools;36.0.0`.
- `local.properties` con `sdk.dir=/ruta/al/Android/Sdk` (o `ANDROID_HOME` definido).

```bash
./gradlew assembleDebug
```

APK de depuración:

```text
app/build/outputs/apk/debug/app-debug.apk
```

El `applicationId` de debug lleva el sufijo `.debug`, por lo que puede convivir con la versión de release. Para release:

```bash
./gradlew assembleRelease
```

El build de release aplica R8 y `shrinkResources`. El APK resultante no está firmado; hay que configurar un keystore de release antes de distribuirlo.

## Tests y calidad

```bash
./gradlew testDebugUnitTest        # lógica de tiempo, timeline, codec, migración y formato
./gradlew connectedDebugAndroidTest # flujo Compose en emulador/dispositivo
./gradlew lintDebug                 # Android Lint
./gradlew assembleRelease           # R8 + shrinkResources
```

La suite unitaria cubre:

- Formato de tiempos y redondeo de cuenta atrás.
- Construcción de la timeline y límites entre intervalos.
- Conteo, pausa, reanudación, salto, parada y finalización del motor.
- Cues de intervalo y cuenta atrás 3-2-1.
- Código de la migración antigua, incluyendo datos corruptos y duplicados.
- Serialización versionada, tolerancia a campos desconocidos y reparación de IDs.

Los tests instrumentados verifican que la pantalla de configuración aparece, que una sesión arranca y se detiene, y que se puede crear un programa desde la UI.

CI: [`.github/workflows/android.yml`](.github/workflows/android.yml) compila, ejecuta tests unitarios, lint y sube el APK; un segundo job ejecuta los tests instrumentados en un emulador API 35.

## Estructura

```text
app/src/main/java/com/profesorfalken/jiittimer/
├── MainActivity.kt
├── JiitTimerApp.kt              # Application + AppContainer + canal de notificación
├── data/
│   ├── ProgramRepository.kt     # fuente de verdad + DataStore
│   ├── ProgramsCodec.kt         # JSON versionado y validación
│   └── LegacyWorkoutParser.kt   # migración del formato 2018
├── model/
│   ├── WorkoutProgram.kt
│   ├── UserSettings.kt
│   └── TimeFormat.kt
├── timer/
│   ├── TimerEngine.kt
│   ├── TimerState.kt
│   ├── WorkoutTimeline.kt
│   └── WorkoutCuePlayer.kt
├── audio/
│   └── AndroidCuePlayer.kt
├── service/
│   └── WorkoutTimerService.kt
└── ui/
    ├── MainScreen.kt
    ├── MainViewModel.kt
    └── theme/
```

## Permisos

- `POST_NOTIFICATIONS` (Android 13+): se solicita al iniciar el primer entrenamiento. Si se deniega, el servicio sigue funcionando pero la notificación puede no mostrarse en el cajón.
- `FOREGROUND_SERVICE` y `FOREGROUND_SERVICE_SPECIAL_USE`: servicio en primer plano de tipo `specialUse` para un temporizador de entrenamiento.
- `WAKE_LOCK`: mantener la CPU despierta durante la sesión.
- `VIBRATE`: avisos hápticos.

La app no usa red, no pide permisos de ubicación ni almacenamiento y no envía datos a ningún servidor.

## Verificación

Estado verificado en esta modernización (11 de noviembre de 2026):

- `./gradlew assembleDebug`: correcto.
- `./gradlew testDebugUnitTest`: 34 tests correctos (lógica de tiempo, timeline, motor, migración, códec, formato y smoke test Robolectric de la Activity).
- `./gradlew lintDebug`: 0 errores y 0 avisos.
- `./gradlew assembleRelease`: correcto con R8 y reducción de recursos.
- Prueba manual en emulador API 28: arranque, inicio de sesión, pausa, salto de intervalo, reanudación, parada, notificación persistente y limpieza del servicio.
- `./gradlew connectedDebugAndroidTest`: 3/3 tests correctos en emulador API 28 (detalle en [`docs/VERIFICATION.md`](docs/VERIFICATION.md)).

## Limitaciones conocidas

- Una sesión activa no se restaura si el proceso muere por completo; el servicio en primer plano y el wake lock están pensados para evitarlo durante el uso normal.
- El modelo actual permite un único trabajo y un único descanso por programa. No incluye secuencias de ejercicios distintos, historial ni estadísticas.
- El APK de release generado por CI no está firmado.
- Para publicar en Google Play habrá que declarar el uso del servicio en primer plano de tipo `specialUse` en la ficha de la app.
- No hay licencia definida todavía en el repositorio; añádela antes de redistribuir.
