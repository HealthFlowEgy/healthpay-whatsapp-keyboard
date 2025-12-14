# HealthPay WhatsApp Keyboard 💳

A custom keyboard mini-app for Android and iOS that embeds HealthPay wallet payments directly into WhatsApp conversations.

## 📱 Overview

Users download the keyboard, authenticate with their HealthPay wallet, and can then send/request payments directly within WhatsApp without leaving the app.

```
┌─────────────────────────────────────────────────────────────┐
│                     WhatsApp Application                     │
├─────────────────────────────────────────────────────────────┤
│              HealthPay Custom Keyboard (IME)                 │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐    │
│  │  💳   │ │  💰   │ │  📊   │ │  🔍   │ │  ⚙️   │    │
│  │  Pay  │ │Request│ │Balance│ │  QR   │ │Settings│    │
│  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │           Standard QWERTY Keyboard                  │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

## 🏗️ Project Structure

```
healthpay-keyboard/
├── android/                      # Android IME Implementation
│   ├── app/src/main/
│   │   ├── java/tech/healthpay/keyboard/
│   │   │   ├── ime/             # Input Method Service
│   │   │   ├── ui/              # Keyboard UI Components
│   │   │   ├── api/             # HealthPay API Client
│   │   │   ├── security/        # Encryption & Auth
│   │   │   ├── viewmodel/       # MVVM ViewModels
│   │   │   ├── model/           # Data Models
│   │   │   ├── di/              # Dependency Injection
│   │   │   └── utils/           # Utilities
│   │   └── res/                 # Android Resources
│   └── gradle/                  # Gradle Configuration
│
├── ios/                         # iOS Keyboard Extension
│   ├── HealthPayKeyboard/       # Main App
│   │   ├── Sources/
│   │   │   ├── Core/           # Core Services
│   │   │   ├── UI/             # SwiftUI Views
│   │   │   ├── API/            # API Layer
│   │   │   ├── Security/       # Keychain & Biometrics
│   │   │   └── Utils/          # Utilities
│   │   └── Resources/          # Assets & Localization
│   └── HealthPayKeyboardExtension/  # Keyboard Extension
│
├── shared/                      # Shared Resources
│   ├── assets/                  # Icons & Images
│   └── localization/            # Arabic/English Strings
│
├── postman/                     # API Testing Collection
└── docs/                        # Documentation
```

## 🚀 Features

### Core Functionality
- **Quick Pay**: Send money using phone number
- **Request Payment**: Generate payment requests
- **Balance Check**: Real-time wallet balance
- **QR Payments**: Scan/generate QR codes
- **Transaction History**: Recent transactions list
- **Payment Links**: Shareable deep links

### Security
- Biometric authentication (Face ID/Touch ID/Fingerprint)
- PIN fallback authentication
- Encrypted token storage (Keystore/Keychain)
- Session timeout management
- Device binding

### Localization
- Arabic (RTL) support
- English support
- Egyptian Pound (EGP) formatting
- Hijri calendar support

## 🔧 Tech Stack

### Android
- **Language**: Kotlin 1.9+
- **Min SDK**: 24 (Android 7.0)
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Network**: Retrofit2 + OkHttp
- **Security**: Android Keystore, BiometricPrompt
- **Async**: Coroutines + Flow

### iOS
- **Language**: Swift 5.9+
- **Min iOS**: 14.0
- **Architecture**: MVVM + Combine
- **Network**: URLSession + async/await
- **Security**: Keychain, LocalAuthentication
- **UI**: UIKit + SwiftUI hybrid

### HealthPay API Integration
- **Base URL**: `https://portal.beta.healthpay.tech`
- **Auth**: OAuth2 Bearer Token
- **Format**: REST JSON

## 📋 API Endpoints Used

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/auth/login` | POST | User authentication |
| `/api/auth/refresh` | POST | Token refresh |
| `/api/wallet/balance` | GET | Get wallet balance |
| `/api/wallet/send` | POST | Send payment |
| `/api/wallet/request` | POST | Request payment |
| `/api/wallet/transactions` | GET | Transaction history |
| `/api/qr/generate` | POST | Generate payment QR |
| `/api/qr/scan` | POST | Process scanned QR |

## 📱 User Flow

```
1. Download HealthPay Keyboard from App Store/Play Store
         ↓
2. Enable keyboard in System Settings
         ↓
3. Open HealthPay Keyboard app → Login with credentials
         ↓
4. Setup biometric authentication
         ↓
5. Open WhatsApp → Switch to HealthPay Keyboard
         ↓
6. Tap 💳 Pay button → Authenticate → Enter amount
         ↓
7. Payment confirmation message inserted in chat
```

## 🛠️ Installation

### Android
```bash
cd android
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### iOS
```bash
cd ios
pod install
open HealthPayKeyboard.xcworkspace
# Build and run on device (keyboard requires real device)
```

## 🔐 Security Considerations

1. **No Keylogging**: Only payment-related data is processed
2. **Isolated Storage**: Credentials stored in secure enclave
3. **Network Security**: All traffic over TLS 1.3
4. **Session Management**: Auto-logout after 15 minutes
5. **Biometric Gate**: Required for all payment actions

## 📄 License

Proprietary - HealthFlow Group © 2024
