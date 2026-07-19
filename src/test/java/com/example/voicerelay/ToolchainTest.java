package com.example.voicerelay;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ToolchainTest {

    @Test
    void runningOnJava21OrNewer() {
        int feature = Runtime.version().feature();
        assertTrue(feature >= 21, "This course needs Java 21+, found " + feature);
    }
}
