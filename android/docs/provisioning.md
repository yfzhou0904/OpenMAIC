# Provisioning & escape

## One-time setup on a fresh tablet

Requires: fresh device (no Google account added, no existing device owner), USB debugging on, `adb` authorized.

```sh
# from android/
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home ./gradlew installDebug
adb shell dpm set-device-owner com.instaclass.kiosk/.KioskAdminReceiver
adb shell am start -n com.instaclass.kiosk/.MainActivity
```

After this, reboot persistence and launcher takeover are automatic.

## Escape hatch

The app registers an `UnlockReceiver` for `com.instaclass.kiosk.UNLOCK`. Use it to recover without a factory reset.

```sh
# soften: clear restrictions + un-hide all packages (still device owner)
adb shell am broadcast -a com.instaclass.kiosk.UNLOCK -n com.instaclass.kiosk/.UnlockReceiver

# full release: above + relinquish device owner so the app can be uninstalled
adb shell am broadcast -a com.instaclass.kiosk.UNLOCK -n com.instaclass.kiosk/.UnlockReceiver --es mode release
```

## Last resort

If adb is unavailable and the device is stuck, factory reset via recovery:
power off → hold Volume Up + Power (connect charger if it doesn't trigger) → Android Recovery → Wipe data/factory reset.
