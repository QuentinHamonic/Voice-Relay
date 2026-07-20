package com.example.voicerelay.codec;

public class CodecRegistry {

    public static Codec byId(byte id) {
        switch (id) {
            case PcmCodec.ID:
                return new PcmCodec();

            case MuLawCodec.ID:

                return new MuLawCodec();

            default:
                throw new IllegalArgumentException("Unkown codec: " + id);
        }
    }

    private CodecRegistry() {

    }
}
