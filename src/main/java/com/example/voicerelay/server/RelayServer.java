package com.example.voicerelay.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.sound.sampled.LineUnavailableException;

import com.example.voicerelay.audio.Speaker;
import com.example.voicerelay.codec.Codec;
import com.example.voicerelay.codec.MuLawCodec;
import com.example.voicerelay.protocole.InvalidPacketException;
import com.example.voicerelay.protocole.Packet;

/** Minimal relay: accepts one TCP sender and plays its voice. */
public class RelayServer {
    public static final int PORT = 9600;

    public static void main(String[] args) throws IOException, LineUnavailableException, InvalidPacketException {
        Codec codec = new MuLawCodec();

        System.out.println("Waiting for a sender on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT);
                Socket socket = serverSocket.accept(); // blocks until a sender connects
                DataInputStream input = new DataInputStream(socket.getInputStream());
                Speaker speaker = new Speaker()) {
            System.out.println("Connected: " + socket.getRemoteSocketAddress());
            while (true) {
                // Framing: TCP is a stream with no message boundaries, so we read
                // the length first, then exactly that many bytes.
                int size;
                try {
                    size = input.readInt();
                } catch (IOException endOfStream) {
                    break; // sender hung up: normal end
                }
                byte[] bytes = new byte[size];
                input.readFully(bytes); // readFully waits for ALL bytes (unlike read)
                Packet packet = Packet.fromBytes(bytes);
                speaker.play(codec.decode(packet.getPayload()));
            }
            System.out.println("Sender disconnected.");
        }
    }
}
