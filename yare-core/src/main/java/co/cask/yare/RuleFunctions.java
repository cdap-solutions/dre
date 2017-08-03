package co.cask.yare;

import org.apache.commons.lang3.StringUtils;

/**
 * Class description here.
 */
public class RuleFunctions {
  public static final int UNI_SUR_HIGH_START = 0xD800;
  public static final int UNI_SUR_HIGH_END = 0xDBFF;
  public static final int UNI_SUR_LOW_START = 0xDC00;
  public static final int UNI_SUR_LOW_END = 0xDFFF;

  public static boolean present(Object value) {
    if (value == null) {
      return false;
    }
    return true;
  }

  public static boolean isnull(Object value) {
    if (value == null) {
      return true;
    }
    return false;
  }

  public static boolean ishex(char c) {
    return c >= 65 && c <= 70 || c >= 48 && c <= 57;
  }

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
