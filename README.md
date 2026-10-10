# NOVA AI — Advanced Personal AI Assistant & Launcher for Android 13

**NOVA AI** is an intelligent personal AI assistant and Android Launcher Hub built with Jetpack Compose, Kotlin coroutines, Material Design 3, Room database persistence, and Google Gemini.

---

## Key Features

1. **3D AI Girl Companion ("Nova")**
   - Interactive 3D Cyber Anime Character Avatar (`nova_3d_girl_avatar` / `nova_3d_girl_full`) featuring cybernetic audio headsets and glowing neon circuitry.
   - Dynamic breathing & holographic motion: soft vertical floating hover animation, 360-degree rotating holographic aura rings, and responsive pulse scaling.
   - Real-time Mood & Activity States:
     - **Listening**: Glowing neon cyan energy.
     - **Speaking**: Bright teal soundwave aura pulsing as she speaks answers aloud (TTS enabled).
     - **Idle**: Gentle floating posture inviting questions.
     - **Thinking**: Revolving orbital cyber rings.
   - Persistent presence as a top compact avatar in Chat and a full-screen holographic model in Voice Mode.

2. **Full-Screen Voice Chat & Continuous Voice Commands**
   - Hands-Free continuous dialogue loop (automatic speech recognition reactivates after Nova speaks).
   - Instant voice commands and phone controls:
     - Open apps (YouTube, Chrome, Google Maps, Camera, Calculator, Clock, WhatsApp, etc.).
     - Open phone settings (Wi-Fi, Bluetooth, Display & Brightness, Sound & Volume, App Management, Accessibility, General settings).
     - Hardware actions (Flashlight toggle via CameraManager, Battery status check).
   - Live transcript cards displaying both user input and Nova's spoken responses.

3. **Complete Nova App Launcher & Hub (`LauncherScreen`)**
   - **Quick Launch Grid**: One-tap access to primary apps (YouTube, Chrome, Google Maps, Camera, Calculator, Settings, etc.).
   - **System Shortcuts**: Quick settings tiles for Wi-Fi, Bluetooth, Torch/Flashlight, and Battery level.
   - **Search & Voice Launch**: Integrated search bar with instant app filtering and voice-activated app launching.
   - **Full Installed App Roster**: Dynamically enumerates all launchable applications installed on the device.

4. **Advanced Chat & Multimodal Brain**
   - Integrated with Google Gemini API (`gemini-2.5-flash` / `gemini-3.5-flash`).
   - Markdown rendering with syntax-styled code blocks, horizontal scrolling, and a 1-tap copy-code button.
   - Zero-permission Android Photo Picker for multimodal image analysis.
   - Text document reader and summarizer.
   - Google Web Search Grounding with clickable source citations.
   - Request cancellation, regenerate, and retry controls.

5. **Long-Term Memory Vault & Task Manager**
   - **Memory Vault**: Stores user-approved preferences, facts, and guidelines locally in Room database. These memories are injected into Nova's system context.
   - **Task Manager**: Organize objectives with completion checkboxes, notes, and due dates.
   - **Dialogue Archives**: Reopen, manage, and delete past conversations.

---

## Build and Installation Instructions

### 1. Requirements
- **Android Studio**: Ladybug / Hedgehog or newer.
- **JDK**: Java 11 or Java 17.
- **Android Device / Emulator**: Running Android 13 (API level 33) or higher (minimum SDK 24).

### 2. Opening the Project
1. Clone or extract the project repository.
2. In Android Studio, select **File > Open** and select the root directory.
3. Allow Gradle to sync dependencies.

### 3. Configuring the Gemini API Key
- **In AI Studio**: Add your API key in the **Secrets Panel** (`GEMINI_API_KEY`).
- **In Android Studio / Local Development**:
  Copy `.env.example` to `.env` in the project root:
  ```properties
  GEMINI_API_KEY=AIzaSyYourActualKeyHere
  ```
- **In-App Override**: You can also enter or update your API key directly inside the app under **Settings > Custom API Key Override**.

### 4. Building the Debug APK
In the terminal, run:
```bash
gradle :app:assembleDebug
```
The output APK is generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### 5. Installing on Android 13 Device
Ensure USB Debugging is enabled on your device:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 6. Testing Features
1. **Voice Input**: Tap the glowing microphone button in Voice Chat. Grant microphone permission when prompted. Speak a question or command.
2. **Spoken Output**: Nova will reply aloud using Android's TextToSpeech engine. You can adjust the speech rate or switch to Hindi in Settings.
3. **App Launching**: Say *"Open YouTube"* or *"Open Chrome"* — Nova will validate the intent and launch the application.
4. **Hardware Controls**: Say *"Turn on flashlight"* or *"Check battery"* to toggle the torch or view battery percentage.
5. **Image Analysis**: Tap the attachment paperclip icon in Chat, choose **Photo / Image Analysis**, select a picture, and ask Nova to describe it.

### 7. Building a Signed Release APK
Configure your signing keystore environment variables or keystore path, then run:
```bash
gradle :app:assembleRelease
```
