package com.jamie.builder.models;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandTask extends Task {
    private String[] command;

    public CommandTask(String[] command, boolean continuous, String initialBuildCompletionString) {
        this.command = command;
        this.continuous = continuous;
        this.initialBuildCompletionString = initialBuildCompletionString;
    }

    public boolean performTask() throws Exception {
        updateLog("Running command: " + String.join("; ", this.command));
        ProcessBuilder builder = new ProcessBuilder(this.command);
        builder.redirectErrorStream(true);
        Process p = builder.start();

        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        if (continuous) {
            checkInitialBuildComplete("");
        }
        while (true) {
            StringBuilder read = new StringBuilder();

            while (input.ready()) {
                read.append((char) input.read());
            }
            if (read.length() > 0) {
                String line = read.toString();
                if (continuous && !complete.get()) {
                    this.checkInitialBuildComplete(line);
                }
                updateLog(line);
            }
            if (this.kill) {
                p.destroyForcibly();
                break;
            }
            if (!p.isAlive()) {
                break;
            }
            Thread.sleep(100);
        }

        return !this.kill && !log.get().contains("ERROR") && p.exitValue() == 0;
    }

    private void checkInitialBuildComplete(String line) {
        if (this.initialBuildCompletionString == null || line.contains(this.initialBuildCompletionString)) {
            successful.set(!log.get().contains("ERROR"));
            complete.set(true);
        }
    }
}
