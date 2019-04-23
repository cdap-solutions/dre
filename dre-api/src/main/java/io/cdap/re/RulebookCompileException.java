/*
 * Copyright © 2017-2018 Cask Data, Inc.
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


import co.cask.wrangler.api.parser.SyntaxError;

import java.util.Iterator;

/**
 * Class description here.
 */
public class RulebookCompileException extends Exception {
  private Iterator<SyntaxError> it = new Iterator<SyntaxError>() {
    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public SyntaxError next() {
      return null;
    }

    @Override
    public void remove() {
      // no-op
    }
  };

  public RulebookCompileException(String message) {
    super(message);
  }

  public RulebookCompileException(String message, Iterator<SyntaxError> it) {
    super(message);
    this.it = it;
  }

  public Iterator<SyntaxError> iterator() {
    return it;
  }
}
