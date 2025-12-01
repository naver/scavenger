package com.navercorp.scavenger.util;

import com.navercorp.scavenger.util.HashGenerator.DefaultHash;
import com.navercorp.scavenger.util.HashGenerator.HashAlgorithm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashGeneratorTest {

    @Test
    void testHash() {
        String signature = "test";

        assertEquals("ac7d28cc74bde19d9a128231f9bd4d82", HashAlgorithm.MURMUR.hash(signature));
        assertEquals("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", HashAlgorithm.SHA256.hash(signature));
        assertEquals("98f6bcd4621d373cade4e832627b4f6", HashAlgorithm.MD5.hash(signature)); // Only 31 characters due to leading zero being dropped
        assertEquals(HashAlgorithm.MD5.hash(signature), DefaultHash.from(signature));
    }

}
