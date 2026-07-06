package com.example.voicerelay.audio;

import java.util.Arrays;
import java.util.Objects;

/** An immutable 20ms audio frame: sequence number, timestamp, and PCM payload. */
public class AudioFrame {

    private final int sequence;
    private final int timestamp;
    private final byte[] pcm;

    public AudioFrame(int sequence, int timestamp, byte[] pcm) {
        if (pcm == null) {
            throw new IllegalArgumentException("An audio frame's PCM cannot be null");
        }
        this.sequence = sequence;
        this.timestamp = timestamp;
        this.pcm = pcm;
    }

    public static AudioFrame forSequence(int sequence, byte[] pcm) {
        return new AudioFrame(sequence, sequence * AudioSettings.SAMPLES_PER_FRAME, pcm); // timestamp counts samples, not bytes
    }

    public int getSequence() {
        return sequence;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public byte[] getPcm() {
        return pcm;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequence, timestamp, Arrays.hashCode(pcm));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AudioFrame)) {
            return false;
        }
        AudioFrame that = (AudioFrame) o;
        return sequence == that.sequence
                && timestamp == that.timestamp
                && Arrays.equals(pcm, that.pcm); // pcm is an array: compare content, not identity
    }

    @Override
    public String toString() {
        return "AudioFrame{seq=" + sequence + ", ts=" + timestamp + ", " + pcm.length + " octets}";
    }

}
