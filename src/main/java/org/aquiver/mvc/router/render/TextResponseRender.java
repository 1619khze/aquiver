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
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.aquiver.RequestContext;
import org.aquiver.mvc.router.RouteInfo;
import org.aquiver.mvc.router.views.ViewType;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.aquiver.mvc.http.MediaType.TEXT_PLAIN_VALUE;

/**
 * @author WangYi
 * @since 2020/6/17
 */
public class TextResponseRender extends AbstractResponseRender implements ResponseRender {

  @Override
  public boolean support(ViewType viewType) {
    return viewType.equals(ViewType.TEXT);
  }

  @Override
  public void render(RouteInfo routeInfo, RequestContext requestContext) {
    FullHttpRequest httpRequest = requestContext.request().httpRequest();
    Object result = routeInfo.getInvokeResult();

    if (Objects.nonNull(result)) {
      String stringResult = String.valueOf(result);
      if (stringResult.startsWith("redirect:/")) {
        String url = stringResult.replaceFirst("redirect:/", "");
        FullHttpResponse response = redirectResponse(url);
        requestContext.request().channelHandlerContext()
                .writeAndFlush(response)
                .addListener(ChannelFutureListener.CLOSE);
        return;
      }
    }

    ByteBuf byteBuf = Unpooled.copiedBuffer(Objects.isNull(result) ?
            "".getBytes(CharsetUtil.UTF_8) : String.valueOf(result)
            .getBytes(StandardCharsets.UTF_8));

    HttpResponseStatus status = httpRequest.decoderResult().isSuccess() ? OK : BAD_REQUEST;
    FullHttpResponse response = buildRenderResponse(status, byteBuf);

    setHeader(response, httpRequest, TEXT_PLAIN_VALUE);
    requestContext.request().channelHandlerContext().writeAndFlush(response);
  }
}
