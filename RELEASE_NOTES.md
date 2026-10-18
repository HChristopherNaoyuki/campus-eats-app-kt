# Campus Eats - Release Notes

## Version 10.0.0

**Release Date:** 2026-10-18

---

### Overview

Version 10.0.0 is a milestone release delivering full web platform parity across all user roles (Administrator, Vendor, Student, and Standard), complete feedback review and resolution loops, 10-second automatic background cloud synchronization with administrator feedback pull, tab re-tap navigation reset, Google SSO Credential Manager integration, Apple HIG dark mode tokens, and updated unit test coverage with 69 passing unit tests (`versionCode 12`).

---

### Key Features & Technical Deliverables

**1. Administrator Role & Audit Modules**
- Administrator Home page displays the signed-in user name ("Amara Nkosi") and welcome header: *"Welcome to Campus Eats - Manage users, vendors, menus, and orders for your campus food network, all in one place."*
- Management modules under Browse: **User Management**, **Vendor Management**, **Order Management**, and real-time **Security Log** audit trail (`AdminSecurityLogView`).
- Orders tab modules: **Global Receipts** (receipt summary revision on row tap), **Reports** (System Summary), and **Insight Reports** (ordering filter supports "New to old", "Month", "Year").
- **Generate Coupons**: Supports targeted user selection, item quantities, sample coupon chips (`CAMPUS10`, `EATS20`, `WELCOME5`), and active coupon lists.
- **Feedback Review**: Cloud feedback pull in `FirebaseSyncManager` automatically loads complaints and compliments submitted across all devices into local Room storage. Administrators review submissions in `AdminFeedbackWindow` and mark pending items as resolved with status updates mirrored to Firebase (`feedback/{feedbackId}/status`).

**2. Vendor Role Modules**
- **New Menu Item**: Features 4 bottom tray navigation icons with "Add Item" control positioned above the bar.
- **Inventory Manager**: Full inventory management with 4 bottom tray icons.
- **Live Orders**: Tapping an order opens a detailed window displaying picked items (Check modal), special request notes, and preferred pickup times.
- **Financial Reports**: Displays total food items count, total orders count, and total revenue metrics.

**3. Student & Standard Role Modules**
- **Live Status & Order History**: Tapping a receipt row opens a summary revision window displaying food items, special requests, pickup times, payment methods, and grand totals.
- **Tray Navigation Reset**: Re-tapping an active bottom navigation icon immediately redirects back to that tab's root window.

**4. Settings Platform Link**
- Platform Information card with a **WEBSITE** button launching `https://campus-eats-platform.lovable.app/` via external browser intent.

**5. Technical Architecture & Verification**
- **Google SSO**: Verified Credential Manager API with Web OAuth Client ID `64265928399-tl7gi0kkolvke8k7etaio9h66ov44hi3.apps.googleusercontent.com` and `[FIREBASE_SSO]` passwordHash sentinel flag.
- **Synchronization**: 10-second continuous sync loop with exponential backoff (1s to 60s), isolated node failure protection, and active `Job` lifecycle guard.
- **Unit Testing**: Executed `./gradlew :app:testDebugUnitTest` with **69 passed, 0 skipped, 0 failed**.

---

### Technical Details

**Version Code:** 12

**Version Name:** 10.0.0

**Package Name:** com.example.campus_eats_app_kt

---

## Version 9.0.0

**Release Date:** 2026-10-18

---

### Overview

Version 9.0.0 introduced cloud feedback synchronization for Administrators, role alignment, and updated unit test coverage.

---

## Version 8.0.0

**Release Date:** 2026-10-17

---

### Overview

Version 8.0.0 delivered role alignment with web platform modules, navigation tab re-tap behavior, sample coupon selection, full feedback resolution workflow, and Apple HIG dark mode tokens.

---

## Version 7.0.0

**Release Date:** 2026-10-17

---

### Overview

Version 7.0.0 aligned role modules with web platform structures, updated administrator home copy, introduced Security Log audit trail, and added the Settings platform information link.

---

## Version 6.0.0

**Release Date:** 2026-10-16

---

### Overview

Version 6.0.0 introduced the Apple HIG-style Dark Mode palette, website brand color alignment, and DataStore-persisted appearance mode selection (System, Light, Dark).

---

## Version 5.0.0

**Release Date:** 2026-10-15

---

### Overview

Version 5.0.0 enhanced food item presentation across all user roles by displaying food photographs above food names and prices, and introduced Room schema version 12.
