package com.walmart.iota.io;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Io {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static boolean createDirectoryIfNotExists(Path path) {
        if (path == null) {
            return false;
        }

        if (Files.exists(path) && Files.isDirectory(path)) {
            return true;
        }

        return path.toFile().mkdirs();
    }

    public static <T> void writeObjectJSON(Path filepath, T object) throws IOException {
        if (filepath == null || object == null) {
            return;
        }

        Path parent = filepath.getParent();
        if (Files.notExists(parent)) {
            Files.createDirectories(parent);
        }

        if (Files.notExists(filepath)) {
            File file = Files.createFile(filepath).toFile();

            String payload = OBJECT_MAPPER.writeValueAsString(object);
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
                bufferedWriter.write(payload);
                bufferedWriter.flush();
            }
        }
    }
}