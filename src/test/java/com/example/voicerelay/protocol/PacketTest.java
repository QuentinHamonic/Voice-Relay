package com.example.voicerelay.protocol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PacketTest {

    @Test
    void headerBytesMatchTheSpec() {
        Packet p = new Packet(PacketType.COMMAND, (byte) 0, (byte) 0,
                0x01020304, 0, 0, new byte[] { 'H', 'i' });
        byte[] bytes = p.toBytes();
        byte[] expected = {
            1,            // version
            2,            // type = COMMAND
            0,            // codec
            0,            // flags
            1, 2, 3, 4,   // ssrc = 0x01020304, big-endian
            0, 0, 0, 0,   // sequence
            0, 0, 0, 0,   // timestamp
            'H', 'i'      // payload
        };
        assertArrayEquals(expected, bytes);
    }

    @Test
    void roundTripReturnsAnEqualPacket() throws Exception {
        Packet original = Packet.text(1234, "hello");
        Packet reread = Packet.fromBytes(original.toBytes());
        assertEquals(original, reread);
        assertEquals("hello", reread.getTextMessage());
    }

    @Test
    void tooShortIsRefused() {
        assertThrows(InvalidPacketException.class, () -> Packet.fromBytes(new byte[] { 1, 2, 3 }));
    }

    @Test
    void wrongVersionIsRefused() {
        byte[] bytes = Packet.text(1, "x").toBytes();
        bytes[0] = 99;
        assertThrows(InvalidPacketException.class, () -> Packet.fromBytes(bytes));
    }
}
