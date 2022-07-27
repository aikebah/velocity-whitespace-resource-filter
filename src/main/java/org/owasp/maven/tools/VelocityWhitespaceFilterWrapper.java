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

import java.io.Reader;
import org.apache.maven.shared.filtering.FilterWrapper;

/**
 * A Maven Filter Wrapper used to add the
 * {@link org.owasp.maven.tools.VelocityWhitespaceFilteringReader} to the
 * processing of Velocity Template resources.
 *
 * @author Jeremy Long
 */
public class VelocityWhitespaceFilterWrapper extends FilterWrapper {

    /**
     * {@inheritDoc}
     */
    @Override
    public Reader getReader(Reader reader) {
        return new VelocityWhitespaceFilteringReader(reader);
    }
}
