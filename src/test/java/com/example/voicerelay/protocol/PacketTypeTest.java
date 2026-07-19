package com.example.voicerelay.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PacketTypeTest {

    @Test
    void codeRoundTrips() throws Exception {
        assertEquals(PacketType.AUDIO, PacketType.fromCode((byte) 0));
        assertEquals(PacketType.COMMAND, PacketType.fromCode((byte) 2));
    }

    @Test
    void unknownCodeIsRefused() {
        assertThrows(InvalidPacketException.class, () -> PacketType.fromCode((byte) 99));
    }
}
