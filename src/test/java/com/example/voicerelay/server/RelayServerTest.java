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

    @Test
    void aSecondClientIsAnnouncedToTheRoom() throws Exception {
        RelayServer server = new RelayServer(0);
        int port = server.getPort();
        Thread door = new Thread(server::start);
        door.setDaemon(true);
        door.start();

        try (WebSocketConnection alice = WebSocketConnection.toServer("localhost", port)) {
            handshakeAndJoin(alice, "alice");

            try (WebSocketConnection bob = WebSocketConnection.toServer("localhost", port)) {
                handshakeAndJoin(bob, "bob");

                String arrived = nextCommandContaining(alice, "ARRIVED");
                assertTrue(arrived.contains("bob"), "alice should see bob arrive, got: " + arrived);
            }
        } finally {
            server.stop();
        }

    }

    private static void handshakeAndJoin(WebSocketConnection c, String name) throws Exception {
        c.sendBinary(Packet.command(0, "HELLO " + name).toBytes());
        Packet welcome = Packet.fromBytes(c.receive().getPayload());
        int ssrc = Integer.parseInt(welcome.getTextMessage().split(" ")[1]);
        c.sendBinary(Packet.command(ssrc, "JOIN general").toBytes());
        nextCommandContaining(c, "JOIN-OK"); // wait for the server to confirm membership
    }

    private static String nextCommandContaining(WebSocketConnection c, String needle) throws Exception {
        for (int i = 0; i < 10; i++) {
            String text = Packet.fromBytes(c.receive().getPayload()).getTextMessage();
            if (text.contains(needle)) {
                return text;
            }
        }
        throw new AssertionError("never received a command containing " + needle);
    }

}
