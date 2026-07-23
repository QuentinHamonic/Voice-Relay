package com.example.voicerelay.server;

import java.io.IOException;
import java.net.Socket;

import com.example.voicerelay.protocol.InvalidPacketException;
import com.example.voicerelay.protocol.Packet;
import com.example.voicerelay.transport.WebSocketConnection;
import com.example.voicerelay.transport.WebSocketFrame;

public class ConnectedClient implements Runnable {

    public static final int SERVER_SSRC = 0;

    private final Socket socket;
    private final RelayServer server;

    private WebSocketConnection connection;
    private int ssrc;
    private String nickname = "?";
    private Room room;

    public ConnectedClient(Socket socket, RelayServer server) {
        this.socket = socket;
        this.server = server;
    }

    public int getSsrc() {
        return ssrc;
    }

    public String getNickname() {
        return nickname;
    }

    public void send(Packet packet) throws IOException {
        connection.sendBinary(packet.toBytes());
    }

    public void run() {
        try {
            connection = WebSocketConnection.fromClient(socket);
            System.out.println("New client: " + connection.getRemoteAddress());

            while (true) {
                WebSocketFrame.Frame frame = connection.receive();
                try {
                    process(Packet.fromBytes(frame.getPayload()));
                } catch (InvalidPacketException rejected) {
                    System.out.println("Invalid packet from " + nickname + ": " + rejected.getMessage());
                }
            }
        } catch (IOException disconnected) {
            System.out.println(nickname + " left (" + disconnected.getMessage() + ")");
        } finally {
            leaveRoom();
            if (connection != null) {
                connection.close();
            }
        }
    }

    private void process(Packet packet) throws IOException {
        switch (packet.getType()) {
            case COMMAND:
                processCommand(packet.getTextMessage());
                break;
            case AUDIO:
            case TEXT:
                if (room != null) {
                    room.broadcast(packet, ssrc);
                }
                break;
        }
    }

    private void processCommand(String command) throws IOException {
        String[] parts = command.split(" ", 2);
        String verb = parts[0];
        String arguments = (parts.length > 1) ? parts[1] : "";

        switch (verb) {
            case "HELLO":
                nickname = arguments.isEmpty() ? "anonymous" : arguments;
                ssrc = server.assignSsrc();
                reply("WELCOME " + ssrc);
                System.out.println(nickname + " -> ssrc " + ssrc);
                break;
            default:
                reply("ERROR unknown command: " + verb);
        }
    }

    private void reply(String command) throws IOException {
        send(Packet.command(SERVER_SSRC, command));
    }

    private void leaveRoom() {
        if (room != null) {
            room.leave(ssrc);
            room = null;
        }
    }

}
