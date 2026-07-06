package com.example.voicerelay.client;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;

import com.example.voicerelay.audio.AudioFrame;
import com.example.voicerelay.audio.AudioSettings;
import com.example.voicerelay.audio.Microphone;
import com.example.voicerelay.audio.Speaker;
import com.example.voicerelay.codec.Codec;
import com.example.voicerelay.codec.MuLawCodec;
import com.example.voicerelay.codec.PcmCodec;
import com.example.voicerelay.protocole.InvalidPacketException;
import com.example.voicerelay.protocole.Packet;

/**
 * Voice relay client.
 */
public class VoiceClient {

    public static void main(String[] args) throws InvalidPacketException {
        Packet packet = Packet.text(1234, "Salut !");
        byte[] bytes = packet.toBytes();

        System.out.println("Packet     : " + packet);
        System.out.println("On the wire: " + toHex(bytes));
        System.out.println("             └─ ver│type│codec│flags│──ssrc───│──seq────│──ts─────│payload...");

        Packet reread = Packet.fromBytes(bytes);
        System.out.println("Reread     : " + reread + " -> \"" + reread.getTextMessage() + "\"");
        System.out.println(packet.equals(reread) ? "Round-trip OK." : "PROBLEM!");
    }

    static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02x", bytes[i]));
            if (i == 0 || i == 1 || i == 2 || i == 3 || i == 7 || i == 11 || i == 15) {
                sb.append('|');
            }
        }
        return sb.toString();
    }

}
