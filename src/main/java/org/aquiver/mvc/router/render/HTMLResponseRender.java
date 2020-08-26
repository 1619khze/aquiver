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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.aquiver.ModelAndView;
import org.aquiver.mvc.http.MediaType;
import org.aquiver.mvc.router.Route;
import org.aquiver.mvc.router.views.ViewType;
import org.aquiver.RequestContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.aquiver.Const.PATH_SERVER_VIEW_SUFFIX;
import static org.aquiver.Const.SERVER_VIEW_SUFFIX;

/**
 * @author WangYi
 * @since 2020/6/17
 */
public class HTMLResponseRender extends AbstractResponseRender implements ResponseRender {

  @Override
  public boolean support(ViewType viewType) {
    return viewType.equals(ViewType.HTML);
  }

  @Override
  public void render(Route route, RequestContext requestContext) {
    Map<String, Object> params = new HashMap<>();

    final String viewSuffix = aquiver.environment().getString(PATH_SERVER_VIEW_SUFFIX, SERVER_VIEW_SUFFIX);
    final Object invokeResult = route.getInvokeResult();

    String fullHtmlPath = "templates/";

    if (!Objects.isNull(invokeResult) &&
            String.class.isAssignableFrom(invokeResult.getClass())) {

      fullHtmlPath = fullHtmlPath + invokeResult;
    } else if (!Objects.isNull(invokeResult) &&
            ModelAndView.class.isAssignableFrom(invokeResult.getClass())) {

      ModelAndView modelAndView = (ModelAndView) invokeResult;
      fullHtmlPath = fullHtmlPath + modelAndView.htmlPath();
      params.putAll(modelAndView.params());
    }

    if (!fullHtmlPath.endsWith(viewSuffix) && fullHtmlPath.contains(".")) {
      String[] split = fullHtmlPath.split("\\.");
      fullHtmlPath = (split[0] + viewSuffix);
    } else if (!fullHtmlPath.endsWith(viewSuffix) && !fullHtmlPath.contains(".")) {
      fullHtmlPath = fullHtmlPath + viewSuffix;
    }

    fullHtmlPath = fullHtmlPath.replace("//", "/");

    try {
      final String renderView = route.getHtmlView().renderView(fullHtmlPath, params);
      FullHttpRequest httpRequest = requestContext.request().httpRequest();
      HttpResponseStatus status = httpRequest.decoderResult().isSuccess() ? OK : BAD_REQUEST;
      ByteBuf byteBuf = Unpooled.copiedBuffer(renderView.getBytes(StandardCharsets.UTF_8));

      FullHttpResponse fullHttpResponse = buildRenderResponse(status, byteBuf);
      this.setHeader(fullHttpResponse, httpRequest, MediaType.TEXT_HTML_VALUE);
      requestContext.request().channelHandlerContext().writeAndFlush(fullHttpResponse);
    } catch (IOException e) {
      log.error("Can't get view template:{}", fullHtmlPath, e);
    }
  }
}
