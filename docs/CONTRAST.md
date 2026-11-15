# Контраст статических цветовых токенов

Причина чёрного текста устранена явной передачей LocalContentColor через корневую Surface, contentColor Scaffold и onPrimaryContainer карточки Aurora. Подложки обычных карточек теперь непрозрачны, свечение ослаблено. Ниже — математический расчёт исходных цветовых пар, а не проверка отрисованного экрана. Он не учитывает сглаживание шрифта, фотографии, disabled-состояния или наложение Aurora; полная проверка на устройстве не выполнялась по указанию пользователя.

| Палитра | Пара | Контраст |
|---|---|---|
| blueLight | onBackground / background | 13.17:1 |
| blueLight | onSurface / surfaceVariant | 12.24:1 |
| blueLight | onSurfaceVariant / surfaceVariant | 5.48:1 |
| blueLight | onPrimaryContainer / primaryContainer | 9.88:1 |
| pinkLight | onBackground / background | 13.32:1 |
| pinkLight | onSurface / surfaceVariant | 12.15:1 |
| pinkLight | onSurfaceVariant / surfaceVariant | 5.36:1 |
| pinkLight | onPrimaryContainer / primaryContainer | 8.92:1 |
| blueDark | onBackground / background | 15.08:1 |
| blueDark | onSurface / surfaceVariant | 12.37:1 |
| blueDark | onSurfaceVariant / surfaceVariant | 8.40:1 |
| blueDark | onPrimaryContainer / primaryContainer | 9.43:1 |
| pinkDark | onBackground / background | 14.67:1 |
| pinkDark | onSurface / surfaceVariant | 11.75:1 |
| pinkDark | onSurfaceVariant / surfaceVariant | 8.08:1 |
| pinkDark | onPrimaryContainer / primaryContainer | 8.81:1 |
