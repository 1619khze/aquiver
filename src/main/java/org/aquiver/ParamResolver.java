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
package org.aquiver;

import org.aquiver.route.RouteParam;
import org.aquiver.route.RouteParamType;

import java.io.IOException;
import java.lang.reflect.Parameter;

public interface ParamResolver {
  /**
   * Determine whether the parameter type is supported
   *
   * @param parameter Information about method parameters.
   * @return support
   */
  boolean support(Parameter parameter);

  /**
   * Parse parameters into RouteParam
   *
   * @param parameter Information about method parameters
   * @param paramName method parameter name
   * @return route param
   */
  RouteParam resolve(Parameter parameter, String paramName);

  /**
   * Assign parameters in advance according to the amount
   * of methods called by reflection
   *
   * @param handlerParam   route param
   * @param requestContext request context
   * @param url            request url
   * @return Assigned parameters
   */
  Object dispen(RouteParam handlerParam, RequestContext requestContext, String url) throws IOException;

  /**
   * Get the parameter type to be assigned
   *
   * @return Routing parameter type
   */
  RouteParamType dispenType();
}
