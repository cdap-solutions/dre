/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package io.cdap.re;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Class description here.
 */
public class TestingRig {

  public static Rulebook compile(String filename) throws RulebookCompileException, RuleCompileException {
    InputStream resourceAsStream = RulebookGrammarCompilerTest.class
      .getClassLoader().getResourceAsStream(filename);
    Reader reader = new InputStreamReader(resourceAsStream);

    // Compile Rulebook.
    Compiler compiler = new RulebookCompiler();
    Rulebook rulebook = compiler.compile(reader);
    return rulebook;
  }
}
