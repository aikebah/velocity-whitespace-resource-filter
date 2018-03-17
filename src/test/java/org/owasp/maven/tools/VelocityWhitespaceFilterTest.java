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
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jeremy Long
 */
public class VelocityWhitespaceFilterTest {

    /**
     * Test of shouldFilter method, of class VelocityWhitespaceFilter.
     */
    @Test
    public void testShouldFilter() {
        VelocityWhitespaceFilter instance = new VelocityWhitespaceFilter();
        File from = null;
        boolean expResult = false;
        boolean result = instance.shouldFilter(from);
        assertEquals(expResult, result);

        from = new File("some.txt");
        expResult = false;
        result = instance.shouldFilter(from);
        assertEquals(expResult, result);

        from = new File("some.vm");
        expResult = true;
        result = instance.shouldFilter(from);
        assertEquals(expResult, result);

        from = new File("some.vtl");
        expResult = true;
        result = instance.shouldFilter(from);
        assertEquals(expResult, result);

        from = new File("some.vsl");
        expResult = true;
        result = instance.shouldFilter(from);
        assertEquals(expResult, result);
    }
}
