package com.example.voicerelay.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AudioSettingsTest {

    @Test
    void derivedNumbersStayConsistent() {
        assertEquals(160, AudioSettings.SAMPLES_PER_FRAME);
        assertEquals(320, AudioSettings.BYTES_PER_FRAME);
        assertEquals(50, 1000 / AudioSettings.MS_PER_FRAME);
    }
}
