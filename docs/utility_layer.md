# Utility Layer Documentation

The Utility Layer contains helper classes and business engines that provide non-UI specific logic to
the application.

## Table of Contents

1. [Responsibilities](#responsibilities)
2. [Financial Engine (`CheckoutEngine`)](#financial-engine-checkoutengine)
3. [Identity Generator (`IdGenerator`)](#identity-generator-idgenerator)
4. [Order Lifecycle Engine (`OrderStatusEngine`)](#order-lifecycle-engine-orderstatusengine)
5. [Validation Engine (`ValidationEngine`)](#validation-engine-validationengine)

---

## Responsibilities

- **Pure Logic**: Implementing business rules without side effects.
- **Format Consistency**: Enforcing system-wide patterns for IDs and validation.
- **Statelessness**: Providing utility functions that are easy to test.

---

## Financial Engine (`CheckoutEngine`)

The `CheckoutEngine` is the most critical logic component, responsible for all monetary
calculations.

### Responsibilities

- Calculating a fixed 20% tax.
- Applying a tiered service fee (10%, 6.5%, or 0%).
- Granting a 2.5% discount to students.
- Rounding final totals up to the nearest R5.

### Usage Example

```kotlin
val summary = CheckoutEngine.calculateSummary(
    subtotal = 450.0,
    role = UserRole.STUDENT
)
```

---

## Identity Generator (`IdGenerator`)

Generates the 16-character alphanumeric identifiers used as the primary key for users and as
recovery tokens.

### Responsibilities

- Ensuring the `XXXX-XXXX-XXXX-XXXX` format.
- Using a restricted character set (A-Z, 0-9) to avoid ambiguity.

---

## Order Lifecycle Engine (`OrderStatusEngine`)

Manages the finite state machine for order transitions.

### Valid Transitions

- `PENDING` -> `ACCEPTED` | `CANCELLED`
- `ACCEPTED` -> `PREPARING` | `CANCELLED`
- `PREPARING` -> `READY` | `CANCELLED`
- `READY` -> `COMPLETED` | `CANCELLED`

---

## Validation Engine (`ValidationEngine`)

Provides centralized regex and logic checks for user inputs.

### Supported Validations

- **Email**: Standard RFC-compliant regex.
- **Password**: Minimum 8-character strength check.
- **Price/Quantity**: Positive numeric checks.

---

*END OF DOCUMENT*
