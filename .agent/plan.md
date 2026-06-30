# Project Plan

Campus Eats is an offline-first Android mobile application for Rosebank International University College. It handles food ordering, vendor management, and administrative oversight using a local Room Database. It supports Student, Standard, Vendor, and Admin roles with a unique 16-character User ID system. UI must follow Material Design 3 and be adaptive.

## Project Brief

# Campus Eats Project Brief

Campus Eats is an offline-first mobile solution designed for the Rosebank International University College ecosystem. It streamlines the food ordering process between students and vendors through a robust local management system, ensuring service availability even without active internet connectivity.

### Features
*   **Secure Identity & Authentication**: A dedicated registration system that generates unique 16-character alphanumeric User IDs (XXXX-XXXX-XXXX-XXXX) for all roles (Student, Vendor, Admin), serving as the primary key for account recovery and identity.
*   **Offline-First Ordering & Vendor Management**: Full CRUD capabilities for vendors to manage menus and for users to browse, add items to carts, and place orders locally.
*   **Smart Checkout Engine**: Automated fee calculation system implementing specific business logic: 20% tax, dynamic service fees, a 2.5% student discount, and final price rounding to the next R5 increment.
*   **Role-Based Dashboards**:
    *   **Students/Standard**: Real-time order tracking and JSON transaction history exports.
    *   **Vendors**: Order lifecycle management (Pending, Active, Completed) and sales analytics.
    *   **Admins**: Comprehensive system oversight, user/vendor moderation, and credit wallet management.

### High-Level Technical Stack
*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Material Design 3)
*   **Navigation**: Jetpack Navigation 3 (State-driven architecture)
*   **Adaptive Layouts**: Compose Material 3 Adaptive library (supporting various screen sizes and foldables)
*   **Local Persistence**: Room Database (Core architecture for offline-first functionality)
*   **Asynchrony**: Kotlin Coroutines & Flow
*   **Image Loading**: Coil
*   **Serialization**: Kotlinx Serialization (for JSON reporting and data export)

## Implementation Steps
**Total Duration:** 32m 35s

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

### Task_4_Refinement_And_Verification: Refine the UI/UX using Material Design 3 and Adaptive layouts. Create the app icon and conduct final stability and requirement verification.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - The app uses Material Design 3 with a vibrant, energetic color scheme (light/dark themes).
  - Layouts are adaptive for different screen sizes and foldables.
  - Adaptive app icon matching Campus Eats branding is implemented.
  - Application is stable, does not crash, and build passes.
  - Critic agent confirms alignment with all project requirements (offline-first, unique IDs, checkout logic).
- **StartTime:** 2026-06-30 15:07:56 SAST

