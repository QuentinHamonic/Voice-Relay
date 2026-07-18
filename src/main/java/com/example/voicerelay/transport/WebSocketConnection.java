package com.example.voicerelay.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * A ready-to-use WebSocket connection over a TCP socket. Sends are synchronized:
 * on the server, several threads write to the same client and their frames must not interleave.
 */
public class WebSocketConnection implements AutoCloseable {
    private final Socket socket;
    private final InputStream input;
    private final OutputStream output;
    private final boolean shouldMask;

    private WebSocketConnection(Socket socket, boolean shouldMask) throws IOException {
        this.socket = socket;
        this.socket.setTcpNoDelay(true);
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
        this.shouldMask = shouldMask;
    }

    /** Client side: connects and does the handshake (mask=true). */
    public static WebSocketConnection toServer(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);
        try {
            Handshake.requestFromServer(socket.getInputStream(), socket.getOutputStream(), host + ":" + port, "/radio");
            return new WebSocketConnection(socket, true);
        } catch (IOException error) {
            socket.close();
            throw error;
        }
    }

    /** Server side: takes an accepted socket and does the handshake (mask=false). */
    public static WebSocketConnection fromClient(Socket socket) throws IOException {
        try {
            Handshake.respondToClient(socket.getInputStream(), socket.getOutputStream());
            return new WebSocketConnection(socket, false);
        } catch (IOException error) {
            socket.close();
            throw error;
        }
    }

    public synchronized void sendBinary(byte[] data) throws IOException {
        WebSocketFrame.write(output, WebSocketFrame.OPCODE_BINARY, data, shouldMask);
    }

    public synchronized void sendText(String text) throws IOException {
        WebSocketFrame.write(output, WebSocketFrame.OPCODE_TEXT,
                text.getBytes(StandardCharsets.UTF_8), shouldMask);
    }

    /** Returns the next USEFUL message (text/binary), handling ping/pong/close transparently. */
    public WebSocketFrame.Frame receive() throws IOException {
        while (true) {
            WebSocketFrame.Frame frame = WebSocketFrame.read(input);
            switch (frame.getOpcode()) {
                case WebSocketFrame.OPCODE_PING:
                    synchronized (this) {
                        WebSocketFrame.write(output, WebSocketFrame.OPCODE_PONG,
                                frame.getPayload(), shouldMask);
                    }
                    break;
                case WebSocketFrame.OPCODE_PONG:
                    break;
                case WebSocketFrame.OPCODE_CLOSE:
                    try {
                        synchronized (this) {
                            WebSocketFrame.write(output, WebSocketFrame.OPCODE_CLOSE,
                                    frame.getPayload(), shouldMask);
                        }
                    } catch (IOException alreadyClosed) {

                    }
                    throw new IOException("Connection cleanly closed by the other end");
                case WebSocketFrame.OPCODE_TEXT:
                case WebSocketFrame.OPCODE_BINARY:
                    return frame;
                default:
                    throw new IOException("Unexpected opcode: " + frame.getOpcode());
            }
        }
    }

    public String getRemoteAddress() {
        return String.valueOf(socket.getRemoteSocketAddress());
    }

    @Override
    public void close() {
        try {
            synchronized (this) {
                WebSocketFrame.write(output, WebSocketFrame.OPCODE_CLOSE, new byte[0], shouldMask);
            }
        } catch (IOException alreadyClosed) {

        }
        try {
            socket.close();
        } catch (IOException ignored) {

        }
    }

}
