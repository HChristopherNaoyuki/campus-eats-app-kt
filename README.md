# Campus Eats - Mobile Campus Dining Platform (v5.0.0)

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
9. [AI Usage & Contributions Policy](#9-ai-usage--contributions-policy)
10. [Commit Message Convention](#10-commit-message-convention)

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

A recorded demonstration of the Campus Eats mobile application is available online.

* Title: OPSC6312 and XISD6329: POE Campus Eats Mobile App Part 2
* Link: https://youtu.be/XHF5T_HrPOI

The video presents the application in operation, including the primary user workflows for each role.

---

## 4. Technical Overview

The application is built using modern Android development practices, architecture patterns, and Jetpack libraries:

* Language: Kotlin 2.x
* UI Framework: Jetpack Compose with Material 3 and Navigation 3
* Image Loading: Coil Compose with fallback vector placeholders
* Architecture: Model-View-ViewModel (MVVM) with Repository Pattern and Dependency Wiring
* Persistence: Room Database (version 12) for local offline storage and Jetpack DataStore Preferences for setting storage
* Cloud Backend: Firebase Realtime Database with 10 MB disk persistence enabled
* Authentication: Firebase Authentication with Credential Manager for Google Single Sign-On (SSO)
* Synchronizer: Single background coroutine engine (10-second interval, exponential backoff 1s to 60s, session-gated)
* Networking: Retrofit 2.x for Fake Restaurant REST API communication
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
5. On initial launch, the application seeds the local Room database with sample data and loads saved language preferences.
6. Use the credentials provided in the Demo Accounts section to log in, or use Continue with Google.
7. Perform role-specific workflows such as browsing food items with photographs, food names, and prices (Student/Standard), managing vendor inventory with item thumbnails (Vendor), or administrative moderation (Admin).
8. Toggle network connectivity to observe offline operation and automatic background synchronization upon reconnection.

---

## 7. Sample Coupons

Administrators can use the following sample coupons for testing:

* CAMPUS10: 10% Discount
* EATS20: 20% Discount
* WELCOME5: 5% Discount

---

## 8. Functional Features

* Food Item Presentation: Full-width food photographs displayed on each food item card with food name and price clearly presented below, accompanied by list thumbnails across vendor inventory and cart screens.
* Secure Authentication: Local SHA-256 hashed email/password sign-in plus Google Single Sign-On (SSO) via Credential Manager.
* Offline-First Architecture: Instant local registration, cart management, and order placement in Room database.
* Automated Cloud Synchronization: Single background synchronizer loop syncing unsynced records to Firebase Realtime Database every 10 seconds with exponential backoff on network errors.
* Local Order Notifications: Real-time status transition alerts delivered through Android notification channels on order progress updates.
* Persistent Localization: Dynamic UI language switching for English and Afrikaans persisted across application restarts with Jetpack DataStore.
* Role-Based Interfaces: Customized dashboards for Students, Vendors, and Administrators.
* Account Management: 16-character User ID generation, copy-to-clipboard control, and User ID account recovery with password strength validation.
* Vendor Fulfillment System: Live order tracking, itemized receipt inspection, vendor inventory thumbnails, and order status updates.
* Administrative Oversight: User suspension controls, tabular order insight reporting, system credit issuance, and coupon generation.
* REST API Catalog: Secondary Retrofit integration for restaurant catalog browsing.

---

## 9. AI Usage & Contributions Policy

We welcome the use of AI tools to assist developers, but we enforce strict human accountability:

* **No Fully AI-Generated PRs:** This project does not accept entirely machine-generated pull requests.
* **Human Oversight:** You must personally understand, verify, and take 100% responsibility for every single line of code you submit.
* **Never Let the LLM Think for You:** AI models make logical mistakes and cannot be held accountable for security flaws or bugs.
* **Disclosure:** If a significant portion of your contribution was generated by an AI assistant, please explicitly note it in your Pull Request description.

---

## 10. Commit Message Convention

All commits must follow the format described below. This convention is based on the widely used Linux kernel style and is intended to keep the project history readable and useful.

### 10.1 Header Line

The header is a single line that explains the commit in one line. Use the imperative mood. For example, write "Fix login crash on empty email" rather than "Fixed" or "Fixes."

### 10.2 Commit Body

The body of the commit message is a few lines of text that explain the change in more detail. Where relevant, include background about the issue being fixed. Use proper word wrapping and keep columns shorter than 74 characters.

Explain your solution and the reason you are making the change, rather than simply describing what the change does.

### 10.3 Trailers

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
