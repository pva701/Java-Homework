package ru.ifmo.ctddev.peresadin.walk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

/**
 * Created by pva701 on 2/14/15.
 */
public class HashCalcFileVisitor extends SimpleFileVisitor<Path> {
    private static final String ERROR_HASH = "00000000";
    private HashFunction function;
    private Path startPath;
    private HashMap<Path, String> hashes = new HashMap<Path, String>();

    public HashCalcFileVisitor(Path startPath, HashFunction hashFunction) {
        this.startPath = startPath;
        this.function = hashFunction;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        InputStream inputStream = null;
        try {
            inputStream = Files.newInputStream(file);
            hashes.put(file, function.hash(inputStream));
        } catch (AccessDeniedException e) {
            System.out.println("Access denied to file " + e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            hashes.put(file, ERROR_HASH);
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException ignore) {}
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        System.out.println(exc.getMessage());
        hashes.put(file, ERROR_HASH);
        return FileVisitResult.CONTINUE;
    }

    public HashMap<Path, String> calcHashes() {
        try {
            Files.walkFileTree(startPath, this);
        } catch (IOException e) {
        }
        return hashes;
    }
}
