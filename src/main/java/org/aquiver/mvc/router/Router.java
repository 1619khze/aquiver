package org.aquiver.mvc.router;

import org.aquiver.RequestHandler;
import org.aquiver.mvc.annotation.HttpMethod;

/**
 * @author WangYi
 * @since 2020/8/28
 */
public interface Router {
  void registerRoute(String path, Object object) throws Exception;

  void registerRoute(String path, RequestHandler handler, HttpMethod httpMethod) throws Exception;

  RouteInfo lookup(String url);
}
