package ru.ifmo.ctddev.peresadin.walk;

import java.io.*;
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
    private Writer writer;

    public HashCalcFileVisitor(Path startPath, HashFunction hashFunction, Writer writer) {
        this.startPath = startPath;
        this.function = hashFunction;
        this.writer = writer;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String hash = ERROR_HASH;
        try {
            InputStream inputStream = Files.newInputStream(file);
            try {
                hash = function.hash(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (AccessDeniedException e) {
            System.out.println("Access denied to file " + e.getMessage());
        } catch (IOException e) {
        }

        String resPath = file.toAbsolutePath().toString();
        writer.write(hash + " " + resPath.substring(resPath.indexOf(startPath.toString())) + "\n");
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        System.out.println(exc.getMessage());
        String resPath = file.toAbsolutePath().toString();
        writer.write(ERROR_HASH + " " + resPath.substring(resPath.indexOf(startPath.toString())) + "\n");
        return FileVisitResult.CONTINUE;
    }

    public void walkHash() throws IOException {
        Files.walkFileTree(startPath, this);
    }
}
