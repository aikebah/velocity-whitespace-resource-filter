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

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class VelocityWhitespaceFilteringReaderBenchmark {

    @State(Scope.Thread)
    public static class BenchmarkState {
        public String simpleInput;
        public String complexInput;
        public String velocityTemplateInput;
        public String largeInput;

        @Setup(Level.Trial)
        public void setup() {
            simpleInput = "   test\n   more text\n   final line\n";
            
            complexInput = "   test\n#[[unmodified]]\n]]#\n\n\n  first\n#*\ncomment  \r*#test\n";
            
            velocityTemplateInput = buildVelocityTemplate();
            
            StringBuilder large = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                large.append("   line ").append(i).append(" with content\n");
                large.append("$variable.property\n");
                large.append("#if($condition)\n");
                large.append("   nested content\n");
                large.append("#end\n");
            }
            largeInput = large.toString();
        }

        private String buildVelocityTemplate() {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "   <project>\n" +
                   "      <name>$project.name</name>\n" +
                   "      <version>$project.version</version>\n" +
                   "      #if($dependencies)\n" +
                   "         <dependencies>\n" +
                   "         #foreach($dep in $dependencies)\n" +
                   "            <dependency>\n" +
                   "               <groupId>$dep.groupId</groupId>\n" +
                   "               <artifactId>$dep.artifactId</artifactId>\n" +
                   "            </dependency>\n" +
                   "         #end\n" +
                   "         </dependencies>\n" +
                   "      #end\n" +
                   "   </project>\n";
        }
    }

    @Benchmark
    public void simpleRead(BenchmarkState state, Blackhole blackhole) throws IOException {
        try (StringReader in = new StringReader(state.simpleInput);
             VelocityWhitespaceFilteringReader reader = new VelocityWhitespaceFilteringReader(in)) {
            int c;
            while ((c = reader.read()) != -1) {
                blackhole.consume(c);
            }
        }
    }

    @Benchmark
    public void complexRead(BenchmarkState state, Blackhole blackhole) throws IOException {
        try (StringReader in = new StringReader(state.complexInput);
             VelocityWhitespaceFilteringReader reader = new VelocityWhitespaceFilteringReader(in)) {
            int c;
            while ((c = reader.read()) != -1) {
                blackhole.consume(c);
            }
        }
    }

    @Benchmark
    public void velocityTemplateRead(BenchmarkState state, Blackhole blackhole) throws IOException {
        try (StringReader in = new StringReader(state.velocityTemplateInput);
             VelocityWhitespaceFilteringReader reader = new VelocityWhitespaceFilteringReader(in)) {
            int c;
            while ((c = reader.read()) != -1) {
                blackhole.consume(c);
            }
        }
    }

    @Benchmark
    public void largeInputRead(BenchmarkState state, Blackhole blackhole) throws IOException {
        try (StringReader in = new StringReader(state.largeInput);
             VelocityWhitespaceFilteringReader reader = new VelocityWhitespaceFilteringReader(in)) {
            int c;
            while ((c = reader.read()) != -1) {
                blackhole.consume(c);
            }
        }
    }

    @Benchmark
    public void simpleReadBuffered(BenchmarkState state, Blackhole blackhole) throws IOException {
        try (StringReader in = new StringReader(state.simpleInput);
             VelocityWhitespaceFilteringReader reader = new VelocityWhitespaceFilteringReader(in)) {
            char[] buffer = new char[1024];
            int n;
            while ((n = reader.read(buffer, 0, buffer.length)) != -1) {
                blackhole.consume(n);
                blackhole.consume(buffer);
            }
        }
    }

    @Benchmark
    public void largeInputReadBuffered(BenchmarkState state, Blackhole blackhole) throws IOException {
        try (StringReader in = new StringReader(state.largeInput);
             VelocityWhitespaceFilteringReader reader = new VelocityWhitespaceFilteringReader(in)) {
            char[] buffer = new char[1024];
            int n;
            while ((n = reader.read(buffer, 0, buffer.length)) != -1) {
                blackhole.consume(n);
                blackhole.consume(buffer);
            }
        }
    }
}
