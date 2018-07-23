package com.jamie.builder.components.root;

import com.jamie.builder.components.componentlistitem.ComponentListItemControl;
import com.jamie.builder.components.editproject.EditProjectController;
import com.jamie.builder.data.DataController;
import com.jamie.builder.models.Build;
import com.jamie.builder.models.Component;
import com.jamie.builder.models.Project;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class RootController implements Initializable {

    @FXML
    private ScrollPane consoleScrollPane;
    @FXML
    private TextFlow console;

    @FXML
    private Button buildSingleButton;

    @FXML
    private Button buildChainButton;

    @FXML
    private Button buildCancelButton;

    @FXML
    private Button newProjectButton;

    @FXML
    private Button editProjectButton;

    @FXML
    private ComboBox<Project> projectSelector;

    @FXML
    private ListView<Component> componentList;

    private BooleanProperty buildInProgress = new SimpleBooleanProperty(false);
    private BooleanProperty buildCancelled = new SimpleBooleanProperty(false);
    private Build boundBuild = null;

    private Queue<List<Component>> buildQueue = new ArrayDeque<>();

    private ListChangeListener<Text> textListener = event -> {
        while (event.next()) {
            if (event.wasAdded()) {
                console.getChildren().addAll(event.getAddedSubList());
            }
            if (event.wasRemoved()) {
                console.getChildren().removeAll(event.getRemoved());
            }
        }
    };
    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        projectSelector.setItems(FXCollections.observableArrayList(DataController.get().getProjects()));
        projectSelector.getSelectionModel().select(0);

        componentList.setCellFactory(param -> new ListCell<Component>() {
            @Override
            protected void updateItem(Component item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    setGraphic(new ComponentListItemControl(item));
                } else {
                    setGraphic(null);
                }
            }
        });
        componentList.setItems(FXCollections.observableList(projectSelector.getSelectionModel().getSelectedItem().getComponents()));

        componentList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                Platform.runLater(() -> {
                    if (this.boundBuild != null) {
                        this.boundBuild.getConsoleLogBuilder().getText().removeListener(textListener);
                    }
                    if (newValue != null) {
                        Build latestBuild = newValue.getLatestBuild();
                        if (latestBuild != null) {
                            bindBuildToConsole(latestBuild);
                        } else {
                            console.getChildren().clear();
                        }
                    } else {
                        console.getChildren().clear();
                    }
                }));

        buildSingleButton.disableProperty().bind(componentList.getSelectionModel().selectedItemProperty().isNull().or(buildInProgress));
        buildSingleButton.visibleProperty().bind(buildInProgress.not());
        buildChainButton.disableProperty().bind(componentList.getSelectionModel().selectedItemProperty().isNull().or(buildInProgress));
        buildChainButton.visibleProperty().bind(buildInProgress.not());

        buildCancelButton.visibleProperty().bind(buildInProgress);
        buildCancelButton.disableProperty().bind(buildCancelled);

        buildCancelButton.managedProperty().bind(buildCancelButton.visibleProperty());
        buildSingleButton.managedProperty().bind(buildSingleButton.visibleProperty());
        buildChainButton.managedProperty().bind(buildChainButton.visibleProperty());

        projectSelector.disableProperty().bind(buildInProgress);
        newProjectButton.disableProperty().bind(buildInProgress);
        editProjectButton.disableProperty().bind(buildInProgress);

        buildInProgress.addListener((observable, oldValue, newValue) -> {
            if (!newValue && stage != null) {
                Platform.runLater(() -> this.stage.toFront());
            }
        });
    }

    private void bindBuildToConsole(Build latestBuild) {
        Platform.runLater(() -> {
            if (this.boundBuild != null) {
                this.boundBuild.getConsoleLogBuilder().getText().removeListener(textListener);
                this.boundBuild.setOnLogUpdate(null);
            }
            console.getChildren().clear();
            console.getChildren().addAll(latestBuild.getConsoleLogBuilder().getText());
            latestBuild.getConsoleLogBuilder().getText().addListener(textListener);
            this.boundBuild = latestBuild;

            this.consoleScrollPane.layout();
            this.consoleScrollPane.setVvalue(1.0);

            this.boundBuild.setOnLogUpdate((line) -> {
                if (this.consoleScrollPane.getVvalue() > 0.99) {
                    this.consoleScrollPane.layout();
                    this.consoleScrollPane.setVvalue(1.0);
                }
            });
        });
    }

    @FXML
    public void buildChain() {

        List<Component> components = componentList.getItems().subList(componentList.getSelectionModel().getSelectedIndex(), componentList.getItems().size());

        List<Component> buildStep = new ArrayList<>();
        List<String> dependancies = new ArrayList<>();
        components.forEach(c -> {
            if (dependancies.contains(c.getName())) {
                buildQueue.add(new ArrayList<>(buildStep));
                buildStep.clear();
                dependancies.clear();
            }
            buildStep.add(c);
            dependancies.addAll(Arrays.asList(c.getDependantComponents()));
        });
        buildQueue.add(new ArrayList<>(buildStep));

        buildInProgress.set(true);
        runBuildChain();
    }

    private void runBuildChain() {
        List<Component> components = buildQueue.poll();
        IntegerProperty runCount = new SimpleIntegerProperty(0);
        BooleanProperty failed = new SimpleBooleanProperty(false);
        if (components != null && !components.isEmpty()) {
            for (Component component : components) {
                Build build = component.startNewBuild();
                build.completeProperty().addListener((observable, oldValue, newValue) -> {
                    if (!build.successfulProperty().get()) {
                        failed.set(true);
                    }
                    if(!build.fullyCompleteProperty().get()) {
                        build.fullyCompleteProperty().addListener(i -> {
                            if((buildQueue.isEmpty() || failed.get() || buildCancelled.get()) && !isBuildRunning()) {
                                buildInProgress.set(false);
                                buildCancelled.set(false);
                            }
                        });
                    }
                    runCount.set(runCount.get() + 1);
                });
            }
            componentList.getSelectionModel().select(components.get(0));
            bindBuildToConsole(components.get(0).getLatestBuild());

            runCount.addListener((observable, oldValue, newValue) -> {
                if (newValue.intValue() == components.size()) {
                    if (failed.get() || buildCancelled.get()) {
                        if (!isBuildRunning()) {
                            buildInProgress.set(false);
                            buildCancelled.set(false);
                        }
                        buildQueue.clear();
                    } else {
                        runBuildChain();
                    }
                }
            });
        } else {
            if (!isBuildRunning()) {
                buildInProgress.set(false);
                buildCancelled.set(false);
            }
        }
    }

    @FXML
    public void buildSingle() {
        Build build = startBuild();
        build.fullyCompleteProperty().addListener((observable, oldValue, newValue) -> {
            buildInProgress.set(false);
            buildCancelled.set(false);
        });
    }

    private Build startBuild() {
        return startBuild(componentList.getSelectionModel().getSelectedItem());
    }

    private Build startBuild(Component component) {
        Build build = component.startNewBuild();
        bindBuildToConsole(build);
        buildInProgress.set(true);
        return build;
    }

    @FXML
    public void cancelBuild() {
        if (this.buildInProgress.get()) {
            this.buildCancelled.set(true);
            componentList.getItems().forEach((component) -> {
                Build build = component.getCurrentBuild();
                if (build != null) {
                    build.kill();
                }
            });
        }
    }

    public boolean isBuildRunning() {
        return componentList.getItems().stream().anyMatch(c -> c.getCurrentBuild() != null);
    }

    @FXML
    public void newProject() throws IOException {
        openEditScreen(null);
    }

    @FXML
    public void edit() throws IOException {
        openEditScreen(projectSelector.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void selectProject() {
        if (projectSelector.getSelectionModel().getSelectedItem() != null) {
            componentList.setItems(FXCollections.observableList(projectSelector.getSelectionModel().getSelectedItem().getComponents()));
        } else {
            componentList.setItems(FXCollections.observableArrayList());
        }
    }

    private void openEditScreen(Project project) throws IOException {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/icon.png")));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/editProject.fxml"));
        Parent root = loader.load();
        EditProjectController controller = loader.getController();
        controller.init(project);
        controller.setStage(stage);
        controller.setOnClose((saved, savedProject) -> {
            if (saved) {
                projectSelector.setItems(FXCollections.observableArrayList(DataController.get().getProjects()));
                projectSelector.getSelectionModel().select(savedProject);
                componentList.setItems(FXCollections.observableList(savedProject.getComponents()));
            }
        });
        stage.setTitle("Edit Project");
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
