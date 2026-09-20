# Campus Eats - Mobile Campus Dining Platform (v3.0.0)

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
9. [AI Usage and Contributions Policy](#9-ai-usage-and-contributions-policy)
10. [Commit Message Convention](#10-commit-message-convention)
11. [Visual Media Policy](#11-visual-media-policy)
12. [Liability and Issue Reporting](#12-liability-and-issue-reporting)

---

## 1. Introduction and Purpose

Campus Eats is a production-quality Android application designed to modernize
and streamline the dining experience within university ecosystems. The
application is tailored for Rosebank International South Africa and facilitates
interaction between students, food vendors, and administrators.

The application addresses common campus dining inefficiencies such as long
queues and manual transaction recording by providing a reliable, digital-first
solution. It uses a centralized REST API to ensure data consistency across
multiple devices while maintaining an offline-first architecture for
resilience.

This repository supports the following academic modules:

- OPSC6312: Open Source Coding
- XISD6329: Work Integrated Learning

---

## 2. Project Team

This section lists the contributors for each academic module, together with
their roles and student numbers.

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

A recorded demonstration of the Campus Eats mobile application is available
online.

- Title: OPSC6312 and XISD6329: POE Campus Eats Mobile App Part 2
- Link: https://youtu.be/XHF5T_HrPOI

The video presents the application in operation, including the primary user
workflows for each role.

---

## 4. Technical Overview

The application is built using modern Android development practices and
libraries:

- Language: Kotlin 2.x
- UI Framework: Jetpack Compose with Material 3
- Architecture: Model-View-ViewModel (MVVM) with the Repository pattern
- Persistence: Room Database for local caching and offline support
- Networking: Retrofit 2.x for REST API communication
- Backend: Firebase Realtime Database and Firebase Authentication
- Security: SHA-256 password encryption and RBAC (Role-Based Access Control)

---

## 5. Demo Accounts

The following demo accounts are provided to facilitate testing and
demonstration of the various system roles. The local database is pre-populated
with these accounts on initial launch.

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

To verify the application functionality, follow these steps:

1. Clone the repository and open the project in Android Studio.
2. Synchronize the project with Gradle files.
3. Start an Android Emulator (API 24 or higher) or connect a physical device.
4. Build and run the application.
5. On the first launch, the application seeds the local database with sample
   data.
6. Use the credentials provided in the Demo Accounts section to log in.
7. Perform role-specific workflows such as placing an order (Student) or
   fulfilling an order (Vendor).

---

## 7. Sample Coupons

Administrators can use the following sample coupons for testing:

- CAMPUS10: 10% Discount
- EATS20: 20% Discount
- WELCOME5: 5% Discount

---

## 8. Functional Features

- Secure Authentication: Email and password login, plus Google Single Sign-On.
- Role-Based Access Control: Specialized dashboards for Students, Vendors, and
  Administrators.
- Responsive UI: Dynamic layouts that adapt to both phone and tablet sizes.
- Order Management: Itemized ordering with platform-native time selection.
- Financial Reporting: Real-time revenue tracking and item-level sales
  breakdowns.
- Analytics: Visual spending charts and structured JSON data export.
- System Moderation: Administrative tools for user status management and credit
  issuance.

---

## 9. AI Usage and Contributions Policy

We welcome the use of AI tools to assist developers, but we enforce strict
human accountability:

- No Fully AI-Generated PRs: This project does not accept entirely
  machine-generated pull requests.
- Human Oversight: You must personally understand, verify, and take full
  responsibility for every line of code you submit.
- Never Let the LLM Think for You: AI models make logical mistakes and cannot
  be held accountable for security flaws or bugs.
- Disclosure: If a significant portion of your contribution was generated by
  an AI assistant, note it explicitly in your Pull Request description.

---

## 10. Commit Message Convention

All commits must follow the format described below. This convention is based
on the widely used Linux kernel style and is intended to keep the project
history readable and useful.

### 10.1 Header Line

The header is a single line that explains the commit in one line. Use the
imperative mood. For example, write "Fix login crash on empty email" rather
than "Fixed" or "Fixes."

### 10.2 Commit Body

The body of the commit message is a few lines of text that explain the change
in more detail. Where relevant, include background about the issue being
fixed.

The body may be several paragraphs. Use proper word wrapping and keep each
column shorter than about 74 characters. This ensures that "git log" displays
the message cleanly, even when the output is indented.

Explain your solution and the reason you are making the change, rather than
simply describing what the change does. Reviewers and your future self can
read the patch to see what changed, but they may not understand why a
particular solution was chosen.

### 10.3 Trailers

End the commit message with the appropriate trailers. Common examples include:

- Reported-by: whoever-reported-it
- Signed-off-by: Your Name

### 10.4 Format Overview
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

### 10.5 Example
```
Fix order total when a coupon is applied

The total was calculated before the coupon discount was applied,
which caused the displayed amount to differ from the amount
charged. The calculation now happens after the discount step.

This keeps the summary, receipt, and stored order value in
agreement.

Reported-by: Makaya G.
Signed-off-by: Naoyuki Christopher H.
```

---

## 11. Visual Media Policy

Under no circumstances should images or emojis be included directly in this
file. All visual media, including screenshots and images of the application,
must be stored in a dedicated folder within the project directory.

This folder should be clearly structured and named accordingly to indicate
that it contains all visual content related to the application. Suitable
examples include a folder named Images, Screenshots, or Media.

---

## 12. Liability and Issue Reporting

The author is not liable or responsible for any malfunctions, defects, or
issues that may occur as a result of copying, modifying, or using this
software.

If any problems or errors are encountered, do not attempt to fix them silently
or outside the project. Instead, submit a pull request or open an issue on the
corresponding GitHub repository so that it can be addressed appropriately by
the maintainers or contributors.

---

End of document.

---
