WeightTracker
Overview

WeightTracker is an Android application that helps a single user record and monitor daily weight entries. The app was built in Kotlin for Android 16 with minimum SDK set to API level 36.

The project demonstrates a complete mobile flow:

Local log in backed by SQLite

A dashboard screen that displays weight entries in a grid

Full create, read, update, and delete operations on the database

SMS notification logic that alerts the user when a goal condition is met

Runtime permission handling so the app continues to work even when SMS access is denied

This README explains how to set up, run, and test the app, and how each feature supports the course Project Three rubric.

Main features

Log in screen with local user account creation

Secure password entry where characters are obscured

Dashboard with a grid style list of weight records

Add, edit, and delete individual weight entries

Persistent SQLite database that keeps data after the app closes

SMS settings screen that allows the user to

enter a phone number

configure a goal weight condition

enable or disable SMS alerts

SMS notification logic that sends a message when the most recent weight entry meets the configured condition

Graceful handling of SMS permission denial while preserving core app functionality

Project structure

Kotlin package com.example.weighttracker contains the main code:

LoginActivity

Handles log in and new account creation

Checks credentials against the users table in SQLite

DashboardActivity

Shows the weight grid

Connects to DBHelper to load, insert, update, and delete records

Opens dialog style views for adding or editing entries

SmsSettingsActivity

Allows the user to turn SMS alerts on or off

Stores target phone number and goal values in the database or shared preferences

Requests runtime permission for SMS and records the user choice

DBHelper

Manages SQLite database creation and upgrade

Provides helper methods for CRUD operations on

users table

weights table and any table used for SMS settings

Layout resources:

activity_login.xml for the log in screen

activity_dashboard.xml for the weight grid

activity_sms_settings.xml for SMS configuration

item_weight_row.xml for each row in the grid

Database design

The SQLite database includes at least the following tables.

users

id integer primary key

username text unique

password text

weights

id integer primary key

date text (stored in a clear format such as YYYY MM DD)

weight real

note text optional

A separate table or preference store holds SMS settings, such as goal value, phone number, and an enabled flag.

This design supports all four CRUD operations required by the rubric and keeps user data persistent between app sessions.

Prerequisites

Android Studio with support for Android 16 and API level 36

Android Emulator or physical device that targets API 36 or higher

Gradle configured from the project settings created by Android Studio

For real SMS testing, a physical device with a valid SIM card

The emulator will not show live SMS messages, so log output is used to confirm behavior

How to build and run the app

Open Android Studio.

Select Open an existing project and choose the WeightTracker project folder.

Wait for Gradle sync to complete. Resolve any sync prompts if they appear.

In the toolbar, select an emulator or connect a physical device that targets API 36 or higher.

Press Run. Android Studio builds the project and deploys the app.

If deployment is successful, the log in screen appears.

Using the app
Step one: create a user and log in

On the log in screen enter a new username and password.

Select Create Account.

The app stores the new user in the users table.

Status text at the bottom confirms that the account was created.

Enter the same credentials and select Log In.

On success, the app navigates to DashboardActivity.

Screenshot suggestion 1: log in screen showing a newly created account and a success message.

Step two: manage weight entries

Once on the dashboard screen:

Select the add entry control to open the add entry dialog.

Enter date, weight value, and any note then save.

The new record appears in the grid.

Tap an existing row to edit it, then save to update the entry.

Use the delete control on a row to remove that entry from the database.

This sequence demonstrates create, read, update, and delete against the weights table.

Screenshot suggestion 2: dashboard grid with several entries visible.
Screenshot suggestion 3: edit entry dialog open for an existing record.

Step three: configure and test SMS notifications

From the dashboard, open the SMS settings screen.

Enter a test phone number and goal value.

Turn the SMS alerts switch to the on position.

When prompted for SMS permission:

First run, choose Allow so the app can send text messages.

Return to the dashboard and add a new weight entry that meets or passes the goal condition.

The app checks the latest entry against the goal and attempts to send an SMS message that summarizes the event, such as reaching a target or crossing a threshold.

On the emulator, the message will not appear in a real messaging app, so the code writes a log message that includes the message content.

Screenshot suggestion 4: SMS settings screen with a configured number and goal.
Screenshot suggestion 5: Android Studio Logcat view filtered to show the confirmation log entry when an SMS is sent.

Handling permission denial

To show that the app still works when SMS access is denied:

Open the SMS settings screen again.

Go to the device settings and reset SMS permissions for WeightTracker.

Return to the app and try enabling alerts.

When the dialog reappears, choose Deny.

Expected behavior:

The app does not crash.

Core features for log in and database management continue to work.

SMS is simply unavailable, and the app can show a short message on screen that alerts are disabled.

This demonstrates correct handling of both accepted and denied permission outcomes.

How the app meets the Project Three rubric

Log in

Provides a log in screen with username and password fields.

Password input uses a secure text type so characters are obscured.

Allows creation of a new account for first time users.

Stores user credentials in the SQLite users table.

Database

Stores weight entries in persistent SQLite tables.

Dashboard shows data in a grid style layout.

Supports create, delete, update, and read operations through the UI.

SMS notifications

Requests runtime permission for sending SMS messages.

Uses the user choice to either send notifications or skip them.

Sends SMS style alerts, or shows log output, when the latest weight entry meets configured conditions.

Coding best practices

Uses separate classes for activities, database helper, and adapters.

Employs clear class and method names that describe purpose.

Includes inline comments in key sections, especially around database operations and permission handling.

Future improvements

If this app were to be extended, natural next steps would include:

Supporting multiple users or profiles on a single device.

Adding data visualization such as charts for weight trends.

Integrating cloud backup and synchronization.

Improving SMS templates so messages are localized and customizable.
