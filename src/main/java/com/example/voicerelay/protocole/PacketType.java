package com.example.voicerelay.protocole;

/** The three kinds of packet. The code (one byte) is what travels on the wire. */
public enum PacketType {

    AUDIO((byte) 0),
    TEXT((byte) 1),
    COMMAND((byte) 2);

    private final byte code;

    PacketType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    /** Unknown code: hostile packet or a future version we don't understand -> reject. */
    public static PacketType fromCode(byte code) throws InvalidPacketException {
        for (PacketType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new InvalidPacketException("unknow packet type: " + code);
    }

}
