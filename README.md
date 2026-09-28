# Voice Relay

Voice relay chat by rooms.

The goal is to provide a relay server able to route audio streams between multiple clients grouped into rooms, with encryption of the exchanges.

**Current state:** multi-room relay working end to end over WebSocket (voice + text + room commands). Encryption is not wired yet.

## Requirements

- JDK 21
- Maven 3.8+

## Project structure

```
src/main/java/com/example/voicerelay/
  audio/            # AudioSettings, Microphone (capture), Speaker (playback), AudioFrame
  codec/            # Codec interface, PcmCodec (passthrough), MuLawCodec (G.711, 2x), CodecRegistry
  protocol/         # Packet, PacketType, InvalidPacketException (binary "home-made RTP")
  transport/        # Handshake, WebSocketFrame, WebSocketConnection (WebSocket from scratch)
  server/           # RelayServer (accept loop), ConnectedClient (one thread per client), Room (broadcast)
  client/           # VoiceClient: capture, receive and console threads
```

## Build and run

Three terminals — start the server first:

```powershell
# Terminal 1 — the relay server:
mvn -q compile exec:java "-Dexec.mainClass=com.example.voicerelay.server.RelayServer"

# Terminal 2 — alice:
mvn -q compile exec:java "-Dexec.mainClass=com.example.voicerelay.client.VoiceClient" "-Dexec.args=localhost alice general"

# Terminal 3 — bob:
mvn -q compile exec:java "-Dexec.mainClass=com.example.voicerelay.client.VoiceClient" "-Dexec.args=localhost bob general"
```

Client arguments are `<host> <nickname> <room>` (defaults: `localhost`, a random nickname, `general`).

Speak — everyone in the same room hears you. Type text to chat, `/who` for the member list, `/mute` to toggle the microphone, `/quit` to leave.

## Tests

```bash
mvn clean test
```

## License

Distributed under the [MIT](LICENSE) license.
