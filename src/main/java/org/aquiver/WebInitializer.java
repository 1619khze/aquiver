package org.aquiver;

import org.aquiver.loader.WebLoader;

import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author WangYi
 * @since 2020/8/23
 */
public final class WebInitializer {
  public static void initialize(Map<String, Object> instances, Aquiver aquiver) throws Exception {
    if (instances.isEmpty()) {
      return;
    }
    ServiceLoader<WebLoader> webLoaders = ServiceLoader.load(WebLoader.class);
    for (WebLoader webLoader : webLoaders) {
      webLoader.load(instances, aquiver);
    }
  }
}
