package org.aquiver.loader;

import org.aquiver.Aquiver;
import org.aquiver.mvc.annotation.Path;
import org.aquiver.mvc.annotation.RestPath;
import org.aquiver.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/8/12
 */
public class RequestRouteLoader implements WebLoader {
  private static final Logger log = LoggerFactory.getLogger(RequestRouteLoader.class);

  /**
   * Filter out the routing class from the scanned
   * result set and add it to the routing manager
   *
   * @param instances Scanned result
   * @param aquiver aquiver
   */
  @Override
  public void load(Map<String, Object> instances, Aquiver aquiver) {
    for (Map.Entry<String, Object> entry : instances.entrySet()) {
      Class<?> next = entry.getValue().getClass();
      String url = "/";
      boolean normal = ReflectionUtils.isNormal(next);
      if (!normal) {
        continue;
      }
      if (log.isDebugEnabled()) {
        log.debug("Is this class normal: {}", next.getSimpleName());
      }
      if (Objects.isNull(next.getAnnotation(RestPath.class)) &&
              Objects.isNull(next.getAnnotation(Path.class))) {
        continue;
      }
      url = url(next, url);
      aquiver.routeManager().addRoute(entry.getValue(), url);
    }
  }

  /**
   * Get path value
   *
   * @param cls route class
   * @param url default url
   * @return The complete url after stitching
   */
  private String url(Class<?> cls, String url) {
    Path classPath = cls.getAnnotation(Path.class);
    if (classPath == null) {
      return url;
    }
    String className = cls.getName();
    String value = classPath.value();
    if (!"".equals(value)) {
      url = value;
    }
    if (log.isDebugEnabled()) {
      log.debug("Registered rest controller:[{}:{}]", url, className);
    }
    return url;
  }
}
