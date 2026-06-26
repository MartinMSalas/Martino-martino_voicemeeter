# Martino Voicemeeter Controller

Prototype 1: Windows media-volume keys control Voicemeeter Potato VAIO3 gain.

## Requirements

- Windows 10/11 64-bit
- Java 25
- Maven
- Voicemeeter Potato installed and running

## Run

```bash
mvn clean compile exec:java
```

## Important configuration

Open `src/main/java/com/martino/AppConfig.java`.

Check:

```java
VOICEMEETER_DLL_PATH
TARGET_STRIP_INDEX
```

For Voicemeeter Potato, VAIO3 is commonly one of:

```text
Strip[5]
Strip[6]
Strip[7]
```

Start testing with `Strip[7]`. If the wrong fader moves, change the index.

## Controls

- Volume Up: +1 dB
- Volume Down: -1 dB
- Mute: toggle mute
- ESC: exits the app

## Notes

This project uses native Windows keyboard hook via JNA:

```text
Java -> JNA -> user32.dll -> WH_KEYBOARD_LL
```

And controls Voicemeeter through:

```text
VoicemeeterRemote64.dll
```
