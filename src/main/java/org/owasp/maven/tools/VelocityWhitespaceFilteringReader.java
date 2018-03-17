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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Reads a Velocity Template and filters leading whitespace and injects Velocity
 * comments (##) to the end of each line.
 *
 * @author Jeremy Long
 */
public class VelocityWhitespaceFilteringReader extends FilterReader {

    /**
     * A cache of the previous three characters read.
     */
    private final ReaderCache cache = new ReaderCache();
    /**
     * A buffer for added content.
     */
    private final Deque<Character> buffer = new ArrayDeque<>();

    /**
     * Tracks if a velocity comment is being read. Note, these are not filtered.
     */
    private boolean inComment = false;
    /**
     * Tracks if an uninterpreted section of a velocity template is being read.
     */
    private boolean inUninterpretted = false;
    /**
     * Tracks if we are starting a new line (we can strip leading spaces).
     */
    private boolean isNewLine = true;
    /**
     * Tracks if we are at the end of the file.
     */
    private boolean isEOF = false;

    /**
     * Tracks whether or not a velocity variable is being output (e.g.
     * $prop.something).
     */
    private boolean needsTrailingSpace = false;

    /**
     * Creates a new Velocity whitespace filtering reader.
     *
     * @param reader the underlying reader
     */
    public VelocityWhitespaceFilteringReader(Reader reader) {
        super(reader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        if (!buffer.isEmpty()) {
            return (int) buffer.pop();
        }
        int c = super.read();
        if (c == -1) {
            return -1;
        }
        cache.push(c);

        if (inUninterpretted) {
            if (cache.checkSequence(']', ']', '#')) {
                inUninterpretted = false;
            }
            return c;
        } else if (inComment) {
            if (cache.checkSequence('*', '#')) {
                inComment = false;
            }
            return c;
        } else if (cache.checkSequence('#', '[', '[')) {
            inUninterpretted = true;
        } else if (cache.checkSequence('#', '*')) {
            inComment = true;
        }
        if (!inComment && !inUninterpretted) {
            if (isNewLine) {
                while (c == '\t' || c == ' ' || c == '\n' || c == '\r') {
                    c = super.read();
                    if (c == -1) {
                        return -1;
                    }
                    cache.push(c);
                }
                isNewLine = false;
            } else if (c == '\n' || c == '\r') {
                isNewLine = true;
                if (needsTrailingSpace && (cache.checkSequence(')', '\n') || cache.checkSequence(')', '\r')
                        || cache.checkSequence(']', '\n') || cache.checkSequence(']', '\r'))) {
                    needsTrailingSpace = false;
                }
                final char retVal;
                if (needsTrailingSpace) {
                    buffer.add('#');
                    retVal = ' ';
                } else {
                    retVal = '#';
                }
                needsTrailingSpace = false;
                buffer.add('#');
                buffer.add((char) c);
                return retVal;
            }
            if (c == '$') {
                needsTrailingSpace = true;
            } else if (needsTrailingSpace && checkIfNeedsTrailingSpace(c)) {
                needsTrailingSpace = false;
            }
        }
        return (char) c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(char cbuf[], int offset, int length) throws IOException {
        if (isEOF) {
            return -1;
        }
        int n;
        for (n = 0; n < length; n++) {
            final int c = read();
            if (c == -1) {
                isEOF = true;
                return n;
            }
            cbuf[offset + n] = (char) c;
        }
        return n;
    }

    /**
     * Determines if the current velocity expression requires a trailing space
     * before a single line comment is added (##).
     *
     * @param c the character to check
     * @return <code>true</code> if a whitespace is needed; otherwise
     * <code>false</code>
     */
    private boolean checkIfNeedsTrailingSpace(int c) {
        return !(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9'
                || c == '-' || c == '!' || c == '_' || c == '.'
                || c == '(' || c == ')' || c == '[' || c == ']');
    }

    /**
     * Small internal cache that is used to track the previous three characters
     * read.
     */
    private static class ReaderCache {

        /**
         * The cache.
         */
        private final int[] cache = new int[3];

        /**
         * Pushes a new element onto the stack. If more then three characters
         * have been pushed onto the stack the oldest character is removed.
         *
         * @param c the character to push onto the stack
         */
        public void push(int c) {
            cache[0] = cache[1];
            cache[1] = cache[2];
            cache[2] = c;
        }

        /**
         * Checks if the cache contains the given character sequence.
         *
         * @param one character one
         * @param two character two
         * @param three character three
         * @return <code>true</code> if the cache contains the three characters
         * in order; otherwise <code>false</code>
         */
        public boolean checkSequence(char one, char two, char three) {
            return one == cache[0] && two == cache[1] && three == cache[2];
        }

        /**
         * Checks if the cache contains the given character sequence.
         *
         * @param one character one
         * @param two character two
         * @return <code>true</code> if the cache contains the two characters in
         * order; otherwise <code>false</code>
         */
        public boolean checkSequence(char one, char two) {
            return one == cache[1] && two == cache[2];
        }
    }
}
