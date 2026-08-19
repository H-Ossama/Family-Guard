# Kid Guard — Release Notes v2.4.7

> Formerly **Family Guard**. This release is a complete overhaul of the app: a brand-new UI, new connectivity options, stronger security, and a large batch of bug fixes.

---

## 📌 Overview

**v2.4.7** — both apps (Parent Controller `com.parentalguard.controller` and Child Agent `com.android.system.services.internal`).
- `versionName` `2.4.7` · `versionCode` 1 · minSdk 26 (Android 8.0) · targetSdk 34
- App rebranded from **Family Guard** to **Kid Guard**
- New app icons (adaptive launcher icons on both apps)

---

## 🎨 Complete UI Overhaul

### Parent Controller
- **Brand-new design language** built on a soft-pastel **Neumorphic** system (raised/inset soft shadows, concave switches, glowing status dots, animated progress rings) layered over a deep-space **Aura** glassmorphism shell with living aurora orbs, spring motion, and staggered entrances.
- **Animated Splash screen** with orbiting ring and brand mark.
- **5-page first-run onboarding** (welcome → find their device → send the child app → scan to connect → guard & guide).
- **Floating dock navigation** with 4 tabs: **Pulse**, **Circle**, **Insights**, **Control**.
- Bundled fonts: **Poppins** for headings, **JetBrains Mono** for stats/countdowns/IPs (works offline).

### New Screens
- **Pulse** (dashboard): greeting masthead, family screen-time hero card with goal ring, Devices/Online/Shields stats, attention banner for locked/offline devices, "Your Circle" device previews, Quick Actions (Lock All, Add Device, Insights, Refresh).
- **Circle** (devices): animated neumorphic **radar sweep**, pair via **QR / network scan / Bluetooth**, connected-device list with connection pill (LOCAL/BT/RELAY), long-press to remove.
- **Device Console**: 6 swipeable segments — **Now** (lock/shield, blocking-screen-style picker, app icon hide, top apps), **Apps** (search + category filters, block app vs block internet, timers, allowances, daily limits, duration & category dialogs), **Boundaries** (per-category limits with presets and slider), **Rhythm** (Take a Break scheduling, warnings, learning mode, extensions), **Activity** (daily report, hourly timeline, category donut, most-used apps), and **Advanced** (Device Owner controls).
- **Insights**: saved daily reports history with 7-day digest and per-report detail view.
- **Request**: real-time unlock / app-access / "one more minute" extension requests from notifications.
- **Control** (settings): Security (PIN, biometric login, Device Owner guide, **Share child APK**), Appearance (theme, language, notifications), About.
- **About**, **Help & Support** (FAQ + troubleshooting + GitHub links), **Device Owner Setup Guide**.

### Child Agent
- **Neumorphic redesign** of Main screen, onboarding, lock screen, and blocking activity.
- New **blocking screen styles** selectable from the parent:
  - **CURRENT** — redesigned lock with countdown ("FREE IN"/"LOCKED FOR")
  - **BLACKOUT** — full-black overlay
  - **QUIET_FOCUS** — calm focus screen with timer motif (with Darija message)
- Localized **fake "System Core" settings** impersonation UI (performance/battery/storage/security dialogs, "Framework v4.2.0-stable" footer).

---

## 🔗 Connectivity & Pairing

- **NEW: Bluetooth pairing & control.** Child runs a classic Bluetooth RFCOMM (SPP) server; parent discovers `PG_Child_*` devices (bonded + scan), pairs, and tunnels the same JSON command protocol. Parent commands fall back **LOCAL → Bluetooth → Cloud relay**.
- **QR pairing v2**: payload now carries `deviceId|ip:port|deviceName|bluetoothName|pairToken`; legacy 3-field codes still parse.
- **Cloud relay hardened**: commands/responses correlated by `requestId` (no more stale-response cross-talk), subscribe-before-send pattern, 10 s timeout, smart reconnect with backoff, DNS failure handling.
- **Share child APK** directly from the parent app (bundled APK sent via Quick Share/Bluetooth/file apps).
- Runtime permission handling per Android version for notifications, nearby devices, and Bluetooth.
- Automatic adoption of child-reported device names; connection type now reported per device.

---

## 🔒 Security & Privacy

- **Authenticated child control plane**: every mutating command now requires the **pairing token** exchanged during QR/BT pairing (`X-Pair-Token` header or `?token=` for the event stream), verified with **constant-time comparison**. Only `/ping` is open.
- **WebSocket frame size bounded** to 64 KB (was unbounded — memory-exhaustion risk).
- **Parent PIN no longer stored in plaintext**: salted SHA-256 hash + constant-time verify, secure random salt.
- **Biometric unlock** (fingerprint / device credential) added to the parent PIN lock screen.
- **`allowBackup` disabled** on both apps (lock state, rules, PIN, and relay identities no longer leak to backups).
- **Removed over-permissions**: location + NSD permissions stripped from the child; parent requests location only below Android 13 and Nearby Wi-Fi on 13+.
- **Removed hardcoded AES key** utility (`SecurityUtils`).
- Device-admin policy trimmed (removed `wipe-data`; only force-lock + watch-login remain).

---

## 🛠️ NEW: Device Owner Mode (Advanced Controls)

- When the child app is provisioned as a **Device Owner** (setup guide included in-app with the ADB command), the parent gains capability-gated controls:
  - **Device lock** (`lockNow`)
  - **App suspension** (`setPackagesSuspended`)
  - **Uninstall protection** (`setUninstallBlocked`)
  - **User restrictions**: block app installs, block account modification, block adding users
  - **Device-wide & per-app daily usage limits** with auto-lock / auto-suspend enforcement
  - **Wi-Fi on/off toggle**
- Capabilities are reported per device; unsupported features show an explanatory dialog with a shortcut to the setup guide.
- Policies persist across restarts and clear gracefully if Device Owner status is lost.

---

## 🐞 Bug Fixes

### Core functionality
- **Fix:** Unlock request from a notification no longer crashes (deviceId was being treated as an IP; now resolved to the real device). *(C1)*
- **Fix:** Cloud-relay pairing actually works — the parent now sends `SET_RELAY_PARENT_ID` to the correct endpoint, so child→parent cloud events are delivered. *(C2)*
- **Fix:** Cloud relay responses are now correlated by request ID — no more stale/stealed responses between concurrent commands. *(C4)*
- **Fix:** "One more minute" extension flow is fully wired: child handles `APPROVE_EXTENSION` (+60 s), `DENY_EXTENSION`, `STOP_BREAK`; parent routes extension requests to notifications. *(I1)*
- **Fix:** Reports history is reachable — Reports tab added to the dock and "View History" buttons wired. *(I2)*
- **Fix:** Permanent lock could silently expire from a stale `lockUntil` timestamp. *(I3)*
- **Fix:** Removing/resetting a device now stops its background monitoring jobs. *(I8)*
- **Fix:** Custom categories and "warning shown" flags are now persisted across app restarts. *(I9)*
- **Fix:** Device rename actually sticks on the child and appears in unlock/extension events. *(I6)*

### Monitoring accuracy
- Screen-off time is no longer counted toward usage limits (prevents false locks while the phone is asleep).
- "Take a Break" usage meter now accrues real elapsed time instead of a flat 1 s per tick.
- Hourly usage splits sessions across hour boundaries instead of crediting the whole session to the end hour.
- Usage logs merged/deduplicated per package; system apps classified and filtered; keyword-based category detection on package name + label.

### Settings
- Changing the language from the parent now reaches the child (child follows the device locale automatically; forced-locale code removed).
- Garbled Arabic `status_initializing` string fixed.

---

## 🌍 Localization

- **English** is now the base locale for both apps.
- **Full French translation** of the child app (NEW).
- **Full Arabic / Moroccan Darija** translations (parent + child), including the child's fake system settings, lock screen, and onboarding.
- Language switcher in parent Settings (English / Darija) that syncs to child devices.

---

## 📦 Build & Engineering

- **Release builds fixed**: missing ProGuard/R8 rules created for all modules; child release APK now assembles with R8 minification (Netty service descriptors excluded from packaging).
- **Stable Gradle 8.7** pinned (was an unreleased milestone build) — verified builds/tests on all modules.
- **Version strings unified** across Gradle and UI.
- Unused **Room** dependencies removed from the child module.
- **Unit tests added** (packet serialization, category mapping, Bluetooth config, app-usage merging).
- Repo hygiene: `.gitignore` added, build artifacts untracked, dangling `cloud-relay-server` gitlink removed.
- Removed orphaned/dead code: `ReportScheduler`, `DeviceStatusRepository`, legacy screens/dialogs/components, unused `sendUnlockRequest()`, obsolete layouts and drawables.

---

## ⚠️ Notes for Testers

- The child APK bundled inside the parent app for "Share child APK" is a **debug build** (no release signing key configured yet).
- Device Owner controls require the child app to be provisioned as Device Owner on a (typically factory-reset) device — they are reported as unavailable otherwise.
- Dark mode theme picker shows a "coming soon" state for now (System/Light selectable).

---

*Generated from a full audit of changes vs. the previous GitHub release.*
