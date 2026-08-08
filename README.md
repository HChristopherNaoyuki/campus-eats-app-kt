# Campus Eats - Mobile Campus Dining Platform

Campus Eats is a high-performance, offline-first Android application designed for university
ecosystems. It facilitates seamless food discovery, ordering, and management for students, vendors,
and administrators.

## Table of Contents

1. [Introduction](#introduction)
2. [Key Responsibilities](#key-responsibilities)
3. [Architecture Overview](#architecture-overview)
4. [Functional Modules](#functional-modules)
5. [User Roles and Permissions](#user-roles-and-permissions)
6. [Design Standards](#design-system)
7. [Technical Specifications](#technical-stack)
8. [Installation and Build](#installation-and-setup)
9. [Continuous Integration](#ci-cd-pipeline)
10. [Database Design](#data-persistence)
11. [Testing Strategy](#quality-assurance)

---

## Introduction

The Campus Eats platform addresses the unique logistical challenges of campus dining by providing a
unified interface for all stakeholders. Developed for Rosebank International University College, the
application prioritizes reliability through an offline-first architecture, ensuring transaction
integrity even in areas with intermittent connectivity.

---

## Key Responsibilities

The system is responsible for the following core operations:

- **Identity Management**: Secure generation and management of 16-character alphanumeric
  identifiers.
- **Menu Lifecycle**: Real-time inventory tracking and menu updates for campus vendors.
- **Transaction Engine**: Precise calculation of university-mandated taxes, tiered service fees, and
  student discounts.
- **Order Orchestration**: Managing the lifecycle of an order from placement to fulfillment.
- **Reporting**: Generating fiscal and trend analysis reports for vendors and administrators.

---

## Architecture Overview

Campus Eats adheres to a strict layered architecture pattern to ensure maintainability and
scalability.

- **Presentation Layer**: Built with Jetpack Compose using the MVVM (Model-View-ViewModel) pattern.
- **Domain Layer**: Houses the business logic engines (Checkout, Validation, Order Status).
- **Data Layer**: Implements the Repository pattern, abstracting Room Database (Local) and
  Retrofit (Network) sources.

For more technical details, refer to the [System Architecture Document](docs/architecture.md).

---

## Functional Modules

### Core Modules

- **Authentication Module**: Handles registration, login, and secure ID-based recovery.
- **Ordering Module**: Manages the smart shopping cart and checkout process.
- **Tracking Module**: Provides real-time visual feedback on order fulfillment progress.
- **Management Module**: Admin and Vendor tools for system oversight.

---

## User Roles and Permissions

The system implements a granular Role-Based Access Control (RBAC) system:

| Role              | Primary Responsibilities                  | Permissions                                             |
|:------------------|:------------------------------------------|:--------------------------------------------------------|
| **Administrator** | System oversight and treasury management. | Global auditing, user moderation, credit issuance.      |
| **Vendor**        | Menu management and order fulfillment.    | Inventory control, order status management.             |
| **Student**       | Food discovery and ordering.              | Menu access, 2.5% discount eligibility, order tracking. |
| **Standard User** | Food discovery and ordering.              | General menu access, order tracking.                    |

---

## Design System

The application follows a professional minimalist design language inspired by modern human interface
guidelines.

- **Primary Palette**: Black (Text/Structure), Orange (Actions/Accents), White (Backgrounds).
- **Typography**: Focused on readability and hierarchical clarity.
- **Components**: Standardized buttons, cards, and input fields for a predictable user experience.

See [GUI Design Standards](docs/presentation_layer.md) for implementation details.

---

## Technical Stack

- **Language**: Kotlin 2.x
- **UI Framework**: Jetpack Compose (Material 3)
- **Database**: Room Persistence Library
- **Networking**: Retrofit 2.x with Moshi Converter
- **Asynchronous**: Kotlin Coroutines and StateFlow
- **DI**: Manual Dependency Injection with ViewModel Factory
- **Navigation**: Jetpack Navigation 3 (Modernized)

---

## Installation and Setup

### Prerequisites

- Android Studio (Ladybug or newer)
- JDK 17
- Android SDK (API 24+)

### Build Steps

```bash
# Clone the repository
git clone https://github.com/example/campus-eats-app-kt.git

# Build the project
./gradlew assembleDebug

# Run unit tests
./gradlew test
```

---

## CI/CD Pipeline

The project utilizes a focused GitHub Actions workflow to maintain code quality.

- **Validation**: Automated linting and static analysis.
- **Compilation**: Continuous build verification.
- **Quality**: Execution of the full unit test suite.

Detailed documentation is available in the [CI/CD Configuration Guide](docs/ci_cd.md).

---

## Data Persistence

Local data is managed through a multi-table Room database. Key entities include:

- `UserEntity`: Profile and authentication metadata.
- `MenuItemEntity`: Catalog of available products.
- `OrderEntity`: Immutable transaction records.
- `FeedbackEntity`: Audit log for quality control.

Refer to the [Data Layer Documentation](docs/data_layer.md) for schema details.

---

## Quality Assurance

The application maintains a high standard of quality through rigorous testing of the domain layer.

- **CheckoutEngine Tests**: Verify financial precision and rounding compliance.
- **ValidationEngine Tests**: Ensure input integrity and security.
- **Repository Tests**: Validate data flow between local and remote sources.

---

*END OF DOCUMENT*
