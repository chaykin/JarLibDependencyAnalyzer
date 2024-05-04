package ru.chaykin.jarlib;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.ArrayUtils;
import ru.chaykin.jarlib.analyze.LibAnalyzer;

public class Application {

    public static void main(String[] args) throws IOException {
	if (ArrayUtils.isEmpty(args)) {
	    System.out.println("There is no lib directory specified");
	    System.exit(1);
	}

	Path libPath = Path.of(args[0]);
	if (!Files.isDirectory(libPath)) {
	    System.out.println("Specified path must be existing directory");
	    System.exit(2);
	}

	new LibAnalyzer(libPath).analyze();
    }
}
