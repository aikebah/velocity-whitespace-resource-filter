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
     * Primitive buffer for pending characters (max 3 chars).
     * Avoids boxing/unboxing overhead of Deque&lt;Character&gt;.
     */
    private char p0;
    private char p1;
    private char p2;
    private int pCount;

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
     * Checks if there are pending characters in the buffer.
     *
     * @return true if the buffer has pending characters
     */
    private boolean hasPending() {
        return pCount != 0;
    }

    /**
     * Enqueues a character to the pending buffer.
     *
     * @param ch the character to enqueue
     */
    private void enqueue(char ch) {
        switch (pCount) {
            case 0:
                p0 = ch;
                break;
            case 1:
                p1 = ch;
                break;
            case 2:
                p2 = ch;
                break;
            default:
                throw new IllegalStateException("Pending buffer overflow");
        }
        pCount++;
    }

    /**
     * Dequeues a character from the pending buffer.
     *
     * @return the next character from the buffer
     */
    private char dequeue() {
        char ch = p0;
        if (pCount > 1) {
            p0 = p1;
            if (pCount > 2) {
                p1 = p2;
            }
        }
        pCount--;
        return ch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        if (hasPending()) {
            return dequeue();
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
                if (needsTrailingSpace) {
                    int prev = cache.prev();
                    if (prev == ')' || prev == ']') {
                        needsTrailingSpace = false;
                    }
                }
                final char retVal;
                if (needsTrailingSpace) {
                    enqueue('#');
                    retVal = ' ';
                } else {
                    retVal = '#';
                }
                needsTrailingSpace = false;
                enqueue('#');
                enqueue((char) c);
                return retVal;
            }
            if (c == '$') {
                needsTrailingSpace = true;
            } else if (needsTrailingSpace && checkIfNeedsTrailingSpace(c)) {
                needsTrailingSpace = false;
            }
        }
        return c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(char cbuf[], int offset, int length) throws IOException {
        if (length == 0) {
            return 0;
        }
        int n = 0;
        
        while (n < length && hasPending()) {
            cbuf[offset + n++] = dequeue();
        }
        
        while (n < length) {
            int ch = read();
            if (ch == -1) {
                break;
            }
            cbuf[offset + n++] = (char) ch;
            
            while (n < length && hasPending()) {
                cbuf[offset + n++] = dequeue();
            }
        }
        
        return n == 0 ? -1 : n;
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
         * The cache - field-based to avoid array overhead.
         * a = oldest, b = middle, c = latest
         */
        private int a;
        private int b;
        private int c;

        /**
         * Pushes a new element onto the stack. If more then three characters
         * have been pushed onto the stack the oldest character is removed.
         *
         * @param ch the character to push onto the stack
         */
        public void push(int ch) {
            a = b;
            b = c;
            c = ch;
        }

        /**
         * Gets the previous (second-to-last) character.
         *
         * @return the previous character
         */
        public int prev() {
            return b;
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
            return one == a && two == b && three == c;
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
            return one == b && two == c;
        }
    }
}
