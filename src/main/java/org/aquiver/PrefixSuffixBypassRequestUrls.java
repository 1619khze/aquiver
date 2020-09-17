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
