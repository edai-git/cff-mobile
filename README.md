# CFF Mobile (v0.0.1)

[English](README.md) | [Bahasa Indonesia](README_ID.md)

---

[![GitHub Release](https://img.shields.io/github/v/release/edai-git/cff-mobile?color=blue&logo=github)](https://github.com/edai-git/cff-mobile/releases/latest)
[![Android Min SDK](https://img.shields.io/badge/Android-10.0%2B%20(API%2029%2B)-brightgreen?logo=android)](https://developer.android.com)
[![PS4 Firmware](https://img.shields.io/badge/PS4%20Firmware-6.00%20--%2011.02-orange?logo=playstation)](https://github.com/ntfargo/CSSFontFace-Exploit)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**CFF Mobile** is a standalone Android application that functions as an embedded local HTTP web server hosting the **CSSFontFace PS4 WebKit Exploit** directly from your smartphone. Run the exploit on your PS4 console without needing a PC, laptop, or internet connection.

---

## 📥 Download & Releases

Download the latest APK release directly:

👉 **[Download CFF Mobile APK (Latest Release)](https://github.com/edai-git/cff-mobile/releases/latest)**

| Release | APK File | Version | Status |
| :--- | :--- | :--- | :--- |
| **Latest** | `cff-mobile-v0.0.1.apk` | `v0.0.1` | Stable |

---

## 📱 Minimum Android Specifications

| Component | Minimum Requirements | Recommended |
| :--- | :--- | :--- |
| **Operating System** | Android 10.0 (API Level 29) | Android 11.0 – Android 15 (API 35) |
| **Root Access** | **Not Required (Non-Root)** | Non-Root |
| **RAM** | 1 GB | 2 GB or more |
| **Storage** | ~100 MB free space | 150 MB free space |
| **CPU Architecture** | `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64` | All Android CPU architectures |
| **Connectivity** | Wi-Fi 2.4 GHz or Hotspot Tethering | Wi-Fi / Hotspot 5 GHz |

---

## 🎮 PS4 Console Requirements

- **Firmware Support**: PS4 System Software **6.00** through **11.02**.
- **Browser**: Built-in PS4 Web Browser or User's Guide.

---

## ✨ Features

- **Standalone Web Server**: All exploit files (HTML, JS, CSS, cache manifest, payload binaries) are embedded directly within the APK.
- **Modern Jetpack Compose UI**: Clean dark theme, responsive layout, and intuitive Start/Stop server toggle.
- **Background Foreground Service**: Keeps the server running continuously in the background even when your phone screen is locked.
- **Automatic IP Detection**: Instantly detects and displays your active local Wi-Fi or Hotspot IPv4 address.
- **Port Selector**: Default port `8080`, with fast preset options (`8000`, `8443`, `8888`, `9000`) or custom port input.
- **Live Access Logs**: Real-time log monitor displaying incoming requests from your PS4 browser (HTTP status code, method, response time, payload transfer size, and client IP).
- **One-Tap Actions**: Copy server URLs to clipboard or preview in your mobile browser with a single tap.

---

## 🚀 How to Use

1. **Connect Network**:
   - Connect your Android device and PS4 console to the **same Wi-Fi network**, **OR**
   - Enable **Portable Hotspot** on your Android phone and connect your PS4 to the phone's hotspot.
2. **Start the Server**:
   - Open **CFF Mobile** on your phone.
   - Tap the **START SERVER** button (default port: `8080`).
3. **Open URL on PS4**:
   - Note the server URL displayed on your phone screen (e.g., `http://192.168.43.1:8080/`).
   - Open the **Web Browser** or **User's Guide** on your PS4, and enter that exact URL.
4. **Monitor**:
   - Check the live access logs in the Android app to observe request processing and payload delivery in real time.

---

## 🛠️ Building from Source

### Prerequisites:
- JDK 17 or JDK 21
- Android SDK (API Level 35)

### Build Debug APK:
```powershell
.\gradlew.bat assembleDebug
```
The APK file will be generated at:
`app/build/outputs/apk/debug/cff-mobile-v0.0.1.apk`

### Install directly to connected Android device:
```powershell
.\gradlew.bat installDebug
```

---

## 📂 Project Structure

```
cff-mobile/
├── app/
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── assets/public/          # Embedded static exploit files & patches
│   │   ├── java/com/example/myapp/
│   │   │   ├── MainActivity.kt
│   │   │   ├── server/             # LocalAssetServer, NetworkUtils, ServerState
│   │   │   ├── service/            # HttpServerService (Foreground Service)
│   │   │   └── ui/                 # HomeScreen (Jetpack Compose)
│   │   └── res/                    # App icons & Strings
├── .github/workflows/
│   └── release.yml                 # Automated GitHub Release CI/CD
├── gradle/wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── README.md                       # English documentation (Default)
└── README_ID.md                    # Indonesian documentation
```

---

## 🔗 Credits & References

- **Mobile Android Application**: [edai-git/cff-mobile](https://github.com/edai-git/cff-mobile)
- **Original CSSFontFace Exploit**: [ntfargo/CSSFontFace-Exploit](https://github.com/ntfargo/CSSFontFace-Exploit)
