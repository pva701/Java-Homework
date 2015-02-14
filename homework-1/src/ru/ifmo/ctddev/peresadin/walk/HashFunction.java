package ru.ifmo.ctddev.peresadin.walk;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by pva701 on 2/14/15.
 */
public interface HashFunction {
    String hash(InputStream inputStream) throws IOException;
}
