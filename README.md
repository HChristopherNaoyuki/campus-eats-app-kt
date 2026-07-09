# Campus Eats

Campus Eats is an offline-first Android mobile application built for Rosebank International
University College. It handles food ordering, vendor management, and administrative oversight
within a university campus environment.

## Table of Contents

1. [Project Overview](#project-overview)
2. [System Architecture](#system-architecture)
3. [Features by User Role](#features-by-user-role)
    - [Admin Features](#admin-features)
    - [Student and Standard User Features](#student-and-standard-user-features)
    - [Vendor Features](#vendor-features)
4. [Technology Stack](#technology-stack)
5. [Installation Instructions](#installation-instructions)
6. [Build and Run Instructions](#build-and-run-instructions)
7. [Testing Instructions](#testing-instructions)
8. [Continuous Integration](#continuous-integration)
9. [Database Schema](#database-schema)
9. [User ID Format](#user-id-format)
10. [Role-Based Access Control](#role-based-access-control)
11. [Fee Calculation Logic](#fee-calculation-logic)
12. [Contribution Guidelines](#contribution-guidelines)
13. [Disclaimer](#disclaimer)

---

## Project Overview

Campus Eats is a comprehensive food ordering and management platform designed specifically for
university campus environments. The application operates offline-first, meaning all data is stored
locally using Room Database, ensuring reliable access even without internet connectivity.

The system supports four distinct user roles: Admin, Student, Standard User, and Vendor. Each role
has a customized interface and feature set that aligns with their specific needs and
responsibilities within the campus food ecosystem.

**Key Objectives:**

- Simplify food ordering for students and campus staff
- Streamline vendor menu and order management
- Provide administrative oversight for campus food services
- Maintain offline functionality for reliability
- Ensure secure authentication and role-based access

---

## System Architecture

The application follows a layered architecture with clear separation of concerns. Each layer has
distinct responsibilities and communicates with adjacent layers through well-defined interfaces.

**Data Layer:**

- Room Database for local data persistence
- Repository pattern for data access abstraction
- Data models and data transfer objects
- Type converters for complex data types

**Business Logic Layer:**

- Use cases and interactors for business operations
- Calculation logic for fees, taxes, and discounts
- Order status management
- Role-based permission validation

**Presentation Layer:**

- Jetpack Compose for declarative UI components
- ViewModels for state management and business logic integration
- StateFlow for reactive UI updates
- Navigation 3 for modern screen transitions

**Utility Layer:**

- DataStore for lightweight preference storage
- Formatting utilities for dates and currency
- JSON serialization and deserialization (Kotlin Serialization)
- ID generation and validation logic

---

## Features by User Role

### Admin Features

**Home Dashboard**

- Personalized greeting by name
- User ID display with copy functionality
- System-wide revenue aggregation
- Global statistics including user counts, vendor status, and order volume

**Services Management**

- User Management: Administrative tools to view, moderate, suspend, or delete accounts
- Vendor Management: Overview of registered vendors and their shop statuses
- Order Management: System-wide oversight and status override capabilities

**Reports and Analytics**

- Daily Trends: Statistical tracking of orders and revenue over the past week
- Vendor Revenue: Rankings of top-performing vendors by revenue share
- Popular Items: Identification of the best-selling menu items system-wide
- Analytics: Detailed user distribution breakdowns

**Settings**

- Secure profile updates (Email/Password)
- System treasury management (Credits issuance)
- Feedback review module for quality control
- Application version tracking and logout

---

### Student and Standard User Features

**Home Dashboard**

- Personalized greeting and quick ID access
- Dynamic vendor marketplace with real-time shop status indicators

**Services**

- Receipts: History of all personal transactions with advanced month/year filtering
- Financial sorting by amount or date
- Total Spending: Cumulative audit of all expenditures

**Order Management**

- Smart Cart: Real-time calculation of taxes, fees, and student discounts
- Secure Checkout: Pickup time scheduling and payment method selection
- Order Tracking: Visual progress monitoring from Pending to Completed

**Reports**

- Local transaction audit reports
- JSON data export for personal record keeping

**Settings**

- Account and security management
- Wallet and card link management
- Direct feedback submission (Complaints/Compliments)

---

### Vendor Features

**Home Dashboard**

- Shop-specific revenue metrics and active order counters
- Real-time shop status toggle (Open, Preparing, Busy, Closed)

**Services**

- Menu Management: Tools to add, edit, or remove food items and categories
- Inventory tracking and stock level management

**Orders Management**

- Live Orders: Consolidated view of pending and active fulfillment tasks
- Temporal Filtering: Order history search by month and year

**Reports**

- Vendor sales reports with customer volume and average order value metrics
- Exportable sales data in JSON format

**Settings**

- Business profile and payout configuration
- Direct student feedback monitoring
- Session management

---

## Technology Stack

**Programming Language:** Kotlin

**UI Framework:** Jetpack Compose (Material 3)

**Database:** Room Database (SQLite)

**Navigation:** Navigation 3

**Asynchronous Programming:** Kotlin Coroutines and Flow

**Serialization:** Kotlinx Serialization

**Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern

**Minimum SDK:** API 24 (Android 7.0 Nougat)

**Target SDK:** API 35 (Android 15)

---

## Installation Instructions

**Prerequisites:**

1. Android Studio (Ladybug or newer recommended)
2. JDK 17 or newer
3. Android SDK platform tools
4. Physical Android device or Emulator

**Step 1: Clone the Repository**

```bash
git clone https://github.com/example/campus-eats-app-kt.git
cd campus-eats-app-kt
```

**Step 2: Initialize the Project**

1. Open Android Studio
2. Select "Open" and navigate to the project directory
3. Allow Gradle to sync and download required dependencies

**Step 3: Build Configuration**

1. Verify the `local.properties` file is generated
2. Ensure `compileSdk` and `targetSdk` match the system requirements

---

## Build and Run Instructions

**Compiling the Project:**

- In Android Studio, go to `Build > Rebuild Project`
- To build via terminal: `./gradlew assembleDebug`

**Executing the Application:**

- Select your device/emulator in the toolbar
- Click the "Run" (Play) icon
- Alternatively, use: `./gradlew installDebug`

**Generating Release Builds:**

- Go to `Build > Generate Signed Bundle / APK`
- Follow the instructions to sign the application for production

---

## Testing Instructions

**Unit Testing:**
The project includes a robust test suite covering business logic and utilities.

- Run all tests: `./gradlew test`
- Location: `app/src/test/java`

**Test Coverage Requirements:**

- Business Logic (Engines/Repositories): Minimum 80%
- Utility Functions: Minimum 70%

**Specific Modules Tested:**

- CheckoutEngine (Fees, Tax, Rounding)
- OrderStatusEngine (Valid state transitions)
- IdGenerator (Format and uniqueness)
- ValidationEngine (Inputs and security)

---

## Continuous Integration

The project uses GitHub Actions for automated building and testing. The CI pipeline is triggered
on every push to the `release` branch and can also be executed manually.

**Workflow Path:** `.github/workflows/build-release.yml`

**Key Features:**

- Automated Unit Testing: Runs the full JUnit test suite.
- Build Verification: Compiles the full project and creates artifacts.
- Artifact Generation:
    - Debug APK: For internal testing and QA.
    - Release APK: For distribution.
    - Android App Bundle (AAB): For Google Play Store submission.

**Accessing Artifacts:**

1. Navigate to the "Actions" tab in the GitHub repository.
2. Select the latest successful workflow run.
3. Scroll down to the "Artifacts" section to download the generated files.

---

## Database Schema

**Users Table:** Primary user records including roles (Admin, Student, Vendor) and balances.
**Menu Items Table:** Catalog of vendor products, pricing, and stock levels.
**Orders Table:** Transaction records with fulfillment metadata and items JSON.
**Cart Items Table:** Transient storage for active shopping sessions.
**Feedback Table:** Log of user-submitted compliments and complaints.
**Coupons Table:** Registry of active discount codes.
**Debit Cards Table:** Local record of linked payment methods.

---

## User ID Format

The system enforces a 16-character alphanumeric User ID format for security and recovery.

**Pattern:** XXXX-XXXX-XXXX-XXXX
**Example:** BK92-M5PQ-88RT-Z10W

- Characters are generated randomly from A-Z and 0-9.
- Hyphens are included for human readability.
- The ID is required for password recovery as an offline authentication token.

---

## Role-Based Access Control

Access is strictly enforced at the UI and Repository layers.

1. **Admin:** Access to global statistics and user moderation tools.
2. **Vendor:** Access to inventory and order fulfillment for their specific shop.
3. **Student:** Access to ordering, discounts (2.5%), and personal history.
4. **Standard User:** Access to ordering and personal history (no student discount).

---

## Fee Calculation Logic

**Tax:** Fixed 20% on subtotal.
**Student Discount:** Fixed 2.5% reduction on subtotal.
**Service Fees:**

- Subtotal < R500: 10% fee.
- R500 <= Subtotal <= R1000: 6.5% fee.
- Subtotal > R1000: R0 fee.
  **Rounding:** Final totals are rounded up to the next R5 increment for cash handling.

---

## Contribution Guidelines

Please adhere to the following standards when contributing to this project:

1. **Code Style:** Use Allman-style brace placement (braces on new lines).
2. **Documentation:** Every new class and public function must include KDoc comments.
3. **Architecture:** Maintain the MVVM + Repository pattern.
4. **Testing:** No pull request will be accepted without corresponding unit tests.
5. **UI:** Follow the Minimalist Apple HIG aesthetic tokens provided in `DesignSystem.kt`.

---

## Disclaimer

UNDER NO CIRCUMSTANCES SHOULD IMAGES OR EMOJIS BE INCLUDED DIRECTLY IN THE README FILE. ALL VISUAL
MEDIA, INCLUDING SCREENSHOTS AND IMAGES OF THE APPLICATION, MUST BE STORED IN A DEDICATED FOLDER
WITHIN THE PROJECT DIRECTORY. THIS FOLDER SHOULD BE CLEARLY STRUCTURED AND NAMED ACCORDINGLY TO
INDICATE THAT IT CONTAINS ALL VISUAL CONTENT RELATED TO THE APPLICATION (FOR EXAMPLE, A FOLDER
NAMED IMAGES, SCREENSHOTS, OR MEDIA). I AM NOT LIABLE OR RESPONSIBLE FOR ANY MALFUNCTIONS,
DEFECTS, OR ISSUES THAT MAY OCCUR AS A RESULT OF COPYING, MODIFYING, OR USING THIS SOFTWARE. IF
YOU ENCOUNTER ANY PROBLEMS OR ERRORS, PLEASE DO NOT ATTEMPT TO FIX THEM SILENTLY OR OUTSIDE THE
PROJECT. INSTEAD, KINDLY SUBMIT A PULL REQUEST OR OPEN AN ISSUE ON THE CORRESPONDING GITHUB
REPOSITORY, SO THAT IT CAN BE ADDRESSED APPROPRIATELY BY THE MAINTAINERS OR CONTRIBUTORS.

---

*END OF DOCUMENT*
