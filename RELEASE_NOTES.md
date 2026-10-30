# Campus Eats - Release Notes

## Version 15.0.0

**Release Date:** 2026-10-23

---

### Overview

Version 15.0.0 is a production version bump and Google SSO integration verification release for the Campus Eats Android platform. This release confirms Web OAuth Client ID binding (`64265928399-tl7gi0kkolvke8k7etaio9h66ov44hi3.apps.googleusercontent.com`), ProGuard keep rules for Credential Manager classes in release builds, zero-warning build quality, 266 passing unit tests, and version metadata updates (`versionCode 17`).

---

### Key Features & Technical Deliverables

**1. Google Single Sign-On (SSO) Verification**
- Verified Android Credential Manager API integration with Web OAuth Client ID `64265928399-tl7gi0kkolvke8k7etaio9h66ov44hi3.apps.googleusercontent.com` in `GetGoogleIdOption`.
- Verified ProGuard rules in `proguard-rules.pro` retaining `androidx.credentials.**` and `com.google.android.gms.auth.api.identity.**` classes.
- Verified `passwordHash = "[FIREBASE_SSO]"` sentinel flag for Google SSO user records.

**2. Zero Warnings & Build Quality**
- IDE static analysis on all XML, Kotlin, and Gradle configuration files confirmed 0 compilation errors and 0 warnings.
- Executed `./gradlew clean lint testDebugUnitTest assembleDebug` with status **Build finished successfully**.

**3. Financial Controls & Reports (All Roles)**
- **Administrator**: Live platform revenue calculations (`AdminGlobalSummary`), order insight reports (`AdminInsightReportsHub`), wallet credit distribution, and coupon issuance backed by Room and Realtime Database entities.
- **Vendor**: Total completed revenue, top revenue item, item-by-item sales breakdown (`VendorReportHub`), active order count, and total items offered.
- **Student / Standard**: Total expenditure tracking, vendor spend percentage breakdown (`StudentTotalSpending`), wallet balance tracking, and payment receipts.

**4. Vendor Fulfillment & Order Notifications**
- Vendors update live order statuses (`ACCEPTED`, `PREPARING`, `READY`, `COMPLETED`, `CANCELLED`) in `VendorOrderHub`.
- Order items modal via **Check** action showing picked items, quantities, special requests, and pickup times.
- Status changes persist to Room, push to Firebase (`orders/{orderId}/status`), and post a high-priority local push notification to the customer (`campus_eats_orders` channel).

**5. Student & Standard Receipts**
- `OrderDetailWindow` renders current order status, pickup state, payment method, special requests, and itemized cost breakdowns under headers "Live Status" and "Order History".

**6. Cross-Device Feedback Synchronization**
- `FirebaseSyncManager` automatically pulls remote complaints and compliments from Firebase into Room for Administrator accounts. Administrators review submissions in `AdminFeedbackWindow` and mark pending items as resolved with status updates mirrored to Firebase (`feedback/$stableKey/status`).

---

### Technical Details

**Version Code:** 17

**Version Name:** 15.0.0

**Package Name:** com.example.campus_eats_app_kt

---

## Version 14.0.0

**Release Date:** 2026-10-23

---

### Overview

Version 14.0.0 introduced deterministic feedback keys in `FeedbackKeys.kt`, isolated node synchronization in `FirebaseSyncManager.kt`, and Play Services provider class rules.

---

## Version 13.0.0

**Release Date:** 2026-10-20

---

### Overview

Version 13.0.0 delivered administrator home session binding, Google SSO Credential Manager provider rules, and continuous cloud synchronization.

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
