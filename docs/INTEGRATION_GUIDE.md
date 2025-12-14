# HealthPay WhatsApp Keyboard - Integration Guide

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [API Integration](#api-integration)
4. [Security Implementation](#security-implementation)
5. [User Flows](#user-flows)
6. [Testing Guide](#testing-guide)
7. [Deployment](#deployment)
8. [Troubleshooting](#troubleshooting)

---

## Overview

The HealthPay WhatsApp Keyboard is a custom input method (IME) that embeds HealthPay wallet functionality directly into WhatsApp conversations. Users can send payments, request money, and check balances without leaving the chat.

### Key Features

| Feature | Description |
|---------|-------------|
| **Quick Pay** | Send money using phone number |
| **Request Payment** | Generate shareable payment links |
| **Balance Check** | Real-time wallet balance display |
| **QR Payments** | Scan/generate QR codes |
| **Transaction History** | View recent transactions |
| **Biometric Auth** | Face ID/Touch ID/Fingerprint |
| **Arabic Support** | Full RTL keyboard layout |

### Platform Support

- **Android**: 7.0+ (API 24+)
- **iOS**: 14.0+

---

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      User Device                             │
│  ┌─────────────────────────────────────────────────────────┐│
│  │                   WhatsApp App                          ││
│  │  ┌───────────────────────────────────────────────────┐  ││
│  │  │              HealthPay Keyboard                   │  ││
│  │  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ │  ││
│  │  │  │  Pay    │ │ Request │ │ Balance │ │   QR    │ │  ││
│  │  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘ │  ││
│  │  │  ┌───────────────────────────────────────────────┐│  ││
│  │  │  │             QWERTY / Arabic Keyboard          ││  ││
│  │  │  └───────────────────────────────────────────────┘│  ││
│  │  └───────────────────────────────────────────────────┘  ││
│  └─────────────────────────────────────────────────────────┘│
│                           │                                  │
│                           │ HTTPS/TLS 1.3                    │
│                           ▼                                  │
└───────────────────────────┼──────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   HealthPay Backend                          │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐│
│  │   Auth    │  │  Wallet   │  │    QR     │  │   User    ││
│  │  Service  │  │  Service  │  │  Service  │  │  Service  ││
│  └───────────┘  └───────────┘  └───────────┘  └───────────┘│
│                           │                                  │
│                           ▼                                  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                    Database                            │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Component Overview

#### Android Components

| Component | File | Purpose |
|-----------|------|---------|
| `HealthPayInputMethodService` | `ime/HealthPayInputMethodService.kt` | Main keyboard service |
| `HealthPayKeyboardView` | `ui/HealthPayKeyboardView.kt` | Keyboard UI |
| `HealthPayApiClient` | `api/HealthPayApiClient.kt` | API communication |
| `WalletViewModel` | `viewmodel/ViewModels.kt` | Business logic |
| `AuthenticationManager` | `security/Security.kt` | Auth handling |
| `TokenManager` | `security/Security.kt` | Secure token storage |
| `BiometricHelper` | `security/Security.kt` | Biometric auth |

#### iOS Components

| Component | File | Purpose |
|-----------|------|---------|
| `HealthPayKeyboardViewController` | `KeyboardViewController.swift` | Main controller |
| `HealthPayKeyboardView` | `HealthPayKeyboardView.swift` | Keyboard UI |
| `WalletService` | `Services.swift` | API communication |
| `AuthenticationManager` | `Services.swift` | Auth handling |
| `TokenManager` | `Services.swift` | Keychain storage |
| `BiometricHelper` | `Services.swift` | Face/Touch ID |

---

## API Integration

### Base Configuration

```
Production URL: https://portal.healthpay.tech/api
Beta URL: https://portal.beta.healthpay.tech/api
```

### Authentication

All API requests (except login) require a Bearer token:

```http
Authorization: Bearer <access_token>
```

### API Endpoints

#### Authentication

```http
POST /auth/login
Content-Type: application/json

{
    "username": "user@example.com",
    "password": "password123"
}

Response:
{
    "accessToken": "eyJhbG...",
    "refreshToken": "eyJhbG...",
    "expiresIn": 3600,
    "user": {
        "id": "usr_123",
        "name": "John Doe",
        "phone": "+201234567890"
    }
}
```

#### Wallet Balance

```http
GET /wallet/balance

Response:
{
    "available": 5000.00,
    "pending": 150.00,
    "currency": "EGP",
    "lastUpdated": "2024-01-15T10:30:00Z"
}
```

#### Send Payment

```http
POST /wallet/send
Content-Type: application/json

{
    "amount": 100.00,
    "recipientPhone": "+201234567890",
    "description": "Payment via WhatsApp"
}

Response:
{
    "id": "txn_abc123",
    "type": "SENT",
    "status": "COMPLETED",
    "amount": 100.00,
    "recipientPhone": "+201234567890",
    "referenceNumber": "HP-2024-12345",
    "createdAt": "2024-01-15T10:35:00Z"
}
```

#### Request Payment

```http
POST /wallet/request
Content-Type: application/json

{
    "amount": 50.00,
    "description": "Dinner split",
    "expiresIn": 24
}

Response:
{
    "link": "https://pay.healthpay.tech/p/abc123",
    "qrCode": "data:image/png;base64,...",
    "expiresAt": "2024-01-16T10:35:00Z"
}
```

---

## Security Implementation

### Token Storage

#### Android (EncryptedSharedPreferences)

```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "healthpay_secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

#### iOS (Keychain)

```swift
let query: [String: Any] = [
    kSecClass as String: kSecClassGenericPassword,
    kSecAttrAccount as String: key,
    kSecAttrAccessGroup as String: "group.tech.healthpay.keyboard",
    kSecValueData as String: data,
    kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlock
]
```

### Biometric Authentication

Payment actions require biometric authentication before processing:

```kotlin
// Android
biometricHelper.authenticate(
    context = this,
    title = "Authenticate to Pay",
    subtitle = "Confirm your identity",
    onSuccess = { processPayment() },
    onError = { showError() }
)
```

```swift
// iOS
context.evaluatePolicy(
    .deviceOwnerAuthenticationWithBiometrics,
    localizedReason: "Authenticate to pay"
) { success, error in
    if success { processPayment() }
}
```

### Security Best Practices

1. **No Keylogging**: Only payment-related data is processed
2. **Session Timeout**: Auto-logout after 15 minutes of inactivity
3. **Device Binding**: Tokens are device-specific
4. **Network Security**: All traffic over TLS 1.3
5. **PIN Encryption**: User PINs encrypted before transmission

---

## User Flows

### First-Time Setup

```
1. Download App
   └── Play Store / App Store

2. Enable Keyboard
   └── Android: Settings → System → Languages → Keyboards
   └── iOS: Settings → General → Keyboard → Keyboards

3. Grant Permissions
   └── Full Access (iOS)
   └── Network/Camera (Android)

4. Login
   └── Open HealthPay Keyboard app
   └── Enter credentials
   └── Setup biometric auth

5. Ready to Use
   └── Open WhatsApp
   └── Switch to HealthPay keyboard
```

### Payment Flow

```
1. User types in WhatsApp
   └── Switches to HealthPay keyboard

2. Taps 💳 Pay button
   └── Biometric prompt appears

3. Enters payment details
   └── Amount: EGP 100
   └── Recipient: +20123456789
   └── Note: Coffee ☕

4. Confirms payment
   └── API call to /wallet/send
   └── Transaction processed

5. Message inserted
   └── 💳 *HealthPay Payment Sent*
   └── Amount: EGP 100.00
   └── To: +20123456789
   └── Ref: HP-2024-12345
   └── ✅ Powered by HealthPay
```

---

## Testing Guide

### Test Credentials

```
Portal URL: https://portal.beta.healthpay.tech
Username: beta.account@healthpay.tech
Password: BetaAcc@HealthPay2024
```

### Test Cases

| Test Case | Steps | Expected Result |
|-----------|-------|-----------------|
| Login | Enter credentials, tap Login | Tokens stored, balance shown |
| Send Payment | Tap Pay, enter amount & phone | Payment processed, message inserted |
| Request Payment | Tap Request, enter amount | Payment link generated |
| View Balance | Tap balance card | Current balance displayed |
| Session Timeout | Wait 15 mins inactive | Auto logout, re-auth required |
| Network Error | Disable network, try action | Error message shown |

### Postman Collection

Import the collection from `postman/HealthPay-Keyboard-API.postman_collection.json`

---

## Deployment

### Android

```bash
# Debug build
cd android
./gradlew assembleDebug

# Release build (requires signing)
./gradlew assembleRelease

# Install
adb install app/build/outputs/apk/debug/app-debug.apk
```

### iOS

```bash
cd ios
pod install
open HealthPayKeyboard.xcworkspace

# Build and archive through Xcode
# Note: Keyboard extensions must be tested on real devices
```

### Release Checklist

- [ ] Update version numbers
- [ ] Test on multiple devices
- [ ] Verify API endpoints (production vs beta)
- [ ] Check all translations
- [ ] Review security configurations
- [ ] Test biometric auth flows
- [ ] Verify deep links work
- [ ] Submit for store review

---

## Troubleshooting

### Common Issues

#### Keyboard not appearing in list

**Android:**
- Check AndroidManifest.xml has correct IME service declaration
- Verify `android.permission.BIND_INPUT_METHOD` permission
- Restart device after installation

**iOS:**
- Enable "Allow Full Access" in Settings
- Check bundle identifiers match
- Verify App Groups are configured

#### Authentication failures

- Clear app data and re-login
- Check network connectivity
- Verify correct API endpoint (beta vs production)
- Check token expiration

#### Payment not processing

- Verify biometric auth succeeded
- Check sufficient balance
- Validate phone number format
- Check API response for specific error

### Support Contacts

- Technical Support: support@healthpay.tech
- API Issues: api-support@healthpay.tech
- Documentation: docs@healthpay.tech

---

## Appendix

### Error Codes

| Code | Message | Resolution |
|------|---------|------------|
| AUTH001 | Invalid credentials | Check username/password |
| AUTH002 | Token expired | Re-authenticate |
| PAY001 | Insufficient balance | Top up wallet |
| PAY002 | Invalid recipient | Check phone number |
| PAY003 | Daily limit exceeded | Wait or contact support |
| NET001 | Network error | Check connection |
| SRV001 | Server error | Try again later |

### Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2024-01 | Initial release |
