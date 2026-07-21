package com.example.voicerelay.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.voicerelay.protocol.Packet;

public class Room {

    private final String name;
    private final Map<Integer, ConnectedClient> members = new ConcurrentHashMap<>();

    public Room(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void join(ConnectedClient client) {
        members.put(client.getSsrc(), client);
    }

    public void leave(int ssrc) {
        members.remove(ssrc);
    }

    public List<ConnectedClient> getMembers() {
        return new ArrayList<>(members.values());
    }

    public void broadcast(Packet packet, int senderSsrc) {

    }
}
