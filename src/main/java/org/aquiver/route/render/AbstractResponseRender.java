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
package org.aquiver.route.render;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import org.aquiver.Aquiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author WangYi
 * @since 2020/6/25
 */
public abstract class AbstractResponseRender {
  protected static final Logger log = LoggerFactory.getLogger(AbstractResponseRender.class);
  protected final Aquiver aquiver = Aquiver.of();

  protected FullHttpResponse buildRenderResponse(HttpResponseStatus status, ByteBuf byteBuf) {
    return new DefaultFullHttpResponse(HTTP_1_1, status, byteBuf);
  }

  protected void setHeader(FullHttpResponse httpResponse, FullHttpRequest httpRequest, String mediaType) {
    HttpHeaders headers = httpResponse.headers();
    headers.set(HttpHeaderNames.CONTENT_TYPE, mediaType);
    if (HttpUtil.isKeepAlive(httpRequest)) {
      headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
      headers.setInt(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
    }
  }
}
