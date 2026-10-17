# Campus Eats - Release Notes

## Version 8.0.0

**Release Date:** 2026-10-17

---

### Overview

Version 8.0.0 delivers comprehensive role alignment with web platform modules, navigation tab re-tap behavior, enhanced coupon generation with sample chips, full feedback resolution workflow, Apple HIG dark mode tokens, and technical audit verification across database connectivity, Firebase synchronization, Google SSO, and offline resilience.

---

### Added Features & Technical Enhancements

**1. Role & Navigation Alignment**
- Enforced bottom tab re-tap behavior across Home, Browse, Orders, and Settings: re-tapping the active tab returns the sub-view to the tab root window.
- Updated Administrator Home tab header: *"Welcome to Campus Eats - Manage users, vendors, menus, and orders for your campus food network, all in one place."*
- Renamed Browse management modules for Administrators to **User Management**, **Vendor Management**, **Order Management**, and added real-time **Security Log** audit trail.
- Renamed Orders tab system entry for Administrators to **Reports**.

**2. Feedback Resolution Loop**
- Administrators review Complaints and Compliments with pending/resolved status tracking in `AdminFeedbackWindow` and an explicit **Mark as Resolved** action button. Status changes update Room locally and sync to Firebase (`feedback/{feedbackId}/status`).
- Vendors, Students, and Standard users submit feedback through `UserFeedbackWindow` with submission confirmation prompts.

**3. Coupon Generation & Sample Selection**
- Enhanced coupon generation window with sample coupon chips (`CAMPUS10`, `EATS20`, `WELCOME5`) and current active coupon lists.

**4. Platform Information & External Website Link**
- Added Settings platform information card with a **WEBSITE** button opening `https://campus-eats-platform.lovable.app/` via external browser intent.

**5. Technical Audit & Infrastructure**
- **Google SSO**: Verified Web OAuth Client ID (`64265928399-tl7gi0kkolvke8k7etaio9h66ov44hi3.apps.googleusercontent.com`) for Credential Manager token exchange.
- **Synchronization**: Verified 10-second continuous sync loop with exponential backoff and `Job` lifecycle guard.
- **Security & Password Hashing**: Verified local SHA-256 password hashing and `[FIREBASE_SSO]` sentinel flags.
- **Offline Resilience**: Verified Room database local writes with automatic Firebase cloud sync upon network recovery.

---

### Technical Details

**Version Code:** 10

**Version Name:** 8.0.0

**Package Name:** com.example.campus_eats_app_kt

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
