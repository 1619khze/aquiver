package org.aquiver;

import org.apache.commons.lang3.StringUtils;
import org.aquiver.mvc.BypassRequestUrls;

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

  public void setPatterns(String patterns) {
    this.patterns = patterns;
  }

  public void setCache(boolean enabled) {
    if (enabled) {
      cache = new HashMap<>(128);
    }
  }

  public RegexBypassRequestUrls() {
    if (cache != null) {
      cache.put("/favicon.ico", Boolean.TRUE);
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

  @Override
  public boolean accept(Request request, String path) {
    if (cache != null) {
      Boolean found = cache.get(path);
      if (found != null) {
        return found == Boolean.TRUE;
      }
    } else {
      // special code for no-cache
      if ("/favicon.ico".equals(path)) {
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
