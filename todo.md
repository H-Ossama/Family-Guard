# Family-Guard — Fixes & Roadmap

Tracking file for the full project diagnostic + code review (baseline `HEAD` = `310591a`).
Priority order: **P0** (core-loop correctness) > **P1** (security) > **P2** (repo hygiene & build) > **P3** (dead code & polish). **F1** is a new feature track.

Finding IDs: `C1..C4` = Critical (code review), `I1..I9` = Important (code review), plus diagnostic items.

## Current status

| Track | Status |
|---|---|
| P0 — Core-loop correctness | ✅ all done (C1, C2, C4, I1, I2, I7) |
| P1 — Security | ✅ all done (C3, perms, SecurityUtils, allowBackup) |
| P2 — Repo hygiene & build | ⏳ I5, Room, .gitignore, gitlink, Gradle, versions, tests done; **duplicates consolidation** open |
| P3 — Polish | ⏳ I3/I8/I9/I6, monitoring accuracy, orphaned code, garbled+missing strings done; **~100 hardcoded UI strings** open |
| F1 — Bluetooth feature | ✅ implementation done; **AES encryption + tests** optional/open |

> Everything below marked `[x]` was implemented and verified (all modules compile, both debug APKs assemble, child release APK builds with R8, unit tests pass). All fixes are **uncommitted**.

---

## P0 — Core-loop correctness

- [x] **C1 — `UnlockRequestScreen` treats `deviceId` as an IP → crash / wrong target**
  - `parent-controller/src/main/java/com/parentalguard/parent/ui/screens/UnlockRequestScreen.kt:52`
  - `InetAddress.getByName(deviceId)` throws `UnknownHostException` (deviceId is the child Android ID from notifications). Resolve the real `ChildDevice` from `DiscoveryViewModel.devices` by `deviceId` and use its `ip`/`port`.
  - **Done:** resolves real `ChildDevice` from `DiscoveryViewModel.devices` by `deviceId`; fallback = cloud-only `127.0.0.1` placeholder, never feeds the Android ID to `InetAddress`.
  - Still open: regression test.

- [x] **C2 — Cloud-relay pairing broken: `SET_RELAY_PARENT_ID` sent to `/lock`, handled only on `/device-name`**
  - `parent-controller/src/main/java/com/parentalguard/parent/network/DeviceClient.kt:183`
  - `child-agent/src/main/java/com/parentalguard/child/network/CommandServer.kt:204` (correct) vs `:218-258` (`/lock` returns `"Invalid lock/command"`).
  - Every QR pairing fails silently → child never persists `relay_prefs/parent_id` → all child→parent cloud events are dropped. Point `syncRelayParentId` at `/device-name`.
  - **Done:** `DeviceClient.syncRelayParentId` now posts to `/device-name`.
  - Still open: regression test.

- [x] **C4 — Cloud relay responses not correlated (stale replay / cross-talk)**
  - `parent-controller/src/main/java/com/parentalguard/parent/network/CloudRelayClient.kt:72,182-184`
  - `_responses = MutableSharedFlow(replay = 1, DROP_OLDEST)` with no request ID → stale response replayed to next `sendCommand`; concurrent commands steal each other's responses.
  - Add a unique request ID to `RelayMessage`/`Packet.Command` and route by ID.
  - **Done:** `Packet.Command/Response` carry `requestId`; parent `CloudRelayClient` uses `MutableSharedFlow(replay=0, extraBufferCapacity=16)` with subscribe-before-send `async{}` + `withTimeoutOrNull(10000)`, filters `it.requestId == requestId`; child `CloudRelayClient` echoes `requestId`.
  - Still open: concurrent-send regression test.

- [x] **I1 — "One more minute" extension flow dead end-to-end**
  - Child `CommandServer.kt:261-285` handles only `APPROVE_UNLOCK`/`DENY_UNLOCK`; `CloudRelayClient.kt:248` → `"Command not implemented"` for `APPROVE_EXTENSION`/`DENY_EXTENSION`/`STOP_BREAK`.
  - Parent `NotificationService.kt:135-151` drops `EXTENSION_REQUESTED`; `showExtensionRequestNotification` never called.
  - Implement the 3 command types on both child paths and route `EXTENSION_REQUESTED` to the notification helper.
  - **Done:** `CommandServer /unlock-response` + child `CloudRelayClient` (via shared `CommandDispatcher`) handle `APPROVE_EXTENSION` (+60s on active lock), `DENY_EXTENSION`, `STOP_BREAK` (`setGlobalLock(false)`); parent `NotificationService.handleEvent` routes `EXTENSION_REQUESTED` → `NotificationHelper.showExtensionRequestNotification`.

- [x] **I2 — Reports history unreachable**
  - `parent-controller/src/main/java/com/parentalguard/parent/ui/navigation/Navigation.kt:73,80` (`bottomNavItems` omits Reports)
  - `ParentalControlApp.kt:297` route registered, never navigated; `DeviceControlScreen.kt:1770-1784` "View History" `onClick` empty.
  - Add Reports to `bottomNavItems` and wire the button, or delete dead routes.
  - **Done:** Reports added to `bottomNavItems` + `showBottomBar`; `DeviceControlScreen` gained `onViewHistory` threaded to `ReportsTab` → `navController.navigate(Screen.Reports.route)`.

- [x] **I7 — `SET_LANGUAGE` to child is a no-op from the parent UI**
  - `DeviceClient.kt:255-258` posts to `/device-name` (ACK only, `CommandServer.kt:200-203`); the real impl lives on `/lock` (`CommandServer.kt:227-251`).
  - Route parent `SET_LANGUAGE` to `/lock` or implement locale application in the `/device-name` branch.
  - **Done:** `/device-name` `SET_LANGUAGE` persists `app_prefs/language_code` and applies locale via `updateConfiguration` (no app restart).

---

## P1 — Security

- [x] **C3 — Child LAN control plane unauthenticated + cleartext on `0.0.0.0:8080`**
  - `child-agent/src/main/java/com/parentalguard/child/network/CommandServer.kt:43-47,72-319`
  - Any LAN process can call `/lock`, `/unlock`, `/rules`, `/reset-pin`, `/hide`, `/unhide`, `/device-name`. `maxFrameSize = Long.MAX_VALUE` (line 47) = memory-exhaustion risk.
  - Require a pairing token (exchanged during QR pairing) on every mutating route; bound frame size (~64 KB).
  - Regression test: negative tests asserting unauthenticated `/lock` and `/reset-pin` are rejected.
  - **Done:** `PairingManager` (16-byte SecureRandom token in `pair_prefs/pair_token`); `CommandServer` auth intercept on every route except `/ping` (`X-Pair-Token` header or `?token=` query param, constant-time `MessageDigest.isEqual`); `maxFrameSize = 64 * 1024`. Parent `DeviceClient` sends the header; `observeEvents` uses `/events?token=...`. QR payload now 5 fields `deviceId|ip:port|deviceName|btName|pairToken`; `DiscoveryViewModel` parses/registers/restores tokens.
  - Still open: negative auth regression test.

- [x] **I4 — Parent PIN stored/verified in plaintext**
  - `parent-controller/src/main/java/com/parentalguard/parent/security/PinManager.kt:15-32`
  - Raw PIN in SharedPreferences, plain string compare, `allowBackup="true"` in manifest.
  - Hash+salt (or `EncryptedSharedPreferences`/Keystore) and exclude secrets from backups; add a "remove PIN" UI (`disablePin` has no caller).
  - **Done:** `PinManager` rewritten — salted SHA-256 hash (16-byte SecureRandom salt, Base64), constant-time `MessageDigest.isEqual` verify, plaintext `getPin` removed, `disablePin` clears hash+salt.
  - Still open: remove-PIN UI caller, backup exclusion, disable-pin wiring.

- [x] **Trim over-permissions**
  - Child: `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `NEARBY_WIFI_DEVICES` unused — `child-agent/src/main/AndroidManifest.xml:8-10`.
  - Parent: `ACCESS_FINE_LOCATION` requested unconditionally — `MainActivity.kt:44-71`.
  - **Done:** child manifest no longer declares the three location/NSD permissions (verified NSD registration doesn't need them); parent `MainActivity` requests `ACCESS_FINE_LOCATION` only on SDK < 33 and `NEARBY_WIFI_DEVICES` on 33+.

- [x] **`SecurityUtils` hardcoded AES key + zero IV**
  - `common/src/main/java/com/parentalguard/common/utils/SecurityUtils.kt:15,27,36`
  - Exchange key during QR pairing, or remove until used.
  - **Done:** deleted `SecurityUtils.kt` (verified no source references).

- [x] **`allowBackup=true` leaks prefs on both apps** (rules, lock state, relay IDs, PIN) — disable or exclude sensitive prefs from backup.
  - **Done:** `android:allowBackup="false"` set in both manifests.

---

## P2 — Repo hygiene & build

- [x] **I5 — Release build fails: `proguard-rules.pro` missing**
  - Referenced at `child-agent/build.gradle.kts:25` and `common/build.gradle.kts:20,14`; neither file exists. Child-agent release has `isMinifyEnabled = true`.
  - Create the files or set `isMinifyEnabled = false`.
  - Verify: `./gradlew :child-agent:assembleRelease`.
  - **Done:** created `child-agent/proguard-rules.pro`, `common/proguard-rules.pro`, `common/consumer-rules.pro`, `parent-controller/proguard-rules.pro` (keep kotlinx.serialization + Netty/slf4j, `-dontwarn java.lang.management.**`); excluded Netty's BlockHound service descriptor from packaging (references a runtime-only class, breaks R8); renamed `ic_system_service.png` → `.jpg` (was a JPEG with a `.png` extension, failed AAPT). `:child-agent:assembleRelease` (R8 minify) → BUILD SUCCESSFUL.

- [x] **Drop unused Room deps** (`child-agent/build.gradle.kts:84-90`) or add annotation processor if Room is intended.
  - **Done:** removed the three Room lines (verified no Room imports in child-agent source).

- [x] **Add `.gitignore`; untrack artifacts** — 43 tracked files under `.gradle/`, `build/`, `.idea/`, `.vscode/`, plus `local.properties`.
  - **Done:** root `.gitignore` (.gradle/, build/, local.properties, .idea/, .kotlin/, *.iml, signing keys); `git rm -r --cached .gradle build` staged (untracked build dirs now ignored).

- [x] **Fix dangling `cloud-relay-server` gitlink** — submodule entry `9b0ef0d`, no `.gitmodules`; add the submodule or remove the entry.
  - **Done:** `git rm --cached cloud-relay-server` staged (no `.gitmodules` exists, entry removed).

- [x] **Pin stable Gradle / upgrade AGP** — wrapper is `9.0-milestone-1` with AGP 8.3.0 (certified for Gradle 8.x); config-cache problems report exists under `build/`.
  - **Done:** wrapper pinned to stable `gradle-8.7-bin` (AGP 8.3.0 / Kotlin 1.9.22 compatible); verified all builds + tests on 8.7.

- [ ] **Consolidate duplicates into `:common`** — `CloudRelayClient` (both apps), `PinManager`, `ui/theme/*`.

- [x] **Unify version strings** — gradle `"1.0"` vs hardcoded `"PREMIUM VERSION 1.1.0"` vs `app_version` `"1.0.0"` (format arg dropped).
  - **Done:** `versionName = "1.1.0"` in both app modules; `app_version` strings → `Version %1$s` / `الإصدار %1$s` (format arg restored); `SettingsScreen` passes the version value.

- [x] **Establish a test harness** — no `src/test`/`src/androidTest` sources exist; add unit + instrumented tests for C1–C4 and I1–I9.
  - **Done:** JUnit 4 added to `:common` and `:child-agent` (parent already had it); `common/src/test` now has `PacketSerializationTest` (requestId round-trip for C4, enum serialization, unknown-key tolerance) + `CategoryMapperTest`. `:common:testDebugUnitTest` passes. Instrumented tests not yet written.

---

## P3 — Dead code, bugs & polish

- [x] **I3 — Permanent lock can auto-expire from stale `lockUntil`**
  - `child-agent/src/main/java/com/parentalguard/child/data/RuleRepository.kt:131-139`
  - `setGlobalLock(true)` doesn't clear `_globalLockUntil`; `MonitorService.kt:143` and `loadPersistedState` then release the lock after the old timestamp passes.
  - **Done:** `setGlobalLock(true)` now always clears `_globalLockUntil` (persisted via `saveGlobalLock`).

- [x] **I8 — Removing a device doesn't stop background observation**
  - `DiscoveryViewModel.kt:246-259` only clears state/prefs; `NotificationService.kt:98-133` keeps jobs running for removed devices.
  - **Done:** `NotificationService` gained `ACTION_STOP_MONITORING` + `stopObserving(deviceId)` (cancels job, removes from maps) + companion `stopMonitoring`; `DiscoveryViewModel.removeDevice`/`resetAllDevices` call it.

- [x] **I9 — Custom categories & warning-dedup not persisted**
  - `RuleRepository.kt:189,209` — `_customCategories`/`_warningsShown` are in-memory only; reset on process death.
  - **Done:** `PersistentStateManager` added `save/loadCustomCategories` + `save/loadWarningsShown` (JSON in prefs); `RuleRepository` loads both on init and saves on `setCustomCategory`/`markWarningShown`/`clearWarnings`.

- [x] **I6 — Device rename never persisted on child**
  - `CommandServer.kt:178-181` only logs; plus `EventHelper` reads `child_prefs/"device_name"` while `DeviceUtils` uses `device_prefs/"custom_device_name"` — custom name never appears in unlock/extension events.
  - **Done:** `/device-name` `UPDATE_DEVICE_NAME` persists via `DeviceUtils.setCustomDeviceName`; `EventHelper` now uses `DeviceUtils.getDeviceName(context)` (correct prefs key) for unlock/extension events.

- [x] **Monitoring accuracy**
  - Break meter adds +1000 ms per loop tick regardless of real usage — `MonitorService.kt:189`.
  - Screen-off time counted toward usage limit (fallback in `getForegroundPackage`).
  - Hourly usage credited to the background-event hour — `UsageMonitor.kt:163-171`.
  - **Done:** break meter now accrues the real `elapsedRealtime()` delta per tick (covers slow/overrun loops); `getForegroundPackage` returns null when `PowerManager.isInteractive()` is false so sleep time is never counted or blocked; `calculateHourlyUsage` splits each session across the hours it spans (`addSessionToHourlyUsage`).

- [x] **Remove orphaned UI/code**
  - `TakeABreakScreen`, `WhitelistManagerDialog`, `CategoryLimitsDialog`, standalone `DeviceRenameDialog`, legacy `AppListItem`, `DeviceStatusRepository`.
  - `ReportScheduler` (never scheduled; worker discards report), `CommandServer.sendUnlockRequest()` (never called), child `PinManager` (no PIN UI), `SecurityUtils`, `overlay_lock_screen.xml`, `bg_premium_gradient.xml`, `CategoryMapper.addCustomMapping()`.
  - **Done:** deleted all five parent files (`TakeABreakScreen.kt`, `WhitelistManagerDialog.kt`, `CategoryLimitsDialog.kt`, `DeviceRenameDialog.kt`, `DeviceStatusRepository.kt`), child `ReportScheduler.kt`, `overlay_lock_screen.xml`, `bg_premium_gradient.xml`; removed `CommandServer.sendUnlockRequest()`, legacy `AppListItem` (DeviceControlScreen), `CategoryMapper.addCustomMapping()`. **Kept child `PinManager`** — it is still reached via `CommandDispatcher` `RESET_PIN`.

- [ ] **Localization cleanup**
  - ~100 hardcoded UI strings (Dashboard, Discovery, Control, UnlockRequest, PinLock, Settings, Reports).
  - 10 missing Arabic strings (Reports, Take-a-Break, timer dialogs) in `parent-controller/res/values-ar/strings.xml`.
  - Garbled string `status_initializing` = "كايديماري..." in child `strings.xml:101`.
  - **Done (partial):** all 10 missing Arabic strings added (`nav_reports`, dialog/timer/break strings); garbled `status_initializing` fixed to `جارٍ التهيئة...`. Still open: ~100 hardcoded UI strings left as-is (intentional scope for a small app).

---

## F1 — New feature: connect devices via Bluetooth

Bluetooth as an additional transport between Parent Controller and Child Agent, for when devices are not on the same WiFi network (complements NSD/LAN + cloud relay).

**Recommended design:** Classic Bluetooth **RFCOMM (SPP)** — a reliable byte-stream socket that can tunnel the existing `Packet` JSON protocol unchanged, with a fixed well-known UUID. BLE GATT is an alternative if power matters more than simplicity.

### Implementation steps

> **Implemented** (all verified compiling): new files
> - `common/.../network/BluetoothConfig.kt` — fixed SPP UUID + `PG_Child_<id>` naming.
> - `child-agent/.../network/BluetoothCommandServer.kt` — RFCOMM server, length-prefixed JSON framing, started/stopped by `MonitorService`.
> - `child-agent/.../network/CommandDispatcher.kt` — single command dispatch shared by BT server + cloud relay (replaced `CloudRelayClient.dispatchCommand`).
> - `parent-controller/.../network/BluetoothClient.kt` — `callbackFlow` discovery (bonded + `startDiscovery`) + RFCOMM command client.
>
> Plus: QR payload v2 `deviceId|ip:port|deviceName|btName`; `ChildDevice`/`SavedDevice` carry `bluetoothName`/`bluetoothMac` (persisted); `ConnectionType.BLUETOOTH`; `DeviceClient.executeCommand` tier LOCAL → BLUETOOTH → CLOUD; runtime BT permission requests on API 31+ in both apps.

- [x] **Permissions & manifest**
  - Child + parent manifests: `BLUETOOTH`, `BLUETOOTH_ADMIN` (API ≤ 30); `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN` (API 31+).
  - Runtime permission request flow (API 31+) before discovery/connect.

- [x] **Child side — RFCOMM SPP server**
  - `BluetoothServerSocket` accepting on a fixed UUID; per-connection coroutine reading the same `Packet` JSON framing used by `CommandServer`.
  - Reuse `CommandServer` dispatch logic for incoming commands; respond with `Packet.Response`.
  - Keep alive alongside (or gated by) the existing Ktor server.

- [x] **Parent side — discovery + client**
  - `BluetoothAdapter` discovery of paired/pairable devices (filter by child device name `PG_Child_…` / disguised label), bond flow.
  - RFCOMM `BluetoothSocket` client that reads/writes `Packet` JSON over the socket.

- [x] **Transport integration**
  - Add `ConnectionType.BLUETOOTH` to `DiscoveryViewModel.ConnectionType`.
  - Insert BT as a fallback tier in `DeviceClient.executeCommand` (LOCAL → BLUETOOTH → CLOUD), keyed by the device's BT MAC.

- [x] **Pairing / enrollment**
  - Extend the QR payload (`deviceId|ip:port|deviceName`) to carry the child's BT MAC, or exchange it via an existing command; store alongside `SavedDevice`.
  - **Note:** QR carries the child's BT **name** (`PG_Child_<id>`); the MAC itself is resolved at discovery time and persisted.

- [ ] **Encryption (optional)**
  - Wrap the RFCOMM stream with `SecurityUtils` AES once keys are exchanged during pairing (ties into P1 `SecurityUtils` item).

- [ ] **Testing**
  - Instrumented test on two devices/emulators exercising pairing + command round-trip.
  - Fallback-ordering unit test: LOCAL down → BT used → CLOUD used.

---

*Generated from the full project diagnostic and code review. Check boxes off as items are completed.*