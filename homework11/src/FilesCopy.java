import java.io.File;
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
    private Observer observer = new Observer() {//Simple Observer
        @Override
        public void onChangeState(State state) {
        }

        @Override
        public FileVisitResult replaceFile(Path path) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult mergeDirectory(Path path) {
            return FileVisitResult.CONTINUE;
        }
    };

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

        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                currentState.totalSize += Files.size(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                currentState.totalSize += Files.size(dir);
                return FileVisitResult.CONTINUE;
            }
        });

        startTime = System.currentTimeMillis();
        /*new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                currentState.passedSecs++;
            }
        }, 1000, 1000);*/

        Files.walkFileTree(source, new CopyFileVisitor());
        isCanceled = true;
    }

    public void cancel() {
        isCanceled = true;
        //TODO write this
    }

    public void setObserver(Observer observer) {
        this.observer = observer;
    }

    public class CopyFileVisitor extends SimpleFileVisitor<Path> {
        public static final int BUFFER_SIZE = 4096;

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path newDir = target.resolve(source.relativize(dir));
            currentState.copyBytes((int)Files.size(dir));
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
                    while ((c = is.read()) >= 0) {
                        os.write(bytes, 0, c);
                        currentState.copyBytes(c);
                        int sec = (int)((System.currentTimeMillis() - startTime) / 1000);
                        if (sec > currentState.passedSecs) {
                            currentState.passedSecs = sec;
                            observer.onChangeState(currentState);
                            currentState.resetCurrentSpeed();
                        }
                    }
                }
            }
            return FileVisitResult.CONTINUE;
        }
    }

    public interface Observer {
        void onChangeState(State state);
        FileVisitResult replaceFile(Path path);
        FileVisitResult mergeDirectory(Path path);
    }

    public class State {
        private long totalSize;
        private long copiedSize;
        private int passedSecs;
        private int currentSpeed;

        public int getProgress() {
            return (int)(copiedSize * 1.0 / totalSize) * 100;
        }

        public int getPassedSecs() {
            return passedSecs;
        }

        public int getRemainSecs() {
            if (copiedSize == 0)
                return Integer.MAX_VALUE;
            return (int)(1L * passedSecs * (totalSize - copiedSize) / copiedSize);
        }

        public int getAverageSpeed() {
            return (int)(copiedSize / passedSecs);
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
