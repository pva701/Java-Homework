import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by pva701 on 5/10/15.
 */
public class UIFileCopy extends JFrame {

    private JProgressBar jProgressBar = new JProgressBar();
    private JLabel jLblElapsedSecs = new JLabel();
    private JLabel jLblRemSecs = new JLabel();
    private JLabel jLblAvSpeed = new JLabel();
    private JLabel jLblCurSpeed = new JLabel();
    private JButton jButCancel = new JButton("Отмена");

    public UIFileCopy(String title) {
        super(title);
        JPanel panel = new JPanel();
        panel.setMinimumSize(new Dimension(300, 300));
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(jProgressBar);
        panel.add(jLblElapsedSecs);
        panel.add(jLblRemSecs);
        panel.add(jLblAvSpeed);
        panel.add(jLblCurSpeed);
        jButCancel.addActionListener(e -> {
            System.out.println("clicked cancel");
        });
        panel.add(jButCancel);

        add(panel);
    }

    public void update(FilesCopy.State state) {
        jProgressBar.setValue(state.getProgress());
        jLblElapsedSecs.setText("Прошло: " + state.getElapsedSecs() + " сек");
        jLblRemSecs.setText("Осталось примерно: " + state.getRemainSecs() + " сек");
        jLblAvSpeed.setText("Средняя скорость: " + state.getAverageSpeed() / 1024 + " Кб/cек");
        jLblCurSpeed.setText("Текущая скорость: " + state.getCurrentSpeed() / 1024 + " Кб/сек");
    }

    public static void main(String[] args) throws IOException {
        UIFileCopy mainWindow = new UIFileCopy("UIFileCopy");
        mainWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        mainWindow.pack();
        mainWindow.setVisible(true);

        FilesCopy filesCopy = new FilesCopy(Paths.get("/home/pva701/IdeaProjects"), Paths.get("/home/pva701/hw4"));
        filesCopy.setObserver(new FilesCopy.Observer() {
            @Override
            public void onChangeState(FilesCopy.State state) {
                mainWindow.update(state);
            }

            @Override
            public FileVisitResult replaceFile(Path path) {
                System.out.println("replaced file");
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult mergeDirectory(Path path) {
                System.out.println("merge dir");
                return FileVisitResult.CONTINUE;
            }
        });

        filesCopy.start();
        /*try {
            String hello = "hello";
            Files.newOutputStream(Paths.get("/home/pva701/homework4/hello.txt")).write(hello.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        //Files.copy(Paths.get("/home/pva701/homework4/scripts/"),
          //      Paths.get("/home/pva701/IdeaProjects/scripts/"));
    }
}
