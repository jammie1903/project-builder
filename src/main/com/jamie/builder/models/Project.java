package com.jamie.builder.models;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("project")
public class Project {

    @XStreamImplicit(itemFieldName = "component")
    private List<Component> components = new ArrayList<>();

    @XStreamAsAttribute
    private String name = "";

    private Object readResolve() {
        components.forEach(c -> c.setProject(this));
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Component> getComponents() {
        if (components == null) {
            components = new ArrayList<>();
        }
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    protected void addComponent(Component component) {
        if(!this.components.contains(component)) {
            this.components.add(component);
        }
    }

    protected void removeComponent(Component component) {
        this.components.remove(component);
    }

    public Component getNext(Component component) {
        int index = components.indexOf(component);
        if(index != -1 && index != components.size()-1) {
            return components.get(index+1);
        }
        return null;
    }

    public Component getByName(String componentName) {
        return components.stream().filter(c -> c.getName().equals(componentName)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return getName();
    }
}
