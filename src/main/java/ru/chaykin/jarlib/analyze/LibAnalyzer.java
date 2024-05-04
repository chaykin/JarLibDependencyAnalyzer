package ru.chaykin.jarlib.analyze;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import ru.chaykin.jarlib.dependency.Dependency;
import ru.chaykin.jarlib.dependency.DependencyId;

@RequiredArgsConstructor
public class LibAnalyzer {
    private final Path libFolder;

    public void analyze() throws IOException {
	try (Stream<Path> libs = Files.find(libFolder, 1, this::filterJar)) {
	    processLibFolder(libs);
	}
    }

    private void processLibFolder(Stream<Path> libs) {
	Map<DependencyId, Dependency> dependencies = new HashMap<>();
	libs.forEach(jar -> new JarAnalyzer(jar, dependencies).analyze());

	findRootDependencies(dependencies).forEach(d ->writeDependency(d, 0));
    }

    private Stream<Dependency> findRootDependencies(Map<DependencyId, Dependency> dependencies) {
	return dependencies.values().stream()
			.filter(d -> d.getParent() == null)
			.sorted(Comparator.comparing(Dependency::getId));
    }

    private void writeDependency(Dependency dependency, int level) {
	String indent = StringUtils.repeat(" ", level);
	System.out.printf("%s%s%n", indent, dependency);

	dependency.getChildren().forEach(c -> writeDependency(c, level + 2));
    }

    private boolean filterJar(Path lib, BasicFileAttributes attributes) {
	if (!attributes.isDirectory()) {
	    return Optional.ofNullable(lib.getFileName())
			    .map(Path::toString)
			    .map(n -> n.endsWith(".jar"))
			    .orElse(Boolean.FALSE);
	}

	return false;
    }
}
