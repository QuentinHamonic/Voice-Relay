package com.example.voicerelay.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

/** Lab: the three length encodings (7 / 16 / 64 bits), including the >>32 long trap. */
class WebSocketFrameLengthTest {

    private static byte[] header(int payloadLength) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WebSocketFrame.write(out, WebSocketFrame.OPCODE_BINARY, new byte[payloadLength], false);
        return out.toByteArray();
    }

    @Test
    void smallLengthFitsInSevenBits() throws Exception {
        byte[] f = header(100);
        assertEquals(100, f[1] & 0x7F);
    }

    @Test
    void mediumLengthUses126PlusTwoBytes() throws Exception {
        byte[] f = header(200);
        assertEquals(126, f[1] & 0x7F);
        assertEquals(0, f[2] & 0xFF);
        assertEquals(200, f[3] & 0xFF);
    }

    @Test
    void hugeLengthUses127PlusEightBytes() throws Exception {
        byte[] f = header(70000);
        assertEquals(127, f[1] & 0x7F);
        assertEquals(1, f[7] & 0xFF);
        assertEquals(0x11, f[8] & 0xFF);
        assertEquals(0x70, f[9] & 0xFF);
    }
}
