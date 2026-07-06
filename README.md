# Voice Relay

Encrypted voice relay chat by rooms.

The goal is to provide a relay server able to route audio streams between multiple clients grouped into rooms, with encryption of the exchanges.

> ⚠️ Work in progress, built step by step. Current state: **step 1 (the recorder)** — capture the microphone, play it back, and export it to `.wav`. Network, codec, and encryption layers are not implemented yet.

## Requirements

- JDK 21
- Maven 3.8+

## Project structure

```
src/main/java/com/example/voicerelay/
  audio/            # Audio settings, Microphone (capture) and Speaker (playback)
  client/           # VoiceClient: the app's entry point
```

## Build and run

```bash
mvn compile
mvn -q compile exec:java -Dexec.mainClass=com.example.voicerelay.client.VoiceClient
```

Running `VoiceClient` records 3 seconds from the microphone, plays it back, and saves it to `record.wav` in the working directory.

## License

Distributed under the [MIT](LICENSE) license.
