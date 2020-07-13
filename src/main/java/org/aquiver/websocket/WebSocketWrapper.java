package org.aquiver.websocket;

import org.aquiver.RequestContext;
import org.aquiver.mvc.resolver.ParamResolverManager;
import org.aquiver.mvc.route.RouteParam;
import org.aquiver.utils.ReflectionUtils;
import org.aquiver.websocket.action.OnClose;
import org.aquiver.websocket.action.OnConnect;
import org.aquiver.websocket.action.OnError;
import org.aquiver.websocket.action.OnMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author WangYi
 * @since 2020/7/13
 */
public class WebSocketWrapper implements WebSocketChannel {
  private static final Logger log = LoggerFactory.getLogger(WebSocketWrapper.class);

  private final MethodHandles.Lookup lookup = MethodHandles.lookup();
  private final Map<Class<? extends Annotation>, WebSocketActionPair> methodCache;

  private final ParamResolverManager resolver;
  private Class<?> webSocketClass;

  public WebSocketWrapper(ParamResolverManager resolver) {
    this.resolver = resolver;
    this.methodCache = new HashMap<>();
  }

  public void initialize(Class<?> webSocketClass) {
    this.webSocketClass = webSocketClass;
    Method[] methods = webSocketClass.getMethods();
    if (methods.length == 0) {
      return;
    }
    final Map<Class<? extends Annotation>, WebSocketActionPair>
            cache = new HashMap<>(3);

    cacheMethod(cache, methods, OnClose.class);
    cacheMethod(cache, methods, OnConnect.class);
    cacheMethod(cache, methods, OnError.class);
    cacheMethod(cache, methods, OnMessage.class);

    if (cache.isEmpty()) {
      throw new RuntimeException("Do not found any annotation of " +
              "[@OnClose / @OnConnect / @OnError /@OnMessage] in class: " + webSocketClass.getName());
    }
    this.methodCache.putAll(cache);
  }

  private void cacheMethod(Map<Class<? extends Annotation>, WebSocketActionPair> cache, Method[] methods,
                           Class<? extends Annotation> filter) {
    List<Method> methodList = Stream.of(methods)
            .filter(method -> method.isAnnotationPresent(filter))
            .collect(Collectors.toList());

    if (methodList.size() == 1) {
      final Method method = methodList.get(0);
      final Parameter[] parameters = method.getParameters();
      final String[] paramNames = resolver.getMethodParamName(method);
      final List<RouteParam> routeParams = resolver
              .invokeParamResolver(parameters, paramNames);

      cache.put(filter, new WebSocketActionPair(method.getName(), routeParams));
    } else if (methodList.size() > 1) {
      throw new RuntimeException("Duplicate annotation @" + filter.getSimpleName()
              + " in class: " + methodList.get(0).getDeclaringClass().getName());
    }
  }

  @Override
  public void onConnect(WebSocketContext webSocketContext) {
    invokeAction(OnConnect.class, webSocketContext);
  }

  @Override
  public void onMessage(WebSocketContext message) {
    invokeAction(OnMessage.class, message);
  }

  @Override
  public void onClose(WebSocketContext webSocketContext) {
    invokeAction(OnClose.class, webSocketContext);
  }

  @Override
  public void onError(WebSocketContext webSocketContext) {
    invokeAction(OnError.class, webSocketContext);
  }

  private void invokeAction(Class<? extends Annotation> actionAnnotation, WebSocketContext webSocketContext) {
    if (!methodCache.containsKey(actionAnnotation)) {
      return;
    }
    WebSocketActionPair webSocketActionPair = methodCache.get(actionAnnotation);
    List<RouteParam> params = webSocketActionPair.params();
    Object[] paramValues = new Object[params.size()];
    Class<?>[] paramTypes = new Class[params.size()];
    try {
      final RequestContext requestContext = webSocketContext.requestContext();
      requestContext.webSocketContext(webSocketContext);

      ReflectionUtils.invokeParam(requestContext,
              params, paramValues, paramTypes, resolver);

      Method method = ReflectionUtils.getInvokeMethod(webSocketClass,
              webSocketActionPair.method(), paramTypes);

      this.lookup.unreflect(method).bindTo(webSocketClass.newInstance())
              .invokeWithArguments(paramValues);
    } catch (Throwable e) {
      log.error("An exception occurred when obtaining invoke param", e);
      webSocketContext.disconnect();
    }
  }

  static class WebSocketActionPair {
    private String method;
    private List<RouteParam> params;

    public WebSocketActionPair(String method, List<RouteParam> params) {
      this.method = method;
      this.params = params;
    }

    public String method() {
      return method;
    }

    public void method(String method) {
      this.method = method;
    }

    public List<RouteParam> params() {
      return params;
    }

    public void params(List<RouteParam> params) {
      this.params = params;
    }
  }
}
