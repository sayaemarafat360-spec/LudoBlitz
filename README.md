# 🎲 Ludo Blitz - Android Game

**Roll. Race. Reign Supreme!**

A beautiful, feature-rich Ludo game for Android with advanced UI/UX, inspired by Ludo King but better!

---

## ✨ Features

### 🎮 Game Modes
- **Local Multiplayer** - Play with friends on the same device (2-4 players)
- **Online Multiplayer** - Play with friends worldwide via Firebase
- **Play vs AI** - Challenge AI opponents with 4 difficulty levels (Easy, Medium, Hard, Expert)

### 🔥 Firebase Integration (100% Complete!)
- **Authentication**: Email/Password, Google Sign-In, Guest Mode
- **Realtime Database**: Online multiplayer, leaderboards, friends system
- **Remote Config**: Control the entire app dynamically without updates!
- **Cloud Messaging**: Push notifications for game events
- **Analytics**: User behavior tracking
- **Crashlytics**: Automatic crash reporting

### 🎨 AI-Generated Avatars (No Firebase Storage needed!)
- Unique avatars generated for each user
- 6 avatar styles: Geometric, Gradient Circle, Initials, Pattern, Animal, Robot
- Saves money - no paid Firebase plan required!

### 💰 Gamification
- **Coins & Gems** - Virtual currency system
- **XP & Levels** - Level up as you play
- **Daily Rewards** - 7-day reward cycle with bonuses
- **Spin Wheel** - Lucky spin for free rewards
- **14 Achievements** - Unlock achievements for rewards

### 🎵 Premium Sound Effects
- Real synthesized game sounds
- Dice rolling, token movement, captures, victory fanfare
- All sounds generated programmatically

### 📱 Modern UI/UX
- Material Design 3 with Dark/Light mode
- Poppins font family included
- Smooth animations and transitions
- Portrait-only optimized design

---

## 🔧 Firebase Setup (Required Before First Build!)

### 1. Run Your First Build on Codemagic
1. Upload this project to a Git repository (GitHub, GitLab, etc.)
2. Connect to [Codemagic.io](https://codemagic.io)
3. Run the build
4. **Check the build logs** for the SHA-1 fingerprint

### 2. Add SHA-1 to Firebase
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select your project: `ludo-blitz-dde0b`
3. Go to **Project Settings** (gear icon)
4. Scroll to **Your apps** section
5. Click **Add fingerprint**
6. Paste the SHA-1 from Codemagic logs
7. Click **Save**

### 3. Add Remote Config Parameters
Go to **Engage → Remote Config** and add these parameters:

| Parameter | Type | Default Value |
|-----------|------|---------------|
| `ads_enabled` | Boolean | `true` |
| `online_mode_enabled` | Boolean | `true` |
| `coins_per_win` | Number | `50` |
| `xp_per_win` | Number | `100` |
| `daily_reward_coins` | String | `[50, 75, 100, 150, 200, 300, 500]` |
| `require_six_to_release` | Boolean | `true` |

### 4. Set Database Rules
Go to **Build → Realtime Database → Rules**:

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "games": {
      "$gameId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    },
    "rooms": {
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
}
```

---

## 🏗️ Build Instructions

### Option 1: Codemagic (Recommended - No PC needed!)

1. **Push to Git repository**
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin YOUR_REPO_URL
   git push -u origin main
   ```

2. **Connect to Codemagic**
   - Go to [codemagic.io](https://codemagic.io)
   - Connect your repository
   - The `codemagic.yaml` file will configure everything

3. **Build & Download APK**
   - Click "Start new build"
   - Download the APK when complete

4. **Get SHA-1 for Firebase**
   - Check build logs for "SHA-1 FINGERPRINT FOR FIREBASE"
   - Add it to Firebase Console

### Option 2: Android Studio (If you have a PC)

1. Open project in Android Studio
2. Wait for Gradle sync
3. Build → Build Bundle(s) / APK(s) → Build APK(s)
4. APK will be in `app/build/outputs/apk/debug/`

---

## 📁 Project Structure

```
app/src/main/java/com/ludoblitz/app/
├── data/
│   ├── firebase/           # Firebase managers
│   │   ├── FirebaseAuthManager.kt
│   │   ├── FirebaseConfigManager.kt
│   │   └── FirebaseDatabaseManager.kt
│   ├── local/              # Local storage
│   │   ├── PreferenceManager.kt
│   │   └── LocalUserRepository.kt
│   └── model/              # Data models
├── domain/
│   ├── ai/                 # AI opponent logic
│   ├── gamelogic/          # Game engine
│   ├── gamification/       # Coins, XP, achievements
│   └── session/            # Game session management
├── ui/
│   ├── screens/            # Activities & Fragments
│   ├── viewmodel/          # ViewModels
│   └── components/         # Custom views
├── utils/
│   ├── AvatarGenerator.kt  # AI avatar generation
│   ├── SoundManager.kt     # Sound effects
│   ├── NotificationEngine.kt
│   └── AdManager.kt        # AdMob integration
└── LudoBlitzApp.kt         # Application class
```

---

## 🎯 Remote Config - Control Your App Dynamically

You can control these without releasing app updates:

### App Control
- `maintenance_mode` - Put app in maintenance
- `force_update` - Force users to update
- `ads_enabled` - Enable/disable ads globally

### Rewards
- `coins_per_win` - Coins for winning
- `xp_per_win` - XP for winning
- `daily_reward_coins` - Daily reward amounts

### Game Rules
- `require_six_to_release` - Classic vs Modern mode
- `three_sixes_rule` - 3 sixes = skip turn

### Features
- `online_mode_enabled` - Toggle online multiplayer
- `tournaments_enabled` - Enable tournaments
- `friends_system_enabled` - Enable friends features

---

## 🔐 Security Notes

- `google-services.json` is included (for demo purposes)
- In production, use `.gitignore` to exclude it
- Never commit API keys to public repositories

---

## 📱 Requirements

- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 34)
- **Portrait only** - Optimized for mobile play

---

## 💡 Key Features Explained

### AI-Generated Avatars
Instead of using Firebase Storage (which costs money), we generate unique avatars using algorithms! Each user gets a unique avatar based on their user ID seed.

### Remote Config
This allows you to change app behavior without releasing updates. For example:
- Change coin rewards
- Enable/disable features
- Put app in maintenance mode
- Change game rules

### Smart Ad Caching
Ads are preloaded and cached for offline gameplay. Users can earn coins even without internet!

---

## 🐛 Troubleshooting

### Build Fails
- Check internet connection for Gradle dependencies
- Verify `google-services.json` is in `app/src/main/`
- Check Codemagic logs for specific errors

### Google Sign-In Not Working
- Ensure SHA-1 is added to Firebase Console
- Wait a few minutes after adding SHA-1
- Check that the Web Client ID is correct

### Remote Config Not Updating
- Default values are used if fetch fails
- Check internet connection
- Verify Firebase project is active

---

## 📜 License

This project is for educational purposes. Feel free to use and modify.

---

## 🤝 Credits

- **Font**: Poppins by Indian Type Foundry
- **Icons**: Material Design Icons
- **Sounds**: Generated programmatically
- **Firebase**: Google Firebase Platform

---

**Built with ❤️ for Ludo enthusiasts!**
