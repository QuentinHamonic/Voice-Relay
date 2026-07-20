package com.example.voicerelay.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CodecRegistryTest {

    @Test
    void pickTheRightCodec() {
        assertEquals(PcmCodec.ID, CodecRegistry.byId(PcmCodec.ID).getId());
        assertEquals(MuLawCodec.ID, CodecRegistry.byId(MuLawCodec.ID).getId());
    }

    @Test
    void unknownIdIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> CodecRegistry.byId((byte) 99));
    }

}
