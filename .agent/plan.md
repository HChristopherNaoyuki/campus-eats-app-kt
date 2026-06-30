# Project Plan

Implement the Campus Eats app strictly following the minimalist black, orange, and white theme and
the role-based user journeys. Ensure 16-char ID generation, 4-tab bottom navigation (Home, Services,
Activity, Settings), and the smart checkout logic.

Key constraints:

1. Returning to landing page after registration.
2. Greeting "Hello, {Full Name}." on Home.
3. Activity tab handles Receipts and Reports.
4. Settings handles Profile Update, Logout, App Version.
5. Strict RBAC.
6. Offline-first with Room.

## Project Brief

# Campus Eats Project Brief

Campus Eats is a specialized offline-first mobile application tailored for the Rosebank
International University College community. It provides a robust, local-first infrastructure for
food ordering and vendor management, characterized by a high-contrast minimalist aesthetic and
strict role-based access control.

### Features

* **Secure Identity Management**: Registration system generating unique 16-character alphanumeric
  User IDs (XXXX-XXXX-XXXX-XXXX) which serve as the primary credential for account recovery and
  identification.
* **Offline-First Architecture**: Utilizes a local Room database to ensure all features—from menu
  management to order history—remain fully functional without an active internet connection.
* **Role-Specific Dashboards**: A state-driven bottom navigation system (Home, Services, Activity,
  Settings) that dynamically adjusts tools and statistics based on the user's role (Student,
  Standard, Vendor, Admin).
* **Advanced Checkout Logic**: A custom financial engine that automatically calculates a 20% tax,
  tiered service fees, a 2.5% student discount, and rounds final totals to the nearest R5 increment.
* **Activity Reporting**: Dedicated tracking of receipts and the ability to export transaction data
  into standardized JSON reports for personal or business auditing.

### High-Level Technical Stack

* **Language**: Kotlin with Coroutines for asynchronous operations.
* **UI Framework**: Jetpack Compose (Material Design 3) with a strict Black, Orange, and White
  minimalist theme.
* **Navigation**: **Jetpack Navigation 3** (State-driven) for managing role-based destination
  routing.
* **Adaptive Strategy**: **Compose Material Adaptive** library to ensure a consistent experience
  across different device form factors.
* **Persistence**: Room Database to support the mandatory offline-first requirement.
* **Data Handling**: Kotlinx Serialization for report generation and data export.

## Implementation Steps

**Total Duration:** 1h 21m 52s

### Task_1_Foundation: Implement the Room database schema and Authentication system. This includes creating entities for Users, Menu Items, and Orders, and developing the registration logic to generate unique 16-character User IDs (XXXX-XXXX-XXXX-XXXX).
- **Status:** COMPLETED
- **Updates:** Implemented Room database schema (User, MenuItem, Order), unique 16-character ID generation logic, and authentication screens (Landing, Register, Login, Forgot Password) using Jetpack Compose and Material Design 3. Configured Jetpack Navigation 3 and Material 3 Adaptive layout. Created adaptive app icon. Project builds successfully.
- **Acceptance Criteria:**
  - Room database with User, Menu, and Order tables is initialized.
  - Unique 16-character ID generation logic is implemented and verified.
  - Registration and Login screens are functional with role-based redirection (Student, Vendor, Admin).

### Task_2_Ordering_System: Develop the Vendor menu management and the customer ordering flow. Implement CRUD for menus and a local cart system for users to browse and select items.
- **Status:** COMPLETED
- **Updates:** Implemented Vendor menu management (CRUD) and customer ordering flow with a persistent local cart. Added `CartItemEntity` and `CartDao` for offline-first cart persistence. Used Coil for image loading and Material Design 3 for UI. Adaptive layouts used for menu browsing. Navigation routes updated for the new screens. Database version incremented to 2.
- **Acceptance Criteria:**
  - Vendors can create, read, update, and delete menu items locally.
  - Users can browse the menu and add/remove items from a persistent cart.
  - Menu item images load correctly using Coil.
  - Full offline-first functionality using Room is verified for browsing and cart management.
- **Duration:** 7m 52s

### Task_3_Checkout_And_Dashboards: Implement the Smart Checkout Engine and role-specific dashboards. This includes the automated fee/discount calculations and the various views for Students (order tracking), Vendors (order lifecycle), and Admins (system oversight).
- **Status:** COMPLETED
- **Updates:** Implemented Smart Checkout Engine with 20% tax, dynamic service fees, 2.5% student discount, and R5 rounding. Developed Student Dashboard with order tracking and JSON export, Vendor Dashboard with order lifecycle management, and Admin Dashboard with moderation and wallet management. Updated Room database to version 3. Full navigation and adaptive layouts implemented.
- **Acceptance Criteria:**
  - Smart Checkout Engine applies 20% tax, service fees, 2.5% student discount, and rounds to the next R5 increment.
  - Student Dashboard shows real-time order tracking and JSON transaction history export.
  - Vendor Dashboard supports order lifecycle management (Pending, Active, Completed).
  - Admin Dashboard allows user/vendor moderation and wallet management.
- **Duration:** 24m 43s

### Task_4_Theme_And_Navigation: Implement the strict Black, Orange, and White minimalist theme and the state-driven 4-tab navigation (Home, Services, Activity, Settings). Refine the Home screen with personal greetings, the Activity tab for receipts/reports, and the Settings tab for user management. Ensure registration redirects back to the landing page.

- **Status:** COMPLETED
- **Updates:** Refined the UI with a strict Black, Orange, and White minimalist theme. Implemented
  the persistent 4-tab bottom navigation (Home, Services, Activity, Settings) with role-based
  routing. Added the personalized greeting "Hello, {Full Name}." to the Home screen. Consolidated
  Receipts and JSON Reporting in the Activity tab. Fully implemented functional Settings (Profile
  Update, Logout, App Version). Updated Registration to return to the Landing page after ID display.
  Verified all RBAC logic and offline-first persistence. Project builds successfully.
- **Acceptance Criteria:**
  - Strict Black, Orange, and White minimalist theme is applied across all screens.
  - 4-tab bottom navigation is functional and role-aware.
  - Home screen displays 'Hello, {Full Name}.' correctly.
  - Activity tab handles Receipts and JSON Report generation.
  - Settings tab contains Profile Update, Logout, and App Version.
  - Registration successfully returns user to the Landing page.
- **Duration:** 49m 17s

### Task_5_Final_Verification: Final stability and requirement verification. Run the application to ensure all features (Offline-first, RBAC, Checkout Engine, 16-char ID, Navigation) work harmoniously without crashes.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Application is stable and does not crash during end-to-end testing.
  - Build passes successfully.
  - All existing tests pass.
  - Critic agent confirms alignment with all project requirements (theme, navigation, RBAC, smart
    checkout).
- **StartTime:** 2026-06-30 17:19:56 SAST

