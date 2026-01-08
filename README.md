# 🛡️ Parental Guard

Parental Guard is a comprehensive Android-based parental control solution designed to provide parents with robust monitoring and management tools for their children's digital safety. The system comprises two distinct applications: the **Parent Controller** and the **Child Agent**, seamlessly communicating to enforce safety boundaries.

## 🚀 Key Features

### 🎮 Parent Controller
- **Real-time Monitoring**: Track child device status and app usage.
- **App Management**: Remotely lock/unlock specific applications.
- **Category Limits**: Set time limits for entire app categories (e.g., Social, Games).
- **QR Pairing**: Easy and secure device pairing via QR code scanning.
- **Biometric Security**: Protect the parent app with fingerprint or PIN authentication.
- **Multi-Device Support**: Manage multiple child devices from a single controller.

### 🕵️ Child Agent
- **Stealth Protection**: Disguised system service to prevent unauthorized tampering.
- **Robust Blocking**: Unskippable lock screens for blocked apps or exceeded limits.
- **Request Unlock**: Allows children to request temporary access, sent directly to the parent.
- **Darija Support**: Full localization in Moroccan Darija for local relevance.
- **Usage Reporting**: Automatically tracks and syncs app usage data back to the parent.

---

## 🛠️ Technical Stack

- **Languge**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for a modern, responsive interface.
- **Networking**: [Ktor](https://ktor.io/) (Server-side on Child, Client-side on Parent) for real-time synchronization via WebSockets and HTTP.
- **Local Storage**: [Room Database](https://developer.android.com/training/data-storage/room) for persistent storage of usage logs and configurations.
- **Background Tasks**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for reliable background monitoring and data syncing.
- **Dependency Injection**: Modular architecture with a shared `common` module.

---

## 🏗️ Architecture

The project is structured into three main Gradle modules:

1.  `:parent-controller`: The administrative app used by the parent.
2.  `:child-agent`: The monitoring agent installed on the child's device.
3.  `:common`: Shared data models, networking logic, and utility functions.

### Communication Flow
The Child Agent hosts a lightweight Ktor server, allowing the Parent Controller to establish a secure WebSocket connection for real-time commands (lock/unlock) and data retrieval (usage reports).

---

## 🌍 Localization

Parental Guard is built with inclusivity in mind, featuring:
- **Full Arabic/Darija Support**: Context-aware translations for the Moroccan region.
- **Latin Numeral Support**: Ensuring consistency across localized formats.

---

## 🔧 Installation & Setup

### Prerequisites
- Android Studio Iguana (or newer)
- Android SDK 26 (Android 8.0) or higher

### Steps
1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/your-repo/parental-guard.git
    ```
2.  **Parent App**: Build and install the `parent-controller` module on the parent's device.
3.  **Child App**: Build and install the `child-agent` module on the child's device.
4.  **Pairing**:
    - Open the Parent App and navigate to "Add Device".
    - Open the Child App and scan the displayed QR code to establish a secure link.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

*Developed with focus on safety, privacy, and ease of use.*
