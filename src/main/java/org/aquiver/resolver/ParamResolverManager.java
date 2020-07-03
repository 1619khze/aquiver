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
package org.aquiver.resolver;

import org.aquiver.ParamAssignment;
import org.aquiver.RequestContext;
import org.aquiver.route.RouteParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author WangYi
 * @since 2020/7/3
 */
public class ParamResolverManager extends AbstractParamResolver implements ParamAssignment {
  @Override
  public Object assignment(RouteParam handlerParam, RequestContext requestContext) throws Exception {
    Object dispen = null;
    for (ParamResolver paramResolver : getParamResolvers()) {
      if (!paramResolver.dispenType().equals(handlerParam.getType())) {
        continue;
      }
      dispen = paramResolver.dispen(handlerParam.getDataType(),
              handlerParam.getName(), requestContext);
    }
    return dispen;
  }

  public String[] getMethodParamName(final Method method) {
    Parameter[] parameters = method.getParameters();
    List<String> nameList = new ArrayList<>();
    for (Parameter param : parameters) {
      String name = param.getName();
      nameList.add(name);
    }
    return nameList.toArray(new String[0]);
  }
}
