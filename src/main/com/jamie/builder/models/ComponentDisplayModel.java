package com.jamie.builder.models;

import javafx.beans.property.*;

import java.util.Date;

public class ComponentDisplayModel {

    private StringProperty name = new SimpleStringProperty();
    private ObjectProperty<Date> lastBuildTime = new SimpleObjectProperty<>();
    private LongProperty lastBuildDuration = new SimpleLongProperty();
    private BooleanProperty running = new SimpleBooleanProperty();
    private BooleanProperty failed = new SimpleBooleanProperty();
    private ObjectProperty<Date> startTime = new SimpleObjectProperty<>();

    protected void setName(String name) {
        this.name.set(name);
    }

    protected void setLastBuildDuration(long lastBuildDuration) {
        this.lastBuildDuration.set(lastBuildDuration);
    }

    protected void setLastBuildTime(Date lastBuildTime) {
        this.lastBuildTime.set(lastBuildTime);
    }

    protected void setRunning(boolean running) {
        this.running.set(running);
    }

    public void setFailed(boolean failed) {
        this.failed.set(failed);
    }

    protected void setStartTime(Date startTime) {
        this.startTime.set(startTime);
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    public ReadOnlyLongProperty lastBuildDurationProperty() {
        return lastBuildDuration;
    }

    public ReadOnlyObjectProperty<Date> lastBuildTimeProperty() {
        return lastBuildTime;
    }

    public ReadOnlyBooleanProperty runningProperty() {
        return running;
    }

    public BooleanProperty failedProperty() {
        return failed;
    }

    public ReadOnlyObjectProperty<Date> startTimeProperty() {
        return startTime;
    }
}
