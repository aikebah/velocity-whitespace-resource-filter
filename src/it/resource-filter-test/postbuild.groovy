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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import java.nio.charset.Charset;
 

File file1 = new File(basedir, "src/main/resources/template.vm");
File file2 = new File(basedir, "target/classes/template.vm");
if (!FileUtils.contentEquals(file1, file2)) {
    System.out.println("template.vm in src does not match template.vm in target/classes");
    return false;
}

file1 = new File(basedir, "src/main/resources/template.txt");
file2 = new File(basedir, "target/classes/template.txt");
if (!FileUtils.contentEquals(file1, file2)) {
    System.out.println("template.txt in src does not match template.vm in target/classes");
    return false;
}

file1 = new File(basedir, "src/main/resources/template.vtl");
file2 = new File(basedir, "target/classes/template.vtl");
if (FileUtils.contentEquals(file1, file2)) {
    System.out.println("template.vtl in target/classes was NOT FILTERED and matches template.vm in src");
    return false;
}
