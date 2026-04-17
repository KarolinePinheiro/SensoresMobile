package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val viewModel: AudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    AudioPlayerScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun AudioPlayerScreen(viewModel: AudioViewModel) {
    val isPlaying = viewModel.isPlaying
    val currentPosition = viewModel.currentPosition
    val totalDuration = viewModel.totalDuration
    val songIds = viewModel.songIds
    val darkerGrey = Color(0xFFAAAAAA)

    if (songIds.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhuma música encontrada na pasta raw", color = Color.Black)
        }
        return
    }

    // Container Principal
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // --- 1. ELEMENTOS CENTRAIS E BOTÕES DE ÓRBITA ---

        // Capa do Álbum
        Image(
            painter = painterResource(id = R.drawable.album_cover),
            contentDescription = null,
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        // Botão PLAY / PAUSE (Topo)
        IconButton(
            onClick = { viewModel.togglePlayPause() },
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-160).dp)
                .size(56.dp)
        ) {
            Icon(
                painter = painterResource(
                    id = if (isPlaying) R.drawable.pause else R.drawable.play
                ),
                contentDescription = "Play/Pause",
                tint = Color.Unspecified,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Botão ANTERIOR (Esquerda)
        IconButton(
            onClick = { /* viewModel.previous() */ },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.previous),
                contentDescription = "Previous",
                tint = Color.Unspecified,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Botão PRÓXIMO (Direita)
        IconButton(
            onClick = { /* viewModel.next() */ },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.next),
                contentDescription = "Next",
                tint = Color.Unspecified,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Botão STOP (Baixo - logo abaixo da imagem)
        IconButton(
            onClick = { viewModel.stop() },
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 160.dp)
                .size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.stop),
                contentDescription = "Stop",
                tint = Color.Unspecified,
                modifier = Modifier.fillMaxSize()
            )
        }

        // --- 2. BARRA DE PROGRESSO E TIMERS (PARTE INFERIOR) ---

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp), // Espaço para não colar na borda
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { viewModel.seekTo(it.toInt()) },
                valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
                modifier = Modifier.fillMaxWidth(0.85f),
                colors = SliderDefaults.colors(
                    thumbColor = darkerGrey,
                    activeTrackColor = darkerGrey,
                    inactiveTrackColor = darkerGrey.copy(alpha = 0.3f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatTime(currentPosition), color = Color.Black, style = MaterialTheme.typography.bodySmall)
                Text(text = formatTime(totalDuration), color = Color.Black, style = MaterialTheme.typography.bodySmall)
            }
        }

        // --- 3. BOTÕES AUXILIARES (CANTOS) ---

        // SHUFFLE (Canto Inferior Direito)
        IconButton(
            onClick = { /* viewModel.toggleShuffle() */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(32.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.shuffle),
                contentDescription = "Shuffle",
                tint = darkerGrey
            )
        }
    }
}

fun formatTime(milliseconds: Int): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}