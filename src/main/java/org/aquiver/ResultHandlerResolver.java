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

import org.apex.ApexContext;
import org.aquiver.mvc.RequestResult;
import org.aquiver.mvc.result.JsonResultHandler;
import org.aquiver.mvc.result.ModelAndViewResultHandler;
import org.aquiver.mvc.result.StringResultHandler;
import org.aquiver.mvc.result.VoidResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/8/22
 */
public class ResultHandlerResolver {
  private static final Logger log = LoggerFactory.getLogger(ResultHandlerResolver.class);
  private final List<ResultHandler> mapping = new ArrayList<>();
  private final ApexContext context = ApexContext.of();

  public ResultHandlerResolver() {
    this.init();
  }

  private void init() {
    this.register(VoidResultHandler.class);
    this.register(StringResultHandler.class);
    this.register(ModelAndViewResultHandler.class);
    this.register(JsonResultHandler.class);
  }

  public void register(Class<? extends ResultHandler> resultHandlerClass) {
    Objects.requireNonNull(resultHandlerClass, "resultHandlerClass can't be null");

    if (log.isDebugEnabled()) {
      log.debug("register ResultHandler: -> {}", resultHandlerClass.getName());
    }
    ResultHandler resultHandler = this.context.addBean(resultHandlerClass);
    this.mapping.add(resultHandler);
  }

  public ResultHandler lookup(RequestResult result) {
    return mapping.stream().filter(resultHandler -> resultHandler
            .support(result)).findFirst().orElse(null);
  }
}
