package com.example.myapplication;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    // Declare MediaPlayer for audio playback
    private MediaPlayer mediaPlayer;

    // Declare UI elements
    private SeekBar seekBar;
    private TextView textCurrentTime;
    private TextView textTotalTime;
    private ImageView buttonPlay;
    private ImageView buttonPause;
    private ImageView buttonStop;

    // Handler to update SeekBar and current time text every second
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Runnable task that updates SeekBar and current playback time
    private final Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                // Update SeekBar progress and current time text
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                textCurrentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));

                // Repeat this task every 1 second
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout for the activity
        setContentView(R.layout.activity_main);

        // Initialize views from layout
        seekBar = findViewById(R.id.seekBar);
        textCurrentTime = findViewById(R.id.textCurrentTime);
        textTotalTime = findViewById(R.id.textTotalTime);
        buttonPlay = findViewById(R.id.buttonPlay);
        buttonPause = findViewById(R.id.buttonPause);
        buttonStop = findViewById(R.id.buttonStop);

        // Create MediaPlayer instance with a raw audio resource
        mediaPlayer = MediaPlayer.create(this, R.raw.song1);

        // Set listener to configure SeekBar and total time after MediaPlayer is ready
        mediaPlayer.setOnPreparedListener(mp -> {
            seekBar.setMax(mp.getDuration());
            textTotalTime.setText(formatTime(mp.getDuration()));
        });

        // Play button starts the audio and begins updating UI
        buttonPlay.setOnClickListener(v -> {
            mediaPlayer.start();
            handler.post(updateSeekBar);
        });

        // Pause button pauses the audio playback
        buttonPause.setOnClickListener(v -> mediaPlayer.pause());

        // Stop button stops playback and resets UI and MediaPlayer
        buttonStop.setOnClickListener(v -> {
            mediaPlayer.stop();
            mediaPlayer = MediaPlayer.create(this, R.raw.song1);
            seekBar.setProgress(0);
            textCurrentTime.setText("0:00");
            textTotalTime.setText(formatTime(mediaPlayer.getDuration()));
        });

        // Listen for SeekBar user interaction
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            // Called when progress is changed
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    // Seek MediaPlayer to new position and update current time
                    mediaPlayer.seekTo(progress);
                    textCurrentTime.setText(formatTime(progress));
                }
            }

            // Not used, but required to override
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            // Not used, but required to override
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // Format milliseconds into minutes:seconds format (e.g., 1:05)
    private String formatTime(int milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    // Clean up MediaPlayer and handler when activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateSeekBar);
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}