# Presentation Layer Documentation

The Presentation Layer defines the user interface and interaction model of the Campus Eats
application using Jetpack Compose and Material 3.

## Table of Contents

1. [Design Philosophy](#design-philosophy)
2. [Visual Design System](#visual-design-system)
3. [Shared Components](#shared-components)
4. [Screen Architecture](#screen-architecture)
5. [Navigation](#navigation)
6. [Theming](#theming)

---

## Design Philosophy

The interface follows a **professional minimalist** aesthetic inspired by the Apple Human Interface
Guidelines (HIG).

- **Clarity**: High contrast and readable typography.
- **Simplicity**: Neutral backgrounds with purposeful use of color.
- **Consistency**: Standardized spacing and component behavior.

---

## Visual Design System

The system is defined in `DesignSystem.kt` and `Color.kt`.

### Primary Palette

- **Black**: Used for primary text and structural elements.
- **Orange (#FFFF8C00)**: Reserved for brand identity, primary actions, and highlights.
- **Action Blue (#007AFF)**: Used specifically for form-based actions (e.g., "Sign In", "Submit").
- **White**: Default background for a clean, professional feel.

---

## Shared Components

Reusable UI components are centralized in `HIGComponents.kt`.

- **`HIGButton`**: A standardized 50dp height button with configurable primary/secondary styles.
- **`HIGCard`**: Content blocks with consistent padding and large corner radii (28dp).
- **`HIGTopAppBar`**: Centered headers for navigation and title display.
- **`HIGSegmentedControl`**: iOS-style toggle for selecting roles or filtering data.
- **`HIGServiceRow`**: Layout for dashboard services or menu entries.

---

## Screen Architecture

Each screen is composed of a **ViewModel** and a **Composable** function.

### ViewModel Responsibilities

- Managing UI state using `StateFlow`.
- Handling events from the UI.
- Communicating with repositories.
- Performing UI-level validation.

### Composable Responsibilities

- Rendering the current state.
- Emitting events to the ViewModel.
- Managing local animations and transitions.

---

## Navigation

The application uses **Navigation 3** for state-driven transitions.

- **`Route`**: A sealed interface defining all valid navigation destinations and their arguments.
- **`backStack`**: Manages the screen history and atomic updates.

---

## Theming

The `CampusEatsAppTheme` applies the design system globally.

- **Dynamic Color**: Disabled to ensure brand consistency.
- **Shapes**: Utilizes Material 3 `Shapes` with custom corner radii (10dp to 28dp).
- **Typography**: Uses a weighted hierarchy of titles, body text, and labels.

---

*END OF DOCUMENT*
