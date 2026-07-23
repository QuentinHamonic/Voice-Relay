package com.example.voicerelay.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Minimal relay: accepts one WebSocket client and plays its voice (echoes
 * text).
 */
public class RelayServer {

    public static final int DEFAULT_PORT = 9600;

    private final ServerSocket serverSocket;
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    private final AtomicInteger nextSsrc = new AtomicInteger(1000);

    public RelayServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    public static void main(String[] args) throws IOException {
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        RelayServer server = new RelayServer(port);
        System.out.println("Voice Relay -- relay on ws://0.0.0.0" + server.getPort() + "/radio");
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    int assignSsrc() {
        return nextSsrc.getAndIncrement();
    }

    synchronized Room getOrCreateRoom(String name) {
        return rooms.computeIfAbsent(name, Room::new);
    }

    public void start() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                new Thread(new ConnectedClient(socket, this)).start();
            } catch (IOException error) {
                if (serverSocket.isClosed()) {
                    return;
                }
                System.out.println("Connection failed" + error.getMessage());

            }
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }
}
