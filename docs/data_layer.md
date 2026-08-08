# Data Layer Documentation

The Data Layer is responsible for all data acquisition, persistence, and synchronization logic in
the Campus Eats application.

## Table of Contents

1. [Responsibilities](#responsibilities)
2. [Local Persistence (Room)](#local-persistence-room)
3. [Remote Networking (Retrofit)](#remote-networking-retrofit)
4. [Repositories](#repositories)
5. [Data Models and Entities](#data-models-and-entities)
6. [Type Converters](#type-converters)

---

## Responsibilities

- **Abstraction**: Hiding the complexities of database queries and network calls.
- **Single Source of Truth**: Ensuring the local database correctly reflects the state of the
  application.
- **Synchronization**: Propagating local changes to the remote API and vice versa.
- **Error Handling**: Managing network timeouts, database conflicts, and parsing errors.

---

## Local Persistence (Room)

The application uses the Room Persistence Library for local data storage.

### Database: `CampusEatsDatabase`

A singleton database instance containing multiple tables.

### Key Entities

- `UserEntity`: Stores user profiles, credentials (hashes), and `usercode` (API keys).
- `MenuItemEntity`: Stores menu items for vendors.
- `OrderEntity`: Stores immutable transaction records.
- `CartItemEntity`: Stores active cart data for a user.

---

## Remote Networking (Retrofit)

Integration with the **Fake Restaurant API** is handled via Retrofit 2.x.

### Service: `FakeRestaurantApiService`

Defines 15 endpoints covering:

- **Restaurants**: Retrieval and filtering.
- **Users**: Registration, authentication, and updates.
- **Orders**: Creation and tracking of master orders.

### Client: `RetrofitClient`

Configures OkHttpClient with logging interceptors and timeouts.

---

## Repositories

Repositories are the primary entry point for the domain and presentation layers.

### `AuthRepository`

Handles user lifecycle operations.

- `register()`: Persists locally and on the remote API.
- `login()`: Validates credentials and retrieves `usercode`.
- `updateProfile()`: Synchronizes profile updates.

### `MenuRepository`

Manages menu items and vendor discovery.

- `getAllVendors()`: Merges local vendors with remote restaurants.
- `getMenuItemsByVendor()`: Fetches items from both local and remote sources.

### `OrderRepository`

Orchestrates order placement and history.

- `placeOrder()`: Atomic operation to persist order and clear cart.
- `getRemoteOrders()`: Retrieves orders from the API using `usercode`.

---

## Data Models and Entities

The system distinguishes between **Network Models** (DTOs) and **Database Entities**.

- **DTOs**: Classes like `NetworkUser`, `NetworkMenuItem`, and `OrderItemRequest` are used for API
  communication.
- **Entities**: Classes like `UserEntity` and `OrderEntity` are optimized for SQLite storage.

---

## Type Converters

The `Converters` class handles the transformation of complex types for Room storage:

- `List<CartItemEntity>` to JSON String (using Kotlinx Serialization).
- `UserRole`, `UserStatus`, and `OrderStatus` enums to Strings.

---

*END OF DOCUMENT*
