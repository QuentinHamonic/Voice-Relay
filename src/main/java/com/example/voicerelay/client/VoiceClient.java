package com.example.voicerelay.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;

import com.example.voicerelay.audio.AudioSettings;
import com.example.voicerelay.audio.Microphone;
import com.example.voicerelay.audio.Speaker;

/**
 * Entry point. Records 3s of mic, plays it back, and saves it to a .wav file.
 */
public class VoiceClient {

    private static final int SECONDS = 3;

    public static void main(String[] args) throws LineUnavailableException, IOException {
        int totalBytes = (int) (AudioSettings.SAMPLE_RATE * AudioSettings.BYTES_PER_SAMPLE * SECONDS);
        byte[] recording = new byte[totalBytes];

        System.out.println("Recording : 3s...");
        try (Microphone microphone = new Microphone()) {
            int position = 0;
            while (position < totalBytes) {
                byte[] chunk = microphone.readFrame();
                int toCopy = Math.min(chunk.length, totalBytes - position);
                System.arraycopy(chunk, 0, recording, position, toCopy);
                position += toCopy;
            }
        }

        System.out.println("Listen...");
        try (Speaker speaker = new Speaker()) {
            speaker.play(recording);
        }

        File file = new File("record.wav");
        long sampleCount = recording.length / AudioSettings.BYTES_PER_SAMPLE;
        try (AudioInputStream stream = new AudioInputStream(
                new ByteArrayInputStream(recording), AudioSettings.format(), sampleCount)) {
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, file);
        }
        System.out.println("Saved: " + file.getAbsolutePath());
    }

}
