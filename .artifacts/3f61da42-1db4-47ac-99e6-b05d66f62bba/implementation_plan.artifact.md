# Implementation Plan - UI/UX and Authentication Improvements

This plan covers the transition to a grayscale UI, implementation of dynamic tab bar spacing (3.7 mm), and refinement of Google SSO authentication.

## User Review Required

> [!IMPORTANT]
> The UI will become entirely grayscale to accommodate color-blind users. This includes all status indicators (e.g., "Open/Closed", "Order Status") which previously relied on Red/Green. I will use contrast and typography to maintain clarity.

> [!NOTE]
> The app icon on the Landing Screen will retain its iconic colors as an exception, while the rest of the application will be grayscale.

## Proposed Changes

### [Design System & Theming]

#### [MODIFY] [DesignSystem.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/ui/theme/DesignSystem.kt)
- Add a utility function to convert `mm` to `Dp`.
- Define `tabBarHorizontalMargin` as a dynamic calculation of 3.7 mm.

#### [MODIFY] [Color.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/ui/theme/Color.kt)
- Define `IconOrange`, `IconRed`, and `IconGreen` for the app icon exception.
- Refine grayscale shades to ensure high contrast for all UI elements.

### [UI Components]

#### [MODIFY] [MainScreen.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/ui/screens/MainScreen.kt)
- Update the bottom navigation bar to use the dynamic 3.7 mm horizontal margin.
- Ensure the tab bar correctly adapts to orientation and screen size.

#### [MODIFY] [MainTabs.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/ui/screens/MainTabs.kt)
- Replace all hardcoded colors (`Color.Red`, `Color.Green`) with grayscale equivalents.
- Use visual indicators (like bold text or borders) for states like "Open" vs "Closed".

#### [MODIFY] [LandingScreen.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/ui/screens/LandingScreen.kt)
- Use `IconOrange` for the branding icon background.
- Ensure the rest of the screen adheres to the grayscale palette.

### [Authentication]

#### [MODIFY] [LoginScreen.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/ui/screens/LoginScreen.kt)
- Refine the "Continue with Google" button to be more prominent and grayscale-compliant.
- Ensure the Google SSO flow handles errors and cancellations with clear grayscale feedback.
- Use the provided public-facing project name `project-google-sso` in the authentication configuration.

## Verification Plan

### Automated Tests
- Run existing `AuthRepositoryTest` and `LoginViewModelTest` to ensure no regression.
- Add a screenshot test (if infrastructure allows) to verify grayscale UI.

### Manual Verification
- Deploy to an emulator/device and verify the tab bar margin (3.7 mm).
- Check every screen (Home, Browse, Orders, Reports, Settings) for any stray colors.
- Verify Google SSO flow: Login success, failure (wrong account), and cancellation.
- Toggle between Light and Dark modes to ensure grayscale system works in both.
