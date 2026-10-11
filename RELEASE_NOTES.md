# Campus Eats - Release Notes

## Version 3.1.0

**Release Date:** 2026-04-02

---

### Overview

Version 3.1.0 is a comprehensive technical update for the Campus Eats Android platform. This release resolves critical Single Sign-On credential exchange issues, integrates persistent dual-language support, establishes a single session-gated cloud synchronizer with exponential backoff, adds local order status transition notifications, and hardens network connectivity validation across the application.

---

### Added Features

**1. Persistent Dual-Language Localization**
- Integrated Jetpack DataStore preferences (`campus_eats_language`) in `LanguageManager` to persist user language selection across application restarts.
- Initialized saved language preference at startup in `MainActivity` before UI composition.
- Provided UI language selection controls for English and Afrikaans in `SettingsScreenTab`. UI chrome (tabs, labels, buttons, headers, dialogs, empty states, and error prompts) updates immediately upon selection.
- Ensured domain entities (food item titles, descriptions, shop names, user names, and customer feedback text) remain as stored without modification.

**2. Local Order Status Transition Notifications**
- Implemented `NotificationHelper` and registered notification channel `campus_eats_orders` at application launch in `MainActivity`.
- Added `POST_NOTIFICATIONS` permission declaration in `AndroidManifest.xml`.
- Triggered localized local push notifications whenever an order status transition occurs in `OrderRepository` (Accepted, Preparing, Ready, Completed).

**3. Enhanced Network Connectivity Validation**
- Updated `NetworkConnectivityManager` to inspect both `NET_CAPABILITY_INTERNET` and `NET_CAPABILITY_VALIDATED` network capabilities.
- Added `ACCESS_NETWORK_STATE` permission declaration in `AndroidManifest.xml` to allow capability checking and eliminate false positives from captive portals.

---

### Improvements

**1. Single Authoritative Synchronizer Engine**
- Refactored `FirebaseSyncManager` to manage continuous cloud synchronization using a single coroutine `Job` guard, preventing duplicate concurrent sync loops.
- Gated synchronization cycles on active network connectivity and authenticated Firebase sessions (`firebaseAuth.currentUser != null`).
- Applied exponential backoff logic (1s doubling up to 60s cap) on transient database or network failures.
- Ensured local pending flags (`isSynced` / `isPendingSync`) are cleared only after successful `setValue` / `await()` completion in Firebase Realtime Database.

**2. Authentication Session Establishment**
- Updated `AuthRepository.register` to create a Firebase Auth user (`createUserWithEmailAndPassword`) and mirror profile data to `users/{userId}` in Firebase Realtime Database when online.
- Updated `AuthRepository.login` to establish a Firebase Auth session after local SHA-256 password hash validation when online, ensuring subsequent cloud writes satisfy `auth != null` rules.

**3. Explicit Client ID Exposure in Build Configuration**
- Updated `app/build.gradle.kts` to expose distinct build fields for `GOOGLE_WEB_CLIENT_ID` (`64265928399-k9qvcvf2e17vh61f98591d860v8md05q.apps.googleusercontent.com`), `GOOGLE_ANDROID_CLIENT_ID` (`64265928399-tl7gi0kkolvke8k7etaio9h66ov44hi3.apps.googleusercontent.com`), and backward-compatible `GOOGLE_CLIENT_ID`.

---

### Bug Fixes

- Fixed Google SSO authentication failures by supplying the Web OAuth Client ID (`GOOGLE_WEB_CLIENT_ID`) to Credential Manager's `GetGoogleIdOption.setServerClientId` in `LoginScreen` and `RegistrationScreen`.
- Fixed feedback synchronization by filtering feedback entries for the active session user and serializing entries under stable `feedbackId` keys.
- Added missing `passwordHash` field (`"[FIREBASE_SSO]"`) in `AuthRepository.mapToFirebase` to satisfy user node security rule validations.
- Added trailing comma in `buildTypes.release.proguardFiles` in `app/build.gradle.kts` to resolve build script analyzer warnings.
- Fixed KDoc comment formatting in `FirebaseSyncManager` to prevent unresolved symbol warnings.

---

### Breaking Changes

- None. All database schemas and repository public contracts remain backward compatible with prior 3.x releases.

---

### Dependencies and Compatibility

- Minimum SDK: API 24 (Android 7.0 Nougat)
- Target SDK: API 37 (Android 15)
- Kotlin Version: 2.0.x
- Added Dependency: `androidx.datastore:datastore-preferences`

---

### Technical Details

**Version Code:** 5

**Version Name:** 3.1.0

**Package Name:** com.example.campus_eats_app_kt

---

### Installation Instructions

**For First-Time Installation:**

1. Download the APK file from the provided repository artifacts.
2. Enable "Install from Unknown Sources" in device settings.
3. Open the APK file on your Android device.
4. Tap "Install" and confirm the installation.
5. Launch the application and log in using demo credentials or Google SSO.

**For Upgrading Existing Installation:**

1. Run `./gradlew :app:assembleDebug` or install the updated APK over the existing version (`adb install -r`).
2. Local database schema and DataStore preferences automatically migrate upon launch.

---

### APK Information

**Debug APK:**
- Filename: campus-eats-debug-3.1.0.apk
- Purpose: Development and testing
- Signing: Debug keystore

**Release APK:**
- Filename: campus-eats-release-3.1.0.apk
- Purpose: Production distribution
- Signing: Release keystore

---

### CI/CD Workflow Information

**Workflow Name:** CI

**Trigger Events:**
- Push to master branch
- Pull request to master branch
- Manual dispatch (workflow_dispatch)

**Workflow Steps:**
1. Checkout repository code
2. Set up JDK 17
3. Make Gradle wrapper executable
4. Run unit tests
5. Build debug APK
6. Build release APK
7. Upload build artifacts

---

### Contributors

- **Naoyuki Christopher H.** - Lead Full Stack Engineer

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
