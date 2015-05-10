import javax.swing.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by pva701 on 5/10/15.
 */
public class UIFileCopy extends JFrame {

    public static void addComponents(JPanel panel) {//swing is holy shit
        panel.add(new JProgressBar());
        JLabel jLabel = new JLabel("Прошло: ");
        jLabel.setMinimumSize(new Dimension(40, 20));
        panel.add(jLabel);
        panel.add(new JLabel("Осталось: "));
        panel.add(new JLabel("Средняя скорость: "));
        panel.add(new JLabel("Текущая скорость: "));
        panel.add(new JButton("Отмена"));
    }

    public static void main(String[] args) throws IOException {
        JFrame mainWindow = new JFrame("UIFileCopy");
        mainWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setMinimumSize(new Dimension(300, 300));
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        addComponents(panel);
        mainWindow.add(panel);

        mainWindow.pack();
        mainWindow.setVisible(true);

        Files.copy(Paths.get("/home/pva701/homework4/scripts/"),
                Paths.get("/home/pva701/IdeaProjects/scripts/"));
    }
}
