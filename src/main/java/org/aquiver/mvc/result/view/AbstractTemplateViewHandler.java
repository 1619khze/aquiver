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
package org.aquiver.mvc.result.view;

import org.apex.Environment;
import org.aquiver.Aquiver;
import org.aquiver.RequestContext;
import org.aquiver.ResponseBuilder;
import org.aquiver.ViewHandler;
import org.aquiver.server.Const;

import java.io.File;
import java.net.URL;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/8/22
 */
public abstract class AbstractTemplateViewHandler implements ViewHandler {
  protected final Environment environment = Aquiver.of().environment();

  public abstract String getPrefix();

  @Override
  public void render(RequestContext ctx, String viewPathName) throws Exception {
    String viewPath = viewPathName;
    if (!getPrefix().equals("")) {
      viewPath = "/" + getPrefix() + viewPath;
    }

    viewPath = Const.SERVER_TEMPLATES_FOLDER + File.separator + viewPath;
    if (!viewPath.endsWith(getSuffix())) {
      viewPath = viewPath + "." + getSuffix();
    }
    URL viewUrl = Thread.currentThread().getContextClassLoader().getResource(viewPath);
    if (Objects.isNull(viewUrl)) {
      ctx.tryPush(ResponseBuilder.builder().body(viewPathName).build());
    } else {
      doRender(ctx, viewPath);
    }
  }

  protected abstract void doRender(RequestContext ctx, String viewPathName) throws Exception;
}
