# CFF Mobile (v0.0.1)

[English](README.md) | [Bahasa Indonesia](README_ID.md)

---

[![GitHub Release](https://img.shields.io/github/v/release/edai-git/cff-mobile?color=blue&logo=github)](https://github.com/edai-git/cff-mobile/releases/latest)
[![Android Min SDK](https://img.shields.io/badge/Android-10.0%2B%20(API%2029%2B)-brightgreen?logo=android)](https://developer.android.com)
[![PS4 Firmware](https://img.shields.io/badge/PS4%20Firmware-6.00%20--%2011.02-orange?logo=playstation)](https://github.com/ntfargo/CSSFontFace-Exploit)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**CFF Mobile** adalah aplikasi Android mandiri (*standalone*) yang berfungsi sebagai server HTTP lokal untuk meng-host **CSSFontFace PS4 WebKit Exploit** langsung dari smartphone Anda. Anda dapat menjalankan exploit pada konsol PS4 tanpa memerlukan PC, laptop, ataupun koneksi internet.

---

## 📥 Download & Rilis

Unduh file APK rilis terbaru langsung melalui link berikut:

👉 **[Download CFF Mobile APK (Latest Release)](https://github.com/edai-git/cff-mobile/releases/latest)**

| Rilis | File APK | Versi | Status |
| :--- | :--- | :--- | :--- |
| **Latest** | `cff-mobile-v0.0.1.apk` | `v0.0.1` | Stable |

---

## 📱 Spesifikasi Minimum Android (Min Spec)

| Komponen | Spesifikasi Minimum | Rekomendasi |
| :--- | :--- | :--- |
| **Sistem Operasi** | Android 10.0 (API Level 29) | Android 11.0 – Android 15 (API 35) |
| **Root Access** | **Tidak Perlu (Non-Root)** | Non-Root |
| **RAM** | 1 GB | 2 GB atau lebih |
| **Ruang Penyimpanan** | ~100 MB ruang kosong | 150 MB ruang kosong |
| **Arsitektur CPU** | `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64` | Semua tipe CPU Android |
| **Jaringan** | Wi-Fi 2.4 GHz atau Hotspot Tethering | Wi-Fi / Hotspot 5 GHz |

---

## 🎮 Kebutuhan Konsol PS4

- **Firmware PS4**: Versi **6.00** hingga **11.02**.
- **Aplikasi**: Web Browser bawaan PS4 atau User's Guide (Panduan Pengguna).

---

## ✨ Fitur Utama

- **Standalone Web Server**: Semua file exploit (HTML, JS, CSS, cache manifest, payload binaries) sudah ter-embed di dalam aplikasi.
- **Modern Jetpack Compose UI**: Tampilan dark mode yang elegan, responsif, dan mudah digunakan dengan tombol Start/Stop server yang jelas.
- **Background Foreground Service**: Server tetap berjalan stabil di latar belakang meski layar HP mati atau terkunci (*lock screen*).
- **Auto IP Detection**: Mendeteksi alamat IP lokal (Wi-Fi atau Portable Hotspot) secara otomatis.
- **Port Selector**: Port default `8080`, dilengkapi preset cepat (`8000`, `8443`, `8888`, `9000`) atau custom port.
- **Live Access Logs**: Pantau setiap request yang masuk dari browser PS4 secara real-time (Status code, Method, Latency ms, Ukuran byte, IP Client).
- **One-Tap Copy & Open**: Salin URL server dengan sekali klik atau buka langsung di browser HP untuk preview.

---

## 🚀 Panduan Penggunaan

1. **Hubungkan Jaringan**:
   - Sambungkan HP Android dan konsol PS4 ke **jaringan Wi-Fi yang sama**, **ATAU**
   - Aktifkan **Hotspot Portabel** di HP Android Anda, lalu sambungkan Wi-Fi PS4 ke hotspot tersebut.
2. **Jalankan Aplikasi**:
   - Buka aplikasi **CFF Mobile** di HP.
   - Tekan tombol **START SERVER** (port default: `8080`).
3. **Buka di PS4**:
   - Lihat URL yang tertera pada layar aplikasi (misal: `http://192.168.43.1:8080/`).
   - Buka **Web Browser** atau **User's Guide** pada PS4, lalu ketik URL tersebut.
4. **Pantau Proses**:
   - Pantau live access logs pada aplikasi untuk melihat pengiriman file dan payload secara langsung.

---

## 🛠️ Build dari Source Code

### Prasyarat:
- JDK 17 atau JDK 21
- Android SDK (API Level 35)

### Build Debug APK:
```powershell
.\gradlew.bat assembleDebug
```
File APK akan otomatis dihasilkan di:
`app/build/outputs/apk/debug/cff-mobile-v0.0.1.apk`

### Install Langsung ke HP (via USB Debugging):
```powershell
.\gradlew.bat installDebug
```

---

## 📂 Struktur Project

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
├── README.md                       # Dokumentasi Bahasa Inggris (Default)
└── README_ID.md                    # Dokumentasi Bahasa Indonesia
```

---

## 🔗 Referensi & Kredit

- **Aplikasi Mobile Android**: [edai-git/cff-mobile](https://github.com/edai-git/cff-mobile)
- **Original CSSFontFace Exploit**: [ntfargo/CSSFontFace-Exploit](https://github.com/ntfargo/CSSFontFace-Exploit)
