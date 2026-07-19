package com.example.voicerelay.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HandshakeTest {

    @Test
    void computeAcceptMatchesTheRfc6455Vector() {
        assertEquals("s3pPLMBiTxaQ9kYGzzhZRbK+xOo=",
                Handshake.computeAccept("dGhlIHNhbXBsZSBub25jZQ=="));
    }
}
