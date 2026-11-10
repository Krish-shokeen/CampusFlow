# CampusFlow – Smart Student Management & Productivity System

CampusFlow is an Android application developed using Kotlin to help students manage their academic activities and improve productivity through a single platform.

The application brings together essential student tools such as task management, assignment tracking, attendance monitoring, quizzes, and academic progress tracking.

## 🎯 Project Objectives

- Provide a centralized platform for managing academic activities.
- Help students organize tasks and assignment deadlines.
- Track subject-wise attendance.
- Provide interactive quizzes for self-assessment.
- Improve time management and academic productivity.
- Demonstrate modern Android development using Kotlin.

## ✨ Key Features

### 1. Dashboard
- Centralized overview of academic activities.
- Quick access to application modules.
- Academic statistics and progress indicators.

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

### 6. Additional Modules
Planned enhancements:
- Notes Manager
- Smart Study Planner
- Detailed Academic Statistics
- Profile and Settings

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Kotlin | Primary programming language |
| Android Studio | Development environment |
| Jetpack Compose | Modern UI development |
| Material 3 | UI components and theming |
| Room Database | Local data persistence |
| KSP | Kotlin Symbol Processing |
| Kotlin Coroutines | Asynchronous operations |
| StateFlow | Reactive UI state management |
| DataStore | User preference storage |
| Navigation Compose | Screen navigation |
| MVVM | Application architecture |

## 🏗️ Architecture

CampusFlow follows the **MVVM (Model-View-ViewModel)** architecture with a Repository pattern.

```text
CampusFlow
│
├── data
│   ├── database
│   ├── dao
│   ├── entities
│   ├── repository
│   └── preferences
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
│   └── profile
│
├── utils
│
└── di
```

## ⚙️ Installation and Setup

Follow these steps to run CampusFlow locally.

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

- Notes management with categorization.
- Smart study planner and scheduling.
- Cloud synchronization and backup.
- Notifications for upcoming deadlines.
- Detailed performance analytics.
- Improved personalization and accessibility.

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