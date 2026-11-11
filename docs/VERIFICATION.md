# Verificación de la modernización

Fecha: 11 de noviembre de 2026.

## Entorno

| Elemento | Valor |
|---|---|
| Host | Omarchy Linux x86_64, 16 CPUs, 31 GB RAM |
| JDK | Temurin 21.0.13 |
| Gradle | 8.13 (wrapper) |
| Android Gradle Plugin | 8.13.2 |
| Kotlin | 2.2.21 |
| compileSdk / targetSdk | 36 |
| Android SDK Build-Tools | 36.0.0 |
| Emulador | Android 28 x86, `-accel off` (sin `/dev/kvm`) |

El entorno se instaló en espacio de usuario bajo `.toolchain/` porque el contenedor no permite `sudo` y el directorio `$HOME` es de solo lectura. El proyecto usa `local.properties` para apuntar al SDK local; ese archivo está ignorado por Git.

## Resultados automáticos

| Comprobación | Comando | Resultado |
|---|---|---|
| Compilación debug | `./gradlew assembleDebug` | ✅ |
| Tests unitarios | `./gradlew testDebugUnitTest` | ✅ 34 tests, 0 fallos |
| Lint | `./gradlew lintDebug` | ✅ `No issues found` (0 errores, 0 warnings) |
| Release + R8 | `./gradlew assembleRelease` | ✅ APK de 1,7 MB |
| Compilación de tests instrumentados | `./gradlew assembleDebugAndroidTest` | ✅ |
| Tests instrumentados | `./gradlew connectedDebugAndroidTest` | ✅ 3/3 en emulador API 28 |

Detalle de los 34 tests unitarios:

- 6 de migración del formato antiguo, incluyendo filas corruptas, duplicados y valores extremos.
- 5 del códec JSON versionado y su recuperación ante datos inválidos.
- 5 de validación y cálculo del modelo de programa.
- 4 de formato de tiempos y redondeo.
- 6 de construcción y localización de la timeline.
- 7 del motor: conteo, pausa, reanudación, salto, parada, finalización y cues 3-2-1.
- 1 smoke test Robolectric que crea la Activity y compone la UI en JVM.

Los tests instrumentados cubren:

1. La pantalla de configuración aparece, una sesión arranca y se detiene.
2. Se crea un programa nuevo desde el diálogo.
3. Un programa de 5 segundos recorre la sesión completa y llega a la pantalla de finalización.

## Verificación manual en emulador

Flujo ejecutado sobre el APK de debug:

1. Arranque en frío con programa por defecto (5 s preparación, 30 s trabajo, 10 s descanso, 8 rondas, 60 s enfriamiento; total 06:15).
2. Inicio de sesión y notificación persistente:
   - Título: `Work · Round 1/8`.
   - Texto: `00:20 · 06:00 left`.
   - Acciones: `Pause`, `Skip`, `Stop`.
3. Pausa:
   - La UI muestra `PAUSED`, el tiempo se congela y el botón cambia a `Resume`.
   - La notificación cambia su acción principal a `Resume`.
4. Salto de intervalo estando en pausa:
   - Pasa de `Rest · Round 1/8` a `Work · Round 2/8`.
   - El estado sigue en pausa.
5. Reanudación:
   - La notificación vuelve a mostrar `Pause` y el tiempo avanza.
6. Parada:
   - Se abre el diálogo de confirmación; al confirmar vuelve la configuración.
   - El servicio en primer plano se detiene y la notificación se elimina.
7. Capturas reales:
   - `docs/setup.png`: pantalla de configuración con botón de inicio fijo.
   - `docs/timer.png`: intervalo de preparación.
   - `docs/timer-paused.png`: descanso en pausa, con notificación activa.

## Notas sobre el entorno de emulación

El host no tiene `/dev/kvm`, por lo que el emulador x86 se ejecutó con emulación de CPU por software (`-accel off`). El arranque tarda aproximadamente 10 minutos. Durante las pruebas, una repetición intermedia de la suite abortó con `INSTRUMENTATION_ABORTED: System has crashed` porque el `system_server` del emulador se volvió inestable bajo carga; se reinició el emulador y la ejecución final de los 3 tests terminó con 0 fallos.

Ese fallo es del sistema Android del emulador, no de la app: los tests unitarios, lint, build debug y release se ejecutaron repetidamente sin errores, y la suite instrumentada final pasó completa. En un host con KVM o en un dispositivo físico la ejecución es mucho más rápida y estable.

## Reproducir

```bash
./gradlew assembleDebug testDebugUnitTest lintDebug assembleRelease
# con un emulador o dispositivo conectado:
./gradlew connectedDebugAndroidTest
```
