# InstaClass Kiosk (Android)

A thin native shell that turns a Samsung tablet into a single-purpose device for the InstaClass web app.

## Model: soft kiosk

Not a hard lockdown. Students can always reach system settings (Wi-Fi, brightness, volume) via the status-bar pulldown. The goal is only to prevent casual use/installation of *other* apps — not to defeat a determined user. If someone reaches developer settings or triggers a factory reset, that's acceptable.

Enforced by being Device Owner:
- Our app is set as the default launcher (`addPersistentPreferredActivity` for `CATEGORY_HOME`), so the home button returns to InstaClass.
- Every third-party / pre-installed app that has a launcher entry gets `setApplicationHidden(true)`. This sweep runs on every `onResume` and on `PACKAGE_ADDED`.
- User restrictions: `DISALLOW_INSTALL_UNKNOWN_SOURCES`, `DISALLOW_UNINSTALL_APPS`, `DISALLOW_ADD_USER`, `DISALLOW_MODIFY_ACCOUNTS`.
- Immersive-sticky mode — status/nav bars hidden until swipe-from-edge, then auto-hide.
- `BOOT_COMPLETED` receiver auto-launches the app on reboot.

Explicitly **not** set: `DISALLOW_INSTALL_APPS` (breaks adb installs) and `DISALLOW_FACTORY_RESET` (breaks the user's last-resort escape). The Play Store being hidden is what practically blocks student installs.

## File map

```
android/
├── app/src/main/
│   ├── AndroidManifest.xml              permissions, receivers, launcher intent filter
│   ├── java/com/instaclass/kiosk/
│   │   ├── MainActivity.kt              WebView host + device-owner policy application
│   │   ├── KioskAdminReceiver.kt        DeviceAdminReceiver stub
│   │   ├── BootReceiver.kt              auto-launch on BOOT_COMPLETED
│   │   ├── PackageChangeReceiver.kt     hide newly-installed apps
│   │   └── UnlockReceiver.kt            adb-triggered escape hatch
│   └── res/xml/device_admin.xml         device admin policy declaration
├── app/build.gradle.kts                 KIOSK_URL buildConfig, SDK levels, deps
└── docs/
    ├── README.md                        this file
    └── provisioning.md                  one-time setup + escape hatch commands
```

## Key config

- `applicationId` / `namespace`: `com.instaclass.kiosk`
- `KIOSK_URL`: set in `app/build.gradle.kts` as a `buildConfigField` → `BuildConfig.KIOSK_URL`
- `minSdk` 26, `targetSdk` / `compileSdk` 35
- Kotlin 2.0.21, AGP 8.7.2, JDK 21 for builds

## Gotchas learned

- `Theme.Material.NoActionBar` works with plain `Activity`. `Theme.AppCompat.*` would require `AppCompatActivity` + the appcompat dependency. We chose plain `Activity`.
- Setting `DISALLOW_INSTALL_APPS` also blocks adb installs — do not re-enable it without leaving an escape hatch.
- `setLockTaskPackages` + `startLockTask` disables the status bar pulldown. We do not use lock task mode.
- `setApplicationHidden` is filtered to packages with a launcher intent, to avoid disabling system services (SystemUI, IME, WebView).
