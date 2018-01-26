package com.jamie.builder.data;

import javafx.application.Platform;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class LogDebounce {

    private final Consumer<String> onLogUpdated;
    private Timer timer = new Timer();
    private TimerTask task;
    private StringBuffer logBuffer = new StringBuffer();

    public LogDebounce(Consumer<String> onLogUpdated) {
        this.onLogUpdated = onLogUpdated;
    }

    public void add(String line) {
        logBuffer.append(line);
        schedule();
    }

    private void emit() {
        String log = logBuffer.toString();
        logBuffer.delete(0, logBuffer.length());
        Platform.runLater(() -> {
            onLogUpdated.accept(log);
        });
    }

    private void schedule() {
        if(task != null) {
            task.cancel();
        }
        task = new TimerTask() {
            @Override
            public void run() {
                emit();
            }
        };
        timer.schedule(task, 25);
    }

    public void end() {
        timer.cancel();
        emit();
    }
}
