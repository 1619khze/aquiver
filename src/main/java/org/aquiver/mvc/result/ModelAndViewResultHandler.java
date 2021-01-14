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
package org.aquiver.mvc.result;

import org.apex.Apex;
import org.apex.ApexContext;
import org.apex.Environment;
import org.aquiver.ModelAndView;
import org.aquiver.RequestContext;
import org.aquiver.ResultHandler;
import org.aquiver.ViewHandler;
import org.aquiver.ViewHandlerResolver;
import org.aquiver.mvc.RequestResult;
import org.aquiver.ServerSpec;

/**
 * @author WangYi
 * @since 2020/8/25
 */
public final class ModelAndViewResultHandler implements ResultHandler {
  private final ApexContext context = ApexContext.of();
  private final Environment environment = Apex.of().environment();
  private final ViewHandlerResolver viewHandlerResolver = context.getBean(ViewHandlerResolver.class);

  @Override
  public boolean support(RequestResult requestResult) {
    return ModelAndView.class.isAssignableFrom(requestResult.getResultType());
  }

  @Override
  public void handle(RequestContext ctx, RequestResult result) throws Exception {
    ModelAndView modelAndView = (ModelAndView) result.getResultObject();
    String suffix = this.environment.get(ServerSpec.PATH_SERVER_VIEW_SUFFIX, ServerSpec.SERVER_VIEW_SUFFIX);
    ViewHandler lookup = this.viewHandlerResolver.lookup(suffix);
    lookup.render(ctx, modelAndView.htmlPath());
  }
}
