package org.aquiver;

import java.util.Map;
import java.util.Set;

/**
 * @author WangYi
 * @since 2020/5/26
 */
public interface RouteFinder {
  Map<String, Class<?>> finderRoute(Set<Class<?>> classSet) throws Exception;
}
