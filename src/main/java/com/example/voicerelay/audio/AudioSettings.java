package com.example.voicerelay.audio;

import javax.sound.sampled.AudioFormat;

/** The one audio format shared by the whole project: 8kHz, 16-bit, mono. */
public class AudioSettings {

    public static final float SAMPLE_RATE = 8000f;

    public static final int BYTES_PER_SAMPLE = 2;

    public static final int MS_PER_FRAME = 20;

    public static final int SAMPLES_PER_FRAME = 160;

    public static final int BYTES_PER_FRAME = SAMPLES_PER_FRAME * BYTES_PER_SAMPLE;

    public static AudioFormat format() {
        return new AudioFormat(SAMPLE_RATE, BYTES_PER_SAMPLE * 8, 1, true, false);
    }

    private AudioSettings() {
    }
}
