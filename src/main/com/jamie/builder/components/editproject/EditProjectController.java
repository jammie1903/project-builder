package com.jamie.builder.components.editproject;

import com.jamie.builder.data.DataController;
import com.jamie.builder.models.Component;
import com.jamie.builder.models.Project;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class EditProjectController {

    private BiConsumer<Boolean, Project> onClose;

    @FXML
    private ListView<ComponentEditData> componentList;

    @FXML
    private TextField projectName;

    @FXML
    private TextField buildCommand;

    @FXML
    private TextField distributionFolder;

    @FXML
    private TextField path;

    @FXML
    private CheckBox continuous;

    @FXML
    private TextField initialBuildEndString;

    @FXML
    private ListView<ComponentEditData> dependantComponents;

    @FXML
    private Button shiftUpButton;

    @FXML
    private Button shiftDownButton;

    @FXML
    private VBox editBox;

    private ReadOnlyObjectProperty<ComponentEditData> currentSelection;
    private ReadOnlyIntegerProperty currentSelectionIndex;

    private IntegerProperty componentCount = new SimpleIntegerProperty();

    private Project project = null;
    private Stage stage;
    private boolean createMode = false;

    @FXML
    void cancel() {
        close(null);
    }

    private void close(Project savedProject) {
        stage.close();
        if (onClose != null) {
            onClose.accept(savedProject != null, savedProject);
        }
    }

    public void init(Project projectParam) {
        this.project = projectParam;
        if (this.project == null) {
            this.project = new Project();
            this.createMode = true;
        }
        currentSelection = componentList.getSelectionModel().selectedItemProperty();
        currentSelectionIndex = componentList.getSelectionModel().selectedIndexProperty();
        initialBuildEndString.disableProperty().bind(continuous.selectedProperty().not());

        currentSelection.addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                buildCommand.textProperty().unbindBidirectional(oldValue.buildCommandProperty());
                distributionFolder.textProperty().unbindBidirectional(oldValue.distributionFolderProperty());
                path.textProperty().unbindBidirectional(oldValue.locationProperty());
                continuous.selectedProperty().unbindBidirectional(oldValue.continuousProperty());
                initialBuildEndString.textProperty().unbindBidirectional(oldValue.initialBuildCompleteStringProperty());
            }
            if (newValue != null) {
                buildCommand.textProperty().bindBidirectional(newValue.buildCommandProperty());
                distributionFolder.textProperty().bindBidirectional(newValue.distributionFolderProperty());
                path.textProperty().bindBidirectional(newValue.locationProperty());
                continuous.selectedProperty().bindBidirectional(newValue.continuousProperty());
                initialBuildEndString.textProperty().bindBidirectional(newValue.initialBuildCompleteStringProperty());
            } else {
                buildCommand.setText("");
                distributionFolder.setText("");
                path.setText("");
                continuous.setSelected(false);
                initialBuildEndString.setText("");
            }
            dependantComponents.setItems(FXCollections.observableArrayList());
            dependantComponents.setItems(componentList.getItems());
        });

        List<ComponentEditData> editData = this.project.getComponents().stream()
                .map(ComponentEditData::new).collect(Collectors.toList());
        editData.forEach(c -> c.initDependantComponents(editData));

        componentCount.set(editData.size());

        componentList.getItems().addAll(editData);
        componentList.setCellFactory(param -> new ListCell<ComponentEditData>() {
            @Override
            protected void updateItem(ComponentEditData item, boolean empty) {
                this.textProperty().unbind();
                super.updateItem(item, empty);
                if (!empty) {
                    this.textProperty().bind(item.nameProperty());
                } else {
                    this.textProperty().set("");
                }
            }
        });
        dependantComponents.setItems(componentList.getItems());

        dependantComponents.setCellFactory(f -> new CheckBoxListCell<ComponentEditData>(item -> {
            if (currentSelection.get() != null) {
                return currentSelection.get().getDependantComponents().get(item);
            } else {
                return new SimpleBooleanProperty(false);
            }
        }) {
            @Override
            public void updateItem(ComponentEditData item, boolean empty) {
                this.textProperty().unbind();
                super.updateItem(item, empty);
                if (!empty) {
                    this.setDisable(currentSelection.isNull().get() || item.equals(currentSelection.get()) || currentSelectionIndex.get() > this.getIndex());
                    this.textProperty().bind(item.nameProperty());
                } else {
                    this.textProperty().set("");
                    this.setDisable(false);
                }
            }
        });

        shiftUpButton.disableProperty().bind(currentSelection.isNull().or(componentList.getSelectionModel().selectedIndexProperty().isEqualTo(0)));
        shiftDownButton.disableProperty().bind(currentSelection.isNull()
                .or(componentList.getSelectionModel().selectedIndexProperty().isEqualTo(componentCount.subtract(1))));

        projectName.setText(project.getName());

        editBox.disableProperty().bind(currentSelection.isNull());
    }

    @FXML
    void findPath() {
        String filePath = getFile(path.getText());
        if (filePath != null) {
            path.setText(filePath);
        }
    }

    private String getFile(String initialDirectory) {
        DirectoryChooser chooser = new DirectoryChooser();
        File f = new File(initialDirectory);
        if (f.exists()) {
            chooser.setInitialDirectory(f);
        }
        chooser.setTitle("Select component folder");
        File selection = chooser.showDialog(stage);
        if (selection != null) {
            return selection.getAbsolutePath();
        }
        return null;
    }

    private boolean verifySave() {
        for (ComponentEditData data : dependantComponents.getItems()) {
            if (!data.isValid()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Components");
                alert.setContentText("Some components were missing a directory or distribution folder setting. Please fill in these details before continuing.");
                alert.showAndWait();
                return false;
            }
        }
        boolean invalidDependancies = false;
        for (ComponentEditData data : dependantComponents.getItems()) {
            if (data.hasInvalidDependancies(dependantComponents.getItems())) {
                invalidDependancies = true;
                break;
            }
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Save Project");
        alert.setHeaderText("Confirm Save");
        alert.setContentText((invalidDependancies ? "Some components have invalid dependencies, these will be removed upon save.\n" : "") + "Are you sure you want to save these changes?");
        return ButtonType.OK.equals(alert.showAndWait().orElse(null));
    }

    @FXML
    void save() {
        if (verifySave()) {
            List<Component> newList = dependantComponents.getItems().stream().map(data -> data.createOrUpdateComponent(dependantComponents.getItems())).collect(Collectors.toList());
            Project project = new Project();
            project.setName(projectName.getText());
            newList.forEach(c -> c.setProject(project));
            if (this.createMode) {
                DataController.get().addProject(project);
            } else {
                DataController.get().updateProject(this.project, project);
            }
            close(project);
        }
    }

    @FXML
    void addComponent() {
        String filePath = getFile("");
        if (filePath != null) {
            ComponentEditData newData = new ComponentEditData();
            newData.locationProperty().set(filePath);
            newData.initDependantComponents(componentList.getItems());
            componentList.getItems().forEach(item -> item.getDependantComponents().put(newData, new SimpleBooleanProperty(false)));
            componentList.getItems().add(newData);
            componentList.getSelectionModel().select(newData);
            componentCount.set(componentList.getItems().size());
        }
    }

    @FXML
    void deleteComponent() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Component");
        alert.setHeaderText("Confirm Delete");
        alert.setContentText("Are you sure you want to delete this component?");
        if (ButtonType.OK.equals(alert.showAndWait().orElse(null))) {
            ComponentEditData data = currentSelection.get();
            componentList.getSelectionModel().clearSelection();
            componentList.getItems().remove(data);
            componentCount.set(componentList.getItems().size());
        }
    }

    @FXML
    void shiftUp() {
        ComponentEditData data = currentSelection.get();
        if (data != null) {
            int index = componentList.getItems().indexOf(data);
            if (index > 0) {
                componentList.getItems().remove(data);
                componentList.getItems().add(index - 1, data);
                componentList.getSelectionModel().select(data);
            }
        }
    }

    @FXML
    void shiftDown() {
        ComponentEditData data = currentSelection.get();
        if (data != null) {
            int index = componentList.getItems().indexOf(data);
            if (index != -1 && index < componentCount.get() - 1) {
                componentList.getItems().remove(data);
                componentList.getItems().add(index + 1, data);
                componentList.getSelectionModel().select(data);
            }
        }
    }

    public void setOnClose(BiConsumer<Boolean, Project> onClose) {
        this.onClose = onClose;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
