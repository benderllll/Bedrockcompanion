# Bedrock Mobile Companion

Mobile-first Android app source project with bottom navigation, large touch targets, module toggles, build planner, coordinate notes, HUD preview controls, crosshair/theme previews, and local settings.

This is an original companion app, not an exact copy of Corz Client. It does not inject into Minecraft, modify the game's real HUD, automate combat, reveal hidden players, bypass anti-cheat, or connect directly to multiplayer servers.

## Build an APK from your phone (cloud build)

This project includes a GitHub Actions workflow at `.github/workflows/android-apk.yml`. You can use it to build a debug APK on GitHub's cloud machines without installing Android Studio on your phone.

1. In your phone browser, sign in to GitHub and create a **new repository**. A private repository is fine.
2. Upload the **contents** of this ZIP to the repository (upload the files and folders inside it, including the hidden `.github` folder). If the GitHub mobile upload flow hides `.github`, use GitHub's website in your browser's desktop-site mode, or create the workflow file at `.github/workflows/android-apk.yml` with the contents from this project.
3. Commit the files to the `main` branch.
4. Open the repository's **Actions** tab and select **Build Android APK**. If it hasn't run automatically, choose **Run workflow**.
5. Wait for the run to finish successfully, open that run, and under **Artifacts** download `BedrockCompanion-debug-apk`.
6. Extract the downloaded artifact ZIP, then tap `app-debug.apk` to install it on your Android phone. Android may ask you to allow installs from your browser or file manager; only install APKs you built or trust.

The workflow produces a **debug APK** for testing, not a Play Store release. GitHub artifacts are kept for 7 days by this workflow.

## Build on a computer instead

1. Open this folder in Android Studio.
2. Allow Gradle sync to finish (internet may be needed).
3. Choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

Minimum Android API 26. Portrait-first layout optimized for phones.
