package ru.ifmo.ctddev.peresadin.walk;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pva701 on 2/14/15.
 */
public class Walk {
    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("Input names of files!");
            return;
        }

        String input = args[0];
        String output = args[1];
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"));
            try {
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(output), "UTF-8");
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Path path = Paths.get(line);
                        new HashCalcFileVisitor(path, new FNVHash32(), writer).walkHash();
                    }
                } catch (FileNotFoundException e) {
                    System.out.println(e.getMessage());
                } finally {
                    writer.close();
                }
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
