/*
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.apache.commons.cli;

import java.util.HashMap;
import java.util.Map;

import org.graalvm.polyglot.Value;

/**
 * The class GnuParser provides an implementation of the {@link Parser#flatten(Options, String[], boolean) flatten}
 * method.
 *
 * @deprecated since 1.3, use the {@link DefaultParser} instead
 */
@Deprecated
public class GnuParser extends Parser {

    private static Value clz = ContextInitializer.getPythonClass("gnu_parser.py", "GnuParser");

    public GnuParser() {
        parserObj = clz.execute();
    }

    public GnuParser(Value v) {
        parserObj = v;
    }

    /** Cache for wrappers to python objects  */
    private static Map<Value, GnuParser> cache = new HashMap<>();

    public static GnuParser create(final Value foreignGnuParser) {
        if (foreignGnuParser.isNull()) {
            return null;
        }
        return cache.computeIfAbsent(foreignGnuParser, GnuParser::new);
    }

    /**
     * This flatten method does so using the following rules:
     * <ol>
     * <li>If an {@link Option} exists for the first character of the {@code arguments} entry <b>AND</b> an
     * {@link Option} does not exist for the whole {@code argument} then add the first character as an option to the
     * processed tokens list e.g. "-D" and add the rest of the entry to the also.</li>
     * <li>Otherwise just add the token to the processed tokens list.</li>
     * </ol>
     *
     * @param options The Options to parse the arguments by.
     * @param arguments The arguments that have to be flattened.
     * @param stopAtNonOption specifies whether to stop flattening when a non option has been encountered
     * @return a String array of the flattened arguments
     */
    @Override
    protected String[] flatten(final Options options, final String[] arguments, final boolean stopAtNonOption) {
        return parserObj.invokeMember("flatten", options, arguments, stopAtNonOption).as(String[].class);
    }
}
