# Edam Security Specification (Phase 0 TDD)

## 1. Data Invariants

1. **Multi-User Isolation**: Every document in `/users/{userId}` and `/users/{userId}/courses/{courseId}` belongs exclusively to `userId == request.auth.uid`. No user may read, list, create, update, or delete another user's profile or courses.
2. **Strict Schema & Key Allowlisting**:
   - `UserAccount` (`/users/{userId}`): Required keys `['userId', 'displayName', 'planTier', 'themeMode', 'createdAt', 'updatedAt']`, optional `['email', 'promoCodeUsed']`. `planTier` must be one of `['BASIC', 'PRO', 'MAX']`.
   - `CloudCourse` (`/users/{userId}/courses/{courseId}`): Required keys `['id', 'userId', 'title', 'level', 'goal', 'courseJson', 'completedLessonsJson', 'offlineLessonsJson', 'createdAt', 'updatedAt']`.
3. **Immutable Identity & Temporal Bounds**: `userId`, `id`, and `createdAt` are immutable on `update`. Timestamps (`createdAt`, `updatedAt`) must be Firestore `timestamp` objects and `<= request.time`.

## 2. The "Dirty Dozen" Adversarial Payloads

1. **Unauthenticated Read**: `unauthDb.collection("users").doc("alice").get()` -> Reject.
2. **Cross-User Profile Read (PII Leak)**: `bobDb.collection("users").doc("alice").get()` -> Reject.
3. **Cross-User Course List**: `bobDb.collection("users").doc("alice").collection("courses").get()` -> Reject.
4. **Identity Spoofing on Create**: Alice writes `/users/alice` with `userId: "bob"` -> Reject.
5. **Shadow / Ghost Field Injection**: Alice updates `/users/alice` with `{ isAdmin: true }` -> Reject via `affectedKeys().hasOnly(...)`.
6. **Invalid Plan Enum**: Alice writes `planTier: "SUPER_ADMIN"` -> Reject via `data.planTier in ['BASIC', 'PRO', 'MAX']`.
7. **String Size Overflow (Denial of Wallet)**: Alice writes a 500-char `displayName` -> Reject via `.size() <= 120`.
8. **Immutable `createdAt` Tampering**: Alice updates `createdAt` on an existing course -> Reject via `incoming().createdAt == existing().createdAt`.
9. **Immutable `userId` Reassignment**: Alice updates `userId` on an existing course to `"bob"` -> Reject.
10. **Future Timestamp Injection**: Alice writes `createdAt` in the future (`> request.time`) -> Reject.
11. **Malformed Document ID Poisoning**: Document ID exceeding 128 chars or invalid regex -> Reject via `isValidId()`.
12. **Missing Required Fields on Create**: Alice creates a course omitting `courseJson` -> Reject via `keys().hasAll(...)`.
