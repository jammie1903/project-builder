package com.jamie.builder.models;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("projects")
public class Projects {

    @XStreamImplicit(itemFieldName = "project")
    private List<Project> projects = new ArrayList<>();

    public List<Project> getProjects() {
        if(projects == null) {
            projects = new ArrayList<>();
        }
        return projects;
    }

}
