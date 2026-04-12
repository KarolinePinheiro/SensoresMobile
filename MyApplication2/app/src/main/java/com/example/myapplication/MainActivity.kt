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

    // BUSCA DINÂMICA: Lista os arquivos da pasta raw por ordem alfabética
    val songIds = remember {
        R.raw::class.java.fields
            .filter { it.type == Int::class.javaPrimitiveType }
            .sortedBy { it.name }
            .map { it.getInt(null) }
    }

    var currentSongIndex by remember { mutableIntStateOf(0) }

    // O MediaPlayer agora é recriado sempre que o índice da música muda
    val mediaPlayer = remember(currentSongIndex) {
        if (songIds.isNotEmpty()) {
            MediaPlayer.create(context, songIds[currentSongIndex])
        } else null
    }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var totalDuration by remember { mutableIntStateOf(mediaPlayer?.duration ?: 0) }

    val darkerGrey = Color(0xFFAAAAAA)

    if (mediaPlayer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhuma música encontrada na pasta raw")
        }
        return
    }

    // Lógica Unificada: Configura o player sempre que ele muda (troca de música)
    LaunchedEffect(mediaPlayer) {
        currentPosition = 0 // Reseta a barra para o início
        totalDuration = mediaPlayer.duration
        
        // Se o estado era "tocando", inicia a nova música automaticamente
        if (isPlaying) {
            mediaPlayer.start()
        }

        // Configura o que fazer quando a música terminar
        mediaPlayer.setOnCompletionListener {
            if (currentSongIndex < songIds.size - 1) {
                currentSongIndex++ // Vai para a próxima
            } else {
                currentSongIndex = 0 // Volta para a primeira (Loop)
            }
        }
    }

    // Loop de progresso: Agora depende de 'isPlaying' E do 'mediaPlayer' atual
    LaunchedEffect(isPlaying, mediaPlayer) {
        if (isPlaying) {
            while (true) {
                try {
                    currentPosition = mediaPlayer.currentPosition
                } catch (e: Exception) {
                    // Evita crash se o player for libertado enquanto o loop corre
                    break
                }
                delay(1000)
            }
        }
    }

    // Libertar memória ao fechar ou trocar de música
    DisposableEffect(mediaPlayer) {
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
        Image(
            painter = painterResource(id = R.drawable.album_cover),
            contentDescription = null,
            modifier = Modifier
                .size(250.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        Slider(
            value = currentPosition.toFloat(),
            onValueChange = {
                currentPosition = it.toInt()
                mediaPlayer.seekTo(it.toInt())
            },
            valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
            modifier = Modifier.width(280.dp),
            colors = SliderDefaults.colors(
                thumbColor = darkerGrey,
                activeTrackColor = darkerGrey,
                inactiveTrackColor = darkerGrey.copy(alpha = 0.3f)
            )
        )

        Row(
            modifier = Modifier.width(250.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatTime(currentPosition), color = Color.Black)
            Text(text = formatTime(totalDuration), color = Color.Black)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.width(250.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(40.dp))

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
                    contentDescription = null,
                    tint = darkerGrey,
                    modifier = Modifier.fillMaxSize()
                )
            }

            IconButton(
                onClick = {
                    mediaPlayer.pause()
                    mediaPlayer.seekTo(0)
                    currentPosition = 0
                    isPlaying = false
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.stop),
                    contentDescription = null,
                    tint = darkerGrey,
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
