# Campus Eats: Mobile Campus Dining Platform (v14.0.0)

## Table of Contents

- [1. Introduction and Purpose](#1-introduction-and-purpose)
- [2. Project Team](#2-project-team)
  - [2.1 OPSC6312 Team](#21-opsc6312-team)
  - [2.2 XISD6329 Team](#22-xisd6329-team)
- [3. Project Demonstration Video](#3-project-demonstration-video)
  - [3.1 XISD6329 Demonstration](#31-xisd6329-demonstration)
  - [3.2 OPSC6312 Demonstration](#32-opsc6312-demonstration)
  - [3.3 OPSC6312 Part 3 Demonstration](#33-opsc6312-part-3-demonstration)
- [4. Technical Overview](#4-technical-overview)
- [5. Demo Accounts](#5-demo-accounts)
- [6. Testing Instructions](#6-testing-instructions)
- [7. Sample Coupons](#7-sample-coupons)
- [8. Functional Features](#8-functional-features)
- [9. Play Store Preparation and CI/CD Pipeline](#9-play-store-preparation-and-cicd-pipeline)
- [10. AI Usage and Contributions Disclosure](#10-ai-usage-and-contributions-disclosure)
- [11. Commit Message Convention](#11-commit-message-convention)
- [12. Visual Media Policy](#12-visual-media-policy)
- [13. Support and Issue Reporting](#13-support-and-issue-reporting)

---

## 1. Introduction and Purpose

Campus Eats is a production-quality Android application that modernizes and
streamlines the campus dining experience. It is tailored for Rosebank
International South Africa and connects students, food vendors, and
administrators on one platform.

The application addresses common campus dining problems, such as long queues and
manual transaction recording, by providing a reliable, digital-first solution.
It uses a hybrid offline-first architecture with the following components:

- A local Room database for offline storage.
- Firebase Realtime Database for cloud synchronization.
- Firebase Authentication for user credentials.
- A secondary REST API client for catalog integration.

This repository supports the following academic modules:

- OPSC6312: Open Source Coding.
- XISD6329: Work Integrated Learning.

## 2. Project Team

This section lists the contributors for each academic module, including their
roles and student numbers.

### 2.1 OPSC6312 Team

| Name | Role | Student Number |
| :--- | :--- | :--- |
| Naoyuki Christopher H. | Lead Full Stack Engineer | ST10462415 |
| Makaya G. | Tester / QA | ST10404851 |
| Murendeni H. | Project Manager | ST10377430 |
| Matome M. | UI/UX Designer | ST10341694 |

### 2.2 XISD6329 Team

| Name | Role | Student Number |
| :--- | :--- | :--- |
| Naoyuki Christopher H. | Lead Full Stack Engineer, UI/UX Designer | ST10462415 |
| Makaya G. | Tester / QA, Project Manager | ST10404851 |

## 3. Project Demonstration Video

Recorded video demonstrations and project metadata for each supported academic
module are listed below.

### 3.1 XISD6329 Demonstration

- Title: XISD6329: Part 2 Demo.
- Module Code: XISD6329.
- Module Name: Work Integrated Learning 3B.
- Release Notes: Campus Eats v5.0.0.
- Release Version: v5.0.0.
- Release Date: 2026-10-15.
- Live Website Link: https://campus-eats-platform.lovable.app/
- Website GitHub Link: https://github.com/HChristopherNaoyuki/campus-eats-platform.git
- Mobile App GitHub Link: https://github.com/HChristopherNaoyuki/campus-eats-app-kt
- APK Installation Link: https://github.com/HChristopherNaoyuki/campus-eats-app-kt/releases
- YouTube Demonstration Video: https://youtu.be/FFEtJBy7guA

### 3.2 OPSC6312 Demonstration

- Title: OPSC6312: Part 2 Demo.
- Module Code: OPSC6312.
- Module Name: Open Source Coding (Intermediate).
- Release Notes: Campus Eats v3.0.0.
- Release Version: v3.0.0.
- Release Date: 2026-09-17.
- Live Website Link: https://campus-eats-platform.lovable.app/
- Website GitHub Link: https://github.com/HChristopherNaoyuki/campus-eats-platform.git
- Mobile App GitHub Link: https://github.com/HChristopherNaoyuki/campus-eats-app-kt
- APK Installation Link: https://github.com/HChristopherNaoyuki/campus-eats-app-kt/releases
- YouTube Demonstration Video: https://youtu.be/XHF5T_HrPOI

### 3.3 OPSC6312 Part 3 Demonstration

- Title: OPSC6312: Part 3 Demo.
- Module Code: OPSC6312.
- Module Name: Open Source Coding (Intermediate).
- Release Notes: Campus Eats v13.0.0.
- Release Version: v13.0.0.
- Release Date: 2026-10-21.
- Live Website Link: https://campus-eats-platform.lovable.app/
- Website GitHub Link: https://github.com/HChristopherNaoyuki/campus-eats-platform.git
- Mobile App GitHub Link: https://github.com/HChristopherNaoyuki/campus-eats-app-kt
- APK Installation Link: https://github.com/HChristopherNaoyuki/campus-eats-app-kt/releases
- YouTube Demonstration Video: https://youtu.be/xy2XR5ymF4o

## 4. Technical Overview

The application follows modern Android development practices, architecture
patterns, and Jetpack libraries:

- Language: Kotlin 2.x.
- UI Framework: Jetpack Compose with Material 3 and Navigation 3.
- Brand Palette: Warm Orange (#ED5A1E), Accent Green (#309C5E), and Warm
  Neutrals (#FAF7F5 and #231A16).
- In-App Appearance: user-controlled Light, Dark, and System modes, persisted
  through Jetpack DataStore. Dark mode uses an Apple HIG soft canvas (#1C1C1E),
  a black navigation bar, and cream panels (#F5F2EB).
- Image Loading: Coil Compose with fallback vector placeholders.
- Architecture: Model-View-ViewModel (MVVM) with the Repository Pattern and
  dependency wiring.
- Persistence: Room Database (version 12) for local offline storage, and
  Jetpack DataStore Preferences for settings and appearance persistence.
- Cloud Backend: Firebase Realtime Database with 10 MB disk persistence.
- Authentication: Firebase Authentication with Credential Manager for Google
  Single Sign-On (SSO).
- Synchronizer: a single background coroutine engine that runs every 10
  seconds, applies exponential backoff from 1 second to 60 seconds, is
  session-gated, handles exceptions per node, and pulls cloud feedback for
  administrators.
- Networking: Retrofit 2.x for communication with the Fake Restaurant REST API
  at https://fakerestaurantapi.runasp.net/
- Security: SHA-256 password hashing for local accounts, sentinel SSO tokens,
  and Role-Based Access Control (RBAC).
- Notifications: a local notification channel (campus_eats_orders) for
  real-time order status alerts.
- Localization: dynamic UI chrome language switching between English and
  Afrikaans, persisted through DataStore.

## 5. Demo Accounts

The following demo accounts support testing and demonstration of each system
role. The local database is pre-populated with these records on first launch.

| Role | Name | Email | Password |
| :--- | :--- | :--- | :--- |
| Administrator | Amara Nkosi | amara.nkosi@campuseats.test | Adm1n#Amara |
| Administrator | Pieter van Wyk | pieter.vanwyk@campuseats.test | Adm1n#Pieter |
| Vendor | Thandiwe Mokoena | thandiwe.mokoena@campuseats.test | Vend0r#Thandi |
| Vendor | Sipho Dlamini | sipho.dlamini@campuseats.test | Vend0r#Sipho |
| Vendor | Annelie Botha | annelie.botha@campuseats.test | Vend0r#Annelie |
| Standard | Lerato Khumalo | lerato.khumalo@campuseats.test | Stand@rd#Lerato |
| Standard | Johan Pretorius | johan.pretorius@campuseats.test | Stand@rd#Johan |
| Standard | Zanele Ndlovu | zanele.ndlovu@campuseats.test | Stand@rd#Zanele |
| Standard | Marius Steyn | marius.steyn@campuseats.test | Stand@rd#Marius |
| Student | Naledi Mahlangu | naledi.mahlangu@campuseats.test | Stud3nt#Naledi |

## 6. Testing Instructions

To verify application functionality, follow these steps:

1. Clone the repository and open the project in Android Studio.
2. Synchronize the project with the Gradle files.
3. Start an Android Emulator (API level 24 or higher with Google Play Services),
   or connect a physical device.
4. Build and run the application with `./gradlew :app:assembleDebug`.
5. On first launch, the application seeds the local Room database with sample
   data and loads saved language and appearance preferences.
6. Sign in with the credentials in Section 5, or use Continue with Google.
7. Run the role-specific workflows:
   - Student or Standard: browse menu items with full-width food photos, names,
     and prices; add items to the cart; apply coupons; place an order; view the
     live receipt details and pickup status.
   - Vendor: view live orders; inspect picked item details through the Check
     modal; update order statuses (ACCEPTED, PREPARING, READY, COMPLETED);
     manage inventory stock and prices; review financial reports.
   - Admin: view the admin home welcome header showing the full name and
     username (for example, "Amara Nkosi (@amara)"); manage users, vendors, and
     orders; inspect the Security Log audit trail; generate coupons with the
     sample chips (CAMPUS10, EATS20, WELCOME5); review financial system summary
     reports; resolve complaints and compliments pulled from the cloud.
8. Test the bottom navigation re-tap behavior (re-tapping the active tab returns
   to the tab root window).
9. Open the Settings Platform Information card and tap WEBSITE to launch the
   external web application.
10. Toggle network connectivity to observe offline operation and automatic
    background synchronization after reconnection.

## 7. Sample Coupons

Administrators can use the following sample coupons for testing:

- CAMPUS10: 10 percent discount.
- EATS20: 20 percent discount.
- WELCOME5: 5 percent discount.

## 8. Functional Features

- Deterministic Feedback Keying: URL-safe deterministic keys
  (`${userId}_${createdAt}`) are generated in `FeedbackKeys.kt` to ensure
  idempotent cloud writes and collision-free feedback status resolution.
- Isolated Sync Loop: isolated `try-catch` blocks per sync node in
  `FirebaseSyncManager.kt` ensure that a single-node failure does not abort the
  10-second continuous sync engine.
- Admin Home Session Binding: the administrator home header displays the
  signed-in user's full name and username, consistent with the student,
  standard, and vendor home screens.
- Role Financial Controls: authoritative financial metrics, spending breakdown
  by vendor, wallet balance tracking, vendor item revenue analysis, and global
  system revenue reporting backed by Room and Realtime Database entities.
- Admin System Reports: System Summary and Insight Reports with ordering filter
  options ("New to old", "Month", "Year") that sort real database order records.
- Vendor Order Status Updates: live status transitions (ACCEPTED, PREPARING,
  READY, COMPLETED, CANCELLED) with automatic cloud sync and customer
  notifications.
- Student and Standard Receipts: receipt detail windows that render the current
  order status, pickup state, payment method, special requests, and itemized
  cost breakdown.
- Cloud Feedback Synchronization for Administrators: real-time background
  pulling of cloud-submitted complaints and compliments into Room, so
  administrators see submissions across all devices.
- Role Alignment and Re-Tap Navigation: re-tapping a tab returns the active tab
  to its root window across Home, Browse, Orders, and Settings.
- Admin Management and Audit Log: User Management, Vendor Management, Order
  Management, and a real-time Security Log audit trail under Browse.
- Feedback Loop Parity: a full feedback loop that lets administrators review
  and resolve Complaints and Compliments with pending and resolved status
  tracking, while non-administrators submit feedback with confirmation
  prompts.
- Sample Coupon Quick-Selection: the coupon generation window includes
  quick-selection chips for the sample coupons (CAMPUS10, EATS20, WELCOME5) and
  lists current coupons.
- Platform Information Link: a Settings information block with a WEBSITE action
  button that opens the external website at
  https://campus-eats-platform.lovable.app/
- Apple HIG Dark Mode Palette: a soft dark gray canvas (#1C1C1E), a black bottom
  navigation bar (#000000) with white active icons (#FFFFFF), and warm cream
  content boxes (#F5F2EB).
- In-App Appearance Mode Control: user-selectable Light, Dark, and System theme
  controls in Settings, persisted across restarts through Jetpack DataStore.
- Food Item Presentation: full-width food photographs on each food item card,
  with the food name and price shown below, plus list thumbnails on vendor
  inventory and cart screens.
- Secure Authentication: local SHA-256 hashed email and password sign-in, plus
  Google Single Sign-On through Credential Manager.
- Offline-First Architecture: instant local registration, cart management, and
  order placement in the Room database.
- Automated Cloud Synchronization: a single background synchronizer loop that
  syncs unsynced records to Firebase Realtime Database every 10 seconds, with
  exponential backoff on network errors.
- Local Order Notifications: real-time status transition alerts delivered
  through Android notification channels on order progress updates.
- Persistent Localization: dynamic UI chrome language switching between English
  and Afrikaans, persisted across restarts with Jetpack DataStore.

## 9. Play Store Preparation and CI/CD Pipeline

- Release Signing Configuration: the release build type in
  `app/build.gradle.kts` is configured for Play Store publishing.
- App Icon and Graphics Assets: a solid black background with a high-contrast
  white hamburger mark adaptive launcher icon (`ic_launcher`).
- Automated CI/CD Workflow: `.github/workflows/build.yml` runs
  `./gradlew test` and `./gradlew assembleDebug` on GitHub Actions runners on
  every commit.

## 10. AI Usage and Contributions Disclosure

An AI coding assistant was used during development as an engineering aid for
debugging, refactoring, and test case generation.

- Usage Scope: generating boilerplate Jetpack Compose UI structures, formulating
  MockK unit test assertions, parsing Firebase exceptions, and verifying
  Material 3 color token contrast ratios.
- Human Oversight: all AI-assisted code was manually reviewed, audited line by
  line, tested against the local Room and Firebase backends, and verified
  through 69 passing unit tests (`./gradlew :app:testDebugUnitTest`). No fully
  unreviewed machine-generated code was committed to the repository.

## 11. Commit Message Convention

All commits must follow the format below. This convention is based on the
widely used Linux kernel style and keeps the project history readable and
useful.

```
Header line: Explain the commit in one line (use the imperative)

Body of commit message is a few lines of text, explaining things
in more detail, possibly giving some background about the issue
being fixed, etc.

The body of the commit message can be several paragraphs, and
please do proper word-wrap and keep columns shorter than about
74 characters or so. That way "git log" will show things
nicely even when it's indented.

Make sure you explain your solution and why you're doing, what you're
doing, as opposed to describing what you're doing. Reviewers and your
future self can read the patch, but might not understand why a
particular solution was implemented.

Reported-by: whoever-reported-it
Signed-off-by: Your Name
```

## 12. Visual Media Policy

Images and emojis must not be included directly in this file. All visual media,
including screenshots and images of the application, must be stored in a
dedicated folder within the project directory. This folder should be clearly
structured and named to show that it contains all visual content related to the
application, for example a folder named IMAGES, SCREENSHOTS, or MEDIA.

## 13. Support and Issue Reporting

```
UNDER NO CIRCUMSTANCES SHOULD IMAGES OR EMOJIS BE INCLUDED DIRECTLY IN
THIS FILE. ALL VISUAL MEDIA, INCLUDING SCREENSHOTS AND IMAGES OF THE
APPLICATION, MUST BE STORED IN A DEDICATED FOLDER WITHIN THE PROJECT
DIRECTORY. THIS FOLDER SHOULD BE CLEARLY STRUCTURED AND NAMED
ACCORDINGLY TO INDICATE THAT IT CONTAINS ALL VISUAL CONTENT RELATED TO
THE APPLICATION, FOR EXAMPLE A FOLDER NAMED IMAGES, SCREENSHOTS, OR
MEDIA. THE AUTHOR IS NOT LIABLE OR RESPONSIBLE FOR ANY MALFUNCTIONS,
DEFECTS, OR ISSUES THAT MAY OCCUR AS A RESULT OF COPYING, MODIFYING, OR
USING THIS SOFTWARE. IF ANY PROBLEMS OR ERRORS ARE ENCOUNTERED, PLEASE
DO NOT ATTEMPT TO FIX THEM SILENTLY OR OUTSIDE THE PROJECT. INSTEAD,
SUBMIT A PULL REQUEST OR OPEN AN ISSUE ON THE CORRESPONDING GITHUB
REPOSITORY SO THAT IT CAN BE ADDRESSED APPROPRIATELY BY THE MAINTAINERS
OR CONTRIBUTORS.
```

---

*END OF DOCUMENT*

---
