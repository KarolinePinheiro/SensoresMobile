package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
                    color = Color.Transparent
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
    
    // Cores Frutiger Aero: Azul vibrante e cristalino
    val aeroLightBlue = Color(0xFF00D4FF)
    val aeroDeepBlue = Color(0xFF0056B3)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(aeroLightBlue, aeroDeepBlue)
    )

    if (songIds.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush), 
            contentAlignment = Alignment.Center
        ) {
            Text("Nenhuma música encontrada", color = Color.White)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(24.dp)
    ) {
        // --- 1. SHUFFLE (Canto Superior Direito - Maior) ---
        IconButton(
            onClick = { /* viewModel.toggleShuffle() */ },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(64.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.shuffle),
                contentDescription = "Shuffle",
                tint = Color.White,
                modifier = Modifier.fillMaxSize()
            )
        }

        // --- 2. ELEMENTOS CENTRAIS (Navegação + Capa e Barra) ---
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Linha com Previous - Capa - Next
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Botão Anterior (Esquerdo)
                IconButton(
                    onClick = { /* viewModel.previous() */ },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.previous),
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Capa do Álbum com o Play/Pause por cima
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(200.dp) // Reduzi o tamanho para caber os botões nas laterais
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.album_cover),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Botão PLAY / PAUSE (Mais pequenino, sobre a capa)
                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isPlaying) R.drawable.pause else R.drawable.play
                            ),
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Botão Próximo (Direito)
                IconButton(
                    onClick = { /* viewModel.next() */ },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.next),
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Barra de Progresso (Debaixo da capa)
            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { viewModel.seekTo(it.toInt()) },
                valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
                modifier = Modifier.fillMaxWidth(0.85f),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.4f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatTime(currentPosition), color = Color.White, style = MaterialTheme.typography.bodySmall)
                Text(text = formatTime(totalDuration), color = Color.White, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

fun formatTime(milliseconds: Int): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
