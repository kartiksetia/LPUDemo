# Study Lens — Notes to Interactive Quiz

Native Kotlin + Jetpack Compose demo using Firebase AI Logic. The Android app sends notes and images through Firebase directly to Gemini. No laptop server, terminal API key, or adb reverse is required. Internet is required for live generation.

## Firebase setup (one time)

1. Open https://console.firebase.google.com/ and create a project, or add Firebase to your existing Google Cloud project.
2. Register an Android app with the exact package **com.example.demolpu**.
3. Download `google-services.json` and place it at **DemoLPU/app/google-services.json**. Do not put your Gemini provider secret in Android source. This Firebase configuration is different from the Gemini secret API key previously used by the Python server.
4. In Firebase console, open **AI Services > AI Logic**, choose **Get started**, and select **Gemini Developer API**. Complete API activation.
5. Sync Gradle and rebuild the debug app. The Google Services plugin activates automatically when the configuration file exists. Without configuration, the app still builds and runs its clearly labelled offline sample.
6. Run the app and tap Generate with some notes. In Android Studio Logcat, filter **DebugAppCheckProvider**. Copy the displayed debug token.
7. Firebase console: **Security > App Check > Apps > your Android app > Manage debug tokens**. Register that token, then retry Generate. Keep App Check enforced for AI Logic. Treat the debug token as private and do not include it in screenshots or commits.

Debug builds use the debug App Check provider. Release builds use Play Integrity; a release requires the corresponding Firebase/Play Integrity registration and signing setup. Do not distribute a build using the debug provider.

The model name is `gemini-3.8-flash` in `QuizRepository.kt`. Change it to a supported multimodal model available in your Firebase project if needed.

## Demo flow

- Choose a clear image of study notes, or paste text.
- Tap Generate to explicitly upload the selected material to Google Gemini through Firebase AI Logic.
- Answer three questions, reveal explanations, and review your score.
- **Try sample quiz • no network** uses a fixed data-structures quiz. It does not generate questions from your notes or send any data.

## Code walkthrough

- `MainActivity.kt`: Compose screens, Photo Picker, lifecycle Logcat messages.
- `QuizViewModel.kt`: input/loading/error/quiz state, answer selection, scoring and retry.
- `QuizRepository.kt`: image downsampling, Firebase model request, JSON schema and strict output validation.
- `StudyLensApplication.kt`: initializes configured Firebase and installs the App Check provider.
- `src/debug/.../AppCheckSetup.kt`: debug provider for local demos.
- `src/release/.../AppCheckSetup.kt`: production Play Integrity provider.

Flow: Compose UI → ViewModel → Repository → Firebase AI Logic → Gemini.

No independent OCR engine is used. Gemini receives the selected image via the multimodal SDK. The response schema defines a status and a questions array. The app additionally checks exactly three questions, four distinct nonempty options, integer answer indexes, and required explanations. Structured output does not guarantee factual correctness; review against the original notes.

Filter Logcat by `StudyLensLifecycle` to demonstrate Activity callbacks. ViewModel retains state across configuration changes, not process death. Recomposition is different from the Activity lifecycle.

The `backend/` directory is retained only as the previous Python implementation. The Android app no longer calls it. You may stop any old backend terminal.

## Verification

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.demolpu.QuizFlowTest
```

Live generation cannot be verified until Firebase configuration and App Check registration are complete. Successful offline tests do not verify a cloud request.

Official references:
- https://firebase.google.com/docs/ai-logic/get-started?platform=android
- https://firebase.google.com/docs/ai-logic/generate-structured-output?platform=android
- https://firebase.google.com/docs/app-check/android/debug-provider
