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
        if (args.length != 2) {
            System.out.println("Input names of files!");
            return;
        }

        String input = args[0];
        String output = args[1];
        BufferedReader reader = null;
        OutputStreamWriter writer = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"));
            writer = new OutputStreamWriter(new FileOutputStream(output), "UTF-8");

            String line;
            while ((line = reader.readLine()) != null) {
                Path path = Paths.get(line);
                HashMap<Path, String> hashes = RecursiveWalk.walkHashFNV32(path);
                for (Map.Entry<Path, String> e : hashes.entrySet()) {
                    String resPath = e.getKey().toAbsolutePath().toString();
                    writer.write(e.getValue() + " " + resPath.substring(resPath.indexOf(line)) + "\n");
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (UnsupportedEncodingException e) {}
        catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (reader != null)
                    reader.close();
                if (writer != null)
                    writer.close();
            } catch (IOException ignore) {}
        }
    }
}