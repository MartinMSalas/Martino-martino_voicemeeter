# Martino Voicemeeter Controller

Prototype 1: Windows media-volume keys control Camilla DSP Gui gain.

## Requirements

- Windows 10/11 64-bit
- Java 25
- Maven
- Camilla DSP installed and running

## Run

```bash
mvn clean compile exec:java
```

## Important configuration

Open `src/main/java/com/martino/AppConfig.java`.

Check:

```java

Camilla DSP
```
## Controls

- Volume Up: +0.5 dB
- Volume Down: -0.5 dB
- Mute: toggle mute
- ESC: exits the app

## Notes

This project uses native Windows keyboard hook via JNA:

```text
Java -> JNA -> user32.dll -> WH_KEYBOARD_LL
```

And controls Camilla GUI through:

```text
Camilla DSP 
```
