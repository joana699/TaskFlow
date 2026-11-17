# TaskFlow

Automatizador de tareas con Kotlin, Room y WorkManager (MVVM).

## Cómo abrirlo
1. Android Studio (Ladybug o superior) → *Open* → carpeta `TaskFlow`.
2. Dejar que Gradle sincronice (genera el wrapper y descarga dependencias).
   Si alguna versión de `app/build.gradle.kts` no se resuelve, bájala a la última estable.
3. Ejecutar en un emulador o celular (API 27+).

## Probar rápido
- Crear tarea con **"Ejecutar una sola vez"** y 1 minuto de espera.
- Periódicas: mínimo 15 minutos (límite de WorkManager).
- `TaskWorker.FAILURE_PERCENT` controla los fallos simulados (Reto 5): 0 = nunca, 100 = siempre.
