# Contributing to QRCraft

Thank you for your interest in contributing to QRCraft! 🎉

We welcome contributions from the community and are pleased to have you join us. This document will guide you through the contribution process.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Setup](#development-setup)
- [Coding Guidelines](#coding-guidelines)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Pull Request Process](#pull-request-process)
- [License](#license)

---

## Code of Conduct

By participating in this project, you agree to maintain a respectful and inclusive environment. Please:

- Be respectful and considerate
- Accept constructive criticism gracefully
- Focus on what's best for the community
- Show empathy towards other contributors

---

## How Can I Contribute?

### 🐛 Reporting Bugs

Before creating bug reports, please check existing issues to avoid duplicates.

**When submitting a bug report, include:**
- Clear, descriptive title
- Steps to reproduce the issue
- Expected vs actual behavior
- Screenshots (if applicable)
- Device info (Android version, device model)
- App version

**Example:**
```
**Bug:** Scanner crashes when scanning WiFi QR codes

**Steps to Reproduce:**
1. Open scanner
2. Point camera at WiFi QR code
3. App crashes immediately

**Expected:** Should parse WiFi credentials
**Actual:** App crashes

**Device:** Samsung Galaxy S21, Android 13
**App Version:** 1.0.0
```

### 💡 Suggesting Features

We love new ideas! When suggesting features:

- Check if the feature already exists or is planned
- Explain the problem your feature would solve
- Describe your proposed solution
- Consider alternative approaches
- Explain why this feature would benefit other users

### 🔧 Code Contributions

We welcome pull requests for:

- Bug fixes
- New features
- Performance improvements
- UI/UX enhancements
- Documentation improvements
- Test coverage expansion

---

## Development Setup

### Prerequisites

- **Android Studio** Iguana (2023.2.1) or later
- **JDK** 17 or later
- **Android SDK** with API 24-36
- **Git**

### Setup Instructions

1. **Fork the repository**
  
   Click '**Fork**' on GitHub, then clone your fork

   ```bash
   git clone https://github.com/YOUR_USERNAME/QRCraft.git
   cd QRCraft
   ```

2. **Open in Android Studio**
   - File → Open → Select QRCraft folder
   - Let Gradle sync complete

3. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/bug-description
   ```

4. **Build and run**
   ```bash
   ./gradlew clean build
   ./gradlew test
   ```

### Project Structure

```
QRCraft/
├── app/src/main/
│   ├── java/com/rejown/qrcraft/
│   │   ├── data/           # Data layer (repositories, database)
│   │   ├── domain/         # Domain layer (models, use cases)
│   │   ├── presentation/   # UI layer (screens, ViewModels)
│   │   ├── utils/          # Utility classes
│   │   └── di/             # Dependency injection
├── app/src/test/           # Unit tests
└── app/src/androidTest/    # Instrumented tests (if needed)
```

---

## Coding Guidelines

### Architecture

QRCraft follows **Clean Architecture** with **MVVM** pattern:

- **Domain Layer:** Business logic and models
- **Data Layer:** Repositories, database, mappers
- **Presentation Layer:** UI (Compose), ViewModels, states

### Code Style

We follow **Kotlin official coding conventions**:

- Use 4 spaces for indentation (no tabs)
- Max line length: 120 characters
- Use meaningful variable names
- Add KDoc comments for public APIs
- Keep functions small and focused

**Example:**
```kotlin
/**
 * Validates a URL with security checks
 *
 * @param url The URL to validate
 * @return Error message if invalid, null if valid
 */
fun validateUrl(url: String): String? {
    if (url.isBlank()) return null

    return when {
        !Patterns.WEB_URL.matcher(url).matches() -> "Invalid URL format"
        url.length > 2048 -> "URL too long"
        containsSuspiciousPatterns(url) -> "URL contains suspicious patterns"
        else -> null
    }
}
```

### Testing

- Write unit tests for business logic
- Aim for meaningful test coverage
- Use descriptive test names
- Follow the Arrange-Act-Assert pattern

**Example:**
```kotlin
@Test
fun `validateUrl returns error for malicious javascript protocol`() {
    // Arrange
    val maliciousUrl = "javascript:alert('xss')"

    // Act
    val result = InputValidator.validateUrl(maliciousUrl)

    // Assert
    assertNotNull(result)
    assertTrue(result.contains("suspicious"))
}
```

### UI Guidelines

- Use **Jetpack Compose** for all UI
- Follow **Material 3** design guidelines
- Ensure **accessibility** (content descriptions, contrast)
- Support both **light and dark** themes
- Test on different screen sizes

### Performance

- Avoid blocking the main thread
- Use Kotlin Coroutines for async operations
- Optimize database queries
- Use Flow for reactive data
- Profile before optimizing

---


## Pull Request Process

### Before Submitting

1. **Update your fork**
   ```bash
   git checkout main
   git pull upstream main
   git checkout feature/your-feature
   git rebase main
   ```

2. **Run tests**
   ```bash
   ./gradlew test
   ./gradlew lint
   ```

3. **Build release**
   ```bash
   ./gradlew assembleRelease
   ```

4. **Update documentation**
   - Update README if needed
   - Add/update KDoc comments
   - Update CHANGELOG.md

### Submitting a Pull Request

1. **Push to your fork**
   ```bash
   git push origin feature/your-feature
   ```

2. **Create Pull Request on GitHub**
   - Use a clear, descriptive title
   - Reference related issues
   - Describe what changed and why
   - Add screenshots for UI changes
   - Check "Allow edits from maintainers"

3. **PR Template**
   ```markdown
   ## Description
   Brief description of changes

   ## Type of Change
   - [ ] Bug fix
   - [ ] New feature
   - [ ] Breaking change
   - [ ] Documentation update

   ## Testing
   - [ ] Unit tests added/updated
   - [ ] Manual testing completed
   - [ ] Build passes

   ## Checklist
   - [ ] Code follows project style
   - [ ] Self-review completed
   - [ ] Documentation updated
   - [ ] No new warnings

   ## Related Issues
   Closes #123
   ```

4. **Code Review**
   - Respond to feedback promptly
   - Make requested changes
   - Push updates to the same branch
   - Be open to suggestions

### After Approval

- Squash commits if requested
- Maintainer will merge your PR
- Your contribution will be credited!

---

## Development Tips

### Running Tests

```bash
# All tests
./gradlew test

# Specific test class
./gradlew test --tests InputValidatorTest

# With coverage
./gradlew test jacocoTestReport
```

### Code Quality

```bash
# Run lint checks
./gradlew lint

# Format code (if ktlint is added)
./gradlew ktlintFormat

# Check for updates
./gradlew dependencyUpdates
```

### Debugging

- Use Android Studio's debugger
- Add Timber logs for debugging (remove before commit)
- Use Compose Preview for UI development
- Test on real devices when possible

---

## Common Issues

### Build Fails with OutOfMemoryError

Increase heap size in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m
```

### Camera Not Working in Emulator

Use a physical device for camera-related features.

### Tests Failing

Ensure you're using the correct Android SDK versions:
- compileSdk: 36
- minSdk: 24
- targetSdk: 36

---

## License

> [!IMPORTANT]
> By contributing to QRCraft, you agree that your contributions will be licensed under the **GPL v3.0** license.
>
> This means:
> - Your code will be open source
> - Derivative works must also be GPL v3.0
> - You retain copyright of your contributions
> - You grant rights to use, modify, and distribute

For more details, see the [LICENSE](LICENSE) file.

---

## Questions?

- **Issues:** [GitHub Issues](https://github.com/ahmmedrejowan/QRCraft/issues)
- **Discussions:** [GitHub Discussions](https://github.com/ahmmedrejowan/QRCraft/discussions)
- **Email:** [ahmmadrejowan@gmail.com](mailto:ahmmadrejowan@gmail.com)

---

## Recognition

All contributors will be recognized in:
- README.md acknowledgments
- GitHub contributors page
- Release notes (for significant contributions)

---

Thank you for contributing to QRCraft! 🎉 Your efforts help make this project better for everyone.

---

**Happy Coding!** 💻✨
