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

import org.aquiver.mvc.BypassRequestUrls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author WangYi
 * @since 2020/9/17
 */
public class PrefixSuffixBypassRequestUrls implements BypassRequestUrls {
  private final List<String> prefixs = new ArrayList<>();
  private final List<String> suffixs = new ArrayList<>();
  private final Map<String, Boolean> cache = new HashMap<>();

  public void setPrefixs(List<String> prefixs) {
    this.prefixs.addAll(prefixs);
  }

  public void setSuffixs(List<String> suffixs) {
    this.suffixs.addAll(suffixs);
  }

  public void setCache(Map<String, Boolean> cache) {
    this.cache.putAll(cache);
  }

  @Override
  public boolean accept(Request request, String path) {
    if (!cache.isEmpty() && cache.containsKey(path)) {
      return cache.get(path);
    }

    if (!prefixs.isEmpty()) {
      for (String prefix : prefixs) {
        if (path.startsWith(prefix)) {
          cache.put(path, Boolean.TRUE);
        }
        return true;
      }
    }

    if (!suffixs.isEmpty()) {
      for (String suffix : suffixs) {
        if (path.endsWith(suffix)) {
          if (cache != null) {
            cache.put(path, Boolean.TRUE);
          }
          return true;
        }
      }
    }

    if (cache.isEmpty()) {
      cache.put(path, Boolean.FALSE);
    }
    return false;
  }
}
