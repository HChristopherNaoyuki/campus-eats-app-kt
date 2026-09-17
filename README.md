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
Use the following 10 demo accounts to explore the different user roles:

### 2.1 Demo Accounts
| Role | Name | Email | Password |
| :--- | :--- | :--- | :--- |
| **Administrator** | Amara Nkosi | `amara.nkosi@campuseats.test` | `Adm1n#Amara` |
| **Administrator** | Pieter van Wyk | `pieter.vanwyk@campuseats.test` | `Adm1n#Pieter` |
| **Vendor** | Thandiwe Mokoena | `thandiwe.mokoena@campuseats.test` | `Vend0r#Thandi` |
| **Vendor** | Sipho Dlamini | `sipho.dlamini@campuseats.test` | `Vend0r#Sipho` |
| **Vendor** | Annelie Botha | `annelie.botha@campuseats.test` | `Vend0r#Annelie` |
| **Standard** | Lerato Khumalo | `lerato.khumalo@campuseats.test` | `Stand@rd#Lerato` |
| **Standard** | Johan Pretorius | `johan.pretorius@campuseats.test` | `Stand@rd#Johan` |
| **Standard** | Zanele Ndlovu | `zanele.ndlovu@campuseats.test` | `Stand@rd#Zanele` |
| **Standard** | Marius Steyn | `marius.steyn@campuseats.test` | `Stand@rd#Marius` |
| **Student** | Naledi Mahlangu | `naledi.mahlangu@campuseats.test` | `Stud3nt#Naledi` |

### 2.2 Sample Coupons
- `CAMPUS10`: 10% Discount
- `EATS20`: 20% Discount
- `WELCOME5`: 5% Discount

### 2.3 Instructions for Testing
1. **Start the application** in an Android Emulator or on a physical device.
2. The application will automatically **seed the local database** on first launch if it is empty.
3. **Login** using one of the 10 demo accounts provided above.
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

## AI Usage & Contributions Policy

We welcome the use of AI tools to assist developers, but
we enforce strict human accountability:

* **No Fully AI-Generated PRs:** This project does not accept entirely
  machine-generated pull requests.
* **Human Oversight:** You must personally understand, verify, and take 100%
  responsibility for every single line of code you submit.
* **Never Let the LLM Think for You:** AI models make logical mistakes and
  cannot be held accountable for security flaws or bugs.
* **Disclosure:** If a significant portion of your contribution was generated
  by an AI assistant, please explicitly note it in your Pull Request
  description.

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
