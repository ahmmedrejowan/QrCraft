# QRCraft

<div align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" alt="QRCraft Logo" width="120" height="120">

  <h3>Professional QR Code & Barcode Scanner + Generator</h3>

  <p>
    A modern, feature-rich Android app for scanning and generating QR codes and barcodes with 26 professional templates.
  </p>

  [![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=flat)](https://www.android.com/)
  [![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
  [![License](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](LICENSE)
  [![Kotlin](https://img.shields.io/badge/Kotlin-100%25-purple.svg)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5-blue.svg)](https://developer.android.com/jetpack/compose)
</div>

---

## 📱 Features

### Scanner Features
- **Real-time Scanning** - Fast and accurate QR code and barcode detection
- **Multiple Format Support** - QR Code, EAN-13, EAN-8, UPC-A, UPC-E, Code 39, Code 93, Code 128, ITF, Codabar, Data Matrix, Aztec, PDF417
- **Smart Content Detection** - Automatically detects URLs, emails, phone numbers, WiFi credentials, and more
- **Gallery Import** - Scan QR codes from images in your gallery
- **Flashlight Control** - Toggle flashlight for scanning in low light
- **Scan History** - Keep track of all your scans with timestamps
- **Favorites** - Mark important scans for quick access
- **Export & Share** - Share scan results or export history

### Generator Features
- **26 Professional Templates** including:
  - 📝 **General & Personal**: Plain Text, Numbers, Custom Data
  - 📞 **Communication**: Phone, Email, SMS, Contact (vCard), WhatsApp
  - 🌐 **Social & Web**: URL, Social Profiles, WiFi, YouTube
  - 📍 **Location & Events**: Geo Location, Calendar Event, Google Maps
  - 💼 **Business**: Business Card, Digital Resume, Crypto Wallet, Payment Links
  - 📦 **Product & Inventory**: Product Barcode, Product QR, Inventory Tags
  - 📄 **Documents**: PDF Links, App Downloads, File Sharing
  - 🎟️ **Tickets & Passes**: Event Tickets, Boarding Passes, Coupons

- **Advanced Customization**:
  - Multiple barcode format support (QR Code, EAN-13, Code 128, Data Matrix, etc.)
  - Custom colors (foreground & background)
  - Adjustable size and margin
  - Error correction levels (Low, Medium, Quartile, High)

- **Smart Input Validation** - Real-time validation for URLs, emails, phone numbers
- **History Management** - All generated codes saved with metadata
- **Export Options** - Save to gallery, share, or export as PNG

### Design & UX
- **Modern Material 3 Design** - Beautiful, intuitive interface
- **Dark Mode Support** - System-based or manual theme switching
- **Dynamic Colors** - Android 12+ Material You support
- **Smooth Animations** - Polished transitions and haptic feedback
- **Onboarding Experience** - First-time user guide
- **100% Offline** - No internet required, complete privacy

---

## 📸 Screenshots

*Coming soon - Screenshots will be added in the next update*

---

## 🏗️ Architecture

QRCraft follows **Clean Architecture** principles with **MVVM** pattern:

```
app/
├── data/                      # Data layer
│   ├── local/
│   │   ├── database/         # Room database
│   │   └── preferences/      # DataStore preferences
│   ├── mappers/              # Entity ↔ Domain mapping
│   └── repository/           # Repository implementations
│
├── domain/                    # Domain layer (business logic)
│   ├── models/               # Domain models
│   └── repository/           # Repository interfaces
│
├── presentation/              # Presentation layer (UI)
│   ├── scanner/              # Scanner feature
│   ├── generator/            # Generator feature
│   ├── history/              # History feature
│   ├── settings/             # Settings feature
│   └── navigation/           # Navigation
│
└── utils/                     # Utilities
    ├── generator/            # Code generation utilities
    └── scanner/              # Code scanning utilities
```

### Tech Stack

- **UI Framework**: Jetpack Compose (100% Compose UI)
- **Language**: Kotlin (100%)
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Koin
- **Database**: Room (with proper migrations)
- **Async**: Kotlin Coroutines + Flow
- **Navigation**: Jetpack Navigation Compose
- **Camera**: CameraX
- **ML Kit**: Google ML Kit Barcode Scanning
- **Barcode Generation**: ZXing (Zebra Crossing)
- **Image Loading**: Coil
- **Logging**: Timber
- **Data Storage**: DataStore (Preferences)

---

## 📋 Requirements

- **Minimum SDK**: API 24 (Android 7.0 Nougat)
- **Target SDK**: API 36 (Android 15)
- **Compile SDK**: API 36
- **Gradle**: 8.13
- **AGP**: 8.7.3
- **Kotlin**: 2.1.0
- **Java**: 17

### Permissions
- `CAMERA` - For QR code and barcode scanning
- `VIBRATE` - For haptic feedback on successful scans

**Note:** This app is 100% offline and does not require internet permission.

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17 or newer
- Android SDK with API 36

### Building the Project

1. **Clone the repository**
   ```bash
   git clone https://github.com/ahmmedrejowan/QRCraft.git
   cd QRCraft
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Android Studio will automatically sync Gradle
   - Wait for dependencies to download

4. **Build & Run**
   - Connect an Android device or start an emulator
   - Click "Run" (Shift + F10) or use:
   ```bash
   ./gradlew assembleDebug
   ```

### Generate Release APK

```bash
./gradlew assembleRelease
```

The APK will be generated at: `app/build/outputs/apk/release/`

---

## 🧪 Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

**Note**: Test coverage is currently being developed and will be added in future releases.

---

## 📦 Dependencies

### Core
- AndroidX Core KTX
- AndroidX Lifecycle Runtime
- AndroidX Activity Compose

### UI
- Jetpack Compose BOM (2024.12.01)
- Material 3
- Material Icons Extended
- Compose UI Tooling
- Google Fonts (Compose)

### Navigation
- Navigation Compose

### Dependency Injection
- Koin for Android
- Koin for Compose

### Database
- Room Runtime
- Room KTX
- Room Compiler (KSP)

### Camera & ML
- CameraX (Camera2, Lifecycle, View)
- ML Kit Barcode Scanning

### Barcode Generation
- ZXing Core
- ZXing Android Embedded

### Storage
- DataStore Preferences

### Image Loading
- Coil Compose

### Utilities
- Timber (Logging)
- Kotlin Serialization JSON
- Splash Screen API

### Testing
- JUnit
- AndroidX Test (JUnit, Espresso)

---

## 🗺️ Roadmap

### v1.1 (Planned)
- [ ] Batch QR code generation
- [ ] QR code customization (logo embedding)
- [ ] Export/Import settings and history
- [ ] Advanced search and filtering
- [ ] Scan statistics and analytics

### v1.2 (Future)
- [ ] Cloud backup (optional)
- [ ] Multi-language support
- [ ] QR code templates marketplace
- [ ] Batch scanning mode
- [ ] Widget support

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

### Development Guidelines
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Follow the existing code style and architecture
4. Add tests for new features (when test infrastructure is ready)
5. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
6. Push to the branch (`git push origin feature/AmazingFeature`)
7. Open a Pull Request

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions small and focused
- Follow Clean Architecture principles

---

## 📄 License

This project is licensed under the **GNU General Public License v3.0** - see the [LICENSE](LICENSE) file for details.

### What this means:
- ✅ You can use this code for commercial purposes
- ✅ You can modify and distribute this code
- ✅ You can use this code privately
- ⚠️ You must disclose the source code of your modifications
- ⚠️ You must use the same GPL v3 license
- ⚠️ You must document your changes

---

## 👨‍💻 Author

**K M Rejowan Ahmmed**

- GitHub: [@ahmmedrejowan](https://github.com/ahmmedrejowan)
- Email: [ahmmadrejowan@gmail.com](mailto:ahmmadrejowan@gmail.com)

---

## 🙏 Acknowledgments

- [ZXing](https://github.com/zxing/zxing) - Barcode generation and scanning
- [Google ML Kit](https://developers.google.com/ml-kit/vision/barcode-scanning) - Fast barcode scanning
- [CameraX](https://developer.android.com/jetpack/androidx/releases/camera) - Modern camera API
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern Android UI toolkit
- [Material Design 3](https://m3.material.io/) - Design system

---

## 📝 Changelog

### v1.0.0 (2025-01-13) - Initial Release
- ✨ Real-time QR code and barcode scanning
- ✨ 26 professional code generation templates
- ✨ Multiple barcode format support
- ✨ Scan and generation history
- ✨ Favorites management
- ✨ Material 3 design with dark mode
- ✨ 100% offline functionality
- ✨ Export and share capabilities

---

## ⚠️ Beta Disclaimer

This is a **beta release**. While the app is fully functional, some features are still being polished:
- Test coverage is being developed
- Some edge cases may not be handled
- Performance optimizations ongoing

Please report any issues on the [GitHub Issues](https://github.com/ahmmedrejowan/QRCraft/issues) page.

---

## 💡 Support

If you find this project useful, please consider:
- ⭐ Starring the repository
- 🐛 Reporting bugs and issues
- 💬 Suggesting new features
- 🔀 Contributing code improvements

---

<div align="center">
  <p>Made with ❤️ and Kotlin</p>
  <p>© 2025 K M Rejowan Ahmmed</p>
</div>
