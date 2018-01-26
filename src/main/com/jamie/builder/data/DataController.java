package com.jamie.builder.data;

import com.jamie.builder.models.Component;
import com.jamie.builder.models.Project;
import com.jamie.builder.models.Projects;
import com.thoughtworks.xstream.XStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class DataController {

    private static final String XML_FILE = "data.xml";

    private static DataController instance;
    private Projects projects;
    private XStream xstream = getXstream();

    public static DataController get() {
        if (instance == null) {
            instance = new DataController();
        }
        return instance;
    }

    private DataController() {
        try (InputStream eventStream = Files.newInputStream(Paths.get(XML_FILE))) {
            this.projects = (Projects) xstream.fromXML(eventStream);
        } catch (IOException e) {
            projects = new Projects();
        }
        if(projects.getProjects().isEmpty()) {
            Project project = new Project();
            project.setName("Starting Project");
            projects.getProjects().add(project);
        }
    }

    private XStream getXstream() {
        XStream xstream = new XStream();
        xstream.processAnnotations(Projects.class);
        xstream.processAnnotations(Project.class);
        xstream.processAnnotations(Component.class);
        return xstream;
    }

    public List<Project> getProjects() {
        return projects.getProjects();
    }

    public void save() {
        try {
            Files.write(Paths.get(XML_FILE), xstream.toXML(projects).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addProject(Project project) {
        getProjects().add(project);
        this.save();
    }

    public void updateProject(Project oldProject, Project newProject) {
        getProjects().set(getProjects().indexOf(oldProject), newProject);
        this.save();
    }
}