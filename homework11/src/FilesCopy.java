import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by pva701 on 5/10/15.
 */
public class FilesCopy {
    private Observer observer = new Observer();

    private boolean isCanceled;
    private Path source;
    private Path target;
    private long startTime;

    private State currentState = new State();

    public FilesCopy(Path source, Path target) {
        this.source = source;
        this.target = target;
    }

    public void start() throws IOException {
        if (isCanceled)
            throw new IllegalStateException("Already terminated!");
        if (Files.notExists(source))
            throw new IllegalArgumentException("Source file doesn't exist!");
        if (Files.notExists(target))
            throw new IllegalArgumentException("Target file doesn't exist!");
        if (Files.isRegularFile(target) && !Files.isRegularFile(source))
            throw new IllegalArgumentException("Can't copy folder to file!");

        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (isCanceled)
                    return FileVisitResult.TERMINATE;
                currentState.totalSize += Files.size(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (isCanceled)
                    return FileVisitResult.TERMINATE;
                currentState.totalSize += Files.size(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        System.out.println("total size = " + currentState.totalSize);

        startTime = System.currentTimeMillis();
        Files.walkFileTree(source, new CopyFileVisitor());
        isCanceled = true;
    }

    public void cancel() {
        isCanceled = true;
    }

    public void setObserver(Observer observer) {
        this.observer = observer;
    }


    public class CopyFileVisitor extends SimpleFileVisitor<Path> {
        public static final int BUFFER_SIZE = 4096;

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (isCanceled)
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
            if (isCanceled)
                return FileVisitResult.TERMINATE;
            Path newFile = target.resolve(source.relativize(file));
            //System.out.println("new file = " + newFile.toString());
            if (Files.exists(newFile)) {
                FileVisitResult res = observer.replaceFile(newFile);
                if (res != FileVisitResult.CONTINUE)
                    return res;
            }

            try (InputStream is = Files.newInputStream(file)) {
                try (OutputStream os = Files.newOutputStream(newFile)) {
                    byte[] bytes = new byte[BUFFER_SIZE];
                    int c = 0;
                    while ((c = is.read(bytes)) >= 0 && !isCanceled) {
                        os.write(bytes, 0, c);
                        currentState.copyBytes(c);
                        int sec = (int)((System.currentTimeMillis() - startTime) / 1000);
                        if (sec > currentState.elapsedSecs) {
                            currentState.elapsedSecs = sec;
                            observer.onChangeState(currentState);
                            currentState.resetCurrentSpeed();
                        }
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

    public class State {
        private long totalSize;
        private long copiedSize;
        private int elapsedSecs;
        private int currentSpeed;

        public int getProgress() {
            return (int)(copiedSize * 1.0 / totalSize) * 100;
        }

        public int getElapsedSecs() {
            return elapsedSecs;
        }

        public int getRemainSecs() {
            if (copiedSize == 0)
                return Integer.MAX_VALUE;
            return (int)(1L * elapsedSecs * (totalSize - copiedSize) / copiedSize);//(total - copied)/avSpeed
        }

        public int getAverageSpeed() {
            return (int)(copiedSize / elapsedSecs);
        }

        public int getCurrentSpeed() {
            return currentSpeed;
        }

        private void copyBytes(int bytes) {
            currentSpeed += bytes;
            copiedSize += bytes;
        }

        private void resetCurrentSpeed() {
            currentSpeed = 0;
        }
    }
}
