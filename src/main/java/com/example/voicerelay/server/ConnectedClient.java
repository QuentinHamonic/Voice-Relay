package com.example.voicerelay.server;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

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
                if (packet.getSsrc() != ssrc) {
                    System.out.println(nickname + " annonced ssrc " + packet.getSsrc() + " instead of " + ssrc
                            + ": ignored (impersonation?)");
                    return;
                }
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
            case "JOIN":
                if (ssrc == 0) {
                    reply("ERROR say HELLO first");
                    return;
                }
                leaveRoom();
                room = server.getOrCreateRoom(arguments.isEmpty() ? "general" : arguments);
                List<ConnectedClient> existingMembers = room.getMembers();
                for (ConnectedClient member : existingMembers) {
                    reply("PRESENT " + member.getSsrc() + " " + member.getNickname());
                }
                room.join(this);
                reply("JOIN-OK " + room.getName() + " " + existingMembers.size());
                room.broadcast(Packet.command(SERVER_SSRC, "ARRIVED " + ssrc + " " + nickname), ssrc);
                System.out.println(nickname + " joined \"" + room.getName() + "\"");
                break;

            case "WHO":
                if (room == null) {
                    reply("ERROR join a romm first (JOIN)");
                    return;
                }
                StringBuilder list = new StringBuilder("LIST");
                for (ConnectedClient member : room.getMembers()) {
                    list.append(" ").append(member.getNickname());
                }
                reply(list.toString());
                break;

            case "QUIT":
                throw new IOException("QUIT requested");

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
            room.broadcast(Packet.command(SERVER_SSRC, "LEFT " + ssrc + " " + nickname), ssrc);
            room = null;
        }
    }

}
