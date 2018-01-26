package com.jamie.builder.models;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandTask extends Task {
    private String[] command;

    public CommandTask(String[] command) {
        this.command = command;
    }

    public boolean performTask() throws Exception {
        updateLog("Running command: " + String.join("; ", this.command));
        ProcessBuilder builder = new ProcessBuilder(this.command);
        builder.redirectErrorStream(true);
        Process p = builder.start();

        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while (true) {
            StringBuilder read = new StringBuilder();

            while (input.ready()) {
                read.append((char) input.read());
            }
            if (read.length() > 0) {
                updateLog(read.toString());
            }
            if (this.kill) {
                p.destroyForcibly();
                break;
            }
            if(!p.isAlive()) {
                break;
            }
            Thread.sleep(100);
        }

        return !this.kill && !log.get().contains("ERROR") && p.exitValue() == 0;
    }
}
