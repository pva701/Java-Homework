package ru.ifmo.peresadin.uifilecopy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

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
    private volatile boolean isExit = false;

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
        jButCancel.addActionListener(e->{
            if (isExit)
                exit();
            else
                filesCopy.cancel();
        });
        panel.add(jButCancel);
        add(panel);
    }

    public void exit() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
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
        } else if (state.getStatus() == FilesCopy.State.Status.CANCELED) {
            isExit = true;
            jButCancel.setText("Exit");
            jLblStatus.setText("Cancelled");
        } else if (state.getStatus() == FilesCopy.State.Status.FINISHED) {
            isExit = true;
            jButCancel.setText("Exit");
            jProgressBar.setValue(state.getProgress());
            jLblElapsedSecs.setText("Elapsed: " + state.getElapsedSecs() + " sec");
            jLblStatus.setText("Copying is finished");
        }
    }

    public static void main(String[] args) throws IOException {
        String from = "/home/pva701/IdeaProjects/Dictionary";
        String to = "/home/pva701/tmp/ok";
        /*if (args.length != 2) {
            System.out.println("Usage UIFileCopy <src> <dst>");
            System.exit(0);
        }*/
        //String from = args[0];
        //String to = args[1];
        FilesCopy filesCopy = new FilesCopy(Paths.get(from), Paths.get(to));
        UIFileCopy mainWindow = new UIFileCopy("UIFileCopy", filesCopy);
        mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainWindow.setVisible(true);
        mainWindow.pack();

        new SwingWorker<Integer, FilesCopy.State>() {
            @Override
            protected Integer doInBackground() throws Exception {

                filesCopy.setObserver(new FilesCopy.Observer() {
                    @Override
                    public void onChangeState(FilesCopy.State state) {
                        publish(state);
                    }

                    private volatile boolean skipAll = false;
                    private volatile boolean replaceAll = false;

                    private volatile boolean mergeAll = false;
                    private volatile boolean skipAllDirectory = false;

                    @Override
                    public FileVisitResult replaceFile(Path path) {
                        FutureTask<FileVisitResult> dialogResult = new FutureTask<>(()->{
                            if (replaceAll)
                                return FileVisitResult.CONTINUE;
                            if (skipAll)
                                return FileVisitResult.SKIP_SUBTREE;

                            String[] variants = new String[] {"Replace", "Replace all", "Skip", "Skip all"};
                            Object answer = JOptionPane.showInputDialog(
                                    mainWindow.getContentPane(),
                                    "File '" + path.toString() + "' already exist. Replace?",
                                    "File conflict",
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    variants, 0);
                            if (answer == null) {
                                filesCopy.cancel();
                                return FileVisitResult.TERMINATE;
                            }
                            replaceAll |= answer.equals("Replace all");
                            skipAll |= answer.equals("Skip all");
                            if (answer.equals("Replace") || replaceAll)
                                return FileVisitResult.CONTINUE;
                            return FileVisitResult.SKIP_SUBTREE;
                        });
                        try {
                            SwingUtilities.invokeAndWait(dialogResult);
                        } catch (Exception e) {
                            return FileVisitResult.TERMINATE;
                        }
                        try {
                            return dialogResult.get();
                        } catch (Exception e) {
                            return FileVisitResult.TERMINATE;
                        }
                    }

                    @Override
                    public FileVisitResult mergeDirectory(Path path) {
                        FutureTask<FileVisitResult> dialogResult = new FutureTask<>(()->{
                            if (mergeAll)
                                return FileVisitResult.CONTINUE;
                            if (skipAllDirectory)
                                return FileVisitResult.SKIP_SUBTREE;

                            String[] variants = new String[] {"Merge", "Merge all", "Skip", "Skip all"};
                            Object answer = JOptionPane.showInputDialog(
                                    mainWindow.getContentPane(),
                                    "Directory '" + path.toString() + "' already exist. Merge?",
                                    "Directory conflict",
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    variants, 0);
                            if (answer == null) {
                                filesCopy.cancel();
                                return FileVisitResult.TERMINATE;
                            }
                            mergeAll |= answer.equals("Merge all");
                            skipAllDirectory |= answer.equals("Skip all");
                            if (answer.equals("Merge") || mergeAll)
                                return FileVisitResult.CONTINUE;
                            return FileVisitResult.SKIP_SUBTREE;
                        });

                        try {
                            SwingUtilities.invokeAndWait(dialogResult);
                        } catch (Exception e) {
                            return FileVisitResult.TERMINATE;
                        }
                        try {
                            return dialogResult.get();
                        } catch (Exception e) {
                            return FileVisitResult.TERMINATE;
                        }
                    }
                });

                try {
                    filesCopy.start();
                } catch (RuntimeException e) {
                    JOptionPane.showMessageDialog(mainWindow.getContentPane(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    mainWindow.dispatchEvent(new WindowEvent(mainWindow, WindowEvent.WINDOW_CLOSING));
                    return 1;
                    //e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(mainWindow.getContentPane(), "Something went wrong!", "Error", JOptionPane.ERROR_MESSAGE);
                    mainWindow.dispatchEvent(new WindowEvent(mainWindow, WindowEvent.WINDOW_CLOSING));
                    return 2;
                }
                return 0;
            }

            @Override
            protected void process(List<FilesCopy.State> chunks) {
                mainWindow.update(chunks.get(chunks.size() - 1));
            }
        }.execute();
    }
}
