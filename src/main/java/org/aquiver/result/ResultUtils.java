package org.aquiver.result;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;

/**
 * @author WangYi
 * @since 2020/8/27
 */
public final class ResultUtils {
  public static FullHttpResponse contentResponse(String content) {
    final FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled
            .copiedBuffer(content.getBytes(StandardCharsets.UTF_8)));

    HttpHeaders headers = response.headers();
    headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "x-requested-with,content-type");
    headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "POST,GET");
    headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
    headers.set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
    return response;
  }

  public static FullHttpResponse emptyResponse() {
    return contentResponse("");
  }
}
