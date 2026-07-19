package com.example.voicerelay.client;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import com.example.voicerelay.audio.AudioSettings;
import com.example.voicerelay.audio.Microphone;

import com.example.voicerelay.codec.Codec;
import com.example.voicerelay.codec.MuLawCodec;

import com.example.voicerelay.protocol.Packet;
import com.example.voicerelay.server.RelayServer;
import com.example.voicerelay.transport.WebSocketConnection;

/** Voice relay client. Captures the mic and sends packets over WebSocket. */
public class VoiceClient {

    private static final int SECONDS = 10;

    public static void main(String[] args) throws IOException, LineUnavailableException {
        String host = (args.length > 0) ? args[0] : "localhost";
        Codec codec = new MuLawCodec();
        int ssrc = 1;

        System.out.println("Connecting to " + host + " -- speak for " + SECONDS + " seconds...");
        try (WebSocketConnection connection = WebSocketConnection.toServer(host, RelayServer.PORT);
                Microphone microphone = new Microphone()) {

            int frameCount = SECONDS * 1000 / AudioSettings.MS_PER_FRAME;
            for (int sequence = 0; sequence < frameCount; sequence++) {
                byte[] pcm = microphone.readFrame();
                byte[] encoded = codec.encode(pcm);
                Packet packet = Packet.audio(ssrc, sequence,
                        sequence * AudioSettings.SAMPLES_PER_FRAME,
                        codec.getId(), encoded);
                connection.sendBinary(packet.toBytes());
            }
        }
        System.out.println("Transmission complete.");
    }

}
