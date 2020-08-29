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
import org.apex.ApexContext;
import org.aquiver.*;
import org.aquiver.handler.ExceptionHandlerResolver;
import org.aquiver.mvc.RequestResult;
import org.aquiver.mvc.argument.*;
import org.aquiver.mvc.router.PathVarMatcher;
import org.aquiver.mvc.router.RestfulRouter;
import org.aquiver.mvc.router.RouteInfo;
import org.aquiver.result.ResultUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
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
  private final StaticFileServerHandler fileServerHandler;
  private final RestfulRouter restfulRouter;
  private final ExceptionHandlerResolver exceptionHandlerResolver;
  private final ResultHandlerResolver resultHandlerResolver;
  private final ArgumentGetterResolver argumentGetterResolver;
  private final AnnotationArgumentGetterResolver annotationResolver;

  private final MethodHandles.Lookup lookup = MethodHandles.lookup();
  private final ArgumentGetterContext argumentGetterContext = new ArgumentGetterContext();

  public NettyServerHandler() {
    final ApexContext context = ApexContext.of();
    this.fileServerHandler = new StaticFileServerHandler();
    this.exceptionHandlerResolver = context.getBean(ExceptionHandlerResolver.class);
    this.restfulRouter = context.getBean(RestfulRouter.class);
    this.resultHandlerResolver = context.getBean(ResultHandlerResolver.class);
    this.argumentGetterResolver = context.getBean(ArgumentGetterResolver.class);
    this.annotationResolver = context.getBean(AnnotationArgumentGetterResolver.class);
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
    this.exceptionHandlerResolver.handlerException(cause, argumentGetterContext);
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
      final RequestContext requestContext = this.buildRequestContext(request, ctx);
      this.argumentGetterContext.requestContext(requestContext);
      final RequestResult result = logicExecution(requestContext);
      if (Objects.nonNull(result)) {
        ResultHandler<Object> handler = resultHandlerResolver.lookup(result.getResultType());
        handler.handle(requestContext, result.getResultObject());
      }
    } catch(Throwable throwable) {
      exceptionCaught(ctx, throwable);
    }
  }

  private RequestContext buildRequestContext(FullHttpRequest request, ChannelHandlerContext ctx) {
    return new RequestContext(request, ctx);
  }

  public RequestResult logicExecution(RequestContext context) throws Throwable {
    final RouteInfo routeInfo = lookupRoute(context);
    final Method method = routeInfo.getMethod();
    final Parameter[] parameters = method.getParameters();
    final List<Object> invokeArguments = new ArrayList<>();
    context.route(routeInfo);

    for (Parameter parameter : parameters) {
      if (parameter.getAnnotations().length == 0) {
        ArgumentGetter<?> lookup = argumentGetterResolver.lookup(parameter.getType());
        Object argument = lookup.get(argumentGetterContext);
        invokeArguments.add(argument);
      } else {
        Annotation[] annotations = parameter.getAnnotations();
        for (Annotation annotation : annotations) {
          AnnotationArgumentGetter lookup = annotationResolver.lookup(annotation.annotationType());
          ArgumentContext argumentContext = new ArgumentContext(parameter, annotation, argumentGetterContext);
          invokeArguments.add(lookup.get(argumentContext));
        }
      }
    }
    final Object invokeResult = lookup.unreflect(method).bindTo(routeInfo.getBean())
            .invokeWithArguments(invokeArguments);
    return new RequestResult(method.getReturnType(), invokeResult);
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

    RouteInfo routeInfo = lookupRoute(lookupPath);
    if (Objects.isNull(routeInfo)) {
      lookupStaticFile(context);
    }
    return routeInfo;
  }

  private RouteInfo lookupRoute(String lookupPath) {
    RouteInfo lookup = restfulRouter.lookup(lookupPath);
    if (Objects.nonNull(lookup)) {
      return lookup;
    }
    for (Map.Entry<String, RouteInfo> entry : restfulRouter.getRoutes().entrySet()) {
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
      final NoRouteFoundException exception = new NoRouteFoundException
              (context.request().httpMethodName(), context.request().uri());
      FullHttpResponse response = ResultUtils.contentResponse(exception.getMessage());
      response.setStatus(HttpResponseStatus.NOT_FOUND);
      context.writeAndFlush(response);
      throw exception;
    }
  }
}
