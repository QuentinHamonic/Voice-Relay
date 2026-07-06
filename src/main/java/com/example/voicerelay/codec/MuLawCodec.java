package com.example.voicerelay.codec;

public class MuLawCodec implements Codec {

    public static final byte ID = 1;

    private static final int BIAS = 0x84;
    private static final int CLIP = 32635;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public byte[] encode(byte[] pcm) {
        byte[] result = new byte[pcm.length / 2];
        for (int i = 0; i < result.length; i++) {
            int lowByte = pcm[2 * i] & 0xFF;
            int highByte = pcm[2 * i + 1];
            short sample = (short) ((highByte << 8) | lowByte);
            result[i] = encodeSample(sample);
        }
        return result;
    }

    @Override
    public byte[] decode(byte[] data) {
        byte[] pcm = new byte[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            short sample = decodeSample(data[i]);
            pcm[2 * i] = (byte) (sample & 0xFF);
            pcm[2 * i + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        return pcm;
    }

    static short decodeSample(byte mu) {
        int value = ~mu & 0xFF;
        int sign = value & 0x80;
        int exponent = (value >> 4) & 0x07;
        int mantissa = value & 0x0F;

        int sample = ((mantissa << 3) + BIAS) << exponent;
        sample -= BIAS;

        return (short) (sign != 0 ? -sample : sample);
    }

    static byte encodeSample(short pcm) {
        int value = pcm;
        int sign = (value < 0) ? 0x80 : 0x00;
        if (value < 0) {
            value = -value;
        }
        if (value > CLIP) {
            value = CLIP;
        }
        value += BIAS;

        int exponent = 7;
        for (int mask = 0x4000; (value & mask) == 0 && exponent > 0; exponent--) {
            mask >>= 1;
        }

        int mantissa = (value >> (exponent + 3)) & 0x0F;

        return (byte) ~(sign | (exponent << 4) | mantissa);
    }

}
