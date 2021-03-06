/*
 * Copyright 2013 Jeanfrancois Arcand
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/* ClassFileIterator.java
 * 
 ******************************************************************************
 *
 * Created: Oct 10, 2011
 * Character encoding: UTF-8
 * 
 * Copyright (c) 2011 - XIAM Solutions B.V. The Netherlands, http://www.xiam.nl
 * 
 ********************************* LICENSE ************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atmosphere.util.annotation;

import org.atmosphere.util.FileIterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@code ClassFileIterator} is used to iterate over all Java ClassFile files
 * available within a specific context. For every Java ClassFile ({@code .class})
 * an {@link InputStream} is returned.
 *
 * @author <a href="mailto:rmuller@xiam.nl">Ronald K. Muller</a>
 * @since annotation-detector 3.0.0
 */
final class ClassFileIterator {

    private final FileIterator fileIterator;
    private ZipFileIterator zipIterator;
    private boolean isFile;

    /**
     * Create a new {@code ClassFileIterator} returning all Java ClassFile files
     * available from the class path ({@code System.getProperty("java.class.path")}).
     */
    ClassFileIterator() throws IOException {
        this(classPath());
    }

    /**
     * Create a new {@code ClassFileIterator} returning all Java ClassFile files
     * available from the specified files and/or directories, including sub
     * directories.
     */
    public ClassFileIterator(final File... filesOrDirectories) throws IOException {
        fileIterator = new FileIterator(filesOrDirectories);
    }

    /**
     * Return the name of the Java ClassFile returned from the last call to
     * {@link #next()}. The name is either the path name of a file or the name of
     * an ZIP/JAR file entry.
     */
    public String getName() {
        // Both getPath() and getName() are very light weight method calls
        return zipIterator == null ?
                fileIterator.getFile().getPath() :
                zipIterator.getEntry().getName();
    }

    /**
     * Return {@code true} if the current {@link InputStream} is reading from a
     * plain {@link File}. Return {@code false} if the current {@link InputStream}
     * is reading from a ZIP File Entry.
     */
    public boolean isFile() {
        return isFile;
    }

    /**
     * Return the next Java ClassFile as an {@code InputStream}.
     * <br/>
     * NOTICE: Client code MUST close the returned {@code InputStream}!
     */
    public InputStream next() throws IOException {
        while (true) {
            if (zipIterator == null) {
                final File file = fileIterator.next();
                if (file == null) {
                    return null;
                } else {
                    final String name = file.getName();
                    if (name.endsWith(".class")) {
                        isFile = true;
                        return new FileInputStream(file);
                    } else if (fileIterator.isRootFile() && endsWithIgnoreCase(name, ".jar")) {
                        zipIterator = new ZipFileIterator(file);
                    } // else just ignore
                    continue;
                }
            } else {
                final InputStream is = zipIterator.next();
                if (is == null) {
                    zipIterator = null;
                    continue;
                } else {
                    isFile = false;
                    return is;
                }
            }
        }
    }

    // private

    /**
     * Returns the class path of the current JVM instance as an array of {@link File} objects.
     */
    private static File[] classPath() {
        final String[] fileNames = System.getProperty("java.class.path").split(File.pathSeparator);
        final File[] files = new File[fileNames.length];
        for (int i = 0; i < files.length; ++i) {
            files[i] = new File(fileNames[i]);
        }
        return files;
    }

    private static boolean endsWithIgnoreCase(final String value, final String suffix) {
        final int n = suffix.length();
        return value.regionMatches(true, value.length() - n, suffix, 0, n);
    }
}