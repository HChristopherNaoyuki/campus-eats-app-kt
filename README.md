# Campus Eats - Mobile Campus Dining Platform (v2.0.0)

**Module:** OPEN SOURCE CODING (INTERMEDIATE) (OPSC6312)
**Assessment:** Part 2 - App Prototype Development
**Developer:** Naoyuki Christopher H.

---

## 1. Introduction & Purpose

Campus Eats is a high-performance, offline-first Android application designed for university
ecosystems, specifically tailored for Rosebank International South Africa. The application
facilitates seamless food discovery, ordering, and management for students, vendors, and
administrators.

The primary problem addressed is the inefficiency of manual campus dining processes, such as
long queues, miscommunication, and lack of digital transaction records. Campus Eats provides
a centralized REST API "Source of Truth" to coordinate between all stakeholders, ensuring
reliability and fiscal integrity.

---

## 2. Design Considerations

### 2.1 User Interface (UI)

The application follows a professional minimalist design language inspired by modern Human Interface
Guidelines (HIG).

- **Palette**: Black (Structure), Orange (#FFFF8C00 - Branding), White (Backgrounds), Action Blue (
  #007AFF - Inputs).
- **Architecture**: Jetpack Compose (Material 3) with a focus on generous spacing and clear visual
  hierarchy.
- **Error Handling**: Comprehensive input validation in ViewModels ensures the app handles invalid
  entries (e.g., mismatched passwords, empty fields) without crashing, providing real-time feedback
  to the user.

### 2.2 Coding Standards

- **Style**: Strict adherence to the **Allman style** (opening braces on new lines) across all
  Kotlin files.
- **Pattern**: Model-View-ViewModel (MVVM) with the Repository pattern for clean separation of
  concerns.
- **Security**: Mandatory password encryption using SHA-256 hashing before local persistence or
  network transmission.

---

## 3. Functional Prototype Features (Part 2)

The current v2.0.0 prototype implements the following core requirements:

- **Authentication**: Secure registration and login with SHA-256 encrypted passwords.
- **Global Identity**: Automatic generation of unique 16-character alphanumeric User IDs (
  XXXX-XXXX-XXXX-XXXX).
- **Settings Manager**: Functional settings menu allowing users to update profile metadata and
  role-specific configurations (e.g., vendor bank details).
- **Vendor Fulfillment**: Real-time order tracking for vendors, including itemized receipts and
  status lifecycle management (Pending -> Accepted -> Preparing -> Ready -> Completed).
- **Admin Dashboard**: System-wide oversight with user moderation, vendor status control, and global
  financial reporting.
- **REST API Integration**: Full integration with the
  hosted [Fake Restaurant API](https://fakerestaurantapi.runasp.net/) for multi-device
  synchronization.
- **Financial Engine**: Automated calculation of service fees (tiered), campus tax (20%), student
  discounts (2.5%), and R5 rounding logic.

---

## 4. GitHub & Automated Testing

### 4.1 Version Control

This project is hosted on GitHub, utilizing a structured commit history to document the iterative
development process.

### 4.2 GitHub Actions (CI/CD)

The project utilizes a focused CI pipeline (`ci.yml`) to ensure code quality and build integrity.

- **Workflow**:
    1. **Static Analysis**: Runs Android Lint to detect code smells and HIG violations.
    2. **Compilation**: Verifies the application builds successfully (`assembleDebug`).
    3. **Automated Testing**: Executes the full JUnit 4 unit test suite.
- **Caching**: Implements Gradle dependency caching to optimize CI execution time and resource
  usage.

### 4.3 Unit Testing

Comprehensive tests verify the "Main Functionality":

- `CheckoutEngineTest`: Validates financial precision and rounding compliance.
- `AuthRepositoryTest`: Verifies encryption and authentication logic.
- `MenuRepositoryTest`: Ensures correct DAO interactions and API synchronization.

---

## 5. Technical Specifications

- **Tech Stack**: Kotlin 2.x, Jetpack Compose, Room DB, Retrofit 2.x, Coroutines.
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 37 (Android 15)

---

## 6. Demonstration Video

A professional demonstration of the Campus Eats prototype, including voice-over explanation of the
RBAC system, API integration, and database state, can be viewed here:

[**Watch Campus Eats Prototype Demo (v2.0.0)
**](https://www.youtube.com/watch?v=placeholder_link_unlisted)

---

## 7. AI Usage Disclosure

AI tools were utilized during this project for the following purposes:

- **Boilerplate Generation**: Generating standard Room DAO and Entity structures.
- **Code Refinement**: Assisting in the implementation of the Allman style formatting.
- **Documentation**: Structuring technical guides in professional business English.

All AI-generated snippets were manually reviewed and verified for architectural compliance.

---

*Rosebank International University College - OPSC6312 POE*
