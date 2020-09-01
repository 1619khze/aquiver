package org.aquiver.mvc.interceptor;

import org.apex.ApexContext;
import org.aquiver.RequestContext;
import org.aquiver.mvc.RequestResult;
import org.aquiver.mvc.argument.MethodArgumentGetter;
import org.aquiver.mvc.router.RouteInfo;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

/**
 * @author WangYi
 * @since 2020/9/1
 */
public class AspectInterceptorChain implements InterceptorChain {
  private final List<Interceptor> interceptors;
  private final RequestContext ctx;
  private int currentIndex = 0;
  private RequestResult result;

  private final ApexContext apexContext = ApexContext.of();
  private final MethodHandles.Lookup lookup = MethodHandles.lookup();
  private final MethodArgumentGetter methodArgumentGetter = apexContext.getBean(MethodArgumentGetter.class);

  public AspectInterceptorChain(List<Interceptor> interceptors, RequestContext ctx) {
    this.interceptors = interceptors;
    this.ctx = ctx;
  }

  @Override
  public void invoke() throws Exception {
    if (currentIndex < interceptors.size()) {
      Interceptor interceptor = interceptors.get(currentIndex++);
      interceptor.intercept(ctx, this);
    } else {
      executeAction(ctx);
    }
  }

  public RequestResult getResult() {
    return result;
  }

  private void executeAction(RequestContext ctx) throws Exception {
    RouteInfo routeInfo = ctx.route();
    final Method method = routeInfo.getMethod();
    final Parameter[] parameters = method.getParameters();
    final List<Object> invokeArguments = methodArgumentGetter.getParams(parameters);
    try {
      final Object invokeResult = lookup.unreflect(method).bindTo(routeInfo.getBean())
              .invokeWithArguments(invokeArguments);
      this.result = new RequestResult(method.getReturnType(), invokeResult, method);
    } catch(Throwable throwable) {
      throwable.printStackTrace();
    }
  }
}
