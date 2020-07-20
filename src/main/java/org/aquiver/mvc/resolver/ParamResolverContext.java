package org.aquiver.mvc.resolver;

import org.aquiver.RequestContext;
import org.aquiver.websocket.WebSocketContext;

/**
 * @author WangYi
 * @since 2020/7/20
 */
public class ParamResolverContext {
  private RequestContext requestContext;
  private WebSocketContext webSocketContext;
  private Throwable throwable;

  public void requestContext(RequestContext requestContext) {
    this.requestContext = requestContext;
  }

  public RequestContext requestContext() {
    return requestContext;
  }

  public void webSocketContext(WebSocketContext webSocketContext) {
    this.webSocketContext = webSocketContext;
  }

  public WebSocketContext webSocketContext() {
    return webSocketContext;
  }

  public Throwable throwable() {
    return throwable;
  }

  public void throwable(Throwable throwable) {
    this.throwable = throwable;
  }
}
