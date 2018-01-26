package com.jamie.builder.models;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class LinkDirectoryTask extends Task {
    private File to;
    private File from;

    public LinkDirectoryTask(String from, String to) {
        this.to = new File(to);
        this.from = new File(from);
    }

    public boolean performTask() throws Exception {
        updateLog("Checking for symlink between " + from.getPath() + " and " + to.getPath());
        try {
            if (to.exists() && from.exists() && to.toPath().toRealPath().equals(from.toPath().toRealPath())) {
                updateLog("Symlink Exists");
            } else {
                updateLog("Symlink Not found" + (to.exists() ? ", deleting existing folder at " + to.getPath() : ""));
                if (to.exists()) {
                    if (!deleteDir(to)) {
                        updateLog("Folder could not be deleted, copy aborted");
                        return false;
                    } else {
                        updateLog("Folder deleted");
                    }
                }

                updateLog("Creating Symlink");
                try {
                    Files.createSymbolicLink(to.toPath(), from.toPath());
                    updateLog("Symlink created");
                } catch (Exception e) {
                    updateLog("Symlink creation failed. " + e.getClass().getName() + ": " + e.getMessage());
                    updateLog("Copying directory");
                    Files.createDirectory(to.toPath());
                    copyFolder(from, to);
                    updateLog("Copying completed");
                }
            }
            return true;

        } catch (Exception e) {
            updateLog("Failed to bind " + from.getPath() + " and " + to.getPath() + ": " + e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
    }

    private boolean deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (!deleteDir(f)) {
                    return false;
                }
            }
        }
        return file.delete();
    }

    private void copyFolder(File sourceFolder, File destinationFolder) throws IOException {
        if (sourceFolder.isDirectory()) {
            if (!destinationFolder.exists() && !destinationFolder.mkdir()) {
                updateLog("Directory " + destinationFolder.getPath() + " could not be created");
                return;
            }

            String files[] = sourceFolder.list();
            if (files != null) {
                for (String file : files) {
                    File srcFile = new File(sourceFolder, file);
                    File destFile = new File(destinationFolder, file);
                    copyFolder(srcFile, destFile);
                }
            }
        } else {
            Files.copy(sourceFolder.toPath(), destinationFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
