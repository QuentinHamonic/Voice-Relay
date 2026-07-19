package com.example.voicerelay.bits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Lab: the five bit operators the whole project is built from — proven, and kept. */
class BitsTest {

    @Test
    void aMaskKeepsOnlyTheBitsYouWant() {
        assertEquals(0b0000_0110, 0b1011_0110 & 0x0F);
    }

    @Test
    void shiftingMovesAndScales() {
        assertEquals(8, 1 << 3);
        assertEquals(100, 200 >> 1);
    }

    @Test
    void orTurnsABitOn() {
        assertEquals(0b0010_0000, 0 | (1 << 5));
    }

    @Test
    void andTestsABit() {
        assertTrue((0b0100_0010 & (1 << 1)) != 0);
        assertFalse((0b0100_0010 & (1 << 3)) != 0);
    }

    @Test
    void notWithAndClearsABit() {
        assertEquals(0b1111_1110, 0b1111_1111 & ~(1 << 0));
    }

    @Test
    void theSignedByteTrapAndItsCure() {
        byte b = (byte) 200;
        assertEquals(-56, b);
        assertEquals(200, b & 0xFF);
    }
}
