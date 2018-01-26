package com.jamie.builder.models;

import com.jamie.builder.data.DataController;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import javafx.application.Platform;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@XStreamAlias("component")
public class Component {

    @XStreamOmitField
    private Project project;

    @XStreamOmitField
    private String name;
    @XStreamAsAttribute
    private String location;
    @XStreamAsAttribute
    private String buildCommand;
    @XStreamAsAttribute
    private String distributionFolder;

    @XStreamAsAttribute
    private Date lastBuildTime;

    @XStreamAsAttribute
    private long lastBuildDuration;

    @XStreamImplicit(itemFieldName = "dependant-component")
    private String[] dependantComponents = new String[0];

    @XStreamOmitField
    private List<Build> historicBuilds = new ArrayList<>();
    @XStreamOmitField
    private Build currentBuild;

    @XStreamOmitField
    private ComponentDisplayModel model = new ComponentDisplayModel();

    public Component(String location, String buildCommand, String distributionFolder, String[] dependantComponents) {
        this(location, buildCommand, distributionFolder);
        setDependantComponents(dependantComponents);
    }

    public Component(String location, String buildCommand, String distributionFolder) {
        setLocation(location);
        setBuildCommand(buildCommand);
        setDistributionFolder(distributionFolder);
    }

    private Object readResolve() {
        if (model == null) {
            model = new ComponentDisplayModel();
            model.setLastBuildTime(this.lastBuildTime);
            model.setLastBuildDuration(this.lastBuildDuration);
        }
        if (name == null) {
            name = location.substring(Math.max(0, location.lastIndexOf('\\') + 1));
            model.setName(this.name);
        }
        if (historicBuilds == null) {
            historicBuilds = new ArrayList<>();
        }
        if (dependantComponents == null) {
            dependantComponents = new String[0];
        }
        return this;
    }

    public void setProject(Project project) {
        if (this.project != null) {
            this.project.removeComponent(this);
        }
        this.project = project;
        project.addComponent(this);
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        this.name = location.substring(Math.max(0, location.lastIndexOf('\\') + 1));
        Platform.runLater(() -> model.setName(this.name));
    }

    public String getBuildCommand() {
        return buildCommand;
    }

    public void setBuildCommand(String buildCommand) {
        this.buildCommand = buildCommand;
    }

    public String[] getDependantComponents() {
        return dependantComponents;
    }

    public void setDependantComponents(String[] dependantComponents) {
        this.dependantComponents = dependantComponents;
    }

    public String getDistributionFolder() {
        return distributionFolder;
    }

    public void setDistributionFolder(String distributionFolder) {
        this.distributionFolder = distributionFolder;
    }

    private String[] getFullCommand() {
        if (this.buildCommand == null || this.buildCommand.trim().isEmpty()) {
            return null;
        }
        String drive = location.substring(0, 1);
        String mainCommand = "cd \"" + location + "\" && " + this.buildCommand;
        return new String[]{"cmd.exe", "/" + drive, mainCommand};
    }

    private List<Task> getCopyTasks() {
        return Arrays.asList(this.dependantComponents).stream()
                .map(component -> project.getByName(component))
                .filter(component -> component != null)
                .map(component -> new LinkDirectoryTask(this.getLocation() + File.separator + this.getDistributionFolder(),
                        component.getLocation() + File.separator + "node_modules" + File.separator + this.getName() + File.separator + this.getDistributionFolder()))
                .collect(Collectors.toList());
    }

    public Build getCurrentBuild() {
        return currentBuild;
    }

    public Build getLatestBuild() {
        if (this.currentBuild != null) {
            return this.currentBuild;
        } else if (this.historicBuilds.size() > 0) {
            return this.historicBuilds.get(this.historicBuilds.size() - 1);
        }
        return null;
    }

    public Build startNewBuild() {
        if (currentBuild != null) {
            throw new UnsupportedOperationException("There is already a build running");
        }

        Build build = new Build();
        String[] buildCommand = getFullCommand();
        if (buildCommand != null) {
            build.addTask(new CommandTask(buildCommand));
        }
        build.addTasks(getCopyTasks());
        this.currentBuild = build;
        build.completeProperty().addListener(observable -> {
            historicBuilds.add(this.currentBuild);
            this.currentBuild = null;
            if(build.successfulProperty().get() || !build.isKilled()) {
                this.lastBuildTime = build.startedProperty().get();
                this.lastBuildDuration = build.completeProperty().get().getTime() - build.startedProperty().get().getTime();
            }
            Platform.runLater(() -> {
                if(build.successfulProperty().get() || !build.isKilled()) {
                    model.setLastBuildTime(this.lastBuildTime);
                    model.setLastBuildDuration(this.lastBuildDuration);
                }
                model.setRunning(false);
                model.setFailed(build.successfulProperty().not().get());
            });
            DataController.get().save();
        });
        build.start();
        Platform.runLater(() -> {
            model.setStartTime(build.startedProperty().get());
            model.setRunning(true);
            model.setFailed(false);
        });
        return build;
    }

    public Component getNext() {
        return project.getNext(this);
    }

    public ComponentDisplayModel getDisplayModel() {
        return model;
    }
}
