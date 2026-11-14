# S1 App

Aplicación Android en Kotlin y Jetpack Compose para la gestión de personal:
empleados, turnos, fichajes, solicitudes de tiempo libre, tareas, informes y
sincronización con Firebase/Firestore. La base de datos local Room permite
trabajar con el origen de datos local mientras se sincronizan los cambios.

## Requisitos

- JDK 17 (el proyecto compila el código de la aplicación con compatibilidad Java 11).
- Android Studio reciente con Android SDK 36.1 y Build Tools compatibles.
- Una conexión a Internet para descargar dependencias Gradle.
- Firebase configurado si se van a probar las funciones remotas. El archivo
  `google-services.json` no se versiona.

## Configuración

1. Clona el repositorio y abre la raíz en Android Studio.
2. Copia `.env.example` a `.env` y añade únicamente las claves necesarias para
   tu entorno local. `.env` está excluido de Git.
3. Configura Firebase con `google-services.json` en `app/` si necesitas Auth o
   Firestore. La compilación muestra una advertencia si falta.
4. No guardes credenciales ni claves de firma en el repositorio. Para una
   variante release usa `KEYSTORE_PATH`, `STORE_PASSWORD` y `KEY_PASSWORD`.

## Ejecución y validación

El repositorio incluye el Gradle Wrapper, por lo que los comandos reproducibles
se ejecutan desde la raíz:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew check
```

Los tests instrumentados requieren un emulador o dispositivo conectado:

```bash
./gradlew connectedDebugAndroidTest
```

El workflow de GitHub Actions ejecuta `check`, `lintDebug` y `assembleDebug` en
cada pull request y en los pushes a `main`. Las tareas que requieran un
dispositivo o una configuración real de Firebase no se ejecutan en CI.

## Estructura

```text
app/src/main/java/com/example/
├── data/       # Room, modelos, repositorios y sincronización
├── ui/         # Pantallas, componentes y tema Compose
└── MainActivity.kt
app/src/test/              # Tests unitarios y Robolectric
app/src/androidTest/       # Tests instrumentados
firestore.rules            # Reglas de seguridad de Firestore
FIRESTORE_SCHEMA.md        # Modelo y estrategia de sincronización
gradle/libs.versions.toml  # Versiones y dependencias centralizadas
```

## Contribución y seguridad

Consulta [CONTRIBUTING.md](CONTRIBUTING.md) antes de abrir un pull request.
Para reportar una vulnerabilidad, sigue las instrucciones de
[SECURITY.md](SECURITY.md). El proyecto está bajo desarrollo y no se deben
suponer garantías de disponibilidad para producción.
