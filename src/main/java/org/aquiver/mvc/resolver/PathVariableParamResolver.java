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
package org.aquiver.mvc.resolver;

import org.aquiver.RequestContext;
import org.aquiver.mvc.annotation.bind.PathVar;
import org.aquiver.mvc.route.PathVarMatcher;
import org.aquiver.mvc.route.RouteParam;
import org.aquiver.mvc.route.RouteParamType;

import java.lang.reflect.Parameter;

public class PathVariableParamResolver extends AbstractParamResolver implements ParamResolver {

  @Override
  public boolean support(Parameter parameter) {
    return parameter.isAnnotationPresent(PathVar.class);
  }

  @Override
  public RouteParam resolve(Parameter parameter, String paramName) {
    RouteParam handlerParam = new RouteParam();
    PathVar pathVar = parameter.getAnnotation(PathVar.class);
    handlerParam.setDataType(parameter.getType());
    handlerParam.setName((!"".equals(pathVar.value()) && !pathVar.value().trim().isEmpty()) ?
            pathVar.value().trim() : paramName);
    handlerParam.setRequired(true);
    handlerParam.setType(RouteParamType.PATH_VARIABLE);
    return handlerParam;
  }

  @Override
  public Object dispen(Class<?> paramType, String paramName, RequestContext requestContext) {
    return paramType.cast(
            PathVarMatcher.getPathVariable(requestContext.request().uri(),
                    requestContext.route().getUrl(), paramName));
  }

  @Override
  public RouteParamType dispenType() {
    return RouteParamType.PATH_VARIABLE;
  }
}
