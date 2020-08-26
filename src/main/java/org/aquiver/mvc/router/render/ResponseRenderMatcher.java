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
package org.aquiver.mvc.router.render;

import org.aquiver.RequestContext;
import org.aquiver.mvc.router.RouteInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import static java.util.Objects.requireNonNull;

/**
 * @author WangYi
 * @since 2020/6/10
 */
public class ResponseRenderMatcher {
  private static final Logger log = LoggerFactory.getLogger(ResponseRenderMatcher.class);

  private final List<ResponseRender> responseRenders = new ArrayList<>();

  public ResponseRenderMatcher() {
    ServiceLoader<ResponseRender> renderServiceLoader = ServiceLoader.load(ResponseRender.class);
    try {
      for (ResponseRender render : renderServiceLoader) {
        ResponseRender argsResolver = render.getClass().getDeclaredConstructor().newInstance();
        responseRenders.add(argsResolver);
      }
    } catch (ReflectiveOperationException e) {
      log.error("new instance exception:", e);
    }
  }

  public void adapter(RequestContext requestContext) {
    requireNonNull(requestContext, "RequestContext cant't be null");

    RouteInfo routeInfo = requestContext.route();

    if (responseRenders.isEmpty()) {
      throw new RuntimeException("No available ResponseRender found");
    }
    for (ResponseRender responseRender : responseRenders) {
      if (!responseRender.support(routeInfo.getViewType())) {
        continue;
      }
      responseRender.render(routeInfo, requestContext);
    }
  }

  public void addResponseRenders(List<ResponseRender> responseRenders) {
    this.responseRenders.addAll(responseRenders);
  }

  public void addResponseRender(ResponseRender responseRender) {
    this.responseRenders.add(responseRender);
  }
}
