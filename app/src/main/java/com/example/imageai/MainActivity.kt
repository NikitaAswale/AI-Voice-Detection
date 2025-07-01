package com.example.imageai

import android.Manifest
import android.os.Bundle
import android.speech.tts.Voice
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.imageai.ui.theme.ImageAITheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageAITheme {
                SpeechApp()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SpeechApp() {
    val viewModel: SpeechViewModel = viewModel()
    val context = LocalContext.current
    
    // Permission handling
    val recordAudioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    // Initialize services
    LaunchedEffect(Unit) {
        viewModel.initializeTts(context)
        viewModel.initializeStt(context)
    }
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Text to Speech", "Speech to Text")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Voice Assistant",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = if (index == 0) Icons.AutoMirrored.Rounded.VolumeUp else Icons.Rounded.Mic,
                                contentDescription = title
                            )
                        }
                    )
                }
            }
            
            // Content based on selected tab
            AnimatedContent(
                targetState = selectedTab,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { if (targetState > initialState) 300 else -300 }
                    ) + fadeIn() togetherWith slideOutHorizontally(
                        targetOffsetX = { if (targetState > initialState) -300 else 300 }
                    ) + fadeOut()
                }
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> TextToSpeechScreen(viewModel)
                    1 -> SpeechToTextScreen(viewModel, recordAudioPermissionState.status.isGranted) {
                        recordAudioPermissionState.launchPermissionRequest()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextToSpeechScreen(viewModel: SpeechViewModel) {
    val inputText by viewModel.inputText
    val isSpeaking by viewModel.isSpeaking
    val isTtsInitialized by viewModel.isTtsInitialized
    val availableLanguages by viewModel.availableLanguages
    val selectedLanguage by viewModel.selectedLanguage
    val availableVoices by viewModel.availableVoices
    val selectedVoice by viewModel.selectedVoice
    
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Input Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Enter Text to Speak",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { viewModel.inputText.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Type something to convert to speech...") },
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Language Selection
                        OutlinedButton(
                            onClick = { showLanguageDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Language, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(selectedLanguage?.displayLanguage ?: "Language")
                        }
                        
                        // Voice Selection
                        OutlinedButton(
                            onClick = { showVoiceDialog = true },
                            modifier = Modifier.weight(1f),
                            enabled = availableVoices.isNotEmpty()
                        ) {
                            Icon(Icons.Rounded.RecordVoiceOver, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Voice")
                        }
                    }
                }
            }
        }
        
        item {
            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                // Speak Button
                AnimatedFloatingActionButton(
                    onClick = { viewModel.speak(inputText) },
                    isAnimated = isSpeaking,
                    icon = if (isSpeaking) Icons.AutoMirrored.Rounded.VolumeUp else Icons.Rounded.PlayArrow,
                    text = if (isSpeaking) "Speaking..." else "Speak",
                    enabled = isTtsInitialized && inputText.isNotEmpty(),
                    containerColor = MaterialTheme.colorScheme.primary
                )
                
                // Stop Button
                AnimatedFloatingActionButton(
                    onClick = { viewModel.stopSpeaking() },
                    isAnimated = false,
                    icon = Icons.Rounded.Stop,
                    text = "Stop",
                    enabled = isSpeaking,
                    containerColor = MaterialTheme.colorScheme.error
                )
            }
        }
        
        item {
            // Status Card
            if (!isTtsInitialized) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Initializing Text-to-Speech...")
                    }
                }
            }
        }
    }
    
    // Language Selection Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            languages = availableLanguages,
            selectedLanguage = selectedLanguage,
            onLanguageSelected = { viewModel.setLanguage(it) },
            onDismiss = { showLanguageDialog = false }
        )
    }
    
    // Voice Selection Dialog
    if (showVoiceDialog) {
        VoiceSelectionDialog(
            voices = availableVoices.filter { voice ->
                selectedLanguage?.let { voice.locale.language == it.language } ?: true
            },
            selectedVoice = selectedVoice,
            onVoiceSelected = { viewModel.setVoice(it) },
            onDismiss = { showVoiceDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeechToTextScreen(
    viewModel: SpeechViewModel,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val isListening by viewModel.isListening
    val recognizedText by viewModel.recognizedText
    val sttError by viewModel.sttError
    val availableSttLanguages by viewModel.availableSttLanguages
    val selectedSttLanguage by viewModel.selectedSttLanguage
    
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Permission Check
            if (!hasPermission) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.MicOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "Microphone Permission Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Please grant microphone permission to use speech recognition",
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = onRequestPermission) {
                            Text("Grant Permission")
                        }
                    }
                }
                return@item
            }
        }
        
        item {
            // Language Selection
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Speech Recognition Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedButton(
                        onClick = { showLanguageDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Language, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Language: ${selectedSttLanguage?.displayLanguage ?: "Select Language"}")
                    }
                }
            }
        }
        
        item {
            // Microphone Button
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val scale by animateFloatAsState(
                    targetValue = if (isListening) 1.2f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                
                FloatingActionButton(
                    onClick = {
                        if (isListening) {
                            viewModel.stopListening()
                        } else {
                            viewModel.startListening()
                        }
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .scale(scale),
                    containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                        contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
        
        item {
            // Status Text
            Text(
                text = when {
                    isListening -> "🎤 Listening... Speak now!"
                    recognizedText.isNotEmpty() -> "✅ Speech recognized"
                    sttError.isNotEmpty() -> "❌ Error: $sttError"
                    else -> "🔘 Tap microphone to start"
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Medium
            )
        }
        
        item {
            // Results Card
            if (recognizedText.isNotEmpty() || sttError.isNotEmpty()) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            if (sttError.isNotEmpty()) "Error" else "Recognized Text",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (sttError.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        
                        if (sttError.isNotEmpty()) {
                            Text(
                                sttError,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                recognizedText,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.inputText.value = recognizedText },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy to TTS")
                                }
                                
                                OutlinedButton(
                                    onClick = { viewModel.recognizedText.value = "" },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Rounded.Clear, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Language Selection Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            languages = availableSttLanguages,
            selectedLanguage = selectedSttLanguage,
            onLanguageSelected = { viewModel.setSttLanguage(it) },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
fun AnimatedFloatingActionButton(
    onClick: () -> Unit,
    isAnimated: Boolean,
    icon: ImageVector,
    text: String,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary
) {
    val scale by animateFloatAsState(
        targetValue = if (isAnimated) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.scale(scale),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Icon(icon, contentDescription = text)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
fun LanguageSelectionDialog(
    languages: List<Locale>,
    selectedLanguage: Locale?,
    onLanguageSelected: (Locale) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Language") },
        text = {
            LazyColumn {
                items(languages) { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                onLanguageSelected(language)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedLanguage == language,
                            onClick = { 
                                onLanguageSelected(language)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${language.displayLanguage} (${language.displayCountry})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun VoiceSelectionDialog(
    voices: List<Voice>,
    selectedVoice: Voice?,
    onVoiceSelected: (Voice) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Voice") },
        text = {
            LazyColumn {
                items(voices) { voice ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                onVoiceSelected(voice)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedVoice == voice,
                            onClick = { 
                                onVoiceSelected(voice)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                voice.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "${voice.locale.displayLanguage} • Quality: ${voice.quality}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}