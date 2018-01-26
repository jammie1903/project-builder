package com.jamie.builder.models;

import com.jamie.builder.data.ConsoleLogBuilder;
import com.jamie.builder.data.LogDebounce;
import com.jamie.builder.enums.AnsiCode;
import javafx.beans.property.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class Build {
    private StringProperty log = new SimpleStringProperty("");
    private Consumer<String> onLogUpdate = null;
    private ConsoleLogBuilder consoleLogBuilder = new ConsoleLogBuilder();
    private ObjectProperty<Date> started = new SimpleObjectProperty<>();
    private ObjectProperty<Date> complete = new SimpleObjectProperty<>();
    private BooleanProperty successful = new SimpleBooleanProperty();
    private List<Task> tasks = new ArrayList<>();
    private int stage = -1;
    private boolean killed = false;

    private LogDebounce buffer = new LogDebounce(this::doUpdateLog);

    public void addTask(Task task) {
        if (!started.isNotNull().get()) {
            tasks.add(task);
            task.setBuild(this);
        } else {
            throw new UnsupportedOperationException("Cannot add a task once the build has started");
        }
    }

    public void start() {
        if (!started.isNotNull().get()) {
            if(tasks.isEmpty()) {
                addTask(new PlaceholderTask());
            }
            started.set(new Date());
            nextTask();
        } else {
            throw new UnsupportedOperationException("The Build has already been started");
        }
    }

    public void kill() {
        tasks.get(stage).kill();
        this.killed = true;
    }

    private void nextTask() {
        stage++;
        if (stage >= tasks.size() || killed) {
            successful.set(stage >= tasks.size());
            complete.set(new Date());
            if(killed) {
                updateLog('\n' + AnsiCode.BRIGHT_RED.getDisplayValue() + "BUILD CANCELLED" + AnsiCode.RESET.getDisplayValue());
            }
            buffer.end();
        } else {
            try {
                tasks.get(stage).run();

                tasks.get(stage).complete.addListener(
                        (observable, oldValue, newValue) -> {
                            if (newValue) {
                                if (tasks.get(stage).successfulProperty().get()) {
                                    nextTask(); //TODO check for errors
                                } else {
                                    successful.set(false);
                                    complete.set(new Date());
                                    if(killed) {
                                        updateLog('\n' + AnsiCode.BRIGHT_RED.getDisplayValue() + "BUILD CANCELLED" + AnsiCode.RESET.getDisplayValue());
                                    }
                                    buffer.end();
                                }
                            }
                        }
                );
            } catch (Exception e) {
                e.printStackTrace(); // TODO handle
            }
        }
    }

    protected void updateLog(String log) {
        buffer.add(log);
    }

    private void doUpdateLog(String update) {
        consoleLogBuilder.append(update);
        if (onLogUpdate != null) {
            onLogUpdate.accept(update);
        }
    }

    public void setOnLogUpdate(Consumer<String> onLogUpdate) {
        this.onLogUpdate = onLogUpdate;
    }

    public StringProperty logProperty() {
        return log;
    }

    public ReadOnlyObjectProperty<Date> startedProperty() {
        return started;
    }

    public ReadOnlyObjectProperty<Date> completeProperty() {
        return complete;
    }

    public ReadOnlyBooleanProperty successfulProperty() {
        return successful;
    }

    public void addTasks(List<Task> tasks) {
        tasks.forEach(this::addTask);
    }

    public ConsoleLogBuilder getConsoleLogBuilder() {
        return consoleLogBuilder;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (buffer != null) {
            buffer.end();
        }
    }

    public boolean isKilled() {
        return killed;
    }
}
