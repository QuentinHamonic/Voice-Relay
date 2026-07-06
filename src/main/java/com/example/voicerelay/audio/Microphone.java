package com.example.voicerelay.audio;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/** The machine's microphone. */
public class Microphone implements AutoCloseable {

    private final TargetDataLine line;

    public Microphone() throws LineUnavailableException {
        this.line = AudioSystem.getTargetDataLine(AudioSettings.format());
        this.line.open(AudioSettings.format());
        this.line.start();
    }

    /** Reads exactly one frame; line.read() can return less, hence the loop. */
    public byte[] readFrame() {
        byte[] trame = new byte[AudioSettings.BYTES_PER_FRAME];
        int bytesRead = 0;
        while (bytesRead < trame.length) {
            int n = line.read(trame, bytesRead, trame.length - bytesRead);
            if (n <= 0) {
                break; // line closed: return what we have (rest stays zeroed = silence)
            }
            bytesRead += n;
        }
        return trame;
    }

    @Override
    public void close() {
        line.stop();
        line.close();
    }

}
