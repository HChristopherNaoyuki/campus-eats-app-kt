# Campus Eats - Mobile Campus Dining Platform (v11.0.0)

## Table of Contents

1. [Introduction and Purpose](#1-introduction-and-purpose)
2. [Project Team](#2-project-team)
   - [2.1 OPSC6312 Team](#21-opsc6312-team)
   - [2.2 XISD6329 Team](#22-xisd6329-team)
3. [Project Demonstration Video](#3-project-demonstration-video)
4. [Technical Overview](#4-technical-overview)
5. [Demo Accounts](#5-demo-accounts)
6. [Testing Instructions](#6-testing-instructions)
7. [Sample Coupons](#7-sample-coupons)
8. [Functional Features](#8-functional-features)
9. [Play Store Preparation & CI/CD Pipeline](#9-play-store-preparation--cicd-pipeline)
10. [AI Usage & Contributions Disclosure](#10-ai-usage--contributions-disclosure)
11. [Commit Message Convention](#11-commit-message-convention)

---

## 1. Introduction and Purpose

Campus Eats is a production-quality Android application designed to modernize and streamline the dining experience within university ecosystems. Specifically tailored for Rosebank International South Africa, the platform facilitates seamless interaction between students, food vendors, and administrators.

The application addresses common campus dining inefficiencies, such as long queues and manual transaction recording, by providing a reliable, digital-first solution. It leverages a hybrid offline-first architecture powered by a local Room database, Firebase Realtime Database for cloud synchronization, Firebase Authentication for credentials, and a secondary REST API client for catalog integration.

This repository supports the following academic modules:

* OPSC6312: Open Source Coding
* XISD6329: Work Integrated Learning

---

## 2. Project Team

This section lists the contributors for each academic module, together with their roles and student numbers.

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

---

## 3. Project Demonstration Video

Recorded video demonstrations and project metadata for each supported academic module are available below.

### 3.1 XISD6329 Demonstration

* **Title:** XISD6329: Part 2 Demo
* **Module Code:** XISD6329
* **Module Name:** Work Integrated Learning 3B
* **Release Notes:** Campus Eats v5.0.0
* **Release Version:** v5.0.0
* **Release Date:** 2026-10-15
* **Live Website Link:** https://campus-eats-platform.lovable.app/
* **Website GitHub Link:** https://github.com/HChristopherNaoyuki/campus-eats-platform.git
* **Mobile App GitHub Link:** https://github.com/HChristopherNaoyuki/campus-eats-app-kt
* **APK Installation Link:** https://github.com/HChristopherNaoyuki/campus-eats-app-kt/releases
* **YouTube Demonstration Video:** https://youtu.be/FFEtJBy7guA

---

### 3.2 OPSC6312 Demonstration

* **Title:** OPSC6312: Part 2 Demo
* **Module Code:** OPSC6312
* **Module Name:** Open Source Coding (Intermediate)
* **Release Notes:** Campus Eats v3.0.0
* **Release Version:** v3.0.0
* **Release Date:** 2026-09-17
* **Live Website Link:** https://campus-eats-platform.lovable.app/
* **Website GitHub Link:** https://github.com/HChristopherNaoyuki/campus-eats-platform.git
* **Mobile App GitHub Link:** https://github.com/HChristopherNaoyuki/campus-eats-app-kt
* **APK Installation Link:** https://github.com/HChristopherNaoyuki/campus-eats-app-kt/releases
* **YouTube Demonstration Video:** https://youtu.be/XHF5T_HrPOI

---

## 4. Technical Overview

The application is built using modern Android development practices, architecture patterns, and Jetpack libraries:

* Language: Kotlin 2.x
* UI Framework: Jetpack Compose with Material 3 and Navigation 3
* Brand Palette: Warm Orange (#ED5A1E), Accent Green (#309C5E), and Warm Neutrals (#FAF7F5 / #231A16)
* In-App Appearance: User-controlled Light, Dark (Apple HIG soft canvas #1C1C1E, black nav bar, cream panels #F5F2EB), and System modes persisted via Jetpack DataStore
* Image Loading: Coil Compose with fallback vector placeholders
* Architecture: Model-View-ViewModel (MVVM) with Repository Pattern and Dependency Wiring
* Persistence: Room Database (version 12) for local offline storage and Jetpack DataStore Preferences for settings and appearance persistence
* Cloud Backend: Firebase Realtime Database with 10 MB disk persistence enabled
* Authentication: Firebase Authentication with Credential Manager for Google Single Sign-On (SSO)
* Synchronizer: Single background coroutine engine (10-second interval, exponential backoff 1s to 60s, session-gated, cloud feedback pull for Admins)
* Networking: Retrofit 2.x for Fake Restaurant REST API communication (`https://fakerestaurantapi.runasp.net/`)
* Security: SHA-256 password hashing for local accounts, sentinel SSO tokens, and Role-Based Access Control (RBAC)
* Notifications: Local Notification Channel (campus_eats_orders) for real-time order status transition alerts
* Localization: Dynamic UI chrome language switching (English and Afrikaans) persisted via DataStore

---

## 5. Demo Accounts

The following demo accounts are provided to facilitate testing and demonstration of the various system roles. The local database is pre-populated with these authoritative records on initial launch.

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

To verify application functionality, follow these steps:

1. Clone the repository and open the project in Android Studio.
2. Synchronize the project with Gradle files.
3. Start an Android Emulator (API 24 or higher with Google Play Services) or connect a physical device.
4. Build and run the application (`./gradlew :app:assembleDebug`).
5. On initial launch, the application seeds the local Room database with sample data and loads saved language and appearance preferences.
6. Use the credentials provided in the Demo Accounts section to log in, or use Continue with Google.
7. Perform role-specific workflows:
   - Student/Standard: Browse menu items with full-width food photos, names, and prices; add to cart; apply coupons; place order; view live receipt details.
   - Vendor: View live orders, inspect picked item details via Check modal, update order statuses, manage inventory stock and prices.
   - Admin: View admin home welcome header, manage users/vendors/orders, inspect Security Log audit trail, generate coupons with sample chips (`CAMPUS10`, `EATS20`, `WELCOME5`), and resolve complaints/compliments pulled automatically from cloud.
8. Test navigation tab re-tap behavior (re-tapping active bottom tab returns to tab root window).
9. Test Settings Platform Information card and tap **WEBSITE** button to launch the external web app.
10. Toggle network connectivity to observe offline operation and automatic background synchronization upon reconnection.

---

## 7. Sample Coupons

Administrators can use the following sample coupons for testing:

* CAMPUS10: 10% Discount
* EATS20: 20% Discount
* WELCOME5: 5% Discount

---

## 8. Functional Features

* Cloud Feedback Synchronization for Administrators: Real-time background pulling of cloud-submitted complaints and compliments into Room so Administrators see submissions across all devices.
* Role Alignment & Re-Tap Navigation: Navigation re-tap returns active tab to root window across Home, Browse, Orders, and Settings.
* Admin Management & Audit Log: User Management, Vendor Management, Order Management, and real-time Security Log audit trail under Browse.
* Feedback Loop Parity: Full feedback loop allowing Admins to review and resolve Complaints and Compliments with pending/resolved status tracking, while Non-Admins submit feedback with confirmation prompts.
* Sample Coupon Quick-Selection: Coupon generation window includes quick-selection chips for sample coupons (`CAMPUS10`, `EATS20`, `WELCOME5`) and current coupon lists.
* Platform Information Link: Settings information block with a WEBSITE action button launching the external website (`https://campus-eats-platform.lovable.app/`).
* Apple HIG Dark Mode Palette: Soft dark gray canvas (#1C1C1E), black bottom navigation bar (#000000) with white active icons (#FFFFFF), and warm cream content boxes (#F5F2EB).
* In-App Appearance Mode Control: User-selectable Light, Dark, and System theme controls in Settings, persisted across app restarts via Jetpack DataStore.
* Food Item Presentation: Full-width food photographs displayed on each food item card with food name and price clearly presented below, accompanied by list thumbnails across vendor inventory and cart screens.
* Secure Authentication: Local SHA-256 hashed email/password sign-in plus Google Single Sign-On (SSO) via Credential Manager.
* Offline-First Architecture: Instant local registration, cart management, and order placement in Room database.
* Automated Cloud Synchronization: Single background synchronizer loop syncing unsynced records to Firebase Realtime Database every 10 seconds with exponential backoff on network errors.
* Local Order Notifications: Real-time status transition alerts delivered through Android notification channels on order progress updates.
* Persistent Localization: Dynamic UI language switching for English and Afrikaans persisted across application restarts with Jetpack DataStore.

---

## 9. Play Store Preparation & CI/CD Pipeline

* **Release Signing Configuration:** Configured release build type in `app/build.gradle.kts` for Play Store publishing.
* **App Icon & Graphics Assets:** Solid black background with high-contrast white hamburger mark adaptive launcher icon (`ic_launcher`).
* **Automated CI/CD Workflow:** Configured `.github/workflows/build.yml` running `./gradlew test` and `./gradlew assembleDebug` on GitHub Actions runners on every commit.

---

## 10. AI Usage & Contributions Disclosure

An AI coding assistant was utilized during development as an engineering aid for debugging, refactoring, and test case generation:

- **Usage Scope:** Generating boilerplate Jetpack Compose UI structures, formulating MockK unit test assertions, parsing Firebase exceptions, and verifying Material 3 color token contrast ratios.
- **Human Oversight:** All AI-assisted code was manually reviewed, line-by-line audited, tested against the local Room and Firebase backends, and verified through 69 passing unit tests (`./gradlew :app:testDebugUnitTest`). No fully unreviewed machine code was committed to the repository.

---

## 11. Commit Message Convention

All commits must follow the format described below. This convention is based on the widely used Linux kernel style and is intended to keep the project history readable and useful.

### 11.1 Header Line

The header is a single line that explains the commit in one line. Use the imperative mood. For example, write "Fix login crash on empty email" rather than "Fixed" or "Fixes."

### 11.2 Commit Body

The body of the commit message is a few lines of text that explain the change in more detail. Where relevant, include background about the issue being fixed. Use proper word wrapping and keep columns shorter than 74 characters.

Explain your solution and the reason you are making the change, rather than simply describing what the change does.

### 11.3 Trailers

End the commit message with the appropriate trailers. Common examples include:

* Reported-by: name
* Signed-off-by: Your Name

---

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

---

*END OF DOCUMENT*
