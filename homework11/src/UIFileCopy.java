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
    private final int STRUT_SIZE = 10;
    private JLabel jLblStatus = new JLabel();
    private JProgressBar jProgressBar = new JProgressBar();
    private JLabel jLblElapsedSecs = new JLabel();
    private JLabel jLblRemSecs = new JLabel();
    private JLabel jLblAvSpeed = new JLabel();
    private JLabel jLblCurSpeed = new JLabel();
    private JButton jButCancel = new JButton("Cancel");

    public UIFileCopy(String title, FilesCopy filesCopy) {
        super(title);

        JPanel panel = new JPanel();
        setMinimumSize(new Dimension(500, 250));

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        jLblStatus.setFont(jLblStatus.getFont().deriveFont(15.0f));
        panel.add(jLblStatus);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        jProgressBar.setStringPainted(true);
        panel.add(jProgressBar);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(jLblElapsedSecs);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(jLblRemSecs);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(jLblAvSpeed);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(jLblCurSpeed);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        jButCancel.addActionListener(e -> filesCopy.cancel());
        panel.add(jButCancel);
        add(panel);

        filesCopy.setObserver(new FilesCopy.Observer() {
            @Override
            public void onChangeState(FilesCopy.State state) {
                update(state);
            }

            @Override
            public FileVisitResult replaceFile(Path path) {
                //System.out.println("replaced file = " + path.toString());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult mergeDirectory(Path path) {
                //System.out.println("merge dir");
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void update(FilesCopy.State state) {
        if (state.getStatus() == FilesCopy.State.Status.COPYING) {
            jLblStatus.setText("Copying is running");
            jProgressBar.setValue(state.getProgress());
            jLblElapsedSecs.setText("Elapsed: " + state.getElapsedSecs() + " sec");
            if (state.getRemainSecs() != Integer.MAX_VALUE)
                jLblRemSecs.setText("Remain approximate: " + state.getRemainSecs() + " sec");
            
            jLblAvSpeed.setText("Average speed: " + state.getAverageSpeed() / 1024 + " kB/sec");
            jLblCurSpeed.setText("Current speed: " + state.getCurrentSpeed() / 1024 + " kB/sec");
        } else if (state.getStatus() == FilesCopy.State.Status.PRE) {
            jLblStatus.setText("Preparing to copying, please wait");
            jLblElapsedSecs.setText("Elapsed: " + state.getElapsedSecs() + " sec");
            //jLblRemSecs.setText("Remain approximate: Unknown");
            //jLblAvSpeed.setText("Average speed: Unknown");
            //jLblCurSpeed.setText("Current speed: Unknown");
        } else if (state.getStatus() == FilesCopy.State.Status.CANCELED) {
            jLblStatus.setText("Cancelled");
        } else if (state.getStatus() == FilesCopy.State.Status.FINISHED) {
            jProgressBar.setValue(state.getProgress());
            jLblElapsedSecs.setText("Elapsed: " + state.getElapsedSecs() + " sec");
            jLblStatus.setText("Copying is finished");
        }
    }

    public static void main(String[] args) throws IOException {
        FilesCopy filesCopy = new FilesCopy(Paths.get("/home/pva701/IdeaProjects"), Paths.get("/home/pva701/tmp"));
        UIFileCopy mainWindow = new UIFileCopy("UIFileCopy", filesCopy);
        mainWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainWindow.pack();
        mainWindow.setVisible(true);

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
