package ru.chaykin.jarlib.dependency;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public class Dependency {
    @Getter
    private final DependencyId id;
    private Path jar;

    @Getter
    private Dependency parent;

    private final Map<DependencyId, Dependency> children = new HashMap<>();

    public Dependency(DependencyId id) {
	this(id, null);
    }

    public Dependency(DependencyId id, Path jar) {
	this.id = id;
	this.jar = jar;
    }

    public void addChild(Dependency child) {
	child.parent = this;
	children.merge(child.id, child, Dependency::merge);
    }

    public Dependency merge(Dependency otherDependency) {
	if (!id.equals(otherDependency.id)) {
	    String errMsg = "Unexpected dependencies to merge: %s and %s".formatted(id, otherDependency.id);
	    throw new IllegalArgumentException(errMsg);
	} else if (parent != null && otherDependency.parent != null && !parent.id.equals(otherDependency.parent.id)) {
	    String errMsg = "Unexpected dependencies to merge: %s and %s (parents must be same)"
			    .formatted(id, otherDependency.id);
	    throw new IllegalArgumentException(errMsg);
	} else if (jar != null && otherDependency.jar != null && !jar.equals(otherDependency.jar)) {
	    String errMsg = "Unexpected dependencies to merge: %s and %s (jar files must be same)"
			    .formatted(id, otherDependency.id);
	    throw new IllegalArgumentException(errMsg);
	}

	if (otherDependency.parent != null) {
	    parent = otherDependency.parent;
	}
	if (otherDependency.jar != null) {
	    jar = otherDependency.jar;
	}

	otherDependency.children.forEach((k, v) -> children.merge(k, v, Dependency::merge));
	return this;
    }

    public Collection<Dependency> getChildren() {
	return children.values();
    }

    @Override
    public String toString() {
	String extra = jar != null ? jar.getFileName().toString() : "no-jar";
	return "%s (%s)".formatted(id, extra);
    }
}
