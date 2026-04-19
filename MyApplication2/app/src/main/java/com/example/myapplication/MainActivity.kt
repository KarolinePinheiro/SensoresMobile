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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val currentSongTitle = viewModel.currentSongTitle
    
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

    // Definimos um espaçamento padrão para manter a consistência
    val standardSpacing = 32.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(24.dp)
    ) {
        // --- 1. SHUFFLE ---
        IconButton(
            onClick = { viewModel.playRandomSong() },
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

        // --- 2. ELEMENTOS CENTRAIS ---
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- AUMENTAR SOM (Acima do Álbum) ---
            IconButton(
                onClick = { viewModel.volumeUp() },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.volume_up),
                    contentDescription = "Volume Up",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(standardSpacing))

            // --- Nível da Capa e Navegação (Anterior | Capa | Próximo) ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Botão Anterior
                IconButton(
                    onClick = { viewModel.previous() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.previous),
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Capa do Álbum
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(220.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.album_cover),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )

                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isPlaying) R.drawable.pause else R.drawable.play
                            ),
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Botão Próximo
                IconButton(
                    onClick = { viewModel.next() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.next),
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(standardSpacing))

            // --- TÍTULO DA MÚSICA ---
            Text(
                text = currentSongTitle,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- PROGRESSO (SLIDER) ---
            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { viewModel.seekTo(it.toInt()) },
                valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.4f)
                )
            )

            // --- TIME STAMP ---
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatTime(currentPosition), color = Color.White, style = MaterialTheme.typography.bodySmall)
                Text(text = formatTime(totalDuration), color = Color.White, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(standardSpacing))

            // --- DIMINUIR SOM (Abaixo do Time Stamp e Centrado) ---
            IconButton(
                onClick = { viewModel.volumeDown() },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.volume_down),
                    contentDescription = "Volume Down",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

fun formatTime(milliseconds: Int): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
