package ru.ifmo.ctddev.peresadin.walk;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by pva701 on 2/14/15.
 */
public class FNVHash32 implements HashFunction {
    private static final int BUFFER_SIZE = 1024;
    private byte[] buffer = new byte[BUFFER_SIZE];

    @Override
    public String hash(InputStream inputStream) throws IOException {
        int available;
        int xi = 0; //2_166_135_261;
        int p = 16777619;
        while ((available = inputStream.read(buffer)) >= 0)
            for (int i = 0; i < available; ++i)
                xi = xi * p + buffer[i];
        long res;
        if (xi >= 0) res = xi;
        else res = (1L<<32) + xi;
        return String.format("%08x", res);
    }
}