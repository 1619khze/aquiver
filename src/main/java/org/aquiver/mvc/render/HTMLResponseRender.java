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
package org.aquiver.mvc.render;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.aquiver.Aquiver;
import org.aquiver.RequestContext;
import org.aquiver.mvc.MediaType;
import org.aquiver.mvc.ModelAndView;
import org.aquiver.mvc.Route;
import org.aquiver.mvc.view.ViewType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.aquiver.Const.PATH_SERVER_VIEW_SUFFIX;
import static org.aquiver.Const.SERVER_VIEW_SUFFIX;

/**
 * @author WangYi
 * @since 2020/6/17
 */
public class HTMLResponseRender implements ResponseRender {

  @Override
  public boolean support(ViewType viewType) {
    return viewType.equals(ViewType.HTML);
  }

  @Override
  public void render(Route route, RequestContext requestContext) throws IOException {
    Map<String, Object> params = new HashMap<>();

    final Aquiver aquiver = requestContext.getAquiver();
    final String viewSuffix = aquiver.environment().getString(PATH_SERVER_VIEW_SUFFIX, SERVER_VIEW_SUFFIX);
    final Object invokeResult = route.getInvokeResult();

    String fullHtmlPath = "templates/";

    if (!Objects.isNull(invokeResult) &&
            String.class.isAssignableFrom(invokeResult.getClass())) {

      fullHtmlPath = fullHtmlPath + invokeResult;
    } else if (!Objects.isNull(invokeResult) &&
            ModelAndView.class.isAssignableFrom(invokeResult.getClass())) {

      ModelAndView modelAndView = (ModelAndView) invokeResult;
      fullHtmlPath = fullHtmlPath + modelAndView.getHtmlPath();
      params.putAll(modelAndView.getParams());
    }

    if (!fullHtmlPath.endsWith(viewSuffix) && fullHtmlPath.contains(".")) {
      String[] split = fullHtmlPath.split("\\.");
      fullHtmlPath = (split[0] + viewSuffix);
    }
    fullHtmlPath = fullHtmlPath.replace("//", "/");

    final String renderView = route.getHtmlView().renderView(fullHtmlPath, params);

    FullHttpRequest httpRequest = requestContext.getHttpRequest();
    HttpResponseStatus status = httpRequest.decoderResult().isSuccess() ? OK : BAD_REQUEST;
    byte[] bytes = renderView.getBytes(StandardCharsets.UTF_8);

    FullHttpResponse fullHttpResponse =
            new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer(bytes));

    HttpHeaders headers = fullHttpResponse.headers();
    headers.set(HttpHeaderNames.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE);
    if (HttpUtil.isKeepAlive(httpRequest)) {
      headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
      headers.setInt(HttpHeaderNames.CONTENT_LENGTH, fullHttpResponse.content().readableBytes());
    }
    requestContext.getContext().writeAndFlush(fullHttpResponse);
  }
}
