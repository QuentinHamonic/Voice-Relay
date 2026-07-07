# Voice Relay

Encrypted voice relay chat by rooms.

The goal is to provide a relay server able to route audio streams between multiple clients grouped into rooms, with encryption of the exchanges.

> ⚠️ Work in progress, built step by step. The client captures the microphone, encodes it (µ-law), packetizes it, and sends it over TCP to a relay server that plays it back. WebSocket, multi-room relay, and encryption are not implemented yet.

## Requirements

- JDK 21
- Maven 3.8+

## Project structure

```
src/main/java/com/example/voicerelay/
  audio/            # Audio settings, Microphone (capture), Speaker (playback), AudioFrame
  codec/            # Codec interface, PcmCodec (passthrough), MuLawCodec (G.711, 2x compression)
  protocole/        # Packet, PacketType, InvalidPacketException (binary "home-made RTP")
  client/           # VoiceClient: captures the mic and sends packets over TCP
  server/           # RelayServer: receives packets and plays the voice
```

## Build and run

Two terminals — start the server first:

```bash
# Terminal 1 — the relay server (waits for a sender):
mvn -q compile exec:java -Dexec.mainClass=com.example.voicerelay.server.RelayServer

# Terminal 2 — the client (sends ~10s of mic over TCP):
mvn -q compile exec:java -Dexec.mainClass=com.example.voicerelay.client.VoiceClient
```

Speak into terminal 2; you hear yourself in terminal 1. To target another machine, pass its IP: `-Dexec.args="192.168.X.X"`.

## License

Distributed under the [MIT](LICENSE) license.
