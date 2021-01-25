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
package org.aquiver.common.ansi;

import java.util.Locale;
import java.util.Objects;

/**
 * @author WangYi
 * @version 1.0
 * @since 2019/1/16
 */
public abstract class AnsiOutput {
  private static final String ENCODE_JOIN = ";";
  private static final String OPERATING_SYSTEM_NAME = System.getProperty("os.name")
          .toLowerCase(Locale.ENGLISH);
  private static final String ENCODE_START = "\033[";
  private static final String ENCODE_END = "m";
  private static final String RESET = AnsiColor.RESET.toString();
  private static Enabled enabled = Enabled.ALWAYS;
  private static Boolean consoleAvailable;
  private static Boolean ansiCapable;

  private static void notNull(Object object, String message) {
    if (object == null) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Sets if the System.console() is known to be available.
   *
   * @param consoleAvailable if the console is known to be available or {@code null} to
   *                         use standard detection logic.
   */
  public static void setConsoleAvailable(Boolean consoleAvailable) {
    AnsiOutput.consoleAvailable = consoleAvailable;
  }

  static Enabled getEnabled() {
    return AnsiOutput.enabled;
  }

  /**
   * Encode a single {@link AnsiElement} if output is enabled.
   *
   * @param element the element to encode
   * @return the encoded element or an empty string
   */
  public static String encode(AnsiElement element) {
    if (isEnabled()) {
      return ENCODE_START + element + ENCODE_END;
    }
    return "";
  }

  /**
   * Create a new ANSI string from the specified elements. Any {@link AnsiElement}s will
   * be encoded as required.
   *
   * @param elements the elements to encode
   * @return a string of the encoded elements
   */
  public static String toString(Object... elements) {
    StringBuilder sb = new StringBuilder();
    if (isEnabled()) {
      buildEnabled(sb, elements);
    } else {
      buildDisabled(sb, elements);
    }
    return sb.toString();
  }

  private static void buildEnabled(StringBuilder sb, Object[] elements) {
    boolean writingAnsi = false;
    boolean containsEncoding = false;
    for (Object element : elements) {
      if (element instanceof AnsiElement) {
        containsEncoding = true;
        if (!writingAnsi) {
          sb.append(ENCODE_START);
          writingAnsi = true;
        } else {
          sb.append(ENCODE_JOIN);
        }
      } else {
        if (writingAnsi) {
          sb.append(RESET);
          writingAnsi = false;
        }
      }
      sb.append(element);
    }
    if (containsEncoding) {
      sb.append(writingAnsi ? ENCODE_JOIN : ENCODE_START);
      sb.append(AnsiColor.RESET);
      sb.append(ENCODE_END);
    }
  }

  private static void buildDisabled(StringBuilder sb, Object[] elements) {
    for (Object element : elements) {
      if (!(element instanceof AnsiElement) && Objects.nonNull(element)) {
        sb.append(element);
      }
    }
  }

  private static boolean isEnabled() {
    if (enabled == Enabled.DETECT) {
      if (ansiCapable == null) {
        ansiCapable = detectIfAnsiCapable();
      }
      return ansiCapable;
    }
    return enabled == Enabled.ALWAYS;
  }

  /**
   * Sets if ANSI output is enabled.
   *
   * @param enabled if ANSI is enabled, disabled or detected
   */
  public static void setEnabled(Enabled enabled) {
    notNull(enabled, "Enabled must not be null");
    AnsiOutput.enabled = enabled;
  }

  private static boolean detectIfAnsiCapable() {
    try {
      if (Boolean.FALSE.equals(consoleAvailable)) {
        return false;
      }
      if ((consoleAvailable == null) && (System.console() == null)) {
        return false;
      }
      return !(OPERATING_SYSTEM_NAME.contains("win"));
    } catch (Throwable ex) {
      return false;
    }
  }

  /**
   * Possible values to pass to {@link AnsiOutput#setEnabled}. Determines when to output
   * ANSI escape sequences for coloring application output.
   */
  public enum Enabled {

    /**
     * Try to detect whether ANSI coloring capabilities are available. The default
     * value for {@link AnsiOutput}.
     */
    DETECT,

    /**
     * Enable ANSI-colored output.
     */
    ALWAYS,

    /**
     * Disable ANSI-colored output.
     */
    NEVER

  }

}
