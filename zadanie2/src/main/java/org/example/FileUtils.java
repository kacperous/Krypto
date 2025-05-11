package org.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {

    public static byte[] readFileToBytes(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }

    public static void writeBytesToFile(byte[] content, String path) throws IOException {
        Files.write(Paths.get(path), content);
    }

    public static String readFileToString(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    public static void writeStringToFile(String content, String path) throws IOException {
        Files.write(Paths.get(path), content.getBytes(StandardCharsets.UTF_8));
    }
}
