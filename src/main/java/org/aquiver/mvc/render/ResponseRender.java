package org.aquiver.mvc.render;

import org.aquiver.RequestContext;
import org.aquiver.mvc.Route;
import org.aquiver.mvc.view.ViewType;

import java.io.IOException;

/**
 * @author WangYi
 * @since 2020/6/17
 */
public interface ResponseRender {
  boolean support(ViewType viewType);

  void render(Route route, RequestContext requestContext) throws IOException;
}
