package com.example.voicerelay.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import javax.sound.sampled.LineUnavailableException;

import com.example.voicerelay.audio.Speaker;
import com.example.voicerelay.codec.Codec;
import com.example.voicerelay.codec.MuLawCodec;
import com.example.voicerelay.protocol.InvalidPacketException;
import com.example.voicerelay.protocol.Packet;
import com.example.voicerelay.transport.WebSocketConnection;
import com.example.voicerelay.transport.WebSocketFrame;

/** Minimal relay: accepts one WebSocket client and plays its voice (echoes text). */
public class RelayServer {

    public static final int PORT = 9600;

    public static void main(String[] args) throws IOException, LineUnavailableException {
        Codec codec = new MuLawCodec();

        System.out.println("WebSocket relay server on ws://localhost:" + PORT + "/radio");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            Socket socket = serverSocket.accept();
            try (WebSocketConnection connection = WebSocketConnection.fromClient(socket);
                    Speaker speaker = new Speaker()) {
                System.out.println("Connected: " + connection.getRemoteAddress());
                while (true) {
                    WebSocketFrame.Frame frame = connection.receive();
                    if (frame.getOpcode() == WebSocketFrame.OPCODE_TEXT) {
                        String text = new String(frame.getPayload(), StandardCharsets.UTF_8);
                        System.out.println("   received (text): " + text);
                        connection.sendText(text);
                    } else {
                        try {
                            Packet packet = Packet.fromBytes(frame.getPayload());
                            speaker.play(codec.decode(packet.getPayload()));
                        } catch (InvalidPacketException rejected) {
                            System.out.println("   invalid packet: " + rejected.getMessage());
                        }
                    }
                }
            }
        } catch (IOException endOfStream) {
            System.out.println("Client disconnected.");
        }
    }
}
