# Implementation Plan - Kotlin and Firebase Realtime Database Security Rules Update

This plan outlines the changes required to synchronize the Kotlin application with the new Firebase Realtime Database security rules. The primary focus is on enforcing the security contract at the application level, ensuring data integrity, and handling new authorization requirements (specifically admin claims and restricted field updates).

## User Review Required

> [!IMPORTANT]
> **Administrative Custom Claims**: The new rules rely on `auth.token.admin`. The Kotlin application must verify this claim before allowing administrative UI or operations. If a user is granted admin status, their Firebase Authentication token must be refreshed to reflect this change.

> [!WARNING]
> **Restricted Field Updates**: Normal users can no longer increase their `walletBalance` or change their `email`, `role`, or `status` once created. The existing `updateProfile` and background synchronization logic in `AuthRepository` must be refactored to avoid triggering security rule violations.

## Proposed Changes

### Data Models & Entities

#### [MODIFY] [UserEntity.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/data/entity/UserEntity.kt)
- Update documentation to clarify the 19-character `userId` format (`XXXX-XXXX-XXXX-XXXX`).
- Ensure `passwordHash` defaults to `[FIREBASE_SSO]`.
- Maintain enums for `UserRole` and `UserStatus` that exactly match rule values.

#### [MODIFY] [FeedbackEntity.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/data/entity/FeedbackEntity.kt)
- Add required fields: `userName`, `userEmail`, `status`, `createdAt`, `updatedAt`.
- Change `timestamp` (Long) to `createdAt` (String) and `updatedAt` (String).
- Update `FeedbackType` mapping to ensure string values `'complaint'` and `'compliment'` are used for Firebase.

#### [MODIFY] [UserDao.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/data/dao/UserDao.kt) and [FeedbackDao.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/data/dao/FeedbackDao.kt)
- Update Room queries to reflect model changes.

---

### Repositories & Services

#### [MODIFY] [AuthRepository.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/data/AuthRepository.kt)
- **`register`**: Ensure the `email` field in the user record matches `auth.token.email`.
- **`updateProfile`**: Prevent modification of `email`, `role`, and `status` for normal users.
- **`startBackgroundSync`**: Modify to only sync allowed fields or perform a partial update to avoid `Permission Denied` when locally modified restricted fields are pushed.
- **Admin Verification**: Add a method to check for the `admin` custom claim in the current Firebase token.

#### [MODIFY] [AdminRepository.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/data/AdminRepository.kt)
- Wrap all methods with an admin claim check.
- Handle `admin_claims` path access (if required).

#### [MODIFY] [FeedbackRepository.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/data/FeedbackRepository.kt)
- **`submitFeedback`**: Populate all required fields (`userName`, `userEmail`, `status="pending"`, etc.).
- Ensure `userId` matches `auth.uid`.

---

### ViewModels & UI

#### [MODIFY] [AdminViewModel.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/ui/screens/AdminViewModel.kt)
- Verify admin status before allowing any administrative actions or displaying admin data.

#### [MODIFY] [MainTabs.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/ui/screens/MainTabs.kt)
- Update UI visibility for wallet balance increases, role changes, and status updates to reflect the new authorization model.

---

### Utility & Infrastructure

#### [MODIFY] [IdGenerator.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/util/IdGenerator.kt)
- Ensure the generated `userId` is strictly 19 characters.

#### [MODIFY] [FirebaseExceptionHandler.kt](file:///C:/Users/naoyu/StudioProjects/campus-eats-app-kt/app/src/main/java/com/example/campus_eats_app_kt/data/FirebaseExceptionHandler.kt)
- Improve error mapping for `Permission Denied` errors related to specific validation rules (e.g., "Wallet balance cannot be increased by user").

## Verification Plan

### Automated Tests
- **Unit Tests**:
    - `AuthRepositoryTest`: Test registration with valid/invalid emails and roles.
    - `AdminRepositoryTest`: Test admin operations with and without the admin claim.
    - `FeedbackRepositoryTest`: Test feedback submission with all required fields.
- **Integration Tests (Firebase Emulator)**:
    - Verify that rules correctly reject unauthorized wallet increases.
    - Verify that rules correctly reject email changes for existing users.
    - Verify that feedback status can only be changed to 'resolved' by admins.

### Manual Verification
- Deploy to emulator and attempt to:
    1. Register as a student and increase wallet balance (should fail).
    2. Register as an admin and suspend a user (should succeed).
    3. Update user profile email (should fail in RTDB).
    4. Submit feedback and check for all required fields in RTDB.
