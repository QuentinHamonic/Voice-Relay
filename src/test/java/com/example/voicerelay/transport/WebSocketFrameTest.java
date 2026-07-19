package com.example.voicerelay.transport;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.example.voicerelay.transport.WebSocketFrame;

public class WebSocketFrameTest {

    @Test
    void allerRetourRedonneLaCharge() throws Exception {
        byte[] charge = "coucou".getBytes(StandardCharsets.UTF_8);
        var out = new java.io.ByteArrayOutputStream();
        WebSocketFrame.write(out, WebSocketFrame.OPCODE_BINARY, charge, true);
        var in = new java.io.ByteArrayInputStream(out.toByteArray());
        WebSocketFrame.Frame relue = WebSocketFrame.read(in);
        assertArrayEquals(charge, relue.getPayload());
        assertEquals(WebSocketFrame.OPCODE_BINARY, relue.getOpcode());
    }

    @Test
    void charge200_testeLaLongueurEtendue() throws Exception {
        // 200 > 125 → force l'encodage de longueur sur "126 + 2 octets" (ton TP
        // FrameLengthDrills !)
        byte[] charge = new byte[200];
        var out = new java.io.ByteArrayOutputStream();
        WebSocketFrame.write(out, WebSocketFrame.OPCODE_BINARY, charge, true);
        var in = new java.io.ByteArrayInputStream(out.toByteArray());
        assertArrayEquals(charge, WebSocketFrame.read(in).getPayload());
    }

    @Test
    void chargeVide_tientAussi() throws Exception {
        // cas limite : 0 octet. Beaucoup de bugs se cachent dans le "rien".
        byte[] charge = new byte[0];
        var out = new java.io.ByteArrayOutputStream();
        WebSocketFrame.write(out, WebSocketFrame.OPCODE_BINARY, charge, true);
        var in = new java.io.ByteArrayInputStream(out.toByteArray());
        assertArrayEquals(charge, WebSocketFrame.read(in).getPayload());
    }

}
