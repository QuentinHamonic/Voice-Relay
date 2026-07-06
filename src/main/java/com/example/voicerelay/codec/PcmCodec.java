package com.example.voicerelay.codec;

import java.util.Arrays;

/** The "transparent" codec: PCM passes through unchanged. Useful for comparing/debugging. */
public class PcmCodec implements Codec {

    public static final byte ID = 0;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public byte[] encode(byte[] pcm) {
        return Arrays.copyOf(pcm, pcm.length);
    }

    @Override
    public byte[] decode(byte[] data) {
        return Arrays.copyOf(data, data.length);
    }
}
