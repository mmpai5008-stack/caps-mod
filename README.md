# Caps Mod (Fabric, Minecraft Java 1.21.11)

Client-side mod. Press **K** (changeable in Options > Controls > "Caps Mod") to open the Caps menu.
Works offline (singleplayer) and online (any server) because nothing is sent to the server.
Only you can see your cap.

## Build the mod (.jar)

Requirements: Java 21 and internet access.

    ./gradlew build        (Windows: gradlew.bat build)

The finished mod is `build/libs/capsmod-1.0.0.jar`.
Put it in `.minecraft/mods/` together with Fabric Loader and Fabric API for 1.21.11.

## Use your own downloaded caps

1. Open the Caps menu (K) and click **Open Caps Folder**
   (it is `.minecraft/config/capsmod/caps/`).
2. Put your cap `.png` files there.
3. Click **Reload Caps**, then click your cap in the list.
4. Press **F5** in-game to see it on your head.

## Cap texture format

Use `cap_template.png` as a base. Size is 64x32 (any 2:1 size like 128x64 also works).

- Crown (top of the cap): the area starting at pixel (0, 0), 36 x 13 pixels
- Brim (front peak): the area starting at pixel (0, 16), 26 x 5 pixels
