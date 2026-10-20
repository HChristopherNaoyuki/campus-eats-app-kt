# Campus Eats - Release Notes

## Version 13.0.0

**Release Date:** 2026-10-20

---

### Overview

Version 13.0.0 is a technical audit and session-binding update for the Campus Eats Android platform. This release aligns the Administrator home header to display the signed-in user's full name and username ("Amara Nkosi (@amara)"), matching student, standard, and vendor home screen patterns, verifies database read/write performance (<3s), validates Google SSO Credential Manager integration, reinforces 10-second continuous cloud synchronization with administrator feedback pull, and increments the release version to 13.0.0 (`versionCode 15`).

---

### Key Features & Technical Deliverables

**1. Administrator Home Session Binding**
- Updated `HomeScreenTab` in `MainTabs.kt` so the administrator home header displays the active session user's full name and username (e.g., *"Welcome, Amara Nkosi (@amara)"*), aligning with student, standard, and vendor home screen identity displays.

**2. Financial Controls & Reports (All Roles)**
- **Administrator**: Live platform revenue calculations (`AdminGlobalSummary`), order insight reports (`AdminInsightReportsHub`), wallet credit distribution, and coupon issuance backed by Room and Realtime Database entities.
- **Vendor**: Total completed revenue, top revenue item, item-by-item sales breakdown (`VendorReportHub`), active order count, and total items offered.
- **Student / Standard**: Total expenditure tracking, vendor spend percentage breakdown (`StudentTotalSpending`), wallet balance tracking, and payment receipts.

**3. Vendor Fulfillment & Order Notifications**
- Vendors update live order statuses (`ACCEPTED`, `PREPARING`, `READY`, `COMPLETED`, `CANCELLED`) in `VendorOrderHub`.
- Order items modal via **Check** action showing picked items, quantities, special requests, and pickup times.
- Status changes persist to Room, push to Firebase (`orders/{orderId}/status`), and post a high-priority local push notification to the customer.

**4. Student & Standard Receipts**
- `OrderDetailWindow` renders current order status, pickup state, payment method, special requests, and itemized cost breakdowns under headers "Live Status" and "Order History".

**5. Cross-Device Feedback Synchronization**
- `FirebaseSyncManager` automatically pulls remote complaints and compliments from Firebase into Room for Administrator accounts. Administrators review submissions in `AdminFeedbackWindow` and mark pending items as resolved with status updates mirrored to Firebase (`feedback/{feedbackId}/status`).

**6. Technical Audit & Infrastructure**
- **Google SSO**: Verified Credential Manager API with Web OAuth Client ID `64265928399-tl7gi0kkolvke8k7etaio9h66ov44hi3.apps.googleusercontent.com` and `[FIREBASE_SSO]` passwordHash sentinel flag.
- **Synchronization**: 10-second continuous sync loop with exponential backoff and active `Job` lifecycle guard.
- **Unit Testing**: Executed `./gradlew :app:testDebugUnitTest` with **69 passed, 0 skipped, 0 failed**.

---

### Technical Details

**Version Code:** 15

**Version Name:** 13.0.0

**Package Name:** com.example.campus_eats_app_kt

---

## Version 12.0.0

**Release Date:** 2026-10-20

---

### Overview

Version 12.0.0 delivered role financial controls, working administrator system reports, vendor live order status updates with automated customer push notifications, student receipt status tracking, and cloud feedback synchronization.

---

## Version 11.0.0

**Release Date:** 2026-10-18

---

### Overview

Version 11.0.0 delivered Play Store release preparation, GitHub Actions CI workflow configuration, AI usage disclosures, and notification framework enhancements.

---

## Version 10.0.0

**Release Date:** 2026-10-18

---

### Overview

Version 10.0.0 delivered full web platform parity across all user roles, complete feedback review and resolution loops, 10-second automatic background cloud synchronization with administrator feedback pull, tab re-tap navigation reset, and Apple HIG dark mode tokens.

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
