# SahyadriSiri 🌊

**SahyadriSiri** is a dedicated Android application designed for monitoring and reporting water quality in the **Sahyadri (Western Ghats)** region. This community-driven platform empowers locals and travelers to contribute real-time data about water bodies, helping to preserve one of India's most vital ecosystems.

## 🚀 Live Demo & Download
- **Live Landing Page:** [https://Kottaryananya.github.io/SahyadriSiri/](https://Kottaryananya.github.io/SahyadriSiri/)
- **Download Latest APK:** [Latest Release](https://github.com/Kottaryananya/SahyadriSiri/releases/latest)

## ✨ Features
- 📍 **Interactive Map:** Explore water health data across the Western Ghats using Google Maps.
- 📊 **Community Reporting:** Easily submit reports on water clarity, flow speed, smell, and pollution levels.
- 🔥 **Water Health Heatmaps:** Visualize quality trends and pollution hotspots with dynamic heatmaps.
- 📦 **Smart Clustering:** High-density report areas are neatly clustered for better map readability.
- 📚 **Water Wiki:** Access educational content about local water conservation and the importance of the Sahyadri ecosystem.
- 🚨 **Real-time Alerts:** Stay informed about water safety and local alerts.

## 🛠️ Tech Stack
- **Language:** Kotlin
- **UI Framework:** XML Layouts & Material Design 3
- **Database:** Firebase Realtime Database
- **Maps API:** Google Maps SDK & Utility Library (Heatmaps/Clustering)
- **Location:** Google Play Services Location
- **CI/CD:** GitHub Actions (Auto-build & Release)

## 🛠️ Installation for Developers
1. Clone the repository:
   ```bash
   git clone https://github.com/Kottaryananya/SahyadriSiri.git
   ```
2. Open the project in **Android Studio (Ladybug or newer)**.
3. Create a `local.properties` file in the root directory and add your Google Maps API Key:
   ```properties
   MAPS_API_KEY=YOUR_API_KEY_HERE
   ```
4. Sync the project with Gradle files.
5. Build and run on your device or emulator.

## 🛡️ Privacy & Security
- **API Security:** All API keys are managed via the Secrets Gradle Plugin and are never exposed in the source code.
- **Permissions:** The app requires `ACCESS_FINE_LOCATION` to accurately pin reports to the map.

## 🤝 Contributing
Contributions are welcome! If you'd like to improve SahyadriSiri, please fork the repo and create a pull request.

---
Developed with ❤️ for the Sahyadri region by [Kottaryananya](https://github.com/Kottaryananya).
