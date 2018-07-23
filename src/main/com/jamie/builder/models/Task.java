package com.jamie.builder.models;

import javafx.application.Platform;
import javafx.beans.property.*;

public abstract class Task {

    private Build build;
    protected StringProperty log = new SimpleStringProperty("");
    protected StringBuffer logBuffer = new StringBuffer();
    protected BooleanProperty started = new SimpleBooleanProperty();
    protected BooleanProperty complete = new SimpleBooleanProperty();
    protected BooleanProperty fullyComplete = new SimpleBooleanProperty();
    protected BooleanProperty successful = new SimpleBooleanProperty();
    protected boolean kill = false;

    protected boolean continuous;
    protected String initialBuildCompletionString;

    private Thread thread;

    public Task() {
    }

    abstract boolean performTask() throws Exception;

    final public void run() {
        this.thread = new Thread(() -> {
            try {
                boolean result = performTask();
                if(!complete.get()) {
                    successful.set(result);
                }
                fullyComplete.set(true);
            } catch (Exception e) {
                successful.set(false);
                e.printStackTrace();
            }
            if(!complete.get()) {
                complete.set(true);
            }
        });
        started.set(true);
        thread.start();
    }

    public ReadOnlyBooleanProperty startedProperty() {
        return started;
    }

    public ReadOnlyBooleanProperty completeProperty() {
        return complete;
    }

    public BooleanProperty fullyCompleteProperty() {
        return fullyComplete;
    }

    public ReadOnlyBooleanProperty successfulProperty() {
        return successful;
    }

    public ReadOnlyStringProperty logProperty() {
        return log;
    }

    public void setBuild(Build build) {
        this.build = build;
    }

    protected void updateLog(String line) {
        updateLog(line, true);
    }

    protected void updateLog(String line, boolean newLine) {
        String appendLine = line + (newLine ? '\n' : "");
        logBuffer.append(appendLine);
        if (newLine) {
            logBuffer.append("\n");
        }
        final String updatedLog = logBuffer.toString();
        Platform.runLater(() -> log.set(updatedLog));
        build.updateLog(appendLine);
    }

    public void kill() {
        this.kill = true;
    }
}
