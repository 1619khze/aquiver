package org.aquiver.utils;

/**
 * @author WangYi
 * @since 2020/7/13
 */
public class StringUtils {
  public static boolean isEmpty(String str) {
    return str == null || str.length() == 0;
  }

  public static String substringBefore(String str, String separator) {
    if (!isEmpty(str) && separator != null) {
      if (separator.length() == 0) {
        return "";
      } else {
        int pos = str.indexOf(separator);
        return pos == -1 ? str : str.substring(0, pos);
      }
    } else {
      return str;
    }
  }
}
