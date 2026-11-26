# Campus Eats: Mobile Campus Dining Platform (v18.0.0)

## Table of Contents

- [1. Introduction and Purpose](#1-introduction-and-purpose)
- [2. Project Team](#2-project-team)
- [3. Project Demonstration Video](#3-project-demonstration-video)
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

Campus Eats is a mobile food ordering application for campus dining.
It connects three groups in one place: customers, food vendors, and
administrators.

Many campus dining problems come from long queues and paper-based
record keeping. Campus Eats reduces these problems by moving the
ordering process online. The app works even when the internet is
unavailable, and it synchronizes data with the cloud when a connection
returns.

The app uses the following main technologies:

- A local Room database for offline storage.
- Firebase Realtime Database for cloud synchronization.
- Firebase Authentication for user sign-in.
- A separate REST API client for catalog integration.

This repository supports the following academic module:

- OPSC6312: Open Source Coding

---

## 2. Project Team

This section lists the project contributors, their roles, and their
student numbers.

### 2.1 OPSC6312 Team

| Name | Role | Student Number |
| :--- | :--- | :--- |
| Naoyuki Christopher H. | Lead Full Stack Engineer | ST10462415 |
| Makaya G. | Tester / QA | ST10404851 |
| Murendeni H. | Project Manager | ST10377430 |
| Matome M. | UI/UX Designer | ST10341694 |

---

## 3. Project Demonstration Video

Recorded demonstrations and project links for the supported module
appear below.

### 3.1 OPSC6312 Part 2 Demonstration

- **Title:** OPSC6312: Part 2 Demo
- **Module Code:** OPSC6312
- **Module Name:** Open Source Coding (Intermediate)
- **Release Notes:** Campus Eats v3.0.0
- **Release Version:** v3.0.0
- **Release Date:** 2026-09-17
- **Live Website Link:** https://campus-eats-platform.lovable.app/
- **Website GitHub Link:**
  https://github.com/HChristopherNaoyuki/campus-eats-platform.git
- **Mobile App GitHub Link:**
  https://github.com/HChristopherNaoyuki/campus-eats-app-kt
- **APK Installation Link:**
  https://github.com/HChristopherNaoyuki/campus-eats-app-kt/releases
- **YouTube Demonstration Video:** https://youtu.be/XHF5T_HrPOI

### 3.2 OPSC6312 Part 3 Demonstration

- **Title:** OPSC6312: Part 3 Demo
- **Module Code:** OPSC6312
- **Module Name:** Open Source Coding (Intermediate)
- **Release Notes:** Campus Eats v13.0.0
- **Release Version:** v13.0.0
- **Release Date:** 2026-10-21
- **Live Website Link:** https://campus-eats-platform.lovable.app/
- **Website GitHub Link:**
  https://github.com/HChristopherNaoyuki/campus-eats-platform.git
- **Mobile App GitHub Link:**
  https://github.com/HChristopherNaoyuki/campus-eats-app-kt
- **APK Installation Link:**
  https://github.com/HChristopherNaoyuki/campus-eats-app-kt/releases
- **YouTube Demonstration Video:** https://youtu.be/xy2XR5ymF4o

---

## 4. Technical Overview

The application uses modern Android development tools and standard
architecture patterns. The main technical details are listed below.

- **Language:** Kotlin 2.x
- **UI Framework:** Jetpack Compose, Material 3, and Navigation 3
- **Brand Colors:** Warm Orange (#ED5A1E), Accent Green (#309C5E),
  and Warm Neutrals (#FAF7F5 / #231A16)
- **Appearance Modes:** The user can choose Light, Dark, or System.
  Dark mode uses an Apple HIG soft canvas (#1C1C1E), a black
  navigation bar, and cream panels (#F5F2EB). The choice is saved with
  Jetpack DataStore.
- **Image Loading:** Coil Compose, with vector placeholders when an
  image fails to load.
- **Architecture:** Model-View-ViewModel (MVVM) with the Repository
  pattern and dependency wiring.
- **Local Storage:** Room Database (version 12) for offline storage,
  and Jetpack DataStore Preferences for settings and appearance.
- **Cloud Backend:** Firebase Realtime Database with 10 MB disk
  persistence enabled.
- **Authentication:** Firebase Authentication with Credential Manager
  for Google Single Sign-On (SSO).
- **Synchronizer:** One background coroutine engine. It runs every 10
  seconds, uses exponential backoff from 1 second to 60 seconds, is
  session-gated, handles exceptions per node, and pulls cloud feedback
  for Administrators.
- **Networking:** Retrofit 2.x for the Fake Restaurant REST API
  (https://fakerestaurantapi.runasp.net/).
- **Security:** SHA-256 password hashing for local accounts, sentinel
  SSO tokens, and Role-Based Access Control (RBAC).
- **Notifications:** A local notification channel
  (campus_eats_orders) for order status alerts.
- **Localization:** The interface language can be switched between
  English and Afrikaans. The choice is saved with DataStore.

---

## 5. Demo Accounts

The following demo accounts support testing and demonstrations. On
first launch, the local database is pre-filled with these records.

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

---

## 6. Testing Instructions

Follow these steps to check that the application works correctly.

1. Clone the repository and open the project in Android Studio.
2. Synchronize the project with the Gradle files.
3. Start an Android Emulator (API 24 or higher with Google Play
   Services) or connect a physical device.
4. Build and run the application with `./gradlew :app:assembleDebug`.
5. On first launch, the app fills the local Room database with sample
   data and loads the saved language and appearance settings.
6. Sign in with the credentials in the Demo Accounts section, or use
   Continue with Google.
7. Test the workflows for each role:
   - **Student/Standard:** Browse menu items with full-width food
     photos, names, and prices. Add items to the cart, apply coupons,
     place an order, and view the live receipt and pickup status.
   - **Vendor:** View live orders, inspect item details in the Check
     window, update order status (ACCEPTED, PREPARING, READY,
     COMPLETED), manage stock and prices, and review financial
     reports.
   - **Admin:** View the admin home header, which shows the full name
     and username ("Amara Nkosi (@amara)"). Manage users, vendors, and
     orders. Inspect the Security Log audit trail. Generate coupons
     with the sample chips (CAMPUS10, EATS20, WELCOME5). Review
     financial summary reports. Resolve complaints and compliments
     that are pulled automatically from the cloud.
8. Re-tap an active bottom navigation tab to return to that tab's
   root window.
9. Open the Settings Platform Information card and tap **WEBSITE** to
   launch the external web app.
10. Toggle network connectivity to observe offline operation and
    automatic synchronization after reconnection.

---

## 7. Sample Coupons

Administrators can use the following sample coupons for testing:

- CAMPUS10: 10% discount
- EATS20: 20% discount
- WELCOME5: 5% discount

---

## 8. Functional Features

- **Google SSO Integration:** Uses the Credential Manager API
  (`GetGoogleIdOption`) with Web OAuth Client ID
  `64265928399-tl7gi0kkolvke8k7etaio9h66ov44hi3.apps.googleusercontent.com`.
  ProGuard keeps are set in `proguard-rules.pro`, and a
  `[FIREBASE_SSO]` passwordHash sentinel flag is used.
- **Deterministic Feedback Keying:** URL-safe keys
  (`${sanitizedUser}_${sanitizedTime}`) are generated in
  `FeedbackKeys.kt`. This makes cloud writes idempotent and prevents
  feedback status collisions.
- **Isolated Sync Loop:** Each sync node in `FirebaseSyncManager.kt`
  uses its own try-catch block. A failure in one node does not stop
  the 10-second continuous sync engine.
- **Admin Home Session Binding:** The administrator home header shows
  the signed-in user's full name and username ("Amara Nkosi
  (@amara)"), matching the student, standard, and vendor home screens.
- **Role Financial Controls:** Financial metrics, spending by vendor,
  wallet balance tracking, vendor item revenue analysis, and global
  system revenue reporting are backed by Room and Realtime Database
  entities.
- **Admin System Reports:** System Summary and Insight Reports offer
  order filters ("New to old", "Month", "Year") that sort real
  database order records.
- **Vendor Order Status Updates:** Status changes (ACCEPTED,
  PREPARING, READY, COMPLETED, CANCELLED) sync to the cloud and
  notify the customer automatically.
- **Student and Standard Receipts:** Receipt windows show the current
  order status, pickup state, payment method, special requests, and
  an itemized cost breakdown.
- **Cloud Feedback Synchronization for Administrators:** Complaints
  and compliments from the cloud are pulled into Room in the
  background, so Administrators see submissions from all devices.
- **Role Alignment and Re-Tap Navigation:** Re-tapping an active tab
  returns to the root window on Home, Browse, Orders, and Settings.
- **Admin Management and Audit Log:** User Management, Vendor
  Management, Order Management, and a real-time Security Log audit
  trail are available under Browse.
- **Feedback Loop Parity:** Administrators review and resolve
  Complaints and Compliments with pending and resolved status
  tracking. Non-Administrators submit feedback with confirmation
  prompts.
- **Sample Coupon Quick-Selection:** The coupon generation window
  includes quick-selection chips for CAMPUS10, EATS20, and WELCOME5,
  plus the current coupon list.
- **Platform Information Link:** The Settings information block has a
  WEBSITE button that opens the external website
  (https://campus-eats-platform.lovable.app/).
- **Apple HIG Dark Mode Palette:** A soft dark gray canvas (#1C1C1E),
  a black bottom navigation bar (#000000) with white active icons
  (#FFFFFF), and warm cream content boxes (#F5F2EB).
- **In-App Appearance Mode Control:** Users can choose Light, Dark, or
  System mode in Settings. The choice is saved across restarts with
  Jetpack DataStore.
- **Food Item Presentation:** Each food item card shows a full-width
  food photo, with the food name and price below it. List thumbnails
  appear in vendor inventory and cart screens.
- **Secure Authentication:** Local SHA-256 hashed email and password
  sign-in, plus Google Single Sign-On (SSO) through Credential
  Manager.
- **Offline-First Architecture:** Local registration, cart
  management, and order placement work instantly in the Room
  database.
- **Automated Cloud Synchronization:** One background loop syncs
  unsynced records to Firebase Realtime Database every 10 seconds,
  with exponential backoff on network errors.
- **Local Order Notifications:** Order status changes trigger Android
  notification channel alerts.
- **Persistent Localization:** The interface language can be switched
  between English and Afrikaans, and the choice is saved across
  restarts with Jetpack DataStore.

---

## 9. Play Store Preparation and CI/CD Pipeline

- **Release Signing Configuration:** The release build type in
  `app/build.gradle.kts` is set up for Play Store publishing.
- **App Icon and Graphics Assets:** The adaptive launcher icon
  (`ic_launcher`) uses a solid black background with a high-contrast
  white hamburger mark.
- **Automated CI/CD Workflow:** `.github/workflows/build.yml` runs
  `./gradlew test` and `./gradlew assembleDebug` on GitHub Actions
  runners for every commit.

---

## 10. AI Usage and Contributions Disclosure

An AI coding assistant was used during development as an engineering
aid for debugging, refactoring, and test case generation.

- **Usage Scope:** Generating boilerplate Jetpack Compose UI
  structures, writing MockK unit test assertions, parsing Firebase
  exceptions, and checking Material 3 color token contrast ratios.
- **Human Oversight:** All AI-assisted code was reviewed by hand,
  audited line by line, tested against the local Room and Firebase
  backends, and verified with 266 passing unit tests
  (`./gradlew :app:testDebugUnitTest`). No fully unreviewed machine
  code was committed to the repository.

---

## 11. Commit Message Convention

All commits must follow the format below. The convention is based on
the widely used Linux kernel style and keeps the project history
readable and useful.

### 11.1 Header Line

The header is a single line that explains the commit. Use the
imperative mood. For example, write "Fix login crash on empty email"
instead of "Fixed" or "Fixes."

### 11.2 Commit Body

The body is a few lines that explain the change in more detail. Where
relevant, include background about the issue being fixed. Use proper
word wrapping and keep columns shorter than 74 characters.

Explain your solution and the reason for the change, not only what the
change does.

### 11.3 Trailers

End the commit message with the appropriate trailers. Common examples
include:

- Reported-by: name
- Signed-off-by: Your Name

---

## 12. Visual Media Policy

Do not include images or emojis directly in this file. Store all visual
media, including screenshots and images of the application, in a
dedicated folder within the project directory.

---

## 13. Support and Issue Reporting

```
The author is not liable or responsible for any malfunctions, defects,
or issues that may occur from copying, modifying, or using this
software. If you find a problem or error, submit a pull request or open
an issue on the corresponding GitHub repository so the maintainers or
contributors can address it.
```

---

END OF DOCUMENT

---
