# Hello 3D Game - Android

Ein einfaches 3D "Hello World" Spiel für Android mit OpenGL ES 2.0.

## Features

- Rotierender 3D-Würfel mit bunten Flächen
- Native OpenGL ES 2.0 Implementierung
- Vollbild-Modus
- Landscape-Orientierung

## Projektstruktur

```
android-3d-game/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/hello3d/
│   │   │   ├── MainActivity.kt       # Haupt-Activity mit GLSurfaceView
│   │   │   ├── GameRenderer.kt       # OpenGL Renderer
│   │   │   └── Cube.kt               # 3D-Würfel mit Shadern
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
└── settings.gradle
```

## Anforderungen

- Android Studio Arctic Fox oder neuer
- Android SDK 21 oder höher (Android 5.0+)
- OpenGL ES 2.0 fähiges Gerät

## Installation

1. Öffne das Projekt in Android Studio
2. Warte, bis Gradle die Dependencies heruntergeladen hat
3. Schließe ein Android-Gerät an oder starte einen Emulator
4. Klicke auf "Run" (Grüner Play-Button)

## Build über Kommandozeile

```bash
cd android-3d-game
./gradlew assembleDebug
```

Die APK findest du dann unter: `app/build/outputs/apk/debug/app-debug.apk`

## Was passiert im Code?

1. **MainActivity.kt** - Erstellt eine GLSurfaceView mit OpenGL ES 2.0 Context
2. **GameRenderer.kt** - Rendert jeden Frame:
   - Setzt die Kamera Position
   - Rotiert den Würfel
   - Zeichnet den Würfel
3. **Cube.kt** - Definiert den 3D-Würfel:
   - Vertex Positions (8 Ecken, 6 Flächen)
   - Farben für jede Fläche
   - Vertex und Fragment Shader
   - Draw-Methode

## Erweiterungsmöglichkeiten

- Touch-Steuerung hinzufügen
- Mehrere Objekte rendern
- Texturen hinzufügen
- Kollisionserkennung implementieren
- Spiellogik einbauen

## Lizenz

Freie Verwendung für Lernzwecke.
