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
