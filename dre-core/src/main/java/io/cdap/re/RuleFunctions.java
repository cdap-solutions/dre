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

import org.apache.commons.lang3.StringUtils;

/**
 * Class description here.
 */
public class RuleFunctions {
  public static final int UNI_SUR_HIGH_START = 0xD800;
  public static final int UNI_SUR_HIGH_END = 0xDBFF;
  public static final int UNI_SUR_LOW_START = 0xDC00;
  public static final int UNI_SUR_LOW_END = 0xDFFF;

  /**
   * Checks if field specified exists or not, i.e. if it's null or not.
   *
   * @param value to be checked for existence.
   * @return true if it's not null, false otherwise.
   */
  public static boolean present(Object value) {
    if (value == null) {
      return false;
    }
    return true;
  }

  /**
   * Checks if field specified exists or not, i.e. if it's null or not.
   *
   * @param field to be checked for existence.
   * @return true if it's not null, false otherwise.
   */
  public static boolean has(Object field) {
    if (field == null) {
      return false;
    }
    return true;
  }

  /**
   * Checks if the value is specifical null or not.
   *
   * @param value to be checked for null.
   * @return true if null, false otherwise.
   */
  public static boolean isnull(Object value) {
    return !has(value);
  }

  /**
   * Checks if the character is a hex character or not.
   * @param c character to check whether it's hex or not.
   * @return true if it's hex, false otherwise.
   */
  public static boolean ishex(char c) {
    return c >= 65 && c <= 70 || c >= 48 && c <= 57;
  }

  /**
   * Checks if a string value is null or empty.
   *
   * @param value to checked for null or empty.
   * @return true if null or empty, false otherwise.
   */
  public static boolean isnullorempty(String value) {
    if (value == null) {
      return true;
    } else if (value.trim().isEmpty()) {
      return true;
    }
    return false;
  }

  public static boolean contains(CharSequence seq, int searchChar) {
    return StringUtils.contains(seq, searchChar);
  }

  public static boolean contains(CharSequence seq, CharSequence searchSeq) {
    return StringUtils.contains(seq, searchSeq);
  }

  public static boolean containsignorecase(CharSequence str, CharSequence searchStr) {
    return StringUtils.containsIgnoreCase(str, searchStr);
  }

  public static boolean whitespace(CharSequence seq) {
    return StringUtils.containsWhitespace(seq);
  }

  public static boolean any(CharSequence cs, CharSequence searchChars) {
    return StringUtils.containsAny(cs, searchChars);
  }

  public static boolean any(CharSequence cs, CharSequence... searchCharSequences) {
    return StringUtils.containsAny(cs, searchCharSequences);
  }

  public static boolean only(CharSequence cs, char... valid) {
    return StringUtils.containsOnly(cs, valid);
  }

  public static boolean only(CharSequence cs, String validChars) {
    return StringUtils.containsOnly(cs, validChars);
  }

  public static boolean none(CharSequence cs, char... searchChars) {
    return StringUtils.containsNone(cs, searchChars);
  }

  public static boolean none(CharSequence cs, String invalidChars) {
    return StringUtils.containsNone(cs, invalidChars);
  }

  public static boolean isalpha(CharSequence cs) {
    return StringUtils.isAlpha(cs);
  }

  public static boolean isalphaspace(CharSequence cs) {
    return StringUtils.isAlphaSpace(cs);
  }

  public static boolean isalphanumeric(CharSequence cs) {
    return StringUtils.isAlphanumeric(cs);
  }

  public static boolean isalphanumericspace(CharSequence cs) {
    return StringUtils.isAlphanumericSpace(cs);
  }

  public static boolean isprintable(final CharSequence cs) {
    return StringUtils.isAsciiPrintable(cs);
  }

  public static boolean iswhitespace(final CharSequence cs) {
    return StringUtils.isWhitespace(cs);
  }

  public static boolean isalllower(final CharSequence cs) {
    return StringUtils.isAllLowerCase(cs);
  }

  public static boolean isallupper(final CharSequence cs) {
    return StringUtils.isAllUpperCase(cs);
  }

  public static boolean startswith(final CharSequence str, final CharSequence prefix) {
    return StringUtils.startsWith(str, prefix);
  }

  public static boolean startswithnocase(final CharSequence str, final CharSequence prefix) {
    return StringUtils.startsWithIgnoreCase(str, prefix);
  }

  public static boolean startswithany(final CharSequence sequence, final CharSequence... searchStrings) {
    return StringUtils.startsWithAny(sequence, searchStrings);
  }

  public static boolean endswith(final CharSequence str, final CharSequence suffix) {
    return StringUtils.endsWith(str, suffix);
  }

  public static boolean endswithnocase(final CharSequence str, final CharSequence suffix) {
    return StringUtils.endsWithIgnoreCase(str, suffix);
  }

  public static boolean endswithany(final CharSequence sequence, final CharSequence... searchStrings) {
    return StringUtils.endsWithAny(sequence, searchStrings);
  }

  public static boolean isUTF16String(CharSequence s) {
    final int size = s.length();
    for(int i=0;i<size;i++) {
      char ch = s.charAt(i);
      if (ch >= UNI_SUR_HIGH_START && ch <= UNI_SUR_HIGH_END) {
        if (i < size-1) {
          i++;
          char nextCH = s.charAt(i);
          if (nextCH >= UNI_SUR_LOW_START && nextCH <= UNI_SUR_LOW_END) {
            // Valid surrogate pair
          } else
            // Unmatched high surrogate
            return false;
        } else
          // Unmatched high surrogate
          return false;
      } else if (ch >= UNI_SUR_LOW_START && ch <= UNI_SUR_LOW_END)
        // Unmatched low surrogate
        return false;
    }

    return true;
  }

}
