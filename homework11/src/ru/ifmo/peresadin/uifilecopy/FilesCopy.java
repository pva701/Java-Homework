package ru.ifmo.peresadin.uifilecopy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by pva701 on 5/10/15.
 */
public class FilesCopy {
    private Observer observer = new Observer();

    private Path source;
    private Path target;

    private State currentState = new State();

    public FilesCopy(Path source, Path target) {
        this.source = source;
        this.target = target;
    }

    public void start() throws IOException {
        if (currentState.status == State.Status.CANCELED)
            throw new IllegalStateException("Already terminated!");
        if (Files.notExists(source))
            throw new IllegalArgumentException("Source doesn't exist!");
        if (Files.isRegularFile(target) && !Files.isRegularFile(source))
            throw new IllegalArgumentException("Can't copy folder to file!");
        if (Files.notExists(target) && !Files.isRegularFile(source))
          throw new IllegalArgumentException("Target doesn't exist!");

        setStatusAndPublish(State.Status.PRE);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                publish();
            }
        }, 1000, 1000);

        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (currentState.isCancelled())
                    return FileVisitResult.TERMINATE;
                currentState.totalSize += Files.size(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (currentState.isCancelled())
                    return FileVisitResult.TERMINATE;
                currentState.totalSize += Files.size(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        if (currentState.isCancelled())
            return;

        setStatusAndPublish(State.Status.COPYING);
        if (Files.isRegularFile(source) && Files.isDirectory(target))
            target = target.resolve(source.getFileName());
        Files.walkFileTree(source, new CopyFileVisitor());
        if (currentState.isCancelled())
            return;
        currentState.setCopiedSize(currentState.getTotalSize());
        setStatusAndPublish(State.Status.FINISHED);
    }

    public void cancel() {
        if (currentState.isFinished())
            return;
        setStatusAndPublish(State.Status.CANCELED);
    }

    public void setObserver(Observer observer) {
        this.observer = observer;
    }


    private void setStatusAndPublish(State.Status status) {
        currentState.status = status;
        observer.onChangeState(currentState);
    }

    private void publish() {
        currentState.incElapsed();
        observer.onChangeState(currentState);
        currentState.resetCurrentSpeed();
    }

    public class CopyFileVisitor extends SimpleFileVisitor<Path> {
        public static final int BUFFER_SIZE = 4096;

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (currentState.isCancelled())
                return FileVisitResult.TERMINATE;
            Path newDir = target.resolve(source.relativize(dir));
            currentState.copyBytes((int)Files.size(dir));
            if (newDir.equals(target))
                return FileVisitResult.CONTINUE;

            try {
                Files.copy(dir, newDir);
            } catch (FileAlreadyExistsException e) {
                return observer.mergeDirectory(newDir);//Blocking
            } catch (IOException e) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (currentState.isCancelled())
                return FileVisitResult.TERMINATE;
            Path newFile = target.resolve(source.relativize(file));
            if (Files.exists(newFile)) {
                FileVisitResult res = observer.replaceFile(newFile);
                if (res != FileVisitResult.CONTINUE)
                    return res;
            }
            try (InputStream is = Files.newInputStream(file)) {
                try (OutputStream os = Files.newOutputStream(newFile)) {
                    byte[] bytes = new byte[BUFFER_SIZE];
                    int c = 0;
                    while ((c = is.read(bytes)) >= 0 && !currentState.isCancelled()) {
                        os.write(bytes, 0, c);
                        currentState.copyBytes(c);
                    }
                }
            }
            return FileVisitResult.CONTINUE;
        }
    }


    public static class Observer {
        public void onChangeState(State state) {
        }
        public FileVisitResult replaceFile(Path path) {
            return FileVisitResult.CONTINUE;
        }
        public FileVisitResult mergeDirectory(Path path) {
            return FileVisitResult.CONTINUE;
        }
    }

    public static class State {
        public enum Status {PRE, COPYING, CANCELED, FINISHED}
        private Status status;
        private long totalSize;
        private long copiedSize;
        private int elapsedSecs;
        private int currentSpeed;

        public synchronized int getProgress() {
            if (status == Status.FINISHED)
                return 100;
            return (int)(copiedSize * 1.0 / totalSize * 100);
        }

        public synchronized int getElapsedSecs() {
            return elapsedSecs;
        }

        public synchronized int getRemainSecs() {
            if (copiedSize == 0)
                return Integer.MAX_VALUE;
            return (int)(1L * elapsedSecs * (totalSize - copiedSize) / copiedSize);//(total - copied)/avSpeed
        }

        public synchronized int getAverageSpeed() {
            if (elapsedSecs == 0)
                return 0;
            return (int)(copiedSize / elapsedSecs);
        }

        public synchronized int getCurrentSpeed() {
            return currentSpeed;
        }

        private synchronized void copyBytes(int bytes) {
            currentSpeed += bytes;
            copiedSize += bytes;
        }

        private synchronized void resetCurrentSpeed() {
            currentSpeed = 0;
        }

        public synchronized Status getStatus() {
            return status;
        }

        public synchronized boolean isCancelled() {
            return status == Status.CANCELED;
        }

        public synchronized boolean isFinished() {
            return status == Status.FINISHED;
        }

        private synchronized void incElapsed() {
            elapsedSecs++;
        }

        private synchronized long getTotalSize() {
            return totalSize;
        }

        private synchronized void setCopiedSize(long sz) {
            copiedSize = sz;
        }
    }
}
