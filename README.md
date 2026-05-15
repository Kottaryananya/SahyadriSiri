# 🌿 Sahyadri-Siri

## Community-Driven Water Quality Monitoring System for the Sahyadri Region

Sahyadri-Siri is an Android application designed for community-driven water quality monitoring in the Sahyadri (Western Ghats) region of India. The application enables local communities, farmers, trekkers, and environmental volunteers to report and visualize water quality conditions in realtime using an interactive map-based platform.

The project focuses on environmental awareness, pollution detection, and community participation by transforming users into active contributors for monitoring natural water resources.

---

# 📌 Problem Statement

The Western Ghats are one of the major water sources in Peninsular India, yet many streams and local water bodies remain unmonitored at the grassroots level. Traditional monitoring systems are often expensive, centralized, and unable to provide realtime localized updates.

Sahyadri-Siri addresses this challenge by providing a crowdsourced and geofenced water quality monitoring system where users can report water conditions and visualize water health across the Sahyadri region.

---

# ✨ Key Features

* 🗺️ Interactive Google Maps Integration
* 📍 GPS-Based Water Quality Reporting
* 🌊 Water Health Score Calculation (0–10)
* 🔥 Health Map / Heatmap Visualization
* 📊 Marker Clustering for Better Visualization
* 🚨 Unsafe Water Alerts System
* 🔍 Stream Filtering and Search Functionality
* 🧭 Google Maps Navigation Support
* ☁️ Firebase Realtime Database Integration
* 📶 Offline-Capable Data Handling
* 🌿 Sahyadri Region Geofencing
* 📚 Educational Sahyadri Wiki Section

---

# 🛠️ Technologies Used

## Frontend & Development

* Kotlin
* Android Studio
* Material Design 3
* MVVM Architecture
* Kotlin Coroutines & StateFlow

## Backend & Cloud

* Firebase Realtime Database
* Firebase Local Persistence

## Maps & Geolocation

* Google Maps SDK
* Google Play Services Location
* Android Maps Utility Library

## Tools & Platforms

* GitHub
* Gradle (Kotlin DSL)
* Google AI Studio
* Google Cloud
* Google Developers Resources

---

# 🧠 Water Health Score Logic

The application calculates a Water Health Score based on:

* Water Clarity
* Flow Condition
* Smell Condition
* Visible Pollution

### Scoring Logic

```plaintext
Health Score = (Clarity × 2) + Flow Score − Pollution Penalty − Smell Penalty
```

### Health Status Mapping

| Score Range | Status  |
| ----------- | ------- |
| 7 – 10      | Healthy |
| 4 – 6       | Warning |
| 0 – 3       | Unsafe  |

---

# 📱 Application Screenshots

## Sahyadri-Siri Application Screens

<img width="1280" height="720" alt="SahyadriSiri" src="https://github.com/user-attachments/assets/d5e90220-0ef8-4d12-92ba-b28041273310" />


---

# 📂 Project Structure

```plaintext
SahyadriSiri/
│
├── app/
├── gradle/
├── screenshots/
├── docs/
├── build.gradle.kts
├── settings.gradle.kts
├── README.md
└── google-services.json (excluded from public repo)
```

---

# ⚙️ Setup & Installation

## Prerequisites

* Android Studio
* Android SDK
* Google Maps API Key
* Firebase Project Setup

## Clone Repository

```bash
git clone https://github.com/Kottaryananya/SahyadriSiri.git
```

## Open Project

1. Open Android Studio
2. Select "Open Existing Project"
3. Choose the cloned repository folder

## Configure API Key

Create a `local.properties` file and add:

```properties
MAPS_API_KEY=YOUR_API_KEY
```

## Firebase Setup

1. Create a Firebase project
2. Enable Firebase Realtime Database
3. Add your `google-services.json` file inside:

```plaintext
app/google-services.json
```

## Run the Application

```bash
Build → Run App
```

---

# 🚀 APK / Release

## GitHub Release

APK Download:

[https://github.com/Kottaryananya/SahyadriSiri/releases/tag/v1.0](https://github.com/Kottaryananya/SahyadriSiri/releases/tag/v1.0)

---

# 🎯 Future Improvements

* AI-Based Pollution Prediction
* IoT Sensor Integration
* User Authentication System
* Advanced Analytics Dashboard
* Multi-Language Support
* Government Data Integration

---

# 📖 Internship Context

This project was developed as part of the Android App Development using GenAI Internship at MindMatrix.

The internship involved:

* Android development training
* Generative AI tool exposure
* Practice application development
* Independent project implementation
* Documentation and deployment activities

---

# 👩‍💻 Developer

**Ananya Kottary**
Android App Development using GenAI Intern
Canara Engineering College

---

# 📜 License

This project is developed for educational and internship purposes.
