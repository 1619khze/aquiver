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
package org.aquiver.mvc.http;

import java.text.MessageFormat;
import java.time.LocalDateTime;

/**
 * @author WangYi
 * @since 2020/10/19
 */
public class Cookie {
  private static final String TSPECIALS = "/()<>@,;:\\\"[]?={} \t";
  private final String name;
  private final String value;
  private LocalDateTime expires;
  private String domain;
  private String path;
  private boolean secure;
  private long maxAge = -1;
  private boolean httpOnly = false;

  public Cookie(String name, String value) {
    if (name != null && name.length() != 0) {
      if (this.isToken(name)
              && !name.equalsIgnoreCase("Comment")
              && !name.equalsIgnoreCase("Discard")
              && !name.equalsIgnoreCase("Domain")
              && !name.equalsIgnoreCase("Expires")
              && !name.equalsIgnoreCase("Max-Age")
              && !name.equalsIgnoreCase("Path")
              && !name.equalsIgnoreCase("Secure")
              && !name.equalsIgnoreCase("Version")
              && !name.startsWith("$")) {
        this.name = name;
        this.value = value;
      } else {
        String errMsg = "err.cookie_name_is_token";
        Object[] errArgs = new Object[]{name};
        errMsg = MessageFormat.format(errMsg, errArgs);
        throw new IllegalArgumentException(errMsg);
      }
    } else {
      throw new IllegalArgumentException("err.cookie_name_blank");
    }
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public LocalDateTime getExpires() {
    return expires;
  }

  public void setExpires(LocalDateTime expires) {
    this.expires = expires;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public boolean isSecure() {
    return secure;
  }

  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  public long getMaxAge() {
    return maxAge;
  }

  public void setMaxAge(long maxAge) {
    this.maxAge = maxAge;
  }

  public boolean isHttpOnly() {
    return httpOnly;
  }

  public void setHttpOnly(boolean httpOnly) {
    this.httpOnly = httpOnly;
  }

  private boolean isToken(String value) {
    int len = value.length();
    for (int i = 0; i < len; ++i) {
      char c = value.charAt(i);
      if (c < ' ' || c >= 127 || TSPECIALS.indexOf(c) != -1) {
        return false;
      }
    }
    return true;
  }
}
