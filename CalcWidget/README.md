# CalcWidget — Android Calculator + Home Screen Widget

Полноценное Android-приложение с виджетом для рабочего стола, конвертированное из веб-приложения (React/TypeScript).

## Что внутри

| Компонент | Описание |
|---|---|
| `MainActivity` | Полноэкранный калькулятор с 5 темами |
| `CalculatorWidgetProvider` | Виджет для домашнего экрана Android |
| `CalculatorEngine` | Логика вычислений (SharedPreferences) |
| `ThemeManager` | 5 тем из оригинального веб-приложения |

### Темы
- **Eco Default** — тёмно-зелёная (оригинальная)
- **Material Light** — светлая с фиолетовым акцентом
- **AMOLED Neon** — чисто чёрная с зелёным
- **Pastel Dream** — розово-пастельная
- **Ocean Blue** — глубокая синяя

## Сборка APK

### Требования
- **Android Studio** Hedgehog (2023.1.1) или новее
- **JDK 17** (поставляется вместе с Android Studio)
- Android SDK (API 34), автоматически предложит установить при открытии

### Шаги

1. Откройте Android Studio → **Open** → выберите папку `CalcWidget`
2. Дождитесь синхронизации Gradle (первый раз загружает зависимости, ~2–3 мин)
3. **Build → Build Bundle(s) / APK(s) → Build APK(s)**
4. APK появится по пути:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

### Установка на телефон
```bash
# Через ADB (кабель USB, включить отладку по USB)
adb install app/build/outputs/apk/debug/app-debug.apk

# Или просто скопировать APK на телефон и открыть через файловый менеджер
# (нужно разрешить установку из неизвестных источников)
```

### Минимальные требования к устройству
- Android 8.0 (API 26) и выше
- ~2 МБ свободного места

## Как добавить виджет на рабочий стол

1. Установите приложение
2. Долгий тап на пустом месте рабочего стола
3. «Виджеты» → CalcWidget → перетащить на экран
4. Изменяйте размер виджета (от 3×4 до любого)

## Архитектура

```
CalculatorEngine   ← SharedPreferences (персистентное состояние)
       ↑                      ↑
MainActivity        CalculatorWidgetProvider
(полный экран)        (AppWidgetProvider)
       ↑
ThemeManager       ← SharedPreferences (тема)
```

Состояние калькулятора синхронизировано: нажатие кнопки в приложении сразу обновляет виджет и наоборот.

## Структура проекта

```
CalcWidget/
├── app/src/main/
│   ├── java/com/calcwidget/app/
│   │   ├── CalculatorEngine.kt          # логика вычислений
│   │   ├── ThemeManager.kt              # управление темами
│   │   ├── CalculatorWidgetProvider.kt  # AppWidget провайдер
│   │   └── MainActivity.kt             # главная активити
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml        # разметка приложения
│   │   │   └── widget_calculator.xml   # разметка виджета
│   │   ├── xml/
│   │   │   └── calculator_widget_info.xml # метаданные виджета
│   │   └── values/
│   │       ├── strings.xml
│   │       └── styles.xml
│   └── AndroidManifest.xml
├── build.gradle
├── settings.gradle
└── gradle.properties
```
