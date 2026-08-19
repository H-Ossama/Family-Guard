# Kid Guard

Kid Guard is a family safety suite for Android. It pairs a **Parent Controller** app with a **Child Agent** app so parents can follow their children's phone usage in real time, block apps, set time limits, and receive daily reports — all from a single dashboard.

The child app runs as a disguised system component with an unskippable lock screen, so tampering is difficult.

---

## Key Features

### Parent Controller

- **Real-time monitoring**: track child device status, battery, connection type, and app usage.
- **App management**: lock or unblock individual apps, block internet per app, set timers and daily allowances.
- **Category limits**: set time limits for entire categories (Social, Games, Education, Entertainment, System, Other).
- **Take a Break**: schedule auto-breaks with warnings, learning mode, and "one more minute" extensions.
- **Blocking screen styles**: choose between the standard lock, a full blackout, or a quiet-focus screen per device.
- **Daily reports & insights**: hourly timeline, category donut, most-used apps, and a browsable report history.
- **Device Owner controls**: device lock, app suspension, uninstall protection, usage limits, user restrictions, and Wi-Fi toggle (when the child app is provisioned as a Device Owner).
- **Pairing**: QR code, Wi-Fi network scan, or Bluetooth — with automatic connection fallback (Local, Bluetooth, Cloud relay).
- **Security**: PIN protection with salted hashing, biometric unlock, and authenticated commands to the child device.
- **Share child APK**: send the child app installer directly to the child's phone from settings.
- **Multi-device support**: manage several child devices from a single controller.

### Child Agent

- **Stealth protection**: disguised system component that resists uninstallation and icon removal.
- **Robust blocking**: unskippable lock screens for blocked apps and exceeded limits.
- **Usage monitoring**: screen-aware tracking (sleep time is not counted), per-app and hourly usage.
- **Usage warnings**: notifies the child when an app is close to its daily limit.
- **Unlock requests**: children can request temporary access, sent straight to the parent.
- **Bluetooth server**: controllable over Bluetooth when devices are not on the same network.
- **Localization**: English, French, and Arabic / Moroccan Darija.

---

## Getting Started

### Prerequisites

- Android Studio Iguana (or newer)
- Android SDK 26 (Android 8.0) or higher
- Gradle 8.7 (bundled wrapper)

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/H-Ossama/Family-Guard.git
   ```
2. **Parent app**: build and install the `parent-controller` module on the parent's device.
3. **Child app**: build and install the `child-agent` module on the child's device (or use **Share child APK** from the parent app's settings).
4. **Pair**:
   - Open the child app and keep its QR code on screen.
   - In the parent app, open **The Circle** and choose *Pair with QR* (or *Scan network* / *Pair via Bluetooth*).
   - Grant the requested permissions (notifications, Bluetooth, nearby devices).

### Device Owner Mode (optional)

Advanced controls (app suspension, uninstall protection, device-wide limits, Wi-Fi toggle) require the child app to be provisioned as a **Device Owner**. Follow the in-app guide in the parent app — it typically involves a factory-reset device and an ADB command:

```bash
adb shell dpm set-device-owner com.android.system.services.internal/com.parentalguard.child.receiver.AdminReceiver
```

---

## Architecture

The project is split into three Gradle modules:

1. `:parent-controller` — the administrative app used by the parent.
2. `:child-agent` — the monitoring agent installed on the child's device.
3. `:common` — shared data models, network protocol, and utilities.

### Communication

The child agent exposes a Ktor HTTP server (and a Bluetooth RFCOMM server) that tunnels a shared JSON protocol. The parent controller connects via Wi-Fi (LAN), Bluetooth, or a cloud relay, and falls back automatically between them:

**Local (HTTP/WebSocket) -> Bluetooth (RFCOMM) -> Cloud relay**

Every mutating command is authenticated with a pairing token exchanged during QR/Bluetooth pairing. Command responses are correlated by request ID so relayed traffic stays consistent under concurrency.

### Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with a custom Neumorphic/Aura design system
- **Networking**: Ktor (server on child, client on parent), WebSockets, HTTP, Bluetooth RFCOMM
- **Storage**: SharedPreferences (persistence), local report repository
- **Security**: pairing-token authentication, salted SHA-256 PIN hashing, Android Keystore-backed biometrics
- **Build**: Gradle 8.7, AGP 8.3.0, R8/ProGuard for release builds

---

## Localization

- English (base locale)
- French
- Arabic / Moroccan Darija

The child app follows the device locale automatically; the parent app language can be changed in Settings and synced to paired devices.

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
