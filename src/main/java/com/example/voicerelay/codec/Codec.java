package com.example.voicerelay.codec;

public interface Codec {

    byte getId();

    byte[] encode(byte[] pcm);

    byte[] decode(byte[] data);

}
