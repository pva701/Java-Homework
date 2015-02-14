package ru.ifmo.ctddev.peresadin.walk;

import java.nio.file.Path;
import java.util.HashMap;

/**
 * Created by pva701 on 2/14/15.
 */
public class RecursiveWalk {
    public static HashMap<Path, String> walkHashFNV32(Path startPath) {
        return new HashCalcFileVisitor(startPath, new FNVHash32()).calcHashes();
    }
}
