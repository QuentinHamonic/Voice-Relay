package com.example.voicerelay.client;

import java.io.IOException;
import java.util.Scanner;

import javax.sound.sampled.LineUnavailableException;

import com.example.voicerelay.audio.AudioSettings;
import com.example.voicerelay.audio.Microphone;
import com.example.voicerelay.audio.Speaker;
import com.example.voicerelay.codec.Codec;
import com.example.voicerelay.codec.CodecRegistry;
import com.example.voicerelay.codec.MuLawCodec;
import com.example.voicerelay.protocol.InvalidPacketException;
import com.example.voicerelay.protocol.Packet;
import com.example.voicerelay.server.RelayServer;
import com.example.voicerelay.transport.WebSocketConnection;

/** Voice relay client. Captures the mic and sends packets over WebSocket. */
public class VoiceClient {

    private final String host;
    private final int port;
    private final String nickname;
    private final String roomName;

    private final Codec codec = new MuLawCodec();
    private WebSocketConnection connection;
    private int ssrc;
    private int sequence;

    private volatile boolean running = true;
    private volatile boolean muted = false;

    public VoiceClient(String host, int port, String nickname, String roomName) {
        this.host = host;
        this.port = port;
        this.nickname = nickname;
        this.roomName = roomName;
    }

    public static void main(String[] args) throws IOException {
        String host = (args.length > 0) ? args[0] : "localhost";
        String nickname = (args.length > 1) ? args[1] : "anonymous" + (int) (Math.random() * 1000);
        String room = (args.length > 2) ? args[2] : "general";
        new VoiceClient(host, RelayServer.DEFAULT_PORT, nickname, room).start();
    }

    public void start() throws IOException {
        System.out.println("Connecting to " + host + ":" + port + "...");
        connection = WebSocketConnection.toServer(host, port);

        send(Packet.command(0, "HELLO " + nickname));
        String[] parts = receivePacket().getTextMessage().split(" ");
        if (!parts[0].equals("WELCOME")) {
            throw new IOException("Unexpected server response: " + String.join(" ", parts));
        }
        ssrc = Integer.parseInt(parts[1]);
        System.out.println("Welcome! Your id (ssrc): " + ssrc);

        send(Packet.command(ssrc, "JOIN " + roomName));
        System.out.println("Room \"" + roomName + "\". Talk! (/who /mute /quit, or just type to chat)");

        new Thread(this::receptionLoop).start();
        new Thread(this::captureLoop).start();
        consoleLoop();
    }

    private void captureLoop() {
        try (Microphone microphone = new Microphone()) {
            while (running) {
                byte[] pcm = microphone.readFrame();
                if (muted) {
                    sequence++;
                    continue;
                }
                byte[] encoded = codec.encode(pcm);
                send(Packet.audio(ssrc, sequence,
                        sequence * AudioSettings.SAMPLES_PER_FRAME, codec.getId(), encoded));
                sequence++;
            }
        } catch (LineUnavailableException noMicrophone) {
            System.out.println("No microphone available -- listen-only mode");
        } catch (IOException networkDown) {
            stop("cannot send: " + networkDown.getMessage());
        }
    }

    private void receptionLoop() {
        try (Speaker speaker = new Speaker()) {
            while (running) {
                Packet packet = receivePacket();
                switch (packet.getType()) {
                    case AUDIO:
                        speaker.play(CodecRegistry.byId(packet.getCodec())
                                .decode(packet.getPayload()));
                        break;

                    case TEXT:
                        System.out.println("[" + packet.getSsrc() + "] " + packet.getTextMessage());
                        break;

                    case COMMAND:
                        displayCommand(packet.getTextMessage());
                        break;
                }
            }
        } catch (LineUnavailableException noOutput) {
            stop("no audio output: " + noOutput.getMessage());
        } catch (IOException ended) {
            stop(ended.getMessage());
        }
    }

    private void displayCommand(String command) {
        String[] parts = command.split(" ");
        switch (parts[0]) {
            case "ARRIVED":
                System.out.println(parts[2] + " joined the room");
                break;
            case "PRESENT":
                System.out.println(parts[2] + " is already here");
                break;
            case "LEFT":
                System.out.println(parts[2] + " left");
                break;
            case "LIST":
                System.out.println("In the room:" + command.substring("LIST".length()));
                break;
            default:
                System.out.println("Server: " + command);
        }
    }

    private void consoleLoop() {
        Scanner keyboard = new Scanner(System.in);
        while (running && keyboard.hasNextLine()) {
            String line = keyboard.nextLine().trim();
            try {
                if (line.equals("/quit")) {
                    send(Packet.command(ssrc, "QUIT"));
                    stop("goodbye!");

                } else if (line.equals("/who")) {
                    send(Packet.command(ssrc, "WHO"));

                } else if (line.equals("/mute")) {
                    muted = !muted;
                    System.out.println(muted ? "Microphone muted (/mute to resume)" : "Microphone open");

                } else if (!line.isEmpty()) {
                    send(Packet.text(ssrc, line));

                }
            } catch (IOException networkDown) {
                stop("cannot send: " + networkDown.getMessage());
            }
        }
    }

    private void send(Packet packet) throws IOException {
        connection.sendBinary(packet.toBytes());
    }

    private Packet receivePacket() throws IOException {
        while (true) {
            try {
                return Packet.fromBytes(connection.receive().getPayload());
            } catch (InvalidPacketException rejected) {
                System.out.println("Invalid packet received: " + rejected.getMessage());
            }
        }
    }

    private void stop(String reason) {
        if (running) {
            running = false;
            System.out.println("Disconnected: " + reason);
            connection.close();
        }
    }

}
