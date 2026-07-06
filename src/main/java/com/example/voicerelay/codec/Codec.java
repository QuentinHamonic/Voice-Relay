package com.example.voicerelay.codec;

/** A codec: turns PCM into compressed bytes, and back. */
public interface Codec {

    byte getId();

    byte[] encode(byte[] pcm);

    byte[] decode(byte[] data);

}
