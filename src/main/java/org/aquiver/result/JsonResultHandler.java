package org.aquiver.result;

import com.alibaba.fastjson.JSONObject;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.aquiver.RequestContext;
import org.aquiver.ResultHandler;
import org.aquiver.mvc.RequestResult;
import org.aquiver.mvc.annotation.JSON;
import org.aquiver.mvc.http.MediaType;

import java.lang.reflect.Method;

/**
 * @author WangYi
 * @since 2020/8/31
 */
public final class JsonResultHandler implements ResultHandler {

  @Override
  public boolean support(RequestResult requestResult) {
    Method method = requestResult.getMethod();
    return method.isAnnotationPresent(JSON.class);
  }

  @Override
  public void handle(RequestContext ctx, RequestResult result) throws Exception {
    String jsonString = JSONObject.toJSONString(result.getResultObject());
    HttpHeaders httpHeaders = new DefaultHttpHeaders();
    httpHeaders.add(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    FullHttpResponse jsonResponse = ResultUtils.contentResponse(jsonString);
    ctx.writeAndFlush(jsonResponse);
  }
}
