# Campus Eats - Release Notes

## Version 5.0.0

**Release Date:** 2026-10-15

---

### Overview

Version 5.0.0 is a major feature update for the Campus Eats Android platform. This release enhances food item presentation for all user roles (Students, Standard Users, and Vendors) by displaying food photographs above clear food names and prices. This release also introduces Room database schema version 12 to persist cart item image URLs and updates documentation for version 5.0.0 (versionCode 7).

---

### Added Features & Enhancements

**1. Food Item Presentation (AC-4)**
- Created `FoodItemImage` and `FoodItemThumbnail` UI components (`ui/components/FoodItemImage.kt`) using Coil `AsyncImage` with a restaurant icon fallback.
- Updated Customer Menu Browse to render food item photographs directly above food names and prices.
- Updated Vendor Inventory List to render item thumbnails beside food names, categories, prices, and stock counts.
- Updated Cart Screen and Home Cart Preview to render food thumbnails alongside names and prices.

**2. Room Database Schema Version 12**
- Updated `CartItemEntity` to store `imageUrl`.
- Added migration `MIGRATION_11_12` in `CampusEatsDatabase` to add the `imageUrl` column to table `cart_items` without data loss.

---

### Technical Details

**Version Code:** 7

**Version Name:** 5.0.0

**Package Name:** com.example.campus_eats_app_kt

---

## Version 4.0.0

**Release Date:** 2026-04-02

---

### Overview

Version 4.0.0 introduced adaptive launcher icon branding with a solid black background and white hamburger mark, enforced Web OAuth Client ID token exchange for Google SSO, refactored feedback and coupon sync with payload fingerprinting, and updated documentation.

---

## Version 3.1.0

**Release Date:** 2026-04-02

---

### Overview

Version 3.1.0 was a comprehensive technical update for the Campus Eats Android platform resolving Single Sign-On credential exchange issues, persistent dual-language support, session-gated cloud synchronization with exponential backoff, local order status transition notifications, and validated network connectivity.

---

## Version 3.0.1

**Release Date:** 2026-03-30

---

### Overview

Version 3.0.1 delivered account, admin, and vendor enhancements including 16-character User ID generation, copy control, User ID account recovery, admin insight reporting, financial controls, vendor revenue calculation, and live order item inspection.

---

## Version 2.0.0

**Release Date:** 2026-08-13

---

### Overview

Version 2.0.0 provided a complete technical overhaul shifting to a hybrid offline-first architecture with Fake Restaurant API integration, vendor fulfillment workflow, and a minimalist HIG Compose UI redesign.

---

## Version 1.0.0

**Release Date:** 2026-01-15

---

### Overview

Initial release establishing core features for Students, Vendors, and Administrators with local Room database storage, fee calculation engine, and initial UI screens.
