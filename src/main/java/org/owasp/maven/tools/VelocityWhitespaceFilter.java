/*
 * Copyright 2018 Jeremy Long.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.owasp.maven.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.shared.filtering.DefaultMavenFileFilter;
import org.apache.maven.shared.filtering.FilterWrapper;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.plexus.build.incremental.BuildContext;

import javax.inject.Inject;

/**
 * Simple resource filter that is used to remove excess whitespace from Velocity
 * Templates using standard Maven resource filtering. Leading whitespace is
 * removed from lines and a trailing Velocity comment (##) is appended to each
 * line to swallow the new line from the resulting output.
 *
 * @author Jeremy Long
 */
@Component(role = MavenFileFilter.class, hint = "default")
public class VelocityWhitespaceFilter extends DefaultMavenFileFilter {

    /**
     * The extensions that are supported.
     */
    private final List<String> extensions = Arrays.asList("vm", "vtl", "vsl");

    @Inject
    public VelocityWhitespaceFilter(final BuildContext buildContext) {
        super(buildContext);
    }

    /**
     * Whether or not the given file should be filtered.
     *
     * @param from the file to test
     * @return true if this filter should transform the file, otherwise false
     */
    protected boolean shouldFilter(File from) {
        if (from == null) {
            return false;
        }
        final String ext = FilenameUtils.getExtension(from.getName());
        return extensions.contains(ext.toLowerCase());
    }

    /**
     * {@inheritDoc} Copies the given file using the
     * {@link org.owasp.maven.tools.VelocityWhitespaceFilteringReader} so that
     * the Velocity Template copied will output less whitespace.
     */
    @Override
    public void copyFile(File from, File to, boolean filtering, List<FilterWrapper> filterWrappers,
            String encoding, boolean overwrite) throws MavenFilteringException {
        List<FilterWrapper> wrappers = filterWrappers;
        if (filtering && shouldFilter(from)) {
            wrappers = new ArrayList<>(filterWrappers);
            wrappers.add(new VelocityWhitespaceFilterWrapper());
        }
        super.copyFile(from, to, filtering, wrappers, encoding, overwrite);
    }
}
