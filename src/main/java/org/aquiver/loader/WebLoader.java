package org.aquiver.loader;

import org.aquiver.Aquiver;

import java.util.Map;

/**
 * @author WangYi
 * @since 2020/8/12
 */
@FunctionalInterface
public interface WebLoader {
  void load(Map<String, Object> instances, Aquiver aquiver) throws Exception;
}
