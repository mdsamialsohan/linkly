package com.samialsohan.linkly.service;

import org.hibernate.annotations.Comment;
import org.springframework.stereotype.Component;

@Component
public class Base62Encoder {
    private static final String ALPHABET =  "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length();
    public String encode(long value)
    {
        if(value < 0)
        {
            throw new IllegalArgumentException("Cannot encode negative value: " + value);
        }
        if( value == 0)
        {
            return String.valueOf(ALPHABET.charAt(0));
        }

        StringBuilder sb = new StringBuilder();
        long remaining = value;
        while(remaining > 0)
        {
            int index = (int) (remaining % BASE);
            sb.append(ALPHABET.charAt(index));
            remaining = remaining / BASE;
        }
        return sb.reverse().toString();
    }

    public long decode(String encoded)
    {
        if(encoded == null || encoded.isEmpty())
        {
            throw new IllegalArgumentException("Cannot decode empty string: " + encoded);
        }
        long value = 0;
        for(int i = 0; i < encoded.length(); i++)
        {
            char c = encoded.charAt(i);
            int index = ALPHABET.indexOf(c);
            if(index == -1)
            {
                throw new IllegalArgumentException("Invalid Base62 character: '" + c + "'");
            }
            value = value * BASE + index;
        }
        return value;
    }
}
