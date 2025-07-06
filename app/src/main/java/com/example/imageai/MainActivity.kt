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
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = if (index == 0) Icons.AutoMirrored.Rounded.VolumeUp else Icons.Rounded.Mic,
                                contentDescription = title
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        unselectedContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                }
            }
            
            AnimatedContent(
                targetState = selectedTab,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> if (targetState > initialState) fullWidth else -fullWidth }
                    ) + fadeIn() togetherWith slideOutHorizontally(
                        targetOffsetX = { fullWidth -> if (targetState > initialState) -fullWidth else fullWidth }
                    ) + fadeOut()
                }
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> TextToSpeechScreen(viewModel)
                    1 -> SpeechToTextScreen(
                        viewModel,
                        recordAudioPermissionState.status.isGranted
                    ) {
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Text to Speech",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { viewModel.inputText.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter text to convert to speech") },
                        minLines = 4,
                        maxLines = 8,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSpeaking) {
                            Button(
                                onClick = { viewModel.stopSpeaking() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop")
                                Spacer(Modifier.width(8.dp))
                                Text("Stop")
                            }
                        } else {
                            Button(
                                onClick = { viewModel.speak(viewModel.inputText.value) },
                                modifier = Modifier.weight(1f),
                                enabled = inputText.isNotBlank() && isTtsInitialized,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Speak")
                                Spacer(Modifier.width(8.dp))
                                Text("Speak")
                            }
                        }
                    }
                }
            }
        }

        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Language Selection
                    Text("Language", style = MaterialTheme.typography.titleMedium)
                    DropdownSelector(
                        selectedValue = selectedLanguage?.displayLanguage ?: "Select Language",
                        onClick = { showLanguageDialog = true },
                        enabled = availableLanguages.isNotEmpty()
                    )

                    // Voice Selection
                    Text("Voice", style = MaterialTheme.typography.titleMedium)
                    DropdownSelector(
                        selectedValue = selectedVoice?.name ?: "Select Voice",
                        onClick = { showVoiceDialog = true },
                        enabled = availableVoices.isNotEmpty()
                    )
                }
            }
        }
    }

    if (showLanguageDialog) {
        SelectionDialog(
            title = "Select Language",
            items = availableLanguages,
            onItemSelected = { lang ->
                viewModel.setLanguage(lang as Locale)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false },
            itemLabel = { (it as Locale).displayLanguage }
        )
    }

    if (showVoiceDialog) {
        SelectionDialog(
            title = "Select Voice",
            items = availableVoices,
            onItemSelected = { voice ->
                viewModel.setVoice(voice as Voice)
                showVoiceDialog = false
            },
            onDismiss = { showVoiceDialog = false },
            itemLabel = { (it as Voice).name }
        )
    }
}

@Composable
fun SpeechToTextScreen(
    viewModel: SpeechViewModel,
    hasPermission: Boolean,
    onPermissionRequest: () -> Unit
) {
    val transcribedText by viewModel.transcribedText
    val isListening by viewModel.isListening
    val isSttInitialized by viewModel.isSttInitialized
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (hasPermission) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Speech to Text",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (transcribedText.isEmpty() && !isListening) {
                            Text(
                                "Press the microphone to start speaking",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                        Text(
                            transcribedText,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .size(100.dp)
                    .scale(if (isListening) pulse else 1f)
                    .clip(CircleShape)
                    .background(if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary)
                    .clickable(enabled = isSttInitialized) {
                        if (isListening) {
                            viewModel.stopListening()
                        } else {
                            viewModel.startListening()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else {
            PermissionRequestUI(onPermissionRequest)
        }
    }
}

@Composable
fun PermissionRequestUI(onPermissionRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Warning,
            contentDescription = "Permission Required",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "This feature requires microphone permission to transcribe your speech. Please grant the permission to continue.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onPermissionRequest) {
            Text("Grant Permission")
        }
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
fun DropdownSelector(
    selectedValue: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(selectedValue, fontWeight = FontWeight.Medium)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
        }
    }
}

@Composable
fun <T> SelectionDialog(
    title: String,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    onDismiss: () -> Unit,
    itemLabel: @Composable (T) -> String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn {
                items(items) { item ->
                    Text(
                        text = itemLabel(item),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemSelected(item) }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}