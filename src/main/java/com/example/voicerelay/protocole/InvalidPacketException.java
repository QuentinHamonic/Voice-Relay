package com.example.voicerelay.protocole;

/** Thrown when received bytes don't form a valid packet. */
public class InvalidPacketException extends Exception {

    public InvalidPacketException(String message) {
        super(message);
    }

}
