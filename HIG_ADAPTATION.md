# Apple Human Interface Guidelines (HIG) Adaptation for Campus Eats Android

This document outlines the strategy for adapting Apple HIG principles to the Android platform while
maintaining native functionality and stability.

## 1. Window Management & Navigation

- **Centered Titles:** Standardized Top App Bars to use `CenterAlignedTopAppBar` for a cleaner,
  centered aesthetic consistent with iOS.
- **Atomic Stack Transitions:** Refactored navigation stack logic to prevent empty states during
  screen transitions, fixing P0 login crashes.
- **Edge-to-Edge:** Implemented full edge-to-edge drawing with appropriate status bar and navigation
  bar legibility controls.

## 2. UI Components & Spacing

- **Design System:** Centralized all UI tokens (spacing, typography, corner radius) in
  `DesignSystem.kt` to ensure consistency.
- **Corner Radii:** Adopted larger corner radii (12dp-24dp) for cards and buttons to mirror the
  softer, rounded look of iOS.
- **Typography:** Focused on high legibility with semi-bold weights for primary actions and centered
  alignments for headers.

## 3. Modals & Alerts

- **Button Ordering:** Alerts follow the HIG convention of placing the primary action on the right
  and cancel on the left.
- **Modal Shapes:** Dialogs use `extraLarge` corner radii to simulate the appearance of iOS sheets
  and alerts.

## 4. Stability & Performance

- **Lifecycle Management:** Ensured that navigation operations are safe and do not leave the app in
  an inconsistent state during recomposition.
- **Error Handling:** Centralized login and registration error states with user-friendly, centered
  feedback messages.
