# WeightTracker

WeightTracker is a simple Android app that lets a user log in, record daily weight entries, and optionally receive SMS style alerts when a goal is reached.  

The app was built in Kotlin as an academic project for CS 360 and is configured for **Android 16 (API level 36)**.

---

## Features

- **Local login**
  - Create a local account with username and password
  - Secure password field where characters are obscured
- **Dashboard**
  - Grid style list of weight entries
  - Add new entries with date, weight, and optional note
  - Edit existing entries
  - Delete entries
- **SQLite persistence**
  - Data is stored locally in an SQLite database
  - All CRUD operations (create, read, update, delete) are supported through the UI
- **SMS alert settings**
  - Configure a phone number and goal condition
  - Enable or disable SMS based notifications
  - App requests runtime permission for SMS and handles both Allow and Deny
- **Resilient behavior**
  - App continues to function normally if SMS permission is denied
  - Core tracking and database features never depend on SMS being enabled

---

## Tech stack

- **Language:** Kotlin
- **Minimum / target SDK:** Android 16, API level 36
- **Architecture:** Activity based with a simple MVC style separation
- **Database:** SQLite (via a custom `SQLiteOpenHelper` implementation)
- **IDE:** Android Studio (Giraffe and later should work)

---

## Project structure

Main package: `com.example.weighttracker`

- `LoginActivity`
  - Handles account creation and login
  - Interacts with the `users` table in SQLite
- `DashboardActivity`
  - Displays the weight grid using a `RecyclerView` (and `WeightAdapter`)
  - Connects to `DBHelper` for CRUD operations on weight entries
- `SmsSettingsActivity`
  - Lets the user configure target phone, goal value, and alert toggle
  - Requests and checks SMS permission at runtime
- `DBHelper`
  - Subclass of `SQLiteOpenHelper`
  - Creates and upgrades the database
  - Provides helper methods for:
    - `users` table (login credentials)
    - `weights` table (weight entries)
    - any table / storage used for SMS settings
- `WeightEntry` and `WeightAdapter`
  - Data model and adapter for rendering rows in the dashboard list

Key layouts (in `res/layout`):

- `activity_login.xml`
- `activity_dashboard.xml`
- `activity_sms_settings.xml`
- `item_weight_row.xml`

---

## Database schema

The schema is intentionally small and focused on the project requirements.

**Table: `users`**

| Column     | Type    | Notes                      |
|-----------|---------|----------------------------|
| `id`      | INTEGER | Primary key, autoincrement |
| `username`| TEXT    | Unique                     |
| `password`| TEXT    | Stored as plain text for this course project (not for production use) |

**Table: `weights`**

| Column  | Type    | Notes                          |
|--------|---------|---------------------------------|
| `id`   | INTEGER | Primary key, autoincrement     |
| `date` | TEXT    | Stored as YYYY-MM-DD or similar |
| `weight` | REAL  | User's weight for that date    |
| `note` | TEXT    | Optional description           |

A small storage mechanism (table or shared preferences) is used to keep SMS settings, such as goal value, target phone number, and an enabled flag.

---

## Getting started

### Prerequisites

- Android Studio installed
- Android SDK platform for API 36 (Android 16)
- Emulator or device that runs API 36 or higher  
  - SMS behavior is best seen on a physical device, but logging is used so the emulator can still demonstrate the logic

### Cloning and opening

```bash
git clone https://github.com/<your-username>/WeightTracker.git
cd WeightTracker
