Icon generation guide

Overview
- Place the provided `LOGO_ICON.png` in `app/src/main/res/drawable/` and name it `logo_icon.png`.
- Place the provided transparent splash image `LOGO_TRANSPARENT.png` in `app/src/main/res/drawable/` and name it `logo_transparent.png`.

Automatic (recommended)
1. Ensure ImageMagick is installed and `magick` is on PATH.
2. From the repo root run:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\generate_launcher_icons.ps1 \
  -SourceImage "app/src/main/res/drawable/logo_icon.png" \
  -PlayStoreOut "playstore/icon_512x512.png" \
  -RemoveLegacy
```

This will create mipmap images under `app/src/main/res/mipmap-*` and a Play Store icon at `playstore/icon_512x512.png`. The script will also delete `app/src/main/res/drawable/zerobook_icon.png` if `-RemoveLegacy` is provided.

Manual (Android Studio)
- Open `Image Asset` in Android Studio: `File` → `New` → `Image Asset`.
- Use `logo_icon.png` as the foreground and set background color to `#FAF8F5`.
- Generate adaptive launcher icons and export into your `mipmap-*` folders.

Verification
- Build debug APK: `./gradlew clean assembleDebug`.
- Install on device/emulator: `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
- Confirm launcher icon and animated splash show as expected.

Notes
- This repo already updates code and XML to reference `logo_icon` and `logo_transparent`. Add the two PNG files and run the script. Commit when satisfied.
