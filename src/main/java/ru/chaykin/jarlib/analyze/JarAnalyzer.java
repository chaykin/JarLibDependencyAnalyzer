package ru.chaykin.jarlib.analyze;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.chaykin.jarlib.dependency.Dependency;
import ru.chaykin.jarlib.dependency.DependencyId;

import static javax.xml.xpath.XPathConstants.NODESET;

@RequiredArgsConstructor
public class JarAnalyzer {
    private final Path jar;
    private final Map<DependencyId, Dependency> dependencies;

    public void analyze() {
	DependencyId parentId = null;
	Collection<DependencyId> childrenIds = null;

	try (ZipInputStream jarStream = new ZipInputStream(new FileInputStream(jar.toFile()))) {
	    ZipEntry e = jarStream.getNextEntry();
	    while (e != null) {
		if (!e.isDirectory()) {
		    String entryName = e.getName();
		    if (entryName.startsWith("META-INF/")) {
			if (entryName.endsWith("/pom.properties")) {
			    parentId = processOwn(jarStream);
			} else if (entryName.endsWith("/pom.xml")) {
			    childrenIds = processDependencies(CloseShieldInputStream.wrap(jarStream));
			}
		    }
		}
		e = jarStream.getNextEntry();
	    }
	} catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException e) {
	    throw new AnalyzeException("Could not analyze jar " + jar, e);
	}

	if (parentId != null && childrenIds != null) {
	    Dependency parent = dependencies.get(parentId);
	    childrenIds.forEach(c -> parent.addChild(dependencies.get(c)));
	}
    }

    private DependencyId processOwn(InputStream propStream) throws IOException {
	Properties prop = new Properties();
	prop.load(propStream);

	DependencyId dependencyId = new DependencyId(prop);
	Dependency dependency = new Dependency(dependencyId, jar);
	dependencies.merge(dependencyId, dependency, Dependency::merge);

	return dependencyId;
    }

    private Collection<DependencyId> processDependencies(InputStream pomStream)
		    throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
	DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = builderFactory.newDocumentBuilder();
	XPath xPath = XPathFactory.newInstance().newXPath();

	Collection<DependencyId> childrenIds = new HashSet<>();
	Document pom = builder.parse(pomStream);
	NodeList nodes = (NodeList) xPath.compile("/project/dependencies/dependency").evaluate(pom, NODESET);
	for (int i = 0; i < nodes.getLength(); i++) {
	    DependencyId dependencyId = new DependencyId(nodes.item(i));
	    Dependency dependency = new Dependency(dependencyId);
	    dependencies.merge(dependencyId, dependency, Dependency::merge);
	    childrenIds.add(dependencyId);
	}

	return childrenIds;
    }
}
