package com.jamie.builder.models;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class PlaceholderTask extends Task {

    public PlaceholderTask() {
    }

    public boolean performTask() throws Exception {
        updateLog("No build process required by this task.");
        return true;
    }
}
