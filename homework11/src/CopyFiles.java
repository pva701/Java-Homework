import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by pva701 on 5/10/15.
 */
public class CopyFiles {
    private Observer observer;
    private boolean isCanceled;

    public CopyFiles(String path1, String path2) throws IOException {
        Path from = Paths.get(path1);
        Path to = Paths.get(path2);
        Files.walkFileTree(from, new CopyFileVisitor());
        /*Path sourceFile = Paths.get(path1);
        try (InputStream is = Files.newInputStream(sourceFile)) {
            byte[] bytes = new byte[4096];
            int c = 0;
            while ((c = is.read()) >= 0) {
                System.out.println("c = " + c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public void start() {
        if (isCanceled)
            throw new IllegalStateException("Cancelled before start copy!");
        State currentState = new State();
    }

    public void cancel() {
        isCanceled = true;
        //TODO write this
    }

    public void setObserver(Observer observer) {
        this.observer = observer;
    }

    public class CopyFileVisitor implements FileVisitor<Path> {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return null;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            return null;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return null;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return null;
        }
    }

    public interface Observer {
        public enum Operation {CANCEL, SKIP, REPLACE_OR_MERGE}
        void onChangeState(State state);
        Operation replaceFile(Path path);
        Operation mergeDirectory(Path path);
    }

    public class State {
        private int progress;
        private int passedSecs;
        private int remainSecs;
        private int averageSpeed;
        private int currentSpeed;

        public int getProgress() {
            return progress;
        }

        public int getPassedSecs() {
            return passedSecs;
        }

        public int getRemainSecs() {
            return remainSecs;
        }

        public int getAverageSpeed() {
            return averageSpeed;
        }

        public int getCurrentSpeed() {
            return currentSpeed;
        }
    }
}
