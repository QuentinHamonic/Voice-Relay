package com.example.voicerelay.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MuLawCodecTest {

    @Test
    void silenceEncodesTo0xFF() {
        // The historical convention: silence (0) becomes 0xFF.
        assertEquals((byte) 0xFF, MuLawCodec.encodeSample((short) 0));
    }

    @Test
    void knownVector_300() {
        // Worked out by hand in the course; the standard says 300 -> 0xE4.
        assertEquals((byte) 0xE4, MuLawCodec.encodeSample((short) 300));
    }

    @Test
    void decodeIsCloseNotExact() {
        short back = MuLawCodec.decodeSample(MuLawCodec.encodeSample((short) 300));
        // 300 -> 0xE4 -> ~308: lossy, but the drift is small.
        assertEquals(300, back, 16);
    }

    @Test
    void silenceDecodesNearZero() {
        short back = MuLawCodec.decodeSample((byte) 0xFF);
        assertEquals(0, back, 8);
    }

    @Test
    void encodeHalvesTheLength() {
        assertEquals(160, new MuLawCodec().encode(new byte[320]).length);
    }

    @Test
    void frameRoundTripStaysClose() {
        byte[] pcm = new byte[320];
        for (int i = 0; i < 160; i++) {                 // a rising ramp
            short s = (short) (i * 100);
            pcm[2 * i] = (byte) (s & 0xFF);
            pcm[2 * i + 1] = (byte) ((s >> 8) & 0xFF);
        }
        MuLawCodec codec = new MuLawCodec();
        byte[] restored = codec.decode(codec.encode(pcm));

        assertEquals(pcm.length, restored.length);
        for (int i = 0; i < 160; i++) {
            short a = (short) (((pcm[2 * i + 1]) << 8) | (pcm[2 * i] & 0xFF));
            short b = (short) (((restored[2 * i + 1]) << 8) | (restored[2 * i] & 0xFF));
            int tolerance = Math.max(64, Math.abs(a) / 8);   // ~12% relative, the log scale
            assertEquals(a, b, tolerance, "sample " + i);
        }
    }
}
