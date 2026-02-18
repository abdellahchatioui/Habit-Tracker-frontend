# 📱 Habit Tracker - Android App

[![Java](https://img.shields.io/badge/Java-17-orange?logo=java&logoColor=white)](https://www.java.com/)
[![Android SDK](https://img.shields.io/badge/Android-SDK-green?logo=android&logoColor=white)](https://developer.android.com/)
[![Retrofit](https://img.shields.io/badge/Retrofit-2.9-square?logo=square&logoColor=white)](https://square.github.io/retrofit/)
[![Material Design](https://img.shields.io/badge/Material-Design-blue?logo=materialdesign&logoColor=white)](https://m3.material.io/)

A modern Android frontend for the **Habit Tracker System**. This application allows users to build positive routines by creating, managing, and tracking daily habits. It features secure JWT authentication and connects seamlessly to a Spring Boot backend.

---

## 🚀 Tech Stack & Libraries

* **Language:** Java
* **Networking:** Retrofit 2 (REST API communication), GSON
* **UI Components:** RecyclerView, CardView, Material Design
* **Local Storage:** SharedPreferences (Token Management)
* **UX Features:** SwipeRefreshLayout, ProgressBar, Toast Messages

---

## ▶️ How to Run

Follow these steps to set up the Android development environment.

1. **Prerequisites**
   * Ensure the **Spring Boot Backend** is currently running.
   * Install **Android Studio** (Bumblebee or newer recommended).

2. **Clone & Open**
   ```bash
   git clone https://github.com/abdellahchatioui/Habit-Tracker-Android.git
   
   cd Habit-Tracker-Android
---

## 🛠️ Lancer le projet


1. Ouvrir **Android Studio**

2. **Open** → sélectionner le dossier `MyFirstCrudsAndroidApp`

3. Attendre la synchronisation **Gradle**

4. Cliquer sur **Run ▶** (émulateur ou téléphone)

---
## 📡 Backend Repository

The Android application acts as a client and requires the companion REST API to be active for data persistence and authentication.

### 🔗 Link to Backend
The server-side source code and setup instructions are available at:
👉 **[Habit-Tracker-backend](https://github.com/abdellahchatioui/Habit-Tracker-backend.git)**

> [!IMPORTANT]  
> **Connectivity Check:** Make sure the backend is running and reachable before launching the app. If using the Android Emulator, ensure your base URL is set to `http://localhost:8080`.
---
## 🏗️ Application Architecture

The app follows a modular and clean architecture to ensure scalability and maintainability.

```text
com.habittracker
│
├── activities   # UI Controllers (Screens)
├── adapters     # RecyclerView Adapters
├── models       # Data Models (POJOs)
├── network      # Retrofit Client & Interceptors
├── utils        # Helper classes (Session Manager)
└── api          # API Interface definitions
```
---
## 👥 User Roles & Features

The application adapts its interface and functionality based on the logged-in user's role.

### 👤 User (Standard)
* **Dashboard:** View daily progress summaries and current streak statistics.
* **Habit Management:** Full CRUD capabilities (Create, Edit, and Delete) for personal habits.
* **Tracking:** Mark specific habits as "Completed" for the current day.
* **Search:** Dynamically filter the habit list to find specific entries.

### 🛡️ Admin (Management)
* **User Oversight:** Access a list of all registered users in the system.
* **Moderation:** Ability to **Block** or **Delete** user accounts to maintain system integrity.
* **Global Stats:** View system-wide usage statistics (e.g., total users, active habits).

---

## 📱 App Screens


The application consists of the following key activities:

| Screen | Description |
| :--- | :--- |
| **Splash** | Initial loading screen displaying the app logo. |
| **Login / Register** | Secure entry points for user authentication and account creation. |
| **Habit List** | The main feed displaying active user habits using a `RecyclerView`. |
| **Add / Edit Habit** | Input forms for creating new habits or updating existing details. |
| **Habit Details** | An in-depth view of a specific habit's history and completion logs. |
| **Statistics** | Visual breakdown of consistency, streaks, and completion rates. |
| **Admin Dashboard** | A dedicated panel for administrative tasks (User management). |

---

## 👨‍💻 Author

**Abdellah Chatioui** *Fullstack Developer*

[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/abdellahchatioui)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/abdellah-chatioui-5b9426299/)

