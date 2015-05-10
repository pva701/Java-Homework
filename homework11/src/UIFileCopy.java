import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

        JButton button = new JButton("Отмена");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("clicked cancel");
            }
        });
        panel.add(button);
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
        //new FilesCopy(Paths.get("/home/pva701/homework4"), Paths.get(".")).start();
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
