# Weight Tracker Android Application

## Overview

Weight Tracker is an Android mobile application originally developed for the CS 360 Mobile Architecture and Programming course and later enhanced for the CS 499 Computer Science Capstone ePortfolio. The application allows users to create an account, log in, record weight entries, view saved records through a dashboard, and manage SMS reminder settings.

For the CS 499 final project, this artifact was enhanced across three major computer science categories:

- Software Design and Engineering
- Algorithms and Data Structures
- Databases

The purpose of these enhancements was to improve the application’s maintainability, security, data processing, and database reliability while demonstrating professional software development practices.

## Features

- User account creation and login
- Secure password handling through hashing
- Weight entry creation, editing, and deletion
- Dashboard display for saved weight records
- Input validation for usernames, passwords, dates, and weight values
- SMS reminder settings
- Weekly average weight calculations
- Weight trend detection
- Duplicate entry prevention
- SQLite database storage
- Database indexing for improved query performance

## Technologies Used

- Java
- Android Studio
- Android SDK
- SQLite
- XML layouts
- Git / GitHub

## Enhancement One: Software Design and Engineering

The first enhancement focused on improving the structure, readability, maintainability, and security design of the application.

### Key Improvements

- Added `ValidationHelper.java` to centralize reusable validation logic.
- Added `SecurityHelper.java` to separate password hashing from activity logic.
- Updated `LoginActivity.java` to use reusable validation methods and password hashing.
- Updated `DashboardActivity.java` to improve weight validation and error handling.
- Improved defensive programming by checking database operation results before continuing.
- Added clearer method comments in `DatabaseHelper.java`.

### Purpose

The original version of the application placed validation, security, database calls, and user interface logic directly inside activity classes. The enhancement improved separation of concerns by moving reusable validation and security behavior into dedicated helper classes. This made the code easier to maintain, test, and expand.

## Enhancement Two: Algorithms and Data Structures

The second enhancement focused on improving how the application processes and analyzes stored weight data.

### Key Improvements

- Added weekly average calculations in `DatabaseHelper.java`.
- Used SQL aggregation to group and calculate average weight values.
- Used a `LinkedHashMap` to store and process grouped average data.
- Added trend detection logic in `DashboardActivity.java`.
- Improved sorting by using the weight date column instead of relying only on insertion order.
- Added collection traversal logic to process structured data.

### Purpose

The original application focused mainly on basic CRUD operations. Users could add, view, update, and delete weight entries, but the app did not perform meaningful analysis on stored data. The enhancement added algorithmic processing so the application could identify patterns and summarize user progress.

## Enhancement Three: Databases

The third enhancement focused on improving database performance, integrity, and reliability.

### Key Improvements

- Added indexes for weight username, weight date, and combined username/date lookups.
- Added a `weightEntryExists()` method in `DatabaseHelper.java`.
- Updated `DashboardActivity.java` to prevent duplicate weight entries for the same user and date.
- Improved database integrity by validating records before insertion.
- Improved query performance for user/date based lookups.

### Purpose

The original database stored user and weight information successfully, but it did not prevent duplicate records for the same date and user. It also did not include indexes to support faster lookups as the amount of stored data increased. The enhancement improved data integrity and made the database structure more reliable for future growth.

## Project Structure

```text
app/
└── src/
    └── main/
        ├── java/com/example/weighttracker/
        │   ├── DashboardActivity.java
        │   ├── DatabaseHelper.java
        │   ├── LoginActivity.java
        │   ├── SecurityHelper.java
        │   ├── SmsSettingsActivity.java
        │   ├── ValidationHelper.java
        │   └── WeightEntry.java
        ├── res/layout/
        │   ├── activity_dashboard.xml
        │   ├── activity_login.xml
        │   ├── activity_sms_settings.xml
        │   └── dialog_weight_entry.xml
        └── AndroidManifest.xml
```

## How to Run

1. Open the project in Android Studio.
2. Allow Gradle to sync.
3. Select an Android emulator or connected Android device.
4. Build and run the application.
5. Create a new account.
6. Add weight entries through the dashboard.
7. Test duplicate entry prevention by trying to add two entries with the same date.
8. Check Logcat for weekly average and trend detection output.

## Testing Notes

During testing, the following behaviors were verified:

- New accounts can be created successfully.
- Users can log in with valid credentials.
- Passwords are hashed before being stored or validated.
- Invalid or unrealistic weight values are rejected.
- Weight entries can be saved, updated, and deleted.
- Duplicate entries for the same user and date are prevented.
- Weekly average calculations run successfully.
- Trend detection logic works without crashing the application.
- Database indexes and duplicate checks do not break existing app functionality.

## Course Outcome Alignment

This project demonstrates progress toward the following CS 499 outcomes:

- Designing and evaluating computing solutions using software engineering practices.
- Applying algorithmic principles and data structure concepts to process stored data.
- Using modern tools and techniques such as Android Studio, Java, SQLite, and GitHub.
- Developing a security mindset through validation, hashing, defensive programming, and database integrity improvements.
- Communicating technical decisions through code review, narratives, and ePortfolio documentation.

## ePortfolio Context

This project is part of the CS 499 Computer Science Capstone ePortfolio. The enhanced Weight Tracker application demonstrates the ability to improve an existing software artifact through structured planning, code review, implementation, testing, and reflection.

## Author

Alejandro Coitinho  
CS 499 Computer Science Capstone  
Southern New Hampshire University
