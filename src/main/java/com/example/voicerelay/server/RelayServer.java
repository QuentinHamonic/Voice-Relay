package com.example.voicerelay.server;

import java.io.IOException;
import java.net.ServerSocket;
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

    }

    public void stop() throws IOException {
        serverSocket.close();
    }
}
