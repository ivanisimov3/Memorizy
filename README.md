# Memorizy Android

Android client for Memorizy, a mobile application for intelligent memorization using spaced repetition, answer checking, learning statistics, notifications, and remote synchronization.

## Tech Stack

- Kotlin, Jetpack Compose
- MVVM with repositories
- Room, DataStore
- Retrofit, OkHttp
- WorkManager
- Hilt
- ONNX Runtime for local NLP-assisted answer checking

## Configuration

The API base URL is configured at build time. For emulator development the default is:

```text
http://10.0.2.2:8080/
```

Override it with a Gradle property or environment variable:

```powershell
.\gradlew.bat :app:assembleDebug -PMEMORIZY_BASE_URL=https://your-server.example/
```

or:

```powershell
$env:MEMORIZY_BASE_URL="https://your-server.example/"
.\gradlew.bat :app:assembleDebug
```

## Useful Commands

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

## Notes

Room is the local source of truth. Local entities include synchronization metadata and learning progress fields because the application supports offline-first behavior.
