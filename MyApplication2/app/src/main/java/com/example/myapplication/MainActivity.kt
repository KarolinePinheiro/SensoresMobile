package com.example.myapplication

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AudioPlayerScreen()
                }
            }
        }
    }
}

@Composable
fun AudioPlayerScreen() {
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.song1) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var totalDuration by remember { mutableIntStateOf(mediaPlayer.duration) }

    // Update current position while playing
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = mediaPlayer.currentPosition
            delay(1000)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Reproduzindo: song1",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Slider(
            value = currentPosition.toFloat(),
            onValueChange = {
                currentPosition = it.toInt()
                mediaPlayer.seekTo(it.toInt())
            },
            valueRange = 0f..totalDuration.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatTime(currentPosition))
            Text(text = formatTime(totalDuration))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = {
                mediaPlayer.start()
                isPlaying = true
            }) {
                Text("Play")
            }

            Button(onClick = {
                mediaPlayer.pause()
                isPlaying = false
            }) {
                Text("Pause")
            }

            Button(onClick = {
                mediaPlayer.stop()
                mediaPlayer.prepare() // Prepare for next play
                mediaPlayer.seekTo(0)
                currentPosition = 0
                isPlaying = false
            }) {
                Text("Stop")
            }
        }
    }
}

fun formatTime(milliseconds: Int): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
    return String.format("%d:%02d", minutes, seconds)
}
