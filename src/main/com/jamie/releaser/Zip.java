package com.jamie.releaser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {

    public static File pack(File file) throws IOException {
        File temp = File.createTempFile(file.getName(), ".zip");
        temp.deleteOnExit();
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(temp.toPath()))) {
            Path pp = Paths.get(file.getCanonicalPath());
            Files.walk(pp)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            System.err.println(e);
                        }
                    });
        }
        return temp;
    }
}
