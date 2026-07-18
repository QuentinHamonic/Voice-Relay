package com.example.voicerelay.transport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * The WebSocket handshake (RFC 6455): the opening HTTP conversation before
 * frames.
 */
public final class Handshake {

    static final String MAGIC_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    /**
     * Server side: reads the client's request and replies "101 Switching
     * Protocols".
     */
    public static void respondToClient(InputStream input, OutputStream output) throws IOException {
        String httpHeader = readUntilBlankLine(input);
        String[] lines = httpHeader.split("\r\n");

        if (lines.length == 0 || !lines[0].startsWith("GET")) {
            throw new IOException("This is not a WebSocket request");
        }
        String clientKey = findHeader(lines, "sec-websocket-key");
        if (clientKey == null) {
            throw new IOException("No Sec-WebSocket-Key: the client isn't speaking WebSocket");
        }

        String response = "HTTP/1.1 101 Switching Protocols\r\n"
                + "Upgrade: websocket\r\n"
                + "Connection: Upgrade\r\n"
                + "Sec-WebSocket-Accept: "
                + computeAccept(clientKey) + "\r\n"
                + "\r\n";

        output.write(response.getBytes(StandardCharsets.UTF_8));
        output.flush();
    }

    /** Client side: sends the "Upgrade" request and checks the server's reply. */
    public static void requestFromServer(InputStream input, OutputStream output, String host, String path)
            throws IOException {
        byte[] random = new byte[16];
        new SecureRandom().nextBytes(random);
        String key = Base64.getEncoder().encodeToString(random);

        String request = "GET " + path + " HTTP/1.1\r\n"
                + "Host: " + host + "\r\n"
                + "Upgrade: websocket\r\n"
                + "Connection: Upgrade\r\n"
                + "Sec-WebSocket-Key: " + key + "\r\n"
                + "Sec-WebSocket-Version: 13\r\n"
                + "\r\n";
        output.write(request.getBytes(StandardCharsets.UTF_8));
        output.flush();

        String[] lines = readUntilBlankLine(input).split("\r\n");
        if (lines.length == 0 || !lines[0].contains("101")) {
            throw new IOException("The server refused the handshake");
        }
        if (!computeAccept(key).equals(findHeader(lines, "sec-websocket-accept"))) {
            throw new IOException("Invalid Sec-Websocket-Accept: this server isn't really speaking WebSocket");
        }
    }

    /** The RFC 6455 formula: Base64( SHA-1( client key + magic GUID ) ). */
    static String computeAccept(String key) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] digest = sha1.digest((key + MAGIC_GUID).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    /**
     * Reads the HTTP header byte by byte, stopping EXACTLY at "\r\n\r\n" (not one
     * byte more,
     * or we'd swallow the start of the first frame).
     */
    private static String readUntilBlankLine(InputStream input) throws IOException {
        ByteArrayOutputStream accumulator = new ByteArrayOutputStream();
        int[] lastFour = new int[4];
        while (true) {
            int b = input.read();
            if (b == -1) {
                throw new IOException("Connection closed in the middle of the handshake");
            }
            accumulator.write(b);
            if (accumulator.size() > 16 * 1024) {
                throw new IOException("HTTP header abnormally long: refusing");
            }
            lastFour[0] = lastFour[1];
            lastFour[1] = lastFour[2];
            lastFour[2] = lastFour[3];
            lastFour[3] = b;
            if (lastFour[0] == '\r' && lastFour[1] == '\n' && lastFour[2] == '\r' && lastFour[3] == '\n') {
                return accumulator.toString(StandardCharsets.UTF_8);
            }
        }
    }

    private static String findHeader(String[] lines, String wantedName) {
        for (String line : lines) {
            int colon = line.indexOf(":");
            if (colon > 0 && line.substring(0, colon).trim().equalsIgnoreCase(wantedName)) {
                return line.substring(colon + 1).trim();
            }
        }
        return null;
    }

    private Handshake() {

    }
}
