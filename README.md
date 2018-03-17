[![Build Status](https://travis-ci.org/jeremylong/velocity-whitespace-resource-filter.svg?branch=master)](https://travis-ci.org/jeremylong/velocity-whitespace-resource-filter) [![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://github.com/jeremylong/velocity-whitespace-resource-filter/blob/master/LICENCE.txt) 

velocity-whitespace-resource-filter
--------------------------
The velocity-whitespace-resource-filter is used in Maven builds to remove whitespace from Velocity Templates.
Leading whitespace is removed from each line and a Velocity single line comment (##) is appended to the end of each
line - making the output from the template more compact. This is useful when the templates generate output like XML,
HTML, or JSON.

Files Processed
--------------------------
The `velocity-whitespace-resource-filter` only supports the following file extensions: vm, vtl, and vsl. If a different
extension is used they will not be processed. Additionally, there is no current way to expand the list of file
extensions - pull requests are welcome.

Usage
--------------------------
The following snippet from a standard maven `pom.xml` shows how to use the `velocity-whitespace-resource-filter`.
The resources to filter must be specified in the resources section and the `velocity-whitespace-resource-filter`
must be defined as a dependency of the `maven-resources-plugin:

```xml
<build>
    <resources>
        <resource>
            <directory>src/main/resources</directory>
            <includes>
                <include>**/*.vtl</include>
            </includes>
            <filtering>true</filtering>
        </resource>
    </resources>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-resources-plugin</artifactId>
            <version>3.0.2</version>
            <dependencies>
                <dependency>
                    <groupId>org.owasp.maven-tools</groupId>
                    <artifactId>velocity-whitespace-resource-filter</artifactId>
                    <version>1.0.0</version>
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</build>
```
