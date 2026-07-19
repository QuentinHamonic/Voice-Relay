package com.example.voicerelay.codec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PcmCodecTest {

    @Test
    void idIsZero() {
        assertEquals(0, new PcmCodec().getId());
    }

    @Test
    void roundTripReturnsTheSameBytes() {
        byte[] pcm = { 1, 2, 3, 4, 5 };
        PcmCodec codec = new PcmCodec();
        assertArrayEquals(pcm, codec.decode(codec.encode(pcm)));
    }
}
