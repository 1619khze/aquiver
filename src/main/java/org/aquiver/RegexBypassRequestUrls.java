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
package org.aquiver;

import org.apache.commons.lang3.StringUtils;
import org.aquiver.mvc.BypassRequestUrls;
import org.aquiver.server.Const;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author WangYi
 * @since 2020/9/17
 */
public class RegexBypassRequestUrls implements BypassRequestUrls {
  public static final String DEFAULT_PATTERNS = "^(/static/).+$";
  private final List<Pattern> patternList = new ArrayList<Pattern>(8);
  private String patterns = DEFAULT_PATTERNS;
  private Map<String, Boolean> cache;

  public RegexBypassRequestUrls() {
    if (cache != null) {
      cache.put(Const.FAVICON_PATH, Boolean.TRUE);
    }

    if (patterns != null && patterns.length() > 0) {
      for (String pattern : StringUtils.split(patterns, ',')) {
        pattern = StringUtils.trimToNull(pattern);
        if (pattern != null) {
          patternList.add(Pattern.compile(pattern));
        }
      }
    }
  }

  public void setPatterns(String patterns) {
    this.patterns = patterns;
  }

  public void setCache(boolean enabled) {
    if (enabled) {
      cache = new HashMap<>(128);
    }
  }

  @Override
  public boolean accept(Request request, String path) {
    if (cache != null) {
      Boolean found = cache.get(path);
      if (found != null) {
        return found == Boolean.TRUE;
      }
    } else {
      // special code for no-cache
      if (Const.FAVICON_PATH.equals(path)) {
        return true;
      }
    }

    for (Pattern pattern : patternList) {
      if (pattern.matcher(path).matches()) {
        if (cache != null) {
          cache.put(path, Boolean.TRUE);
        }
        return true;
      }
    }

    if (cache != null) {
      cache.put(path, Boolean.FALSE);
    }
    return false;
  }
}
