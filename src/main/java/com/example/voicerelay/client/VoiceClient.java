package com.example.voicerelay.client;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;

import com.example.voicerelay.audio.AudioFrame;
import com.example.voicerelay.audio.AudioSettings;
import com.example.voicerelay.audio.Microphone;
import com.example.voicerelay.audio.Speaker;
import com.example.voicerelay.codec.Codec;
import com.example.voicerelay.codec.MuLawCodec;
import com.example.voicerelay.codec.PcmCodec;

/**
 * Entry point. Records 3s of mic, plays it back, and saves it to a .wav file.
 */
public class VoiceClient {

    private static final int SECONDS = 3;

    public static void main(String[] args) throws LineUnavailableException {
        int frameCount = SECONDS * 1000 / AudioSettings.MS_PER_FRAME;
        List<AudioFrame> frames = new ArrayList<>();

        System.out.println("Speak! Capturing for " + SECONDS + " seconds...");
        try (Microphone microphone = new Microphone()) {
            for (int sequence = 0; sequence < frameCount; sequence++) {
                frames.add(AudioFrame.forSequence(sequence, microphone.readFrame()));
            }
        }

        Codec pcm = new PcmCodec();
        Codec muLaw = new MuLawCodec();

        System.out.println("1/2 -- raw PCM (" + encodedSize(frames, pcm) + " bytes)...");
        playWith(frames, pcm);

        System.out.println("2/2 -- mu-law (" + encodedSize(frames, muLaw) + " bytes, half as much)...");
        playWith(frames, muLaw);
    }

    private static void playWith(List<AudioFrame> frames, Codec codec) throws LineUnavailableException {
        try (Speaker speaker = new Speaker()) {
            for (AudioFrame frame : frames) {
                byte[] encoded = codec.encode(frame.getPcm());
                byte[] restored = codec.decode(encoded);
                speaker.play(restored);
            }
        }
    }

    private static int encodedSize(List<AudioFrame> frames, Codec codec) {
        int total = 0;
        for (AudioFrame frame : frames) {
            total += codec.encode(frame.getPcm()).length;
        }
        return total;
    }

}
