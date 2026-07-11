# Campus Eats - Release Notes

## Version 1.0.0

**Release Date:** 2026-01-15

---

### Overview

This is the initial release of Campus Eats, an offline-first Android
mobile application built for Rosebank International University College.
The application provides comprehensive food ordering, vendor management,
and administrative oversight capabilities within a university campus
environment.

This release establishes the core functionality for all user roles and
implements the complete user journey from registration to order
completion.

---

### New Features

**User Registration and Authentication**
Users can register with their full name, email address, and password.
The system generates a unique 16-character User ID formatted as
XXXX-XXXX-XXXX-XXXX. Users can log in using their email and password,
and the system implements role-based access control to determine the
appropriate interface.

**Role-Based Interfaces**
The application supports four distinct user roles:

- Admin: Full system oversight and management
- Student: Food ordering with student discount
- Standard User: Food ordering without discount
- Vendor: Menu management and order processing

**Admin Dashboard**
Comprehensive administrative interface including user management,
vendor management, order management, system-wide analytics, and
reporting capabilities. The dashboard displays key metrics including
all-time earnings, total users, active vendors, and revenue statistics.

**Student and Standard User Features**
Complete ordering workflow including vendor browsing, cart management,
fee calculations, checkout with pickup time selection, payment method
options, order confirmation, and order tracking. Receipt viewing with
filtering and sorting capabilities.

**Vendor Features**
Menu item management with add, edit, and delete capabilities. Order
management with status tracking (Pending, Accepted, Preparing, Ready,
Completed, Cancelled). Revenue reporting and analytics.

**Fee Calculation Engine**
Automated calculation of subtotal, tax (20%), service fees (tiered),
student discounts (2.5%), and rounding adjustment to the next R5.

**Offline-First Architecture**
All data stored locally using Room Database, ensuring reliable access
even without internet connectivity.

**User ID System**
16-character alphanumeric User ID format (XXXX-XXXX-XXXX-XXXX) with
copy functionality for secure storage and account recovery.

---

### Enhancements

This is the initial release; no enhancements are applicable at this time.

---

### Bug Fixes

This is the initial release; no bug fixes are applicable at this time.

---

### Known Issues

**Issue 1**
No known issues have been identified at the time of this release.
Any issues discovered post-release will be documented and addressed
in subsequent updates.

---

### Technical Details

**Version Code:** 1

**Version Name:** 1.0

**Minimum SDK:** API 24 (Android 7.0 Nougat)

**Target SDK:** API 37 (Android 15)

**File Size:** [size in MB - to be filled after build]

**Package Name:** com.example.campus_eats_app_kt

**Signature Hash:** [SHA-1 or SHA-256 hash - to be filled after signing]

---

### Installation Instructions

**For First-Time Installation:**

1. Download the APK file from the provided link
2. Enable "Install from Unknown Sources" in device settings
3. Open the APK file on your Android device
4. Tap "Install" and confirm the installation
5. Open the application and register or log in

**For Upgrading Existing Installation:**
This is the initial release; no upgrade path is applicable.

**For Play Store Users:**

1. Open the Google Play Store on your device
2. Search for "Campus Eats"
3. Tap the "Install" button
4. Wait for the download and installation to complete

---

### APK Information

**Debug APK:**

- Filename: campus-eats-debug-1.0.0.apk
- File Size: [size in MB]
- Purpose: Testing and development
- Signing: Debug keystore

**Release APK:**

- Filename: campus-eats-release-1.0.0.apk
- File Size: [size in MB]
- Purpose: Production distribution
- Signing: Release keystore

**App Bundle (AAB):**

- Filename: campus-eats-1.0.0.aab
- File Size: [size in MB]
- Purpose: Google Play Store submission

**Artifact Links:**

- Debug APK: campus-eats-debug-apk (from GitHub Actions)
- Release APK: campus-eats-release-apk (from GitHub Actions)
- AAB Bundle: campus-eats-release-aab (from GitHub Actions)

---

### CI/CD Workflow Information

**Workflow Name:** CI

**Trigger Events:**

- Push to master branch
- Pull request to master branch
- Manual dispatch (workflow_dispatch)

**Workflow Steps:**

1. Checkout repository code
2. Set up JDK 17 (Zulu distribution)
3. Make Gradle wrapper executable
4. Run unit tests
5. Build debug APK
6. Build release APK
7. Build release AAB
8. Upload debug APK artifact
9. Upload release APK artifact
10. Upload release AAB artifact

**Artifact Names:**

- campus-eats-debug-apk
- campus-eats-release-apk
- campus-eats-release-aab

---

### Changelog

**Version 1.0.0 (2026-01-15)**

- Initial release of Campus Eats application
- Implemented user registration and authentication
- Implemented role-based access control for Admin, Student,
  Standard, and Vendor roles
- Implemented admin dashboard with user, vendor, and order management
- Implemented student and standard user ordering workflow
- Implemented vendor menu and order management
- Implemented fee calculation engine (tax, service fees, discounts)
- Implemented offline-first architecture with Room Database
- Implemented 16-character User ID system with copy functionality
- Implemented receipt viewing with filtering and sorting
- Implemented order tracking and status management
- Implemented reporting and analytics capabilities
- Implemented feedback submission system
- Implemented CI/CD pipeline with GitHub Actions

---

### Contributors

We thank the following contributors for their work on this release:

- **Naoyuki Christopher H.** - Lead Developer
    - Application architecture and design
    - Full-stack Android development
    - Database design and implementation
    - Business logic implementation
    - UI/UX design and implementation
    - Testing and quality assurance
    - Documentation
    - CI/CD pipeline implementation

---

### Download Links

| File Type   | Artifact Name           | Link                       |
|-------------|-------------------------|----------------------------|
| Debug APK   | campus-eats-debug-apk   | [GitHub Actions Artifacts] |
| Release APK | campus-eats-release-apk | [GitHub Actions Artifacts] |
| App Bundle  | campus-eats-release-aab | [GitHub Actions Artifacts] |

---

### Checksums

| File Type   | MD5    | SHA-1  | SHA-256 |
|-------------|--------|--------|---------|
| Debug APK   | [hash] | [hash] | [hash]  |
| Release APK | [hash] | [hash] | [hash]  |
| App Bundle  | [hash] | [hash] | [hash]  |

---

### Support and Contact

For support inquiries, bug reports, or feature requests, please use the
following channels:

- GitHub Issues: [repository issues link]
- Email: [support email address]
- Documentation: [documentation link]

---

### License and Copyright

Copyright (c) 2026 Naoyuki Christopher H.

All rights reserved. This software and its documentation are proprietary
and confidential. Unauthorized copying, distribution, or use of this
software is strictly prohibited without prior written permission.

---

## APK DOCUMENTATION

### APK File Naming Convention

**Format:** application-name-buildtype-version.apk

**Examples:**

- campus-eats-debug-1.0.0.apk
- campus-eats-release-1.0.0.apk
- campus-eats-1.0.0.aab

### APK Contents

The APK contains the following main components:

**Classes.dex**

- Compiled Kotlin bytecode
- All application logic and business rules
- Repository and ViewModel implementations

**AndroidManifest.xml**

- Application permissions (Internet, Storage, etc.)
- Activity and service declarations
- Application metadata and version information

**Resources.arsc**

- Compiled resources (strings, dimensions, colors)
- Layout files and drawable references
- Theme and style definitions

**assets/**

- Static application assets
- Configuration files
- JSON data files
- Database migration scripts

**lib/**

- Native libraries for supported architectures
- ARM, ARM64, x86, x86_64 binaries

**META-INF/**

- Certificate and signature information
- Manifest file
- Security metadata

### APK Verification

To verify the APK integrity before installation:

1. **Verify File Checksum:**
   ```bash
   md5sum campus-eats-release-1.0.0.apk
   sha1sum campus-eats-release-1.0.0.apk
   sha256sum campus-eats-release-1.0.0.apk
   ```

2. **Verify APK Signature:**
   ```bash
   keytool -printcert -jarfile campus-eats-release-1.0.0.apk
   ```

3. **Verify APK Contents:**
   ```bash
   unzip -l campus-eats-release-1.0.0.apk
   ```

### APK Installation Commands

**Using ADB:**

```bash
adb install campus-eats-release-1.0.0.apk
```

**Using ADB with Reinstall:**

```bash
adb install -r campus-eats-release-1.0.0.apk
```

**Using ADB with Downgrade:**

```bash
adb install -d campus-eats-release-1.0.0.apk
```

**Installation on Device:**

1. Copy APK to device storage
2. Open file manager and navigate to APK location
3. Tap the APK file
4. Follow the installation prompts

---

## RELEASE CHECKLIST

Before finalizing the release, verify the following:

- [ ] All code changes are committed and merged to master
- [ ] CI/CD workflow runs successfully
- [ ] All unit tests pass in CI pipeline
- [ ] Application builds without errors
- [ ] Debug APK is generated successfully
- [ ] Release APK is generated successfully
- [ ] Release AAB is generated successfully
- [ ] Application installs on target devices
- [ ] Application launches without crashes
- [ ] All new features function as expected
- [ ] No critical bugs remain unresolved
- [ ] APK is signed with release keystore
- [ ] APK size is within acceptable limits
- [ ] Release notes are complete and accurate
- [ ] Artifacts are uploaded successfully
- [ ] Developer attribution is correctly included

---

*END OF DOCUMENT*
