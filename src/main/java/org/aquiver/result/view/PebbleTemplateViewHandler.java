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
package org.aquiver.result.view;

import org.apex.annotation.Value;
import org.aquiver.RequestContext;

/**
 * @author WangYi
 * @since 2020/8/23
 */
public final class PebbleTemplateViewHandler extends AbstractTemplateViewHandler {
  @Value(value = "${server.view.prefix}")
  private String prefix;

  @Override
  public String getPrefix() {
    return null;
  }

  @Override
  protected void doRender(RequestContext ctx, String viewPathName) throws Exception {

  }

  @Override
  public String getSuffix() {
    return ".jsp";
  }

  @Override
  public String getType() {
    return "jsp";
  }

  @Override
  public ViewHandlerType getHandlerType() {
    return ViewHandlerType.TEMPLATE_VIEW;
  }
}
