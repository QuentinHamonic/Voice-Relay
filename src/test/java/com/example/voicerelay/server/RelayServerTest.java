package com.example.voicerelay.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.example.voicerelay.protocol.Packet;
import com.example.voicerelay.transport.WebSocketConnection;

class RelayServerTest {

    @Test
    void helloIsAnsweredWithWelcomeAndAnSsrc() throws Exception {
        RelayServer server = new RelayServer(0);
        int port = server.getPort();
        Thread door = new Thread(server::start);
        door.setDaemon(true);
        door.start();

        try (WebSocketConnection c = WebSocketConnection.toServer("localhost", port)) {
            c.sendBinary(Packet.command(0, "HELLO alice").toBytes());
            Packet reply = Packet.fromBytes(c.receive().getPayload());
            String[] parts = reply.getTextMessage().split(" ");
            assertEquals("WELCOME", parts[0]);
            assertTrue(Integer.parseInt(parts[1]) >= 1000);
        } finally {
            server.stop();
        }
    }

}
