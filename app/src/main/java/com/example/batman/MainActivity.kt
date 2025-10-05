package com.example.batman

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.batman.ui.theme.BatmanTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

val PrimaryDarkBlue = Color(0xFF0F1A3B)
val CardBackground = Color(0xFF1E2746)
val StatusActiveColor = Color(0xFF00C853)
val StatusInactiveColor = Color.DarkGray

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BatmanTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RecordingAppUIScheme()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RecordingAppUIScheme() {
    var isRecording by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Naciśnij START, aby nagrywać co 5 sekund") }
    var currentTime by remember { mutableStateOf("00:00:00") }
    val context = LocalContext.current
    var showFileList by remember { mutableStateOf(false) }
    var fileList by remember { mutableStateOf(listOf<String>()) }

    val recordAudioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isActive) {
                val timestamp = System.currentTimeMillis()
                val fileName = "nagranie_${timestamp}.m4a"
                val file = File(context.filesDir, fileName)
                mediaRecorder = MediaRecorder(context).apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(file.absolutePath)
                    prepare()
                    start()
                }
                delay(5000)
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null

                coroutineScope.launch {
                    try {
                        val requestFile = file.asRequestBody("audio/m4a".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                        val timestampRequestBody = timestamp.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                        RetrofitClient.instance.uploadRecording(body, timestampRequestBody)
                        Log.d("FileUpload", "File uploaded successfully: ${file.name}")
                    } catch (e: Exception) {
                        Log.e("FileUpload", "Error uploading file: ${e.message}")
                    }
                }
            }
        } else {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
        }
    }

    if (showFileList) {
        FileListScreen(files = fileList, onDismiss = { showFileList = false })
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            StatusDisplayCard(
                status = statusText,
                time = currentTime,
                isActive = isRecording
            )

            Spacer(modifier = Modifier.height(64.dp))

            PowerSwitchButton(
                isRecording = isRecording,
                onClick = {
                    if (recordAudioPermission.status.isGranted) {
                        isRecording = !isRecording
                        statusText = if (isRecording) "Nagrywanie (Co 5s)" else "Gotowy do STARTU"
                    } else {
                        recordAudioPermission.launchPermissionRequest()
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            NotificationLogButton(
                onClick = {
                    fileList = context.filesDir.listFiles()
                        ?.map { it.name }
                        ?.sortedDescending()
                        ?: listOf()
                    showFileList = true
                }
            )
        }
    }
}

@Composable
fun FileListScreen(files: List<String>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zapisane pliki") },
        text = {
            Box(modifier = Modifier.height(300.dp)) {
                if (files.isEmpty()) {
                    Text("Brak zapisanych plików.")
                } else {
                    LazyColumn {
                        items(files) { fileName ->
                            Text(fileName, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
fun StatusDisplayCard(status: String, time: String, isActive: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = status.uppercase(),
                color = if (isActive) StatusActiveColor else StatusInactiveColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = time,
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
fun PowerSwitchButton(isRecording: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRecording) Color.Red else StatusActiveColor
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.size(160.dp)
    ) {
        Text(
            text = if (isRecording) "STOP" else "START",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun NotificationLogButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = "Powiadomienia",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "Powiadomienia o Dźwiękach", color = Color.White, fontSize = 16.sp)
        }
    }
}
