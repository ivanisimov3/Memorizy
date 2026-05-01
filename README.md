# Memorizy Android

Android-клиент Memorizy - мобильное приложение для интеллектуального запоминания информации.

Приложение помогает создавать учебные наборы и карточки, повторять материал по расписанию, проверять ответы в режиме тестирования, смотреть статистику и синхронизировать данные с удаленным хранилищем.

## Возможности

* Регистрация и авторизация пользователя
* Создание учебных наборов и карточек
* Режим заучивания с использованием интервальных повторений
* Режим тестирования для смысловой проверки ответов
* Статистика учебных сессий и детализация по каточкам
* Уведомления о повторении
* Импорт и экспорт данных через CSV
* Offline-first хранение с отправкой/получением данных с сервера

## Технологический стек

* Kotlin
* Jetpack Compose
* Room
* DataStore
* Retrofit и OkHttp
* WorkManager
* Hilt
* ONNX Runtime для локальной NLP-проверки ответов

* Архитектура: MVVM, слой репозиториев, offline-first хранение данных

## Сервер

Клиент рассчитан на работу с REST API Memorizy Server. Базовый URL API задаётся во время сборки, поэтому один и тот же исходный код можно использовать как с локальным сервером, так и с собственным удалённым экземпляром.

По умолчанию для разработки в Android-эмуляторе используется:

```text
http://10.0.2.2:8080/
```

Для production/release-сборок рекомендуется использовать HTTPS.

## Конфигурация API

Базовый URL можно переопределить Gradle-свойством:

```powershell
.\gradlew.bat :app:assembleDebug -PMEMORIZY_BASE_URL=https://your-server.example/
```

Или переменной окружения:

```powershell
$env:MEMORIZY_BASE_URL="https://your-server.example/"
.\gradlew.bat :app:assembleDebug
```

URL должен оканчиваться символом `/`.

## Полезные команды

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

## Release APK

Release APK должен быть подписан локальным ключом. Ключ подписи и файл с паролями не должны попадать в git.

Создать ключ:

```powershell
New-Item -ItemType Directory -Force release

& 'C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe' -genkeypair -v `
  -keystore release/memorizy-release.jks `
  -alias memorizy `
  -keyalg RSA `
  -keysize 2048 `
  -validity 10000
```

Создать локальный файл подписи:

```powershell
Copy-Item release-signing.properties.example release-signing.properties
notepad release-signing.properties
```

Заполнить значения:

```properties
MEMORIZY_RELEASE_STORE_FILE=release/memorizy-release.jks
MEMORIZY_RELEASE_STORE_PASSWORD=your-keystore-password
MEMORIZY_RELEASE_KEY_ALIAS=memorizy
MEMORIZY_RELEASE_KEY_PASSWORD=your-key-password
```

Собрать подписанный APK:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleRelease -PMEMORIZY_BASE_URL=https://your-server.example/
```

Готовые APK появятся в:

```text
app/build/outputs/apk/release/
```

Для ручной установки удобнее использовать universal APK:

```text
app-universal-release.apk
```
