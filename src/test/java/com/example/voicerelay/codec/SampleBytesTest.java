package com.example.voicerelay.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Lab: prove the 16-bit <-> two-byte (little-endian) arithmetic before relying on it. */
class SampleBytesTest {

    @Test
    void splitThenJoinRoundTrips() {
        short sample = 12345;                          // 0x3039
        byte low  = (byte) (sample & 0xFF);            // 0x39
        byte high = (byte) ((sample >> 8) & 0xFF);     // 0x30
        short rebuilt = (short) (((high & 0xFF) << 8) | (low & 0xFF));
        assertEquals(sample, rebuilt);
    }

    @Test
    void theLowByteMustBeReadUnsigned() {
        short sample = (short) 0x00C8;                 // 200: the low byte's top bit is set
        byte low = (byte) (sample & 0xFF);
        assertEquals(-56, low);                        // the signed trap: stored as -56...
        assertEquals(200, low & 0xFF);                 // ...defused by & 0xFF
    }
}
