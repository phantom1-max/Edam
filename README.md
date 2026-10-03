# Edam — Learn Anything (Android & Multi-Platform PC)

Edam is an AI-powered interactive learning application built with Kotlin, Jetpack Compose (Material 3), Firebase Firestore, Room Database, and Retrofit.

## Features

- **Daily Learning Streak with Firestore Cloud Sync**: Tracks consecutive days of learning activity both in local preferences and in Firebase Firestore (`/users/{userId}/streaks/daily`). Features a prominent flame icon with current streak counter on the home dashboard, streak freeze protection, and a 7-day consistency strip.
- **4 Distinct Companions with Cursor Eye-Tracking**:
  - **Edam**: Golden Dutch Cheese Wheel & Botanical Sprout with rosy cheek dimples.
  - **Kora**: Quant Bull Fox with pointed crimson ears, curved gold bull horns, fluffy emerald-tipped tail, and Wall Street tie.
  - **Vex**: Grandmaster Owl with feathered purple wings, tufted brow plumes, golden spectacles, diamond beak, and GM chess crown.
  - **Nova**: Futuristic Cyber Synth Bot with a hexagonal chassis, dark LED visor faceplate, side plasma ear-pods, and anti-gravity hover rings.
  - **Idle Cursor Eye-Tracking**: When companions are doing nothing (in `IDLE` state with no active animation), their body remains still while their eyes smoothly follow mouse cursor movements across the screen on PC, ChromeOS, and touch devices.
- **User-Defined Daily Lesson Goal**: Configure daily targets (1–10 lessons/day) backed by Jetpack DataStore with real-time progress bars.
- **Push Notification Goal Reminders**: Scheduled via Android WorkManager to remind learners at custom times.
- **Course Generation with Gemini**: Enter a subject, proficiency level (`Beginner` through `Professional`), and learning goal to generate structured curricula with learning outcomes.
- **On-Demand Interactive Lessons & Quizzes**: Complete interactive lessons, test comprehension with quizzes, and earn badges.

## Pre-Built APK in Repository

The Android APK is compiled and directly available in the repository under the `releases/` directory:
- `releases/edam-android-latest.apk`: Ready to install or sideload immediately on Android.
- `releases/edam-android-v1.2.0.apk`: Versioned release archive.

## Editable Versions & Multi-Platform Release (Windows, macOS, Linux, Android)

You can easily bump versions and trigger builds for **Android**, **Windows**, **macOS**, and **Linux**:

1. **Edit `release-config.env`** in the root directory:
   ```env
   APP_VERSION_NAME=1.3.0
   APP_VERSION_CODE=4
   RELEASE_TAG=v1.3.0
   RELEASE_TITLE=Edam v1.3.0 — Multi-Platform Release
   BUILD_ANDROID_APK=true
   BUILD_WINDOWS_PC=true
   BUILD_MACOS_PC=true
   BUILD_LINUX_PC=true
   ```
2. Commit and push the file to GitHub.
3. The GitHub Actions workflow (`.github/workflows/release-apk.yml`) will:
   - Build the Android APK and commit it to `releases/`.
   - Build desktop packages from `desktop-pc/` for **Windows** (`.msi` / `.exe`), **macOS** (`.dmg`), and **Linux** (`.deb`).
   - Create a GitHub Release with all platform installers attached.

## Running the Desktop PC Version Locally

The standalone desktop project is located in `desktop-pc/`:
```bash
# Run on Windows, macOS, or Linux
gradle -p desktop-pc run

# Package native installer for your current OS
gradle -p desktop-pc packageDistributionForCurrentOS
```
