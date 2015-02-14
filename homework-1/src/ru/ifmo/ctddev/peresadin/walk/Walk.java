package ru.ifmo.ctddev.peresadin.walk;

import java.io.*;
import java.nio.file.Files;
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
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"));

            OutputStreamWriter writer = null;
            try {
                writer = new OutputStreamWriter(new FileOutputStream(output), "UTF-8");
            } catch (IOException e) {
                try {
                    writer.close();
                } catch (IOException ignore) {}
                throw e;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                HashMap<Path, String> hashes = RecursiveWalk.walkHashFNV32(Paths.get(line));
                for (Map.Entry<Path, String> e : hashes.entrySet())
                    writer.write(e.getValue() + " " + e.getKey().toAbsolutePath());
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (UnsupportedEncodingException e) {}
        catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                reader.close();
            } catch (IOException ignore) {}
        }
    }
}
