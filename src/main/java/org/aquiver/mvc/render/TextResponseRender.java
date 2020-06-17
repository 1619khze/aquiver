package org.aquiver.mvc.render;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.aquiver.RequestContext;
import org.aquiver.mvc.Route;
import org.aquiver.mvc.view.ViewType;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.aquiver.mvc.MediaType.TEXT_PLAIN_VALUE;

/**
 * @author WangYi
 * @since 2020/6/17
 */
public class TextResponseRender implements ResponseRender {

  @Override
  public boolean support(ViewType viewType) {
    return viewType.equals(ViewType.TEXT);
  }

  @Override
  public void render(Route route, RequestContext requestContext) {
    FullHttpRequest httpRequest = requestContext.getHttpRequest();
    Object result = route.getInvokeResult();
    ByteBuf byteBuf = Unpooled.copiedBuffer(Objects.isNull(result) ? "".getBytes(CharsetUtil.UTF_8)
            : String.valueOf(result).getBytes(StandardCharsets.UTF_8));

    FullHttpResponse response = new DefaultFullHttpResponse(
            HTTP_1_1, httpRequest.decoderResult().isSuccess() ? OK : BAD_REQUEST, byteBuf);

    HttpHeaders headers = response.headers();
    headers.set(HttpHeaderNames.CONTENT_TYPE, TEXT_PLAIN_VALUE);
    if (HttpUtil.isKeepAlive(httpRequest)) {
      headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
      headers.setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
    }
    requestContext.getContext().writeAndFlush(response);
  }
}
