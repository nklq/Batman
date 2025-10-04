package com.example.batman

import android.Manifest // Do obsługi uprawnień (chociaż teraz nie używamy, dobrze mieć)
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background // Do koloru tła
import androidx.compose.foundation.layout.* // Do Column, Row, Spacer, fillMaxSize, padding
import androidx.compose.foundation.shape.RoundedCornerShape // Do kształtu Card
import androidx.compose.material.icons.Icons // Do użycia ikon
import androidx.compose.material.icons.automirrored.filled.List // Do ikon na dole
import androidx.compose.material.icons.filled.Menu // Do ikon na górze i PowerButton (jako placeholder)
import androidx.compose.material3.* // Do Scaffold, Card, Text, Button, TopAppBar, Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue // Do obsługi stanu (by remember)
import androidx.compose.runtime.mutableStateOf // Do obsługi stanu
import androidx.compose.runtime.remember // Do obsługi stanu
import androidx.compose.runtime.setValue // Do obsługi stanu (by remember)
import androidx.compose.ui.Alignment // Do wyrównania
import androidx.compose.ui.Modifier // Do modyfikatorów
import androidx.compose.ui.graphics.Color // Do użycia kolorów
import androidx.compose.ui.text.font.FontWeight // Do czcionek
import androidx.compose.ui.tooling.preview.Preview // Do podglądu
import androidx.compose.ui.unit.dp // Do wymiarów
import androidx.compose.ui.unit.sp // Do wielkości czcionek
import com.example.batman.ui.theme.BatmanTheme // Twój pakiet motywu
import androidx.activity.enableEdgeToEdge

val PrimaryDarkBlue = Color(0xFF0F1A3B) // Tło
val CardBackground = Color(0xFF1E2746)  // Tło karty
val StatusActiveColor = Color(0xFF00C853) // Zielony
val StatusInactiveColor = Color.DarkGray   // Szary
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

@Composable
fun RecordingAppUIScheme() {
    var isRecording by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Naciśnij START, aby nagrywać co 5 sekund") }
    var currentTime by remember { mutableStateOf("00:00:00") }

    Scaffold( /* ... */ ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Wyśrodkuj zawartość na ekranie
        ) {

            // 1. GÓRNY PASEK STATUSU
            StatusDisplayCard(
                status = statusText,
                time = currentTime,
                isActive = isRecording
            )

            Spacer(modifier = Modifier.height(64.dp))

            // 2. DUŻY PRZYCISK WŁĄCZAJĄCY/WYŁĄCZAJĄCY
            PowerSwitchButton(
                isRecording = isRecording,
                onClick = {
                    // Logika przełączania stanu
                    isRecording = !isRecording
                    statusText = if (isRecording) "Nagrywanie (Co 5s)" else "Gotowy do STARTU"
                    // TUTAJ BĘDZIE POŁĄCZENIE Z LOGIKĄ OSOBY 2
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 3. DOLNY PRZYCISK POWIADOMIEŃ/REJESTRÓW
            NotificationLogButton(
                onClick = { /* Przejście do listy nagrań/powiadomień */ }
            )
        }
    }
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
            // Status (Connected / Nieaktywny)
            Text(
                text = status.uppercase(),
                color = if (isActive) StatusActiveColor else StatusInactiveColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Czas
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
        shape = androidx.compose.foundation.shape.CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRecording) Color.Red else StatusActiveColor // Czerwony dla STOP
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.size(160.dp) // Duża wielkość
    ) {
        // Użyj standardowej ikony "Power" lub podobnej, jeśli dostępna.
        // Na razie używamy Placeholdera z menu.
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
            // Ikona listy
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