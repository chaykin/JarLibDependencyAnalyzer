package ru.chaykin.jarlib.dependency;

import java.util.Properties;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

@EqualsAndHashCode
public class DependencyId implements Comparable<DependencyId> {
    private static final String DEFAULT_SCOPE = "default";

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String scope;

    public DependencyId(Properties jarProps) {
	groupId = jarProps.getProperty("groupId");
	artifactId = jarProps.getProperty("artifactId");
	version = jarProps.getProperty("version");
	scope = DEFAULT_SCOPE;
    }

    public DependencyId(Node dependency) throws XPathExpressionException {
	XPath xPath = XPathFactory.newInstance().newXPath();
	groupId = xPath.compile("groupId/text()").evaluate(dependency);
	artifactId = xPath.compile("artifactId/text()").evaluate(dependency);
	version = xPath.compile("version/text()").evaluate(dependency);
	scope = xPath.compile("scope/text()").evaluate(dependency);
    }

    @Override
    public String toString() {
	if (StringUtils.isBlank(scope) || scope.equals(DEFAULT_SCOPE)) {
	    return "%s:%s:%s".formatted(groupId, artifactId, version);
	} else {
	    return "%s:%s:%s:%s".formatted(groupId, artifactId, version, scope);
	}
    }

    @Override
    public int compareTo(DependencyId o) {
	return toString().compareTo(o.toString());
    }
}
