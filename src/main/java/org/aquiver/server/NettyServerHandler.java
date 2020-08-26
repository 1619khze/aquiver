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
package org.aquiver.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.aquiver.*;
import org.aquiver.function.AdviceManager;
import org.aquiver.function.handler.HttpExceptionHandler;
import org.aquiver.mvc.RequestResult;
import org.aquiver.mvc.argument.ArgumentGetterContext;
import org.aquiver.mvc.argument.ArgumentResolverManager;
import org.aquiver.mvc.router.NoRouteFoundException;
import org.aquiver.mvc.router.PathVarMatcher;
import org.aquiver.mvc.router.RouteInfo;
import org.aquiver.mvc.router.RouteParam;
import org.aquiver.mvc.router.render.ResponseRenderMatcher;
import org.aquiver.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/5/26
 */
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<Object> {
  private static final Logger log = LoggerFactory.getLogger(NettyServerHandler.class);

  private FullHttpRequest request;
  private RequestContext requestContext;
  private final Map<String, RouteInfo> routeMap;
  private final HttpExceptionHandler exceptionHandler;
  private final StaticFileServerHandler fileServerHandler;
  private final AdviceManager adviceManager;
  private final ArgumentResolverManager resolverManager;
  private final MethodHandles.Lookup lookup = MethodHandles.lookup();
  private final ArgumentGetterContext argumentGetterContext = new ArgumentGetterContext();
  private final ResultHandlerResolver resultHandlerResolver = new ResultHandlerResolver();

  public NettyServerHandler() {
    final Aquiver aquiver = Aquiver.of();
    this.routeMap = aquiver.routeManager().getRoutes();
    this.exceptionHandler = new HttpExceptionHandler();
    this.fileServerHandler = new StaticFileServerHandler();
    this.adviceManager = aquiver.adviceManager();
    this.resolverManager = aquiver.resolverManager();
  }

  @Override
  public boolean acceptInboundMessage(final Object msg) {
    return msg instanceof HttpMessage;
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    log.error("An exception occurred when calling the mapping method", cause);
    this.argumentGetterContext.throwable(cause);
    final Object handlerExceptionResult = this.adviceManager
            .handlerException(cause, resolverManager, argumentGetterContext);
    if (Objects.nonNull(handlerExceptionResult)) {
      return;
    }
    ctx.close();
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
    if (msg instanceof HttpRequest) {
      HttpRequest request = (HttpRequest) msg;
      this.request = new DefaultFullHttpRequest(
              request.protocolVersion(), request.method(), request.uri());
    }

    if (msg instanceof FullHttpRequest) {
      this.request = (FullHttpRequest) msg;
    }

    if (Const.FAVICON_PATH.equals(request.uri())) {
      return;
    }
    try {
      this.requestContext = this.buildRequestContext(request, ctx);
      RequestResult result = logicExecution(requestContext);
      if (Objects.nonNull(result)) {
        ResultHandler<Object> handler = resultHandlerResolver.lookup(result.getResultType());
        handler.handle(requestContext, result.getResultObject());
      }
    }
    catch(Throwable throwable) {
      exceptionCaught(ctx,throwable);
    }
  }

  public RequestResult logicExecution(RequestContext context) throws Throwable {
    RouteInfo routeInfo = lookupRoute(context);
    return invokeParam(routeInfo);
  }

  private RouteInfo lookupRoute(RequestContext context) throws Exception {
    String lookupPath = lookupPath(context.request().uri());
    int paramStartIndex = lookupPath.indexOf("?");
    if (paramStartIndex > 0) {
      lookupPath = lookupPath.substring(0, paramStartIndex);
    }

    if (lookupPath.equals("")) {
      lookupPath = "/";
    }

    RouteInfo routeInfo = loopLookUp(lookupPath);
    if (Objects.isNull(routeInfo)) {
      lookupStaticFile(context);
    }
    return routeInfo;
  }

  private RouteInfo loopLookUp(String lookupPath) {
    if (routeMap.containsKey(lookupPath)) {
      return this.routeMap.get(lookupPath);
    }
    for (Map.Entry<String, RouteInfo> entry : routeMap.entrySet()) {
      String[] lookupPathSplit = lookupPath.split("/");
      String[] mappingUrlSplit = entry.getKey().split("/");
      String matcher = PathVarMatcher.getMatch(entry.getKey());
      if (!lookupPath.startsWith(matcher) || lookupPathSplit.length != mappingUrlSplit.length) {
        continue;
      }
      if (PathVarMatcher.checkMatch(lookupPathSplit, mappingUrlSplit)) {
        return entry.getValue();
      }
    }
    return null;
  }

  private String lookupPath(String uri) {
    return uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
  }

  private void lookupStaticFile(RequestContext context) throws Exception {
    final boolean result = this.fileServerHandler.handle(context);
    if (!result) {
      this.handlerNoRouteFoundException(context);
      throw new NoRouteFoundException(context.request().httpMethodName(), context.request().uri());
    }
  }

  private RequestResult invokeParam(RouteInfo routeInfo) throws Throwable {
    List<RouteParam> params = routeInfo.getParams();

    Object[] paramValues = new Object[params.size()];
    Class<?>[] paramTypes = new Class[params.size()];

    this.argumentGetterContext.requestContext(requestContext);
    ReflectionUtils.invokeParam(argumentGetterContext, routeInfo.getParams(),
            paramValues, paramTypes, resolverManager);
    routeInfo.setParamValues(paramValues);
    routeInfo.setParamTypes(paramTypes);
    return invokeMethod(routeInfo);
  }

  private RequestResult invokeMethod(RouteInfo routeInfo) throws Throwable {
    final Method method = ReflectionUtils.getInvokeMethod(routeInfo.getClazz()
            , routeInfo.getMethod(), routeInfo.getParamTypes());

    final Object result = lookup.unreflect(method).bindTo(routeInfo.getBean())
            .invokeWithArguments(routeInfo.getParamValues());

    log.info("{} {} {}", requestContext.request().ipAddress(),
            requestContext.request().httpMethodName(), routeInfo.getUrl());
    return new RequestResult(method.getReturnType(), result);
  }

  private void handlerNoRouteFoundException(RequestContext requestContext) {
    FullHttpRequest httpRequest = requestContext.request().httpRequest();
    NoRouteFoundException noRouteFoundException =
            new NoRouteFoundException(httpRequest.method().name(), httpRequest.uri());
    this.exceptionHandler.handle(requestContext, noRouteFoundException,
            HttpResponseStatus.NOT_FOUND);
  }

  private RequestContext buildRequestContext(FullHttpRequest request, ChannelHandlerContext ctx) {
    return new RequestContext(request, ctx);
  }
}
