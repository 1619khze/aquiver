package org.aquiver.mvc.render;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import org.aquiver.Aquiver;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author WangYi
 * @since 2020/6/25
 */
public abstract class AbstractResponseRender {
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
