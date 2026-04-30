# Memorizy Android

Android-клиент Memorizy - мобильное приложение для интеллектуального запоминания с использованием интервального повторения, проверки ответов, статистики обучения, уведомлений и удалённого хранения данных.

## Технологический стек

* Kotlin, Jetpack Compose
* MVVM с репозиториями
* Room, DataStore
* Retrofit, OkHttp
* WorkManager
* Hilt
* ONNX Runtime для локальной NLP-проверки ответов

## Конфигурация

Базовый URL API задаётся во время сборки. Для разработки в эмуляторе по умолчанию используется:

```text
http://10.0.2.2:8080/
```

Его можно переопределить с помощью Gradle-свойства или переменной окружения:

```powershell
.\gradlew.bat :app:assembleDebug -PMEMORIZY_BASE_URL=https://your-server.example/
```

или:

```powershell
$env:MEMORIZY_BASE_URL="https://your-server.example/"
.\gradlew.bat :app:assembleDebug
```

## Полезные команды

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

## Примечания

Room является локальным источником истины. Локальные сущности включают метаданные синхронизации и поля прогресса обучения, поскольку приложение поддерживает offline-first подход.
