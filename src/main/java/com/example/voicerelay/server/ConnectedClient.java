package com.example.voicerelay.server;

import java.io.IOException;
import java.net.Socket;

import com.example.voicerelay.protocol.Packet;
import com.example.voicerelay.transport.WebSocketConnection;

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

    }

}
