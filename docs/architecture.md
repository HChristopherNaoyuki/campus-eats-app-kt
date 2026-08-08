# System Architecture

This document describes the high-level architecture of the Campus Eats application, detailing the
layers, their responsibilities, and the communication patterns between them.

## Table of Contents

1. [Architectural Overview](#architectural-overview)
2. [Presentation Layer](#presentation-layer)
3. [Domain Layer](#domain-layer)
4. [Data Layer](#data-layer)
5. [Dependency Injection](#dependency-injection)
6. [Data Flow Patterns](#data-flow-patterns)

---

## Architectural Overview

Campus Eats follows the **Clean Architecture** principles combined with the **MVVM (
Model-View-ViewModel)** pattern. The system is divided into three primary layers to ensure
separation of concerns and testability.

- **Presentation Layer**: Handles user interaction and UI state.
- **Domain Layer**: Contains the core business rules and logic.
- **Data Layer**: Manages data persistence and network communication.

---

## Presentation Layer

The presentation layer is built using **Jetpack Compose**, focusing on a declarative and reactive
UI.

### Responsibilities

- Rendering UI components according to state.
- Handling user input and delegating to ViewModels.
- Observing reactive streams (StateFlow) from ViewModels.
- Navigating between screens using Navigation 3.

### Key Components

- **Composables**: Modular UI units.
- **ViewModels**: Maintain UI state and communicate with repositories.
- **StateFlow**: Exposes reactive, lifecycle-aware state updates.

---

## Domain Layer

The domain layer contains the business engines that implement university-specific rules.

### Responsibilities

- Calculating fees, taxes, and discounts.
- Validating state transitions for orders.
- Enforcing input validation rules.

### Key Engines

- `CheckoutEngine`: Financial logic.
- `OrderStatusEngine`: Order lifecycle rules.
- `ValidationEngine`: Security and format checks.

---

## Data Layer

The data layer implements the **Repository Pattern**, providing a clean API for the rest of the
application to access data without knowing its source.

### Responsibilities

- Abstracting local (Room) and remote (Retrofit) data sources.
- Synchronizing data between local and remote systems.
- Handling data transformations (DTOs to Entities).

### Sources

- **Room Database**: Primary source of truth for offline-first operation.
- **Retrofit Service**: For remote API integration (Fake Restaurant API).

---

## Dependency Injection

The project uses a manual dependency injection approach through the `MainActivity`. Dependencies are
initialized in the `onCreate` method and passed to ViewModels via a custom `viewModelFactory`.

```kotlin
// Example from MainActivity
val database = CampusEatsDatabase.getDatabase(this)
val apiService = RetrofitClient.instance
val authRepository = AuthRepository(database.userDao(), apiService)
// ...
initializer
{
    LoginViewModel(authRepository)
}
```

---

## Data Flow Patterns

1. **Unidirectional Data Flow (UDF)**:
    - User events flow from UI to ViewModel.
    - ViewModel updates its state.
    - UI observes state changes and re-renders.

2. **Offline-First Synchronization**:
    - Writes are typically performed locally first.
    - If connectivity is available, operations are synchronized with the remote API.
    - Reads observe local database flows for real-time updates.

---

*END OF DOCUMENT*
