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

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.aquiver.RequestContext;
import org.aquiver.Response;
import org.aquiver.ResponseRender;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.aquiver.mvc.MediaType.APPLICATION_JSON_VALUE;
import static org.aquiver.mvc.MediaType.TEXT_PLAIN_VALUE;

/**
 * @author WangYi
 * @since 2020/6/10
 */
public class JsonResponseRender implements ResponseRender {
  private final static String EMPTY_STRING = "";

  @Override
  public void render(RequestContext requestContext) {
    FullHttpRequest httpRequest = requestContext.getHttpRequest();
    Response response = requestContext.getResponse();
    Object result = response.getResult();
    FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
            HTTP_1_1, httpRequest.decoderResult().isSuccess() ? OK : BAD_REQUEST,
            Unpooled.copiedBuffer(Objects.isNull(result)
                    ? EMPTY_STRING.getBytes(CharsetUtil.UTF_8)
                    : response.isJsonResponse()
                    ? JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8)
                    : result.toString().getBytes(StandardCharsets.UTF_8)
            )
    );
    HttpHeaders headers = fullHttpResponse.headers();
    headers.set(HttpHeaderNames.CONTENT_TYPE, response.isJsonResponse() ? APPLICATION_JSON_VALUE : TEXT_PLAIN_VALUE);
    if (HttpUtil.isKeepAlive(httpRequest)) {
      headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
      headers.setInt(HttpHeaderNames.CONTENT_LENGTH, fullHttpResponse.content().readableBytes());
    }
    requestContext.getContext().writeAndFlush(fullHttpResponse);
  }
}
