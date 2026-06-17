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

## Overview

WeightTracker is an Android mobile application built with Kotlin and SQLite for Android 16 (API level 36). The app helps users track body weight trends over time while providing reminders and SMS notifications that support long term goals. The project demonstrates a full mobile app development cycle, from user centered design and UI planning to database integration, permissions, and launch planning.

---

## Project Requirements and Goals

**What user needs was this app designed to address?**  
The primary goal of WeightTracker is to give users a simple way to log daily weights, see progress at a glance, and stay motivated through automated reminders. During planning, I focused on a busy user who wants quick input, clear feedback, and reassurance that their data persists even if they close the app. The app therefore needed:
- Secure login so progress feels personal and protected  
- A persistent history of entries rather than one time values  
- A way to highlight trends so users can see whether they are moving toward a goal  

These requirements guided both the UI layout and the underlying database design.

---

## Screens, Features, and User Centered Design

**What screens and features were necessary to support user needs and produce a user centered UI? Why were these designs successful?**  

WeightTracker includes three main screens:

1. **Login Screen**  
   - Username and password fields  
   - “Log In” and “Create Account” buttons  
   - Basic validation with user feedback messages  
   This screen supports new and returning users by allowing account creation and re use of existing credentials. The password field obscures input, which keeps the experience consistent with typical Android login patterns.

2. **Dashboard Screen (Weight History Grid)**  
   - A grid style list of weight entries with date and value  
   - Buttons to add, edit, or delete entries  
   - Clear labels and spacing for readability  
   The dashboard is the core of the app. It keeps the most important information (recent entries and actions) above the fold and uses a simple visual hierarchy so users can quickly scan dates and values.

3. **SMS Settings Screen**  
   - Controls for enabling or disabling SMS alerts  
   - A sample preview of the kind of notification users might receive  
   - Clear explanation of why SMS permission is requested  
   This screen gives users control over notifications and makes the permission request transparent, which supports trust and aligns with Android design guidance on permissions.

Across all screens I used ConstraintLayout to keep elements aligned, maintained consistent typography and spacing, and avoided unnecessary decoration. These choices keep the UI focused on actions and data rather than visual noise.

---

## Coding Approach and Techniques

**How did you approach the process of coding your app? What techniques or strategies did you use? How could those techniques or strategies be applied in the future?**  

I approached coding in small, testable steps:

- **Separated concerns** into distinct classes: activities for UI, a helper class for SQLite, and a data class for weight entries.  
- **Implemented CRUD operations** in the database helper so all data access logic lived in one place.  
- **Used adapters and RecyclerView like patterns** to bind database rows to the grid layout, keeping UI updates straightforward.  
- **Handled runtime permissions** for SMS carefully, checking permission state and responding gracefully if access is denied.

This incremental, modular approach made debugging easier and will scale well to more complex apps. In future projects I can reuse this pattern: design the database layer first, then connect UI elements through clear, well named methods that represent user actions (addWeightEntry, deleteEntry, updateEntry, etc.).

---

## Testing and Debugging

**How did you test to ensure your code was functional? Why is this process important, and what did it reveal?**  

I relied on three main forms of testing:

- **Emulator based interaction tests** to walk through the full flow: create an account, log in, add entries, edit and delete entries, and adjust SMS settings.  
- **Logging statements** in key paths such as database inserts, updates, and permission checks. These helped confirm that each operation executed as expected and revealed a few early mistakes in SQL statements.  
- **Edge case testing** including empty input fields, duplicate usernames, and denying SMS permission. This ensured the app continued to function and displayed helpful messages when users took unexpected actions.

The testing process revealed how important clear error messages and defensive checks are. For example, handling null or empty values prevented crashes and created a smoother user experience.

---

## From Planning to Finalization

**Where did you have to innovate to overcome a challenge?**  

The biggest challenge was coordinating the login flow, database structure, and SMS notifications so that each user’s data and alerts stayed consistent. I had to:
- Design a table schema that tied weight entries to a specific account.  
- Ensure that the app still worked even if SMS permissions were denied.  
- Keep the UI responsive while performing database operations.

To solve these issues I simplified the schema, keeping the focus on core fields (id, date, weight, and username), and wrote the SMS logic so it only activates when permission is granted. This kept the user experience stable and avoided blocking the app when permissions changed.

---

## Reflection on Strengths

**In what specific component of your mobile app were you particularly successful in demonstrating your knowledge, skills, and experience?**  

The component I am most proud of is the **weight history management**, which combines UI, database, and user feedback in a cohesive way. The grid view, backed by SQLite CRUD operations, shows that I can:
- Design a schema that supports real user tasks  
- Implement create, read, update, and delete behavior cleanly  
- Reflect database changes immediately in the interface  

This part of the app best demonstrates my ability to connect design decisions with underlying code, and it represents a solid foundation for future enhancements like charts, goal tracking, or cloud synchronization.

---

## How to Run the Project

1. Open the project in Android Studio (Android 16, API level 36).  
2. Sync Gradle and let dependencies download.  
3. Build and run the app on an emulator or device targeting Android 16 or later.  
4. Create a new account on the login screen, then use the dashboard to add and manage weight entries.  
5. Open the SMS settings screen to enable or disable SMS notifications as supported by the emulator or device.

This repository, together with the code and launch plan, documents a full end to end example of designing, implementing, and preparing a mobile application for release.
