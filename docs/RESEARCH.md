# Исследование GitHub

Дата просмотра: 15 ноября 2026. Прочитаны metadata GitHub API, README и перечисленные исходные материалы. Даты pushed_at подтверждают активность репозитория на момент просмотра, но не являются оценкой качества каждого коммита. Зависимости приложения намеренно закреплены на совместимом стабильном наборе, а не на текущих ветках этих проектов.

| Репозиторий | Лицензия | Что изучено и принято |
|---|---|---|
| [android/nowinandroid](https://github.com/android/nowinandroid), push 2026-11-15 | Apache-2.0 | README, `docs/ArchitectureLearningJourney.md`, `OfflineFirstNewsRepository.kt`: единственный источник чтения — локальная база, однонаправленное состояние Flow, отдельные data/domain/UI, неблокирующее фоновое обновление. Использованы в нашем Repository, Room Flow, AppState и Worker. Код образца не копировался. |
| [android/compose-samples](https://github.com/android/compose-samples), push 2026-11-09 | Apache-2.0 | README, Jetsnack `ui/theme/Theme.kt`: семантические токены цветов и типографики, контрастные светлая/тёмная темы, мягкие поверхности. Использованы свой BabyTheme, Surface-карточки, сквозная Material 3 навигация и нативные BottomSheet. |
| [airbnb/lottie-android](https://github.com/airbnb/lottie-android), push 2026-02-15 | Apache-2.0 | README, Compose `animateLottieCompositionAsState.kt`: декларативное управление анимацией. Для прогресса и навигации достаточно Compose `animateFloatAsState`, `AnimatedContent`, переходов NavHost: Lottie не добавлялся ради лишней зависимости и отсутствующих авторских motion-ресурсов. |
| [lucide-icons/lucide](https://github.com/lucide-icons/lucide), push 2026-11-15 | ISC + MIT для производных Feather | Прочитана полная LICENSE. Для воспроизводимости иконки взяты из tag `0.468.0`, преобразованы из SVG в Android VectorDrawable с единым stroke; исходные уведомления включены в APK и docs/licenses. Эмодзи не используются. |
| [android/architecture-samples](https://github.com/android/architecture-samples), push 2026-11-14 | Apache-2.0 | README: Compose + ViewModel + Room + Flow и Navigation Compose. Эти технологии использованы, fake remote из образца не переносился. |
| [androidx/androidx](https://github.com/androidx/androidx), push 2026-11-14 | Apache-2.0 | README и перечень компонентов: Room, DataStore, WorkManager, lifecycle и navigation выбраны как совместимая Jetpack-инфраструктура. |
| [square/retrofit](https://github.com/square/retrofit), push 2026-11-15 | Apache-2.0 | README и условия Android API 21+: типизированные suspend-интерфейсы для forecast и geocoding. Использована стабильная версия 2.11.0 с converter-gson, а не миграция на 3.0 без необходимости. |
| [open-meteo/open-meteo](https://github.com/open-meteo/open-meteo), push 2026-11-13 | Сервер AGPL-3.0; погодные данные CC BY 4.0 | README: публичный API без ключа, 7-дневный hourly-прогноз, free API для некоммерческого использования. Включены атрибуция, время кэша и объяснение сетевых запросов. Код AGPL-сервера не копируется и не линкуется. |
| [google/fonts/ofl/manrope](https://github.com/google/fonts/tree/main/ofl/manrope) | SIL OFL 1.1 | Прочитана OFL.txt, встроен Manrope с кириллицей. Google Sans не используется, поскольку подтверждённых прав на его распространение нет. |

Дополнительно самостоятельно найдены GitHub Search запросами `android compose weather open meteo` и `android offline first room datastore`:

- [Artchknd/Fairweather](https://github.com/Artchknd/Fairweather), Apache-2.0, push 2026-11-15. Изучен README: Open-Meteo fallback, ручной выбор места, отдельное состояние отсутствующего разрешения. Применены явное объяснение разрешений и ручной выбор города. Минимальный API проекта 31 и собственный proxy не переносились: наше приложение API 26+, без proxy разработчика.
- [Hacybeyker/FinFlow](https://github.com/Hacybeyker/FinFlow), MIT, push 2026-11-11. Изучен README: локальная база как source of truth, Room + Flow, no-account UX и Canvas. Применены эти подходы. Его финансовые, биометрические функции и SQLCipher не включались; защита нашей базы честно описана как sandbox/FBE Android, а экспорт шифруется отдельно.
- Репозитории без явной лицензии исключались как источник заимствований. GPL-погодные приложения не использовались как источник кода.

## Условия погоды

Некоммерческий режим и CC BY 4.0 подтверждены официальным README Open-Meteo. Страница `https://open-meteo.com/en/terms` из этой среды возвращала HTTP 403; полный текст актуального соглашения прочитать не удалось. Поэтому документы явно требуют повторной проверки условий перед коммерческим выпуском. В приложении нет утверждения, что бесплатный публичный endpoint разрешён для любых коммерческих сценариев. Политика: https://open-meteo.com/en/privacy .

## Совместимость и размер

minSdk 26 обеспечивает java.time без дополнительных desugaring-библиотек и современное системное файловое меню. Compile/target SDK 35. Kotlin 2.1.20 использует Compose compiler plugin той же версии, KSP 2.1.20-1.0.32 — Room 2.7.0. BOM согласует Compose UI/animation/Material3. Полные Gradle-версии указаны в README. SVG не загружаются в runtime, шрифт поставляется в APK, Lottie и Material-icons-extended не добавлены. Все ключевые функции работают на локальных сущностях и правилах.
