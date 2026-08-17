# CFF Mobile (v0.0.1)

[![Android](https://img.shields.io/badge/Platform-Android%2010%2B%20(API%2029%2B)-brightgreen.svg)](https://developer.android.com)
[![Version](https://img.shields.io/badge/Version-0.0.1-blue.svg)](https://github.com/edai-git/cff-mobile)
[![PS4 Firmware](https://img.shields.io/badge/PS4%20Firmware-6.00%20--%2011.02-orange.svg)](https://github.com/ntfargo/CSSFontFace-Exploit)

**CFF Mobile** is an Android application that acts as a standalone local HTTP web server hosting the **CSSFontFace PS4 WebKit Exploit** directly from your smartphone. No PC or internet connection is required on your PS4 console.

---

## ✨ Features

- **Standalone Embedded Server**: Serves all HTML, CSS, JavaScript, and payload binaries directly from the app's embedded assets.
- **Modern Jetpack Compose UI**: Clean dark theme with intuitive Start/Stop toggle button and real-time status animation.
- **Foreground Service**: Keeps the local server alive in the background even when your phone screen is locked.
- **Network Auto-Detection**: Automatically detects and displays your phone's active Wi-Fi and Hotspot IPv4 addresses.
- **Port Selector**: Configurable port (default `8080`, with presets for `8000`, `8443`, `8888`, `9000`).
- **Live Access Logs**: Real-time console showing incoming browser requests, HTTP status codes, latency, client IP, and payload transfer sizes.
- **One-Tap Actions**: Copy server URLs to clipboard or preview in your mobile browser.

---

## 📋 Requirements

- **Android Device**: Android 10 (API 29) or newer.
- **PS4 Console**: Firmware 6.00 to 11.02.
- **Network**: Both Android device and PS4 connected to the same Wi-Fi network (or PS4 connected to Android Portable Hotspot).

---

## 🚀 How to Use

1. **Connect Devices**:
   - Connect your Android phone and PS4 to the **same Wi-Fi network**, **OR**
   - Turn on **Portable Hotspot** on your Android phone and connect your PS4 to the phone's hotspot.
2. **Start Host Server**:
   - Open **CFF Mobile** on your phone.
   - Tap **START SERVER** (Default port is `8080`).
3. **Open URL on PS4**:
   - Note the URL shown on the screen (e.g., `http://192.168.43.1:8080/`).
   - On your PS4, open the **Web Browser** or **User's Guide** and enter that exact URL.
4. **Monitor**:
   - Watch the live access logs in the Android app to see request progress in real time.

---

## 🛠️ Building from Source

### Prerequisites
- JDK 17 or JDK 21
- Android SDK (API 35, Build Tools 35.0.0)

### Build Debug APK:
```powershell
.\gradlew.bat assembleDebug
```
The generated APK will be at:
`app/build/outputs/apk/debug/app-debug.apk`

### Install directly to connected device:
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
│   │   ├── assets/public/          # Embedded static web files & patches
│   │   ├── java/com/example/myapp/
│   │   │   ├── MainActivity.kt
│   │   │   ├── server/             # LocalAssetServer, NetworkUtils, ServerState
│   │   │   ├── service/            # HttpServerService (Foreground Service)
│   │   │   └── ui/                 # HomeScreen (Jetpack Compose)
│   │   └── res/                    # App icons and strings
├── gradle/wrapper/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🔗 Credits & References

- **Mobile Application**: [edai-git/cff-mobile](https://github.com/edai-git/cff-mobile)
- **Original CSSFontFace Exploit**: [ntfargo/CSSFontFace-Exploit](https://github.com/ntfargo/CSSFontFace-Exploit)
