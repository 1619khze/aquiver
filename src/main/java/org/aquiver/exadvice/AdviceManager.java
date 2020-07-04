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
package org.aquiver.exadvice;

import org.aquiver.RequestContext;
import org.aquiver.mvc.resolver.ParamResolverManager;
import org.aquiver.mvc.route.RouteParam;
import org.aquiver.toolkit.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WangYi
 * @since 2020/7/3
 */
public class AdviceManager {
  private static final Logger log = LoggerFactory.getLogger(AdviceManager.class);

  private final Map<Class<? extends Throwable>, Advice> adviceMap = new ConcurrentHashMap<>(8);

  public void addAdvice(Class<? extends Throwable> throwableCls, Advice advice) {
    this.adviceMap.put(throwableCls, advice);
  }

  public Object handlerException(Throwable throwable, ParamResolverManager resolverManager, RequestContext context) {
    if (adviceMap.isEmpty()) {
      context.request().channelHandlerContext().channel().close();
    }
    if (!adviceMap.containsKey(throwable.getClass())) {
      return null;
    }
    Advice advice = this.adviceMap.get(throwable.getClass());
    List<RouteParam> params = advice.params();

    Object[] paramValues = new Object[params.size()];
    Class<?>[] paramTypes = new Class[params.size()];
    try {
      ReflectionUtils.invokeParam(context, params, paramValues, paramTypes, resolverManager);
      if (Objects.nonNull(advice)) {
        return advice.handlerException(throwable, paramValues, paramTypes);
      }
    } catch (Throwable e) {
      log.error("An exception occurred when calling Advice for exception handling", e);
    }
    return null;
  }
}
