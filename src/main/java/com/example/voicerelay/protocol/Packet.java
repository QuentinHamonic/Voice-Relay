package com.example.voicerelay.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * A Voice Relay packet: the envelope that travels on the network ("home-made RTP").
 * 16-byte header: version, type, codec, flags, ssrc, sequence, timestamp, then payload.
 */
public class Packet {
    public static final byte VERSION = 1;
    public static final int HEADER_SIZE = 16;
    public static final byte FLAG_E2EE = 0b0000_0001; // bit 0: payload is E2EE-encrypted

    private final PacketType type;
    private final byte codec;
    private final byte flags;
    private final int ssrc;
    private final int sequence;
    private final int timestamp;
    private final byte[] payload;

    public Packet(PacketType type, byte codec, byte flags, int ssrc, int sequence, int timestamp, byte[] payload) {
        if (type == null) {
            throw new IllegalArgumentException("A packet's type is required");
        }
        if (payload == null) {
            throw new IllegalArgumentException("The payload cannot be null (empty, yes)");
        }
        this.type = type;
        this.codec = codec;
        this.flags = flags;
        this.ssrc = ssrc;
        this.sequence = sequence;
        this.timestamp = timestamp;
        this.payload = payload;
    }

    public static Packet audio(int ssrc, int sequence, int timestamp, byte codec, byte[] data) {
        return new Packet(PacketType.AUDIO, codec, (byte) 0, ssrc, sequence, timestamp, data);
    }

    public static Packet text(int ssrc, String message) {
        return new Packet(PacketType.TEXT, (byte) 0, (byte) 0, ssrc, 0, 0,
                message.getBytes(StandardCharsets.UTF_8));
    }

    public static Packet command(int ssrc, String command) {
        return new Packet(PacketType.COMMAND, (byte) 0, (byte) 0, ssrc, 0, 0,
                command.getBytes(StandardCharsets.UTF_8));
    }

    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + payload.length);
        buffer.put(VERSION); // ByteBuffer is big-endian by default: the network convention
        buffer.put(type.getCode());
        buffer.put(codec);
        buffer.put(flags);
        buffer.putInt(ssrc);
        buffer.putInt(sequence);
        buffer.putInt(timestamp);
        buffer.put(payload);

        return buffer.array();
    }

    /** Reads bytes back into a Packet, distrusting everything (hostile input). */
    public static Packet fromBytes(byte[] data) throws InvalidPacketException {
        if (data == null || data.length < HEADER_SIZE) {
            throw new InvalidPacketException("Packet too short: " + (data == null ? "null" : data.length + "bytes"));
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        byte version = buffer.get();
        if (version != VERSION) {
            throw new InvalidPacketException("unknow version: " + version);
        }
        PacketType type = PacketType.fromCode(buffer.get());
        byte codec = buffer.get();
        byte flags = buffer.get();
        int ssrc = buffer.getInt();
        int sequence = buffer.getInt();
        int timestamp = buffer.getInt();
        byte[] payload = new byte[buffer.remaining()];
        buffer.get(payload);
        return new Packet(type, codec, flags, ssrc, sequence, timestamp, payload);
    }

    public PacketType getType() {
        return type;
    }

    public byte getCodec() {
        return codec;
    }

    public byte getFlags() {
        return flags;
    }

    public boolean isE2eeEncrypted() {
        return (flags & FLAG_E2EE) != 0;
    }

    public int getSsrc() {
        return ssrc;
    }

    public int getSequence() {
        return sequence;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public byte[] getPayload() {
        return payload;
    }

    public String getTextMessage() {
        return new String(payload, StandardCharsets.UTF_8);
    }

    public Packet withPayload(byte[] newPayload, byte newFlags) {
        return new Packet(type, codec, newFlags, ssrc, sequence, timestamp, newPayload);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Packet)) {
            return false;
        }
        Packet that = (Packet) o;
        return type == that.type
                && codec == that.codec
                && flags == that.flags
                && ssrc == that.ssrc
                && sequence == that.sequence
                && timestamp == that.timestamp
                && Arrays.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, codec, flags, ssrc, sequence, timestamp,
                Arrays.hashCode(payload));
    }

    @Override
    public String toString() {
        return "Packet{" + type + ", ssrc=" + ssrc + ", seq=" + sequence
                + ", " + payload.length + " octets"
                + (isE2eeEncrypted() ? ", E2EE" : "") + "}";
    }
}
