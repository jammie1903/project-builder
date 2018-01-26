package com.jamie.builder.components.editproject;

import com.jamie.builder.models.Component;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import java.util.*;

public class ComponentEditData {
    private Component boundComponent;

    private StringProperty location = new SimpleStringProperty("");
    private StringProperty buildCommand = new SimpleStringProperty("");
    private StringProperty distributionFolder = new SimpleStringProperty("");

    private StringBinding name = Bindings.createStringBinding(this::getName, location);

    private Map<ComponentEditData, BooleanProperty> dependantComponents = new HashMap<>();

    public ComponentEditData() {
        distributionFolder.set("dist");
    }

    public ComponentEditData(Component boundComponent) {
        this.boundComponent = boundComponent;
        location.set(boundComponent.getLocation());
        buildCommand.set(boundComponent.getBuildCommand());
        distributionFolder.set(boundComponent.getDistributionFolder());
    }

    public void initDependantComponents(List<ComponentEditData> allComponents) {
        allComponents.forEach(c -> dependantComponents.put(c, new SimpleBooleanProperty(
                c.boundComponent != null && boundComponent != null &&
                        Arrays.asList(boundComponent.getDependantComponents())
                        .contains(c.boundComponent.getName()))));
    }

    public Component createOrUpdateComponent(ObservableList<ComponentEditData> fullList) {
        List<ComponentEditData> partialList = fullList.subList(fullList.indexOf(this)+1, fullList.size());
        List<String> dependencies = new ArrayList<>();
        partialList.forEach(item -> {
            BooleanProperty set = this.dependantComponents.get(item);
            if(set != null && set.get()) {
                dependencies.add(item.getName());
            }
        });
        String[] dependencyArray = dependencies.toArray(new String[dependencies.size()]);

        if (boundComponent != null) {
            boundComponent.setLocation(location.get());
            boundComponent.setBuildCommand(buildCommand.get());
            boundComponent.setDistributionFolder(distributionFolder.get());
            boundComponent.setDependantComponents(dependencyArray);
        } else {
            boundComponent = new Component(location.get(), buildCommand.get(), distributionFolder.get(), dependencyArray);
        }
        return boundComponent;
    }

    public String getName() {
        return location.get().substring(Math.max(0, location.get().lastIndexOf('\\') + 1));
    }

    public StringProperty buildCommandProperty() {
        return buildCommand;
    }

    public StringProperty distributionFolderProperty() {
        return distributionFolder;
    }

    public StringProperty locationProperty() {
        return location;
    }

    public Map<ComponentEditData, BooleanProperty> getDependantComponents() {
        return dependantComponents;
    }

    public StringBinding nameProperty() {
        return name;
    }

    public boolean isValid() {
        return distributionFolder.isNotEmpty().and(location.isNotEmpty()).get();
    }

    public boolean hasInvalidDependancies(ObservableList<ComponentEditData> fullList) {
        int index = fullList.indexOf(this);
        for(int i = 0; i < index; i++) {
            if(dependantComponents.get(fullList.get(i)).get()){
                return true;
            };
        }
        return false;
    }
}
