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

import java.io.StringReader;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jeremy Long
 */
public class VelocityWhitespaceFilteringReaderTest {

    /**
     * Test of read method, of class VelocityWhitespaceFilteringReader.
     */
    @Test
    public void testRead() throws Exception {
        try (StringReader in = new StringReader("   1");
                VelocityWhitespaceFilteringReader instance = new VelocityWhitespaceFilteringReader(in)) {
            int expResult = '1';
            int result = instance.read();
            assertEquals(expResult, result);
        }

        try (StringReader in = new StringReader("  \t  \n ");
                VelocityWhitespaceFilteringReader instance = new VelocityWhitespaceFilteringReader(in)) {
            int expResult = -1;
            int result = instance.read();
            assertEquals(expResult, result);
        }

        try (StringReader in = new StringReader("a\na\r");
                VelocityWhitespaceFilteringReader instance = new VelocityWhitespaceFilteringReader(in)) {
            int expResult = 'a';
            int result = instance.read();
            assertEquals(expResult, result);
            expResult = '#';
            result = instance.read();
            assertEquals(expResult, result);
            expResult = '#';
            result = instance.read();
            assertEquals(expResult, result);
            expResult = '\n';
            result = instance.read();
            assertEquals(expResult, result);

            expResult = 'a';
            result = instance.read();
            assertEquals(expResult, result);
            expResult = '#';
            result = instance.read();
            assertEquals(expResult, result);
            expResult = '#';
            result = instance.read();
            assertEquals(expResult, result);
            expResult = '\r';
            result = instance.read();
            assertEquals(expResult, result);
            expResult = -1;
            result = instance.read();
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of read method, of class VelocityWhitespaceFilteringReader.
     */
    @Test
    public void testRead_3args() throws Exception {
        try (StringReader in = new StringReader("   test\n#[[unmodified]]\n]]#\n\n\n  first\n#*\ncomment  \r*#test\n");
                VelocityWhitespaceFilteringReader instance = new VelocityWhitespaceFilteringReader(in)) {
            char[] cbuf = new char[50];
            int offset = 0;
            int length = 50;
            int expResult = 50;
            int result = instance.read(cbuf, offset, length);
            String expectedContent = "test##\n#[[unmodified]]\n]]###\nfirst##\n#*\ncomment  \r";
            assertEquals(expResult, result);
            String test = new String(cbuf);
            assertEquals(expectedContent, test);

            cbuf = new char[9];
            result = instance.read(cbuf, offset, length);
            expResult = 9;

            expectedContent = "*#test##\n";
            assertEquals(expResult, result);
            test = new String(cbuf);
            assertEquals(expectedContent, test);
        }
    }
}
