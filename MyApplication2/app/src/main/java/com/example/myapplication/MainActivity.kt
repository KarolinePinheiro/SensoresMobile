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
    val currentSongImage = viewModel.currentSongImage
    val shuffleButtonColor = viewModel.shuffleButtonColor
    val sensorsIcon = viewModel.currentSensorsIcon
    
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
            .padding(16.dp)
    ) {
        // --- BOTÕES DE TOPO ---
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botão de Sensores (Lado Esquerdo)
            IconButton(
                onClick = { viewModel.toggleSensors() },
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    painter = painterResource(id = sensorsIcon),
                    contentDescription = "Toggle Sensors",
                    tint = Color.Unspecified, // Mantém as cores originais do XML se houver
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Botão Shuffle (Lado Direito)
            IconButton(
                onClick = { viewModel.playRandomSong() },
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.shuffle),
                    contentDescription = "Shuffle",
                    tint = shuffleButtonColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- ÁREA DA CRUZ ---
            Box(
                modifier = Modifier.size(320.dp),
                contentAlignment = Alignment.Center
            ) {
                // CAPA DO ÁLBUM
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                    Image(
                        painter = painterResource(id = currentSongImage),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                    // Botão Play central
                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier.size(50.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = if (isPlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // VOLUME UP (Topo)
                IconButton(
                    onClick = { viewModel.volumeUp() },
                    modifier = Modifier.align(Alignment.TopCenter).size(64.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.volume_up),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // VOLUME DOWN (Baixo)
                IconButton(
                    onClick = { viewModel.volumeDown() },
                    modifier = Modifier.align(Alignment.BottomCenter).size(64.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.volume_down),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // PREVIOUS (Esquerda)
                IconButton(
                    onClick = { viewModel.previous() },
                    modifier = Modifier.align(Alignment.CenterStart).size(70.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.previous),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // NEXT (Direita)
                IconButton(
                    onClick = { viewModel.next() },
                    modifier = Modifier.align(Alignment.CenterEnd).size(64.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.next),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // INFO E SLIDER
            Text(text = currentSongTitle, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { viewModel.seekTo(it.toInt()) },
                valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
                modifier = Modifier.fillMaxWidth(0.85f),
                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White)
            )

            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatTime(currentPosition), color = Color.White, fontSize = 12.sp)
                Text(text = formatTime(totalDuration), color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

fun formatTime(milliseconds: Int): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
