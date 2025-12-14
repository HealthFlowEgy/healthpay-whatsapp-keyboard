# HealthPay WhatsApp Keyboard - Setup Guide

This guide will help you set up and build the HealthPay WhatsApp Keyboard project on your local machine.

## Prerequisites

### Android Development
- **Android Studio**: 2023.1 or later
- **Android SDK**: API 34 (Android 14)
- **Gradle**: 8.1.2 (included with Android Studio)
- **Java/Kotlin**: JDK 11 or later
- **Git**: For version control

### iOS Development
- **Xcode**: 15.0 or later
- **iOS Deployment Target**: 14.0 or later
- **CocoaPods**: 1.12.0 or later
- **Swift**: 5.9 or later

## Android Setup

### 1. Clone the Repository

```bash
git clone https://github.com/HealthFlowEgy/healthpay-keyboard.git
cd healthpay-keyboard/android
```

### 2. Configure Android Studio

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the `android` directory
4. Click "Open"

Android Studio will automatically detect the Gradle configuration and sync the project.

### 3. Configure API Keys

Edit `android/app/build.gradle` and update the API configuration:

```gradle
buildConfigField "String", "API_BASE_URL", '"https://portal.beta.healthpay.tech/api/"'
```

For production, use:
```gradle
buildConfigField "String", "API_BASE_URL", '"https://portal.healthpay.tech/api/"'
```

### 4. Build the Project

**Debug Build:**
```bash
./gradlew assembleDebug
```

**Release Build:**
```bash
./gradlew assembleRelease
```

### 5. Run on Emulator or Device

```bash
./gradlew installDebug
```

Or use Android Studio's Run button.

### 6. Run Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## iOS Setup

### 1. Clone the Repository

```bash
git clone https://github.com/HealthFlowEgy/healthpay-keyboard.git
cd healthpay-keyboard/ios
```

### 2. Install Dependencies

```bash
# Install CocoaPods if not already installed
sudo gem install cocoapods

# Install project dependencies
pod install

# If you encounter issues, try:
pod install --repo-update
```

### 3. Open Xcode Workspace

```bash
open HealthPayKeyboard.xcworkspace
```

**Important**: Always open the `.xcworkspace` file, not the `.xcodeproj` file.

### 4. Configure API Keys

In Xcode:
1. Select the project in the navigator
2. Select the target "HealthPayKeyboard"
3. Go to Build Settings
4. Search for "API_BASE_URL"
5. Update the value for Debug and Release configurations

### 5. Build the Project

**Debug Build:**
```bash
xcodebuild -workspace HealthPayKeyboard.xcworkspace -scheme HealthPayKeyboard -configuration Debug
```

**Release Build:**
```bash
xcodebuild -workspace HealthPayKeyboard.xcworkspace -scheme HealthPayKeyboard -configuration Release
```

### 6. Run on Simulator or Device

Using Xcode:
1. Select the target device/simulator from the top toolbar
2. Click the "Play" button to build and run

Using command line:
```bash
xcodebuild -workspace HealthPayKeyboard.xcworkspace -scheme HealthPayKeyboard -configuration Debug -derivedDataPath build
```

### 7. Run Tests

```bash
xcodebuild -workspace HealthPayKeyboard.xcworkspace -scheme HealthPayKeyboard -configuration Debug test
```

## Project Structure

```
healthpay-keyboard/
├── android/
│   ├── app/
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/tech/healthpay/keyboard/
│   │   │   │   ├── res/
│   │   │   │   └── AndroidManifest.xml
│   │   │   ├── test/
│   │   │   └── androidTest/
│   │   ├── build.gradle
│   │   └── proguard-rules.pro
│   ├── build.gradle
│   ├── settings.gradle
│   ├── gradle.properties
│   └── gradlew
│
├── ios/
│   ├── HealthPayKeyboard/
│   │   ├── Sources/
│   │   └── Resources/
│   ├── HealthPayKeyboardExtension/
│   ├── HealthPayKeyboardTests/
│   ├── Podfile
│   └── Podfile.lock
│
├── postman/
│   └── HealthPay-Keyboard-API.postman_collection.json
│
├── docs/
│   └── Integration Guide
│
├── README.md
├── SETUP.md
└── .gitignore
```

## Configuration

### Environment Variables

Create a `.env` file in the project root (not committed to git):

```env
API_BASE_URL=https://portal.beta.healthpay.tech/api/
API_TIMEOUT=30
DEBUG_MODE=true
```

### API Configuration

**Beta Environment:**
- URL: `https://portal.beta.healthpay.tech/api/`
- Use for testing and development

**Production Environment:**
- URL: `https://portal.healthpay.tech/api/`
- Use for release builds only

## Testing

### Android Unit Tests

```bash
./gradlew test
```

### Android Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

### iOS Unit Tests

```bash
xcodebuild -workspace HealthPayKeyboard.xcworkspace -scheme HealthPayKeyboard test
```

### Postman Testing

1. Import `postman/HealthPay-Keyboard-API.postman_collection.json` into Postman
2. Configure environment variables:
   - `api_url`: API base URL
   - `access_token`: Bearer token from login
3. Run the collection

## Troubleshooting

### Android Issues

**Gradle sync fails:**
- Clear cache: `./gradlew clean`
- Invalidate Android Studio cache: File → Invalidate Caches → Invalidate and Restart

**Build fails with "Cannot find symbol":**
- Run: `./gradlew clean build`
- Check that all dependencies are downloaded

**Emulator issues:**
- Use API level 24 or higher
- Ensure sufficient disk space (at least 5GB)
- Try: `emulator -avd <name> -wipe-data`

### iOS Issues

**Pod install fails:**
```bash
pod install --repo-update
rm -rf Pods Podfile.lock
pod install
```

**Xcode build fails:**
- Clean build folder: Cmd + Shift + K
- Delete derived data: `rm -rf ~/Library/Developer/Xcode/DerivedData/*`
- Restart Xcode

**Keyboard extension not appearing:**
- Ensure "Allow Full Access" is enabled in Settings
- Restart the device
- Reinstall the app

## Development Workflow

### 1. Create a Feature Branch

```bash
git checkout -b feature/your-feature-name
```

### 2. Make Changes

- Follow the code style guidelines
- Write tests for new features
- Update documentation

### 3. Commit Changes

```bash
git add .
git commit -m "feat: description of changes"
```

### 4. Push to GitHub

```bash
git push origin feature/your-feature-name
```

### 5. Create a Pull Request

- Go to GitHub
- Create a PR with a clear description
- Wait for review and CI/CD checks

## Code Style Guidelines

### Kotlin (Android)

- Use 4 spaces for indentation
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable names
- Add KDoc comments for public APIs

### Swift (iOS)

- Use 4 spaces for indentation
- Follow [Swift API Design Guidelines](https://swift.org/documentation/api-design-guidelines/)
- Use meaningful variable names
- Add MARK comments for code organization

## Performance Optimization

### Android

- Use ProGuard/R8 for release builds
- Monitor memory usage with Android Profiler
- Use LeakCanary for memory leak detection

### iOS

- Use Instruments for profiling
- Monitor memory with Xcode Memory Debugger
- Use Shark for performance analysis

## Security Considerations

- Never commit API keys or secrets
- Use `.env` files for sensitive data
- Enable code obfuscation for release builds
- Regularly update dependencies
- Use certificate pinning for API communication

## Deployment

### Android

1. Update version code and name in `build.gradle`
2. Create a release build: `./gradlew assembleRelease`
3. Sign the APK with your release key
4. Upload to Google Play Console

### iOS

1. Update version number in Xcode
2. Create an archive: Product → Archive
3. Validate and upload to App Store Connect
4. Submit for review

## Support & Documentation

- **Integration Guide**: See `docs/INTEGRATION_GUIDE.md`
- **API Documentation**: See `postman/` collection
- **Project Analysis**: See `PROJECT_ANALYSIS.md`
- **Issues**: Report on GitHub Issues
- **Email**: support@healthpay.tech

## License

Proprietary - HealthFlow Group

---

**Last Updated**: December 14, 2025
