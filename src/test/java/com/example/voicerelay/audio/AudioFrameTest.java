package com.example.voicerelay.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AudioFrameTest {

    @Test
    void equalityIsByContentNotByIdentity() {
        AudioFrame a = AudioFrame.forSequence(0, new byte[] { 1, 2, 3 });
        AudioFrame b = AudioFrame.forSequence(0, new byte[] { 1, 2, 3 });
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentContentIsNotEqual() {
        AudioFrame a = AudioFrame.forSequence(0, new byte[] { 1, 2, 3 });
        AudioFrame b = AudioFrame.forSequence(0, new byte[] { 1, 2, 4 });
        assertNotEquals(a, b);
    }

    @Test
    void timestampCountsSamplesNotBytes() {
        assertEquals(0, AudioFrame.forSequence(0, new byte[320]).getTimestamp());
        assertEquals(160, AudioFrame.forSequence(1, new byte[320]).getTimestamp());
    }

    @Test
    void nullPcmIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new AudioFrame(0, 0, null));
    }
}
