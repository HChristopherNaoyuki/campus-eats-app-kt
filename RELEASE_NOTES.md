# Campus Eats - Release Notes

## Version 4.0.0

**Release Date:** 2026-04-02

---

### Overview

Version 4.0.0 is a major milestone release for the Campus Eats Android platform. This release updates the application icon branding to a solid black adaptive launcher field with a white hamburger mark, enforces explicit Web OAuth Client ID token exchange for Credential Manager Google SSO, refactors feedback and coupon synchronization with payload fingerprinting and session gates, normalizes persistent language preferences to English and Afrikaans with DataStore, and bumps the application version to 4.0.0 (versionCode 6).

---

### Added Features & Enhancements

**1. Updated Adaptive App Branding**
- Replaced the launcher icon branding with a solid black background and a white centered hamburger mark.
- Updated adaptive vector drawables (`ic_launcher_foreground` and `ic_launcher_background`) and launcher XML definitions across `mipmap-anydpi-v26`.

**2. Enforced Web OAuth Client ID for Credential Manager**
- Explicitly assigned `GOOGLE_WEB_CLIENT_ID` (`64265928399-k9qvcvf2e17vh61f98591d860v8md05q.apps.googleusercontent.com`) to `GetGoogleIdOption.setServerClientId` across `LoginScreen` and `RegistrationScreen`.
- Added empty token checks and improved user-facing exception messages for `NoCredentialException` and `GetCredentialException`.

**3. Optimized Cloud Synchronizer Engine**
- Refactored `FirebaseSyncManager` feedback synchronization to process entries under stable campus `feedbackId` keys while requiring an authenticated session.
- Added payload fingerprint checking to `syncCouponsNode` to skip redundant writes when coupon payloads remain unchanged.

---

### Technical Details

**Version Code:** 6

**Version Name:** 4.0.0

**Package Name:** com.example.campus_eats_app_kt

---

## Version 3.1.0

**Release Date:** 2026-04-02

---

### Overview

Version 3.1.0 was a comprehensive technical update for the Campus Eats Android platform resolving Single Sign-On credential exchange issues, persistent dual-language support, session-gated cloud synchronization with exponential backoff, local order status transition notifications, and validated network connectivity.

---

### Added Features

**1. Persistent Dual-Language Localization**
- Integrated Jetpack DataStore preferences (`campus_eats_language`) in `LanguageManager` to persist user language selection across application restarts.
- Initialized saved language preference at startup in `MainActivity` before UI composition.
- Provided UI language selection controls for English and Afrikaans in `SettingsScreenTab`.

**2. Local Order Status Transition Notifications**
- Implemented `NotificationHelper` and registered notification channel `campus_eats_orders` at application launch in `MainActivity`.
- Triggered localized local push notifications whenever an order status transition occurs in `OrderRepository`.

**3. Enhanced Network Connectivity Validation**
- Updated `NetworkConnectivityManager` to inspect both `NET_CAPABILITY_INTERNET` and `NET_CAPABILITY_VALIDATED` network capabilities.

---

## Version 3.0.1

**Release Date:** 2026-03-30

---

### Overview

Version 3.0.1 delivered account, admin, and vendor enhancements including 16-character User ID generation, copy control, User ID account recovery, admin insight reporting, financial controls, vendor revenue calculation, and live order item inspection.

---

## Version 2.0.0

**Release Date:** 2026-08-13

---

### Overview

Version 2.0.0 provided a complete technical overhaul shifting to a hybrid offline-first architecture with Fake Restaurant API integration, vendor fulfillment workflow, and a minimalist HIG Compose UI redesign.

---

## Version 1.0.0

**Release Date:** 2026-01-15

---

### Overview

Initial release establishing core features for Students, Vendors, and Administrators with local Room database storage, fee calculation engine, and initial UI screens.
