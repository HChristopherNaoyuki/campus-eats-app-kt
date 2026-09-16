# Campus Eats - Mobile Campus Dining Platform (v3.0.0)

**Modules:** 
- OPEN SOURCE CODING (OPSC6312)
- WORK INTEGRATED LEARNING (XISD6329)

**Developer:** Naoyuki Christopher H.

---

## 1. Introduction & Purpose

Campus Eats is a production-ready, high-performance Android application designed for university
ecosystems, specifically tailored for Rosebank International South Africa. This repository serves
as a core project for both **OPSC6312 (Open Source Coding)** and **XISD6329 (Work Integrated Learning)**.

- **OPSC6312 Relevance**: Demonstrates advanced open-source development practices, including version 
  control, automated testing (CI/CD), and integration with external REST APIs.
- **XISD6329 Relevance**: Serves as a Work Integrated Learning project that simulates a real-world
  software engineering environment, requiring full-stack mobile development, stakeholder-specific 
  dashboards, and robust financial processing logic.

The application facilitates seamless food discovery, ordering, and management for students, 
vendors, and administrators, solving common campus dining inefficiencies.

---

## 2. Sample / Test Data

The application includes a pre-configured database seeder to facilitate testing and demonstration.
Use the following credentials to explore the different user roles:

### 2.1 Role-Based Credentials
| Role | Email | Password |
| :--- | :--- | :--- |
| **Administrator** | `pieter.vanwyk@campuseats.test` | `Adm1n#Pieter` |
| **Student** | `aisha.govender@campuseats.test` | `Stud3nt#Aisha` |
| **Vendor** | `mama.nandi@campuseats.test` | `Vend0r#Nandi` |

### 2.2 Sample Coupons
- `CAMPUS10`: 10% Discount
- `EATS20`: 20% Discount
- `WELCOME5`: 5% Discount

### 2.3 Instructions for Testing
1. **Start the application** in an Android Emulator or on a physical device.
2. The application will automatically **seed the local database** on first launch if it is empty.
3. **Login** using one of the credentials provided above.
4. Follow the role-specific workflows (e.g., Student placing an order, Vendor fulfilling it).
5. **Reset Data**: To recreate sample data, clear the application storage or uninstall and reinstall the app.

---

## 3. Functional Prototype Features (v3.0.0)

The current release implements the following core requirements:

- **Authentication**: Secure registration, login, and **Google Single Sign-On (SSO)**.
- **Responsive UI**: Adaptive layouts with dynamic horizontal padding for phones and tablets.
- **Order Management**: Platform-native **TimePicker** for accurate pickup scheduling.
- **Advanced Analytics**: Canvas-based spending charts with numeric values and **JSON Export** functionality.
- **Vendor Reporting**: Detailed revenue breakdowns per menu item sold.
- **REST API Integration**: Synchronization with the hosted [Fake Restaurant API](https://fakerestaurantapi.runasp.net/).
- **Financial Engine**: Automated service fees, campus tax (20%), and student discounts (2.5%).

---

## 4. Technical Specifications

- **Tech Stack**: Kotlin 2.x, Jetpack Compose, Room DB, Retrofit 2.x, Coroutines, Firebase.
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 37 (Android 15)
- **Architecture**: MVVM + Repository Pattern.
- **Style**: Strict Allman style (opening braces on new lines).

---

## 5. Automated Testing & Quality

### 5.1 GitHub Actions (CI/CD)
The project utilizes a focused CI pipeline (`ci.yml`) ensuring:
1. **Static Analysis**: Android Lint with 0 errors/warnings requirement.
2. **Compilation**: Successful build of debug APK and release AAB.
3. **Unit Testing**: Execution of 68+ JUnit tests.

### 5.2 Test Coverage
- `CheckoutEngineTest`: Financial precision and rounding compliance.
- `AuthRepositoryTest`: Encryption and identity management.
- `MenuRepositoryTest`: DAO and API synchronization logic.

---

## 6. Demonstration Video

A professional demonstration of the Campus Eats v3.0.0 release can be viewed here:

[**Watch Campus Eats Production Demo (v3.0.0)**](https://www.youtube.com/watch?v=placeholder_link_v3)

---

## 7. AI Usage Disclosure

AI tools were utilized for boilerplate generation and code refinement (Allman style consistency),
with all output manually verified for architectural compliance.

---

*Rosebank International University College*
