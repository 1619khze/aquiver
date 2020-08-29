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
package org.aquiver.loader;

import org.apex.ApexContext;
import org.aquiver.Aquiver;
import org.aquiver.mvc.annotation.Path;
import org.aquiver.mvc.annotation.RestPath;
import org.aquiver.mvc.router.RestfulRouter;
import org.aquiver.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/8/12
 */
public class RestfulRouterLoader implements WebLoader {
  private static final Logger log = LoggerFactory.getLogger(RestfulRouterLoader.class);

  /**
   * Filter out the routing class from the scanned
   * result set and add it to the routing manager
   *
   * @param instances Scanned result
   * @param aquiver aquiver
   */
  @Override
  public void load(Map<String, Object> instances, Aquiver aquiver) {
    final ApexContext context = ApexContext.of();
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
      context.getBean(RestfulRouter.class).registerRoute(url, entry.getValue());
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
