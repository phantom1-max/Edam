# Edam Releases

This directory contains pre-built installation files for Edam that are tracked in the GitHub repository.

## Files

- `edam-android-latest.apk`: The latest compiled Android APK (v1.3.0) for sideloading on any Android device or emulator.
- `edam-android-v1.3.0.apk`: Version 1.3.0 signed package with Daily Streak Push Notifications, Badges System in Firestore, Mascot Characters (Businessman Robot & Bit Computer Virus), and 3 Hero Highlighted Features + Any Subject AI Builder.
- `edam-android-v1.2.0.apk`: Legacy versioned Android package.

## Releasing New Versions

You can easily change the version and release new builds across Android, Windows, macOS, and Linux:

1. **Edit `release-config.env`** in the repository root:
   ```env
   APP_VERSION_NAME=1.3.0
   APP_VERSION_CODE=4
   RELEASE_TAG=v1.3.0
   RELEASE_TITLE=Edam v1.3.0
   ```
2. Commit and push your changes to GitHub (or push a tag like `git tag v1.3.0 && git push origin v1.3.0`).
3. GitHub Actions (`.github/workflows/release-apk.yml`) automatically:
   - Builds the Android APK and places it in `releases/`.
   - Builds native installers for **Windows** (`.msi` / `.exe`), **macOS** (`.dmg`), and **Linux** (`.deb` / `.rpm`).
   - Publishes all artifacts under GitHub Releases with automatic release notes.
