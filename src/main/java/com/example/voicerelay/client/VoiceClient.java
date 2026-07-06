package com.example.voicerelay.client;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;

import com.example.voicerelay.audio.AudioFrame;
import com.example.voicerelay.audio.AudioSettings;
import com.example.voicerelay.audio.Microphone;
import com.example.voicerelay.audio.Speaker;

/**
 * Entry point. Records 3s of mic, plays it back, and saves it to a .wav file.
 */
public class VoiceClient {

    private static final int SECONDS = 3;

    public static void main(String[] args) throws LineUnavailableException {
        int frameCount = SECONDS * 1000 / AudioSettings.MS_PER_FRAME;
        List<AudioFrame> frames = new ArrayList<>();

        System.out.println("Capturing " + frameCount + " frames...");
        try (Microphone microphone = new Microphone()) {
            for (int sequence = 0; sequence < frameCount; sequence++) {
                frames.add(AudioFrame.forSequence(sequence, microphone.readFrame()));
            }
        }

        System.out.println("First frame: " + frames.get(0));
        System.out.println(frames.get(1));
        System.out.println("Replaying frame by frame...");
        try (Speaker speaker = new Speaker()) {
            for (AudioFrame frame : frames) {
                speaker.play(frame.getPcm());
            }
        }
    }

}
