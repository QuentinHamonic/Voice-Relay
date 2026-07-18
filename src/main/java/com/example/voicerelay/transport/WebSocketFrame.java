package com.example.voicerelay.transport;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

/** Reads and writes WebSocket frames (RFC 6455): opcode + masking + payload. */
public final class WebSocketFrame {

    public static final int OPCODE_TEXT = 0x1;
    public static final int OPCODE_BINARY = 0x2;
    public static final int OPCODE_CLOSE = 0x8;
    public static final int OPCODE_PING = 0x9;
    public static final int OPCODE_PONG = 0xA;

    static final int MAX_PAYLOAD = 1024 * 1024;

    private static final SecureRandom RANDOM = new SecureRandom();

    /** A frame that was read: its opcode + its (already unmasked) payload. */
    public static class Frame {
        private final int opcode;
        private final byte[] payload;

        public Frame(int opcode, byte[] payload) {
            this.opcode = opcode;
            this.payload = payload;
        }

        public int getOpcode() {
            return opcode;
        }

        public byte[] getPayload() {
            return payload;
        }
    }

    /**
     * Writes one frame. mask=true on the client side (required by the RFC), false
     * on the server.
     */
    public static void write(OutputStream output, int opcode, byte[] payload, boolean mask) throws IOException {
        ByteArrayOutputStream frame = new ByteArrayOutputStream(payload.length + 14);

        frame.write(0x80 | opcode);

        int maskBit = mask ? 0x80 : 0x00;
        if (payload.length <= 125) {
            frame.write(maskBit | payload.length);
        } else if (payload.length <= 0xFFFF) {
            frame.write(maskBit | 126);
            frame.write((payload.length >> 8) & 0xFF);
            frame.write(payload.length & 0xFF);
        } else {
            frame.write(maskBit | 127);
            for (int shift = 56; shift >= 0; shift -= 8) {
                frame.write((int) (((long) payload.length >> shift) & 0xFF));
            }
        }

        if (mask) {
            byte[] maskKey = new byte[4];
            RANDOM.nextBytes(maskKey);
            frame.write(maskKey, 0, 4);
            for (int i = 0; i < payload.length; i++) {
                frame.write(payload[i] ^ maskKey[i % 4]);
            }
        } else {
            frame.write(payload, 0, payload.length);
        }

        output.write(frame.toByteArray());
        output.flush();
    }

    /**
     * Reads one full frame (blocks until it's complete), unmasking the payload if
     * needed.
     */
    public static Frame read(InputStream input) throws IOException {
        int byte0 = readOneByte(input);
        boolean fin = (byte0 & 0x80) != 0;
        int opcode = byte0 & 0x0F;
        if (!fin) {
            throw new IOException("Fragmented frame: not handled by the Voice Relay v1 spec");
        }

        int byte1 = readOneByte(input);
        boolean masked = (byte1 & 0x80) != 0;
        long length = byte1 & 0x7F;
        if (length == 126) {
            length = ((long) readOneByte(input) << 8) | readOneByte(input);
        } else if (length == 127) {
            length = 0;
            for (int i = 0; i < 8; i++) {
                length = (length << 8) | readOneByte(input);
            }
        }
        if (length < 0 || length > MAX_PAYLOAD) {
            throw new IOException("Frame of " + length + " bytes: rejected (beyond " + MAX_PAYLOAD + ")");
        }

        byte[] maskKey = masked ? readExactly(input, 4) : null;
        byte[] payload = readExactly(input, (int) length);
        if (masked) {
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) (payload[i] ^ maskKey[i % 4]);
            }
        }
        return new Frame(opcode, payload);
    }

    private static int readOneByte(InputStream input) throws IOException {
        int b = input.read();
        if (b == -1) {
            throw new EOFException("Connection closed in the middle of a frame");
        }
        return b;
    }

    private static byte[] readExactly(InputStream input, int count) throws IOException {
        byte[] buffer = new byte[count];
        int read = 0;
        while (read < count) {
            int n = input.read(buffer, read, count - read);
            if (n == -1) {
                throw new EOFException("Connection closed in the middle of a frame");
            }
            read += n;
        }
        return buffer;
    }

    private WebSocketFrame() {
    }
}
