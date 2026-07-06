package com.example.voicerelay.audio;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/** The machine's audio output. */
public class Speaker implements AutoCloseable {

    private final SourceDataLine line;

    public Speaker() throws LineUnavailableException {
        this.line = AudioSystem.getSourceDataLine(AudioSettings.format());
        this.line.open(AudioSettings.format());
        this.line.start();
    }

    public void play(byte[] pcm) {
        line.write(pcm, 0, pcm.length);
    }

    @Override
    public void close() {
        line.drain(); // finish playing what's queued before stop()/close() cut the line off
        line.stop();
        line.close();
    }
}
