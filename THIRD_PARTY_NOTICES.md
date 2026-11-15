# Сторонние компоненты и материалы

Исходный код приложения написан для этого репозитория. Архитектурные и дизайнерские примеры изучены, но их исходные файлы не копировались.

| Компонент / материал | Использование | Лицензия |
|---|---|---|
| Kotlin 2.1.20, kotlinx.coroutines (транзитивно) | runtime, Flow, suspend | Apache-2.0 |
| AndroidX Compose BOM 2025.04.01 / Material3 / UI / Animation | интерфейс | Apache-2.0 |
| AndroidX Activity 1.10.1, Lifecycle 2.9.0, Navigation 2.8.9, Core 1.16.0 | жизненный цикл, разрешения, навигация | Apache-2.0 |
| ExifInterface 1.4.0 | ориентация выбранных пользователем фото | Apache-2.0 |
| Room 2.7.0, SQLite AndroidX (транзитивно) | локальная БД | Apache-2.0 |
| DataStore 1.1.4 | настройки | Apache-2.0 |
| WorkManager 2.10.0 | фоновые обновления и напоминания | Apache-2.0 |
| Retrofit / converter-gson 2.11.0 | HTTP API | Apache-2.0 |
| OkHttp / Okio (транзитивно через Retrofit) | HTTP, ввод-вывод | Apache-2.0 |
| Gson 2.12.1 | JSON | Apache-2.0 |
| Manrope, Mikhail Sharanda и авторы | встроенный кириллический variable TTF | SIL OFL 1.1 |
| Lucide 0.468.0; Feather, Cole Bemis | SVG преобразованы в Android XML VectorDrawable, stroke 1.8 | ISC / MIT |
| Open-Meteo | только обращение к публичному API | Данные CC BY 4.0; free API non-commercial; исходный сервер AGPL не включён |

Полные тексты: [Apache 2.0](app/src/main/assets/licenses/Apache-2.0.txt), [Manrope OFL](app/src/main/assets/licenses/Manrope-OFL.txt), [Lucide / Feather](app/src/main/assets/licenses/Lucide-LICENSE.txt). Эти файлы также включены в APK.

**Weather data by Open-Meteo.com**, https://open-meteo.com/, [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/). Приложение форматирует прогноз и выбирает ближайший час; данные могут отличаться от наблюдений на месте. Использование сервиса не означает его одобрения приложения.

Изученные проекты и их лицензии, включая не включённый Lottie: [docs/RESEARCH.md](docs/RESEARCH.md).

94 цветные иллюстрации одежды и предметов в art_*.xml — оригинальные материалы этого проекта, генерируются tools/generate_catalog.py. В отличие от навигационных Lucide, они не заимствованы из внешних библиотек.
