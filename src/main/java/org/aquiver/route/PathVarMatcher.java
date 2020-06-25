/*
 * MIT License
 *
 * Copyright (c) 2019 1619kHz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.aquiver.route;

/**
 * @author WangYi
 * @since 2020/5/29
 */
public class PathVarMatcher {
  private PathVarMatcher() {
  }

  public static boolean checkMatch(String[] lookupPathSplit, String... mappingUrlSplit) {
    for (int i = 0; i < lookupPathSplit.length; i++) {
      if (lookupPathSplit[i].equals(mappingUrlSplit[i])) {
        continue;
      }
      if (!mappingUrlSplit[i].startsWith("{")) {
        return false;
      }
    }
    return true;
  }

  public static String getMatch(String url) {
    StringBuilder matcher = new StringBuilder(128);
    for (char c : url.toCharArray()) {
      if (c == '{') {
        break;
      }
      matcher.append(c);
    }
    return matcher.toString();
  }

  public static String getPathVariable(String url, String mappingUrl, String name) {
    String[] urlSplit = url.split("/");
    String[] mappingUrlSplit = mappingUrl.split("/");
    for (int i = 0; i < mappingUrlSplit.length; i++) {
      if (mappingUrlSplit[i].equals("{" + name + "}")) {
        if (urlSplit[i].contains("?")) {
          return urlSplit[i].split("[?]")[0];
        }
        if (urlSplit[i].contains("&")) {
          return urlSplit[i].split("&")[0];
        }
        return urlSplit[i];
      }
    }
    return null;
  }
}
