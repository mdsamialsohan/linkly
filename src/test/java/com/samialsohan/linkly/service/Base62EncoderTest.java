package com.samialsohan.linkly.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Base62EncoderTest {
    private final Base62Encoder encoder = new Base62Encoder();

    @Test
    void encodesZero()
    {
        assertEquals("0", encoder.encode(0));
    }

    @Test
    void encodesSmallNumbers()
    {
        assertEquals("1", encoder.encode(1));
        assertEquals("9", encoder.encode(9));
        assertEquals("a", encoder.encode(10));
        assertEquals("z", encoder.encode(35));
        assertEquals("A", encoder.encode(36));
        assertEquals("Z", encoder.encode(61));
        assertEquals("10", encoder.encode(62));
    }
    @Test
    void encodesLargeNumbers()
    {
        String encoded = encoder.encode(56_800_235_584L);
        assertEquals("1000000", encoded);
    }

    @Test
    void encodeAndDecodeAreInverse()
    {
        for(long value : new long[]{0, 1, 61, 62, 1000, 999_999, Long.MAX_VALUE / 2})
        {
            String encoded = encoder.encode(value);
            long decoded = encoder.decode(encoded);

            assertEquals(value, decoded, "Round trip failed for value:  " + value);
        }
    }
    @Test
    void rejectNegative()
    {
        assertThrows(IllegalArgumentException.class, () -> encoder.encode(-1));
    }

    @Test
    void rejectInvalidCharacter()
    {
        assertThrows(IllegalArgumentException.class, () -> encoder.decode("abc!"));
    }
    @Test
    void rejectsNullOrEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> encoder.decode(""));
        assertThrows(IllegalArgumentException.class, () -> encoder.decode(null));
    }
}
