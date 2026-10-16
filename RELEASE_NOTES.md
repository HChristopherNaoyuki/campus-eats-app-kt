# Campus Eats - Release Notes

## Version 6.0.0

**Release Date:** 2026-10-16

---

### Overview

Version 6.0.0 is a major visual and operational update for the Campus Eats Android platform. This release aligns the light mode color palette with web platform design tokens (Warm Orange `#ED5A1E`, Accent Green `#309C5E`, and Warm Off-White `#FAF7F5`), introduces an Apple HIG-style premium Dark Mode specification, provides in-app user-controlled appearance mode selection (System, Light, Dark) persisted via Jetpack DataStore, and increments the release version to 6.0.0 (versionCode 8).

---

### Added Features & Enhancements

**1. Premium Apple HIG-Style Dark Mode Specification**
- Configured soft system dark gray canvas (`#1C1C1E`) for main application backgrounds to maintain visible elevation and surface separation.
- Configured solid black bottom navigation bar (`#000000`) featuring high-contrast white active icons and labels (`#FFFFFF`) and muted gray inactive icons (`#8E8E93`).
- Configured cream content panels (`#F5F2EB`) with deep near-black text (`#1C1C1E`) for high-contrast card readability on dark shells.
- Configured Apple system red (`#FF453A`) for error states.

**2. Website Palette Light Theme Parity**
- Replaced grayscale primary color tokens with the official website brand palette across [Color.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/ui/theme/Color.kt) and [Theme.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/ui/theme/Theme.kt).
- Configured Light Mode tokens: Primary Warm Orange (`#ED5A1E`), Accent Green (`#309C5E`), Background (`#FAF7F5`), and Deep Brown-Gray Foreground (`#231A16`).

**3. In-App Light / Dark Mode Appearance Control**
- Created `ThemeManager` ([ThemeManager.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/util/ThemeManager.kt)) to manage appearance modes (`System`, `Light`, `Dark`) persisted using Jetpack DataStore (`campus_eats_theme`).
- Added appearance selection controls in `SettingsScreenTab` ([MainTabs.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/ui/screens/MainTabs.kt)) with localized labels for English and Afrikaans (`System/Stelsel`, `Light/Lig`, `Dark/Donker`).
- Wired `ThemeManager.initialize(applicationContext)` and `CampusEatsAppTheme(darkTheme = darkTheme)` in [MainActivity.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/MainActivity.kt) so theme preference is evaluated on cold start before first frame composition.

---

### Technical Details

**Version Code:** 8

**Version Name:** 6.0.0

**Package Name:** com.example.campus_eats_app_kt

---

## Version 5.0.0

**Release Date:** 2026-10-15

---

### Overview

Version 5.0.0 enhanced food item presentation for all user roles (Students, Standard Users, Vendors) by displaying food photographs above food names and prices, and introduced Room database schema version 12 to persist cart item image URLs.

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
