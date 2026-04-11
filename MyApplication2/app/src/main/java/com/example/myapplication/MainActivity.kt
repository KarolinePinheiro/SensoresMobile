package com.example.myapplication

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
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

    // Cor cinza escuro do XML (#FFAAAAAA)
    val darkerGrey = Color(0xFFAAAAAA)

    // Atualiza a posição atual enquanto toca
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
        // 1. Imagem do Álbum (250dp x 250dp como no XML)
        Image(
            painter = painterResource(id = R.drawable.album_cover),
            contentDescription = "Capa do Álbum",
            modifier = Modifier
                .size(250.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Slider (SeekBar do XML)
        Slider(
            value = currentPosition.toFloat(),
            onValueChange = {
                currentPosition = it.toInt()
                mediaPlayer.seekTo(it.toInt())
            },
            valueRange = 0f..totalDuration.toFloat(),
            modifier = Modifier.width(280.dp),
            colors = SliderDefaults.colors(
                thumbColor = darkerGrey,
                activeTrackColor = darkerGrey,
                inactiveTrackColor = darkerGrey.copy(alpha = 0.3f)
            )
        )

        // 3. Tempos (LinearLayout horizontal do XML)
        Row(
            modifier = Modifier.width(250.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatTime(currentPosition), color = Color.Black)
            Text(text = formatTime(totalDuration), color = Color.Black)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 4. Botões de Controle (Play/Pause unificados e Stop)
        Row(
            modifier = Modifier.width(250.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Espaçador para manter o Play/Pause no centro
            Spacer(modifier = Modifier.width(40.dp))

            // Botão Mágico: Play/Pause (Alterna ícone e função)
            IconButton(
                onClick = {
                    if (isPlaying) {
                        mediaPlayer.pause()
                    } else {
                        mediaPlayer.start()
                    }
                    isPlaying = !isPlaying
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isPlaying) R.drawable.pause else R.drawable.play
                    ),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = darkerGrey,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Botão Stop
            IconButton(
                onClick = {
                    mediaPlayer.stop()
                    mediaPlayer.prepare()
                    mediaPlayer.seekTo(0)
                    currentPosition = 0
                    isPlaying = false
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.stop),
                    contentDescription = "Stop",
                    tint = darkerGrey,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// Função de formatar tempo corrigida para evitar avisos de Locale
fun formatTime(milliseconds: Int): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
