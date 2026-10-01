# Edam — Learn Anything (Android)

Edam is an AI-powered interactive learning application for Android built with Kotlin, Jetpack Compose (Material 3), Room, and Retrofit.

## Features

- **Course Generation with Gemini**: Enter a subject, proficiency level (`Beginner` through `Professional`), and learning goal to generate a structured 5–8 unit curriculum with learning outcomes.
- **On-Demand Interactive Lessons**: Open any lesson to generate in-depth teaching sections, highlighted examples, and a lesson summary.
- **Practice Quizzes with Instant Feedback**: Test comprehension with multiple-choice questions and detailed answer explanations.
- **Local Progress & Curriculum Persistence**: Courses, cached lessons, and completed lesson progress are persisted locally using Room Database.

## Configuration

- Configure `GEMINI_API_KEY` in the **Secrets panel in AI Studio** (mapped via `.env` / `.env.example` and Secrets Gradle Plugin to `BuildConfig.GEMINI_API_KEY`).
- When `GEMINI_API_KEY` is not yet set, the app falls back to the original Edam Cloudflare Worker endpoint (`https://edam-ai.rup62012.workers.dev/`).

## Authentication

- Email accounts use email and password; phone accounts use SMS one-time codes.
- Google, Apple, Facebook, and GitHub use their Firebase Authentication providers.
- Users must acknowledge the Terms and Conditions on the sign-in screen before an authentication flow can start.
- Add the Firebase Android configuration file as `app/google-services.json`. In Firebase Authentication, enable Email/Password, Phone, Google, Apple, Facebook, and GitHub, and configure each provider's credentials and redirect settings in the Firebase console.

## Signed APK releases

The `Android APK Release` GitHub Actions workflow builds and attaches a signed APK to a GitHub Release when dispatched or when a `v*` tag is pushed. Configure these repository Actions secrets before running it:

- `FIREBASE_CONFIG_JSON_BASE64`: base64-encoded Firebase `google-services.json` for this app's application ID.
- `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD`: the release signing keystore and credentials. Keep a durable backup of this keystore; future updates must use the same signing identity.
