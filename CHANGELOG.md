# Changelog

All notable changes to QRCraft will be documented in this file.

## [1.0.0] - 2025-01-13

### Added

- **QR Code Scanner** - Real-time scanning with ML Kit for 10+ barcode formats
- **QR Code Generator** - 26 professional templates across 8 categories
- **History Management** - Unified history with search, favorites, and filtering
- **Customization** - Colors, size, error correction, and margin settings
- **Material Design 3** - Modern UI with dark mode and dynamic colors
- **Comprehensive Validation** - Security-focused input validation for all fields
- **Clean Architecture** - Domain-driven design with MVVM pattern
- **100% Offline** - No internet permission, all data stays local

### Technical

- Min SDK 24, Target SDK 36
- Jetpack Compose UI
- Room database with proper migrations
- ProGuard/R8 enabled (30-40% smaller APK)
- Zero storage footprint (regenerates QR codes on-demand)
- Only 2 permissions: CAMERA, VIBRATE

### Known Limitations

- No automated tests (0% coverage)
- Dropdown fields show as read-only text
- No custom template creation

---

## License

GNU General Public License v3.0 - See [LICENSE](LICENSE) for details.
