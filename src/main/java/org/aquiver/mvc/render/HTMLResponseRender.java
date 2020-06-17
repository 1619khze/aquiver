package org.aquiver.mvc.render;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.aquiver.Aquiver;
import org.aquiver.RequestContext;
import org.aquiver.mvc.MediaType;
import org.aquiver.mvc.Route;
import org.aquiver.mvc.view.ViewType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
    params.put("name", "Mitchell");

    final Aquiver aquiver = requestContext.getAquiver();
    final String viewSuffix = aquiver.environment().getString(PATH_SERVER_VIEW_SUFFIX, SERVER_VIEW_SUFFIX);

    final String htmlPath = String.valueOf(route.getInvokeResult());
    String fullHtmlPath = "templates/" + htmlPath;
    if (!fullHtmlPath.endsWith(viewSuffix) && fullHtmlPath.contains(".")) {
      String[] split = fullHtmlPath.split("\\.");
      fullHtmlPath = (split[0] + viewSuffix).replace("//","/");
    }
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
