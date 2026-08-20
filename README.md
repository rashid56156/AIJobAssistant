# AI Job Assistant

An Android app that compares a resume against a job description using Google's Gemini API, and returns a match score with strengths, gaps, and actionable suggestions.

Built as an open-source portfolio project demonstrating Clean Architecture, Jetpack Compose, and LLM API integration on Android.

## What it does

1. Paste a job description.
2. Paste your resume as text, or upload it as a PDF.
3. Tap **Analyze match**.
4. Get back a 0–100 match score, your strengths against the role, gaps to address, and concrete suggestions — all saved locally so you can revisit past analyses in History.

## Why it exists

Built while preparing for Senior/Staff Android interviews, as a way to demonstrate practical LLM integration on top of an otherwise standard Clean Architecture Android app — rather than just listing "AI" as a resume keyword.

## Screenshots

*(Add screenshots here once built — Home, Result, History, Settings)*

## Getting started

This app uses your own Gemini API key. No backend, no account, no data leaves your device except the direct call to Google's Gemini API.

1. Get a free API key at [aistudio.google.com](https://aistudio.google.com/app/apikey).
2. Clone this repo and open it in Android Studio (Ladybug or newer recommended).
3. Build and run on a device or emulator running **Android 12 (API 31) or higher**.
4. On first launch, go to **Settings** and paste your API key. It's encrypted on-device via the Android Keystore (`EncryptedSharedPreferences`) and is never transmitted anywhere except directly to Google's Gemini endpoint.
5. Go to Home, paste a job description and your resume, and tap **Analyze match**.

No `google-services.json`, no Firebase project, no signing config needed to run this locally.

## Architecture

Clean Architecture with three layers:

```
ui/            Compose screens + ViewModels (presentation)
domain/        Models, repository interfaces, use cases (pure Kotlin, no Android deps)
data/          Repository implementations, Room, Retrofit/Gemini REST client, Keystore storage
di/            Hilt modules binding domain interfaces to data implementations
```

**Why this structure:** `domain` has zero dependency on Android, Retrofit, or Room — it only depends on Kotlin coroutines. That's what makes the use case tests in `app/src/test/.../domain/` run as fast, pure-JVM unit tests with no mocking of Android framework classes required.

### Key technical decisions

- **Direct REST calls to the Gemini API**, not a vendor SDK. The entire network surface area is one Retrofit interface (`GeminiApi.kt`) — anyone auditing this repo can read exactly what data leaves the device.
- **BYO API key** instead of a shared backend. This keeps the project genuinely open source: clone it, drop in your own free key, and you're running — no shared billing, no backend to host.
- **Android Keystore-backed storage** (`EncryptedSharedPreferences`) for the API key, not plain `SharedPreferences` and not `BuildConfig`. The encryption key never leaves secure hardware on supporting devices.
- **Sealed `AppResult<T>`** instead of throwing exceptions across layers, so each failure mode (missing key, network error, malformed model response, PDF parse failure) is handled explicitly rather than collapsing into a generic "something went wrong."

## Tech stack

- Kotlin, Jetpack Compose, Material 3
- Hilt (dependency injection)
- Room (local history)
- Retrofit + kotlinx.serialization (Gemini REST client)
- Android Keystore / `EncryptedSharedPreferences` (API key storage)
- PdfBox-Android (resume PDF text extraction)
- Coroutines + Flow throughout
- MockK + Turbine + Truth for testing

## Known limitations (honest scope notes)

This is a v1 portfolio project, not a production app. Known gaps:

- Scanned/image-only PDFs aren't supported — text extraction needs a real text layer in the PDF.
- No retry/backoff on transient Gemini API errors beyond a single attempt.
- No rate-limit handling beyond surfacing the 429 response as an error message.
- Single-user, local-only history; nothing syncs across devices.

## Contributing

Issues and PRs welcome. This is an active learning project — if something looks wrong, it probably is, and a PR explaining the fix is genuinely appreciated.

## License

MIT — see [LICENSE](LICENSE).
