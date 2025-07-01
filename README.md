# Voice Assistant - Android TTS & STT App

A beautiful Android application built with Jetpack Compose that provides both Text-to-Speech (TTS) and Speech-to-Text (STT) functionality using native Android libraries.

## Features

### 🗣️ Text-to-Speech (TTS)
- **Multi-language support**: Choose from 14+ languages including English, Spanish, French, German, Italian, Portuguese, Russian, Chinese, Japanese, Korean, Hindi, and Arabic
- **Multiple voice options**: Select from different voices available for each language
- **Voice quality indicators**: See voice quality ratings to choose the best option
- **Real-time speech control**: Start, stop, and monitor speech synthesis
- **Beautiful animations**: Visual feedback during speech synthesis

### 🎤 Speech-to-Text (STT)
- **Real-time speech recognition**: Convert speech to text in real-time
- **Multi-language support**: Same language support as TTS
- **Permission handling**: Automatic microphone permission management
- **Error handling**: Clear error messages for different failure scenarios
- **Copy to TTS**: Seamlessly transfer recognized text to the TTS tab

### 🎨 Beautiful UI
- **Material 3 Design**: Modern, beautiful interface following Material Design guidelines
- **Smooth animations**: Engaging transitions and visual feedback
- **Responsive layout**: Works great on different screen sizes
- **Gradient backgrounds**: Eye-catching visual design
- **Intuitive navigation**: Easy-to-use tab-based interface

## Technical Implementation

### Architecture
- **MVVM Pattern**: Clean separation of concerns using ViewModel
- **Jetpack Compose**: Modern declarative UI framework
- **State Management**: Reactive UI updates using Compose state

### Native Android APIs Used
- **TextToSpeech**: Android's built-in TTS engine
- **SpeechRecognizer**: Android's speech recognition service
- **Voice API**: Access to different voice options
- **UtteranceProgressListener**: Real-time speech synthesis monitoring

### Key Components
- `SpeechViewModel`: Manages TTS and STT functionality
- `TextToSpeechScreen`: UI for text-to-speech features
- `SpeechToTextScreen`: UI for speech-to-text features
- `LanguageSelectionDialog`: Language picker interface
- `VoiceSelectionDialog`: Voice selection interface

## Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd ImageAI
   ```

2. **Build the project**
   ```bash
   ./gradlew build
   ```

3. **Install on device**
   ```bash
   ./gradlew installDebug
   ```
   
   Or manually install the APK from:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

## Usage

### Text-to-Speech
1. Open the app and ensure you're on the "Text to Speech" tab
2. Type or paste text in the input field
3. Select your preferred language from the language button
4. Choose a voice from the voice selection (if available)
5. Tap "Speak" to hear the text
6. Use "Stop" to interrupt speech synthesis

### Speech-to-Text
1. Switch to the "Speech to Text" tab
2. Grant microphone permission when prompted
3. Select your preferred language for recognition
4. Tap the microphone button to start listening
5. Speak clearly into the microphone
6. View the recognized text in the results card
7. Use "Copy to TTS" to transfer text to the TTS tab

## Permissions

The app requires the following permissions:
- **RECORD_AUDIO**: For speech recognition functionality
- **INTERNET**: For enhanced TTS features (if available)

## Supported Languages

- English (US, GB)
- Spanish (Spain)
- French (France)
- German (Germany)
- Italian (Italy)
- Portuguese (Portugal)
- Russian (Russia)
- Chinese (China)
- Japanese (Japan)
- Korean (Korea)
- Hindi (India)
- Arabic (Saudi Arabia)

*Note: Actual language availability depends on the TTS engines installed on your device.*

## Requirements

- **Android API Level**: 24+ (Android 7.0)
- **Target SDK**: 35
- **Kotlin**: 1.9+
- **Jetpack Compose**: Latest stable version

## Dependencies

- Jetpack Compose BOM
- Material 3
- ViewModel Compose
- Accompanist Permissions
- Material Icons Extended

## Error Handling

The app includes comprehensive error handling for:
- TTS initialization failures
- Speech recognition errors
- Permission denials
- Network connectivity issues
- Audio recording problems

## Future Enhancements

- [ ] Custom TTS speed and pitch controls
- [ ] Audio file export for TTS
- [ ] Speech recognition confidence scores
- [ ] Dark/Light theme toggle
- [ ] Conversation mode (continuous STT/TTS)
- [ ] Text formatting options
- [ ] History of recognized text

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Android TTS and STT APIs
- Material Design 3
- Jetpack Compose team
- Android developer community

---

**Built with ❤️ using Jetpack Compose and native Android APIs** 