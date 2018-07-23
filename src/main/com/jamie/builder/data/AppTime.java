package com.jamie.builder.data;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class AppTime {

    private final ObjectProperty<Date> time = new SimpleObjectProperty<>(new Date());

    private static AppTime instance;
    private Timer timer;

    private AppTime() {
        timer = new Timer();
        timer.scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        Platform.runLater(() -> time.set(new Date()));
                    }
                }, 500, 500
        );
    }

    private void endTimer() {
        timer.cancel();
    }

    public static ReadOnlyObjectProperty<Date> property() {
        if (instance == null) {
            instance = new AppTime();
        }
        return instance.time;
    }

    public static void end() {
        if (instance != null) {
            instance.endTimer();
        }
    }
}
