# CampusFlow – Smart Student Management & Productivity System

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Room DB](https://img.shields.io/badge/Room_Database-000000?style=for-the-badge&logo=sqlite&logoColor=white)

## 📌 Project Overview
**CampusFlow** is a comprehensive academic productivity and student management application built natively for Android. It was developed as a B.Tech Semester V capstone project for **SEC042 – New Age Programming Languages Lab** at K.R. Mangalam University.

The app is designed to help students efficiently manage their academic life through task tracking, assignment deadlines, attendance thresholds, subject notes, quiz evaluations, and study planning—all wrapped in a premium, responsive Material 3 UI.

<<<<<<< Updated upstream
- Provide a centralized platform for managing academic activities.
- Help students organize tasks and assignment deadlines.
- Track subject-wise attendance.
- Provide interactive quizzes for self-assessment.
- Improve time management and academic productivity.
- Demonstrate modern Android development using Kotlin.

## ✨ Key Features

### 1. Dashboard
- Personalized greeting and current date.
- Live pending task and assignment totals, next assignment deadline, and attendance progress.
- Quick actions for tasks, assignments, attendance, and the study planner.

### 2. Task Manager
- Create, update, and delete tasks.
- Set task priorities and due dates.
- Track task completion.
- Search and filter tasks.

### 3. Assignment Tracker
- Manage assignments subject-wise.
- Set assignment deadlines.
- Track pending and completed assignments.

### 4. Attendance Manager
- Manage subjects and attendance records.
- Mark attendance as present or absent.
- Calculate attendance percentages.
- View attendance history.

### 5. Quiz Module
- Attempt multiple-choice quizzes.
- Select answers and view results.
- Calculate scores.
- Track quiz performance.

### 6. Notes Manager
- Create, edit, search, and delete notes stored locally.

### 7. Study Planner
- Schedule subject-wise study sessions with a date, start time, and duration.
- Edit and remove scheduled sessions.

### 8. Statistics
- Review task completion, assignments due, attendance, quiz scores, and study time this week.

### 9. Profile and Settings
- Save a name, course, and semester on the device.
- Choose light, dark, or system appearance; the preference is retained after restart.
=======
## 🚀 Features
- **📊 Interactive Dashboard:** A centralized overview of pending tasks, overdue assignments, and attendance warnings.
- **✅ Task Manager:** Create, filter, and track daily to-do tasks with priority and due date indicators.
- **📚 Assignment Tracker:** Never miss a deadline! Sorts assignments chronologically and highlights overdue submissions.
- **🎓 Attendance Manager:** Tracks present/absent classes. Automatically calculates percentages and predicts how many consecutive classes are needed to reach the 75% threshold!
- **🧠 Quiz Module:** Built-in computer science question bank. Test your knowledge under a countdown timer, review answers, and track your average score.
- **📝 Notes Manager:** Easily jot down subject-wise notes with quick search capability.
- **⏱️ Study Planner:** Schedule time-blocked study sessions for various subjects.
- **📈 Statistics & Analytics:** View your overall performance, task completion rates, and average attendance.
- **🌙 Dark Mode & Theming:** Persisted User Preferences (DataStore) allowing seamless Light/Dark/System theme switching.
>>>>>>> Stashed changes

## 🛠️ Technology Stack
- **Language:** Kotlin (v2.0.20)
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Local Database:** Room Database with KSP (Kotlin Symbol Processing)
- **Asynchronous Programming:** Kotlin Coroutines & Flow / StateFlow
- **Navigation:** Jetpack Navigation Compose
- **Dependency Injection:** Manual DI via custom AppContainer
- **Preferences:** Jetpack DataStore

## 🏗️ Architecture
The project strictly follows the recommended Android Architecture guidelines:
1. **UI Layer:** Compose functions subscribing to `StateFlow` exposed by ViewModels.
2. **Domain/ViewModel Layer:** Manages UI state, applies business logic (like computing attendance thresholds or filtering lists).
3. **Data Layer:** `Repositories` abstract the data sources. `DAOs` (Data Access Objects) map Kotlin functions to SQLite queries executed by Room.

## 📥 Installation Instructions
1. Clone the repository: `git clone https://github.com/Krish-shokeen/CampusFlow.git`
2. Open the project in **Android Studio (Koala or newer)**.
3. Wait for Gradle to synchronize. 
4. Select a physical device or emulator (API 26+).
5. Click **Run** (Shift + F10).

<<<<<<< Updated upstream
```text
CampusFlow
│
├── data
│   ├── database
│   ├── dao
│   ├── entities
│   ├── repository
│
├── domain
│   ├── model
│   └── planner
│
├── viewmodel
│
├── ui
│   ├── theme
│   ├── components
│   ├── navigation
│   ├── auth
│   ├── dashboard
│   ├── tasks
│   ├── assignments
│   ├── attendance
│   ├── quiz
│   ├── notes
│   ├── planner
│   ├── statistics
│   ├── profile
│   └── settings
│
├── utils
│
└── di
```
=======
*(Note: The app ships with a built-in `DatabaseSeeder` that will pre-populate dummy subjects, tasks, and 50 CS quiz questions upon the first launch!)*
>>>>>>> Stashed changes

## 📸 Screenshots
*(To be populated by capturing screens of the live application)*
- Dashboard & Theme Modes
- Task & Assignment Creation
- Attendance Progress Rings
- Quiz Active Session

## 🔮 Future Scope
- **Cloud Sync:** Migrating Room database to Firebase Firestore for cross-device synchronization.
- **Push Notifications:** Integrating WorkManager for local daily reminders for upcoming assignments.
- **CGPA Calculator:** Dynamic grade tracking predicting semester performance.

<<<<<<< Updated upstream
1. Clone the repository:

```bash
git clone https://github.com/YOUR_USERNAME/CampusFlow.git
```

2. Open the project in Android Studio.

3. Allow Gradle to sync and download the required dependencies.

4. Connect an Android device or start an Android Emulator.

5. Run the application using the Android Studio Run button.

### Requirements

- Android Studio
- Kotlin support
- Android SDK
- Minimum Android SDK: API 26 (Android 8.0)
- Internet connection for initial dependency downloads

## 📸 Application Screenshots

Add screenshots of your implemented application in the `screenshots` folder.

| Dashboard | Task Manager |
|---|---|
| Add dashboard screenshot here | Add task screenshot here |

| Assignment Tracker | Attendance Manager |
|---|---|
| Add assignment screenshot here | Add attendance screenshot here |

| Quiz Screen | Quiz Results |
|---|---|
| Add quiz screenshot here | Add results screenshot here |

## 🚀 Future Enhancements

- Notifications for upcoming deadlines.
- Optional data export and backup.
- Expanded academic analytics and accessibility improvements.

## 🎓 Academic Information

**Project Name:** CampusFlow – Smart Student Management & Productivity System  
**Course:** SEC042 – New Age Programming Languages Lab  
**Programming Language:** Kotlin  
**Platform:** Android  
**University:** K.R. Mangalam University  
**School:** School of Engineering & Technology  
**Academic Year:** 2026–2027

## 👨‍💻 Developer

**Krish**  
B.Tech – Computer Science and Engineering (Full Stack Development)  
K.R. Mangalam University, Gurugram

---

*CampusFlow is an academic project developed to demonstrate Kotlin programming concepts and modern Android application development.*
=======
## 👨‍💻 Developer Information
**Developed By:** Krish Shokeen  
**Course:** B.Tech (Semester V)  
**Subject:** SEC042 – New Age Programming Languages Lab  
**University:** K.R. Mangalam University
>>>>>>> Stashed changes
