package com.jamie.builder.components.componentlistitem;

import com.jamie.builder.data.AppTime;
import com.jamie.builder.models.Component;
import com.jamie.builder.models.ComponentDisplayModel;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.LongBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableNumberValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ComponentListItemControl extends VBox {

    private final ComponentDisplayModel model;

    @FXML
    private Label componentName;

    @FXML
    private ProgressIndicator buildInProgress;

    @FXML
    private HBox buildDisplay;

    @FXML
    private Label buildTimeLabel;

    @FXML
    private ProgressBar buildProgress;

    @FXML
    private Label lastBuiltTimeLabel;

    @FXML
    private Label lastBuiltLabel;

    @FXML
    private Label failedLabel;

    private LongBinding durationBinding;
    private StringBinding durationStringBinding;
    private StringBinding lastBuiltTimeBinding;
    private StringBinding lastBuiltStringBinding;

    public ComponentListItemControl(Component component) {
        this.model = component.getDisplayModel();
        load();
        initialise();
    }

    private long getDuration(Date from, Date to) {
        return from == null || to == null ? -1 : to.getTime() - from.getTime();
    }

    private String getDurationString(long duration) {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        return (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }

    private void initialise() {
        componentName.textProperty().bind(this.model.nameProperty());
        buildInProgress.visibleProperty().bind(this.model.runningProperty());
        buildDisplay.visibleProperty().bind(this.model.runningProperty());

        durationBinding = Bindings.createLongBinding(() ->
                        getDuration(this.model.startTimeProperty().get(), AppTime.property().get()),
                this.model.startTimeProperty(), AppTime.property());

        durationStringBinding = Bindings.createStringBinding(() -> getDurationString(durationBinding.get()), durationBinding);
        buildTimeLabel.textProperty().bind(durationStringBinding);

        buildProgress.progressProperty().bind(Bindings.when(model.lastBuildDurationProperty().greaterThan(0))
                .then(Bindings.min(100.0, divideSafe(durationBinding,model.lastBuildDurationProperty())))
                .otherwise(-1.0));

        lastBuiltTimeBinding = Bindings.createStringBinding(() -> getDurationString(model.lastBuildDurationProperty().get()), model.lastBuildDurationProperty());

        lastBuiltTimeLabel.textProperty().bind(lastBuiltTimeBinding);

        DateFormat formatter = new SimpleDateFormat("dd/MM/yy hh:mm:ss");
        lastBuiltStringBinding = Bindings.createStringBinding(() -> model.lastBuildTimeProperty().get()  == null ? "" : "Last built: " + formatter.format(model.lastBuildTimeProperty().get()), model.lastBuildTimeProperty());
        lastBuiltLabel.textProperty().bind(lastBuiltStringBinding);
        lastBuiltLabel.visibleProperty().bind(this.model.runningProperty().not());
        failedLabel.visibleProperty().bind(this.model.failedProperty());
    }

    private void load() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/componentListItem.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

    }


    private static NumberBinding divideSafe(ObservableNumberValue dividend, ObservableNumberValue divisor) {
        return Bindings.createDoubleBinding(() -> {
            if (divisor.getValue().doubleValue() == 0) {
                return 0.0;
            } else {
                return dividend.getValue().doubleValue() / divisor.getValue().doubleValue();
            }
        }, dividend, divisor);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        lastBuiltStringBinding.dispose();
        lastBuiltTimeBinding.dispose();
        durationStringBinding.dispose();
        durationBinding.dispose();
    }
}
