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
import org.aquiver.result.view.*;
import org.aquiver.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/8/22
 */
public final class ViewHandlerResolver {
  private static final Logger log = LoggerFactory.getLogger(ViewHandlerResolver.class);
  private final Map<String, ViewHandler> viewHandlerMap = new HashMap<>();
  private final ApexContext context = ApexContext.of();

  public ViewHandlerResolver() {
    this.init();
  }

  private void init() {
    this.register(CssDataViewHandler.class);
    this.register(GifDataViewHandler.class);
    this.register(PngDataViewHandler.class);
    this.register(XmlDataViewHandler.class);
    this.register(HtmlDataViewHandler.class);
    this.register(JpegDataViewHandler.class);
    this.register(MarkdownDataViewHandler.class);
    this.register(PebbleTemplateViewHandler.class);
  }

  public void register(Class<? extends ViewHandler> viewHandlerClass) {
    Objects.requireNonNull(viewHandlerClass, "viewHandlerClass can't be null");
    Aquiver.requireArgument(viewHandlerClass.isAssignableFrom(ViewHandler.class),
            "Cannot assign a %s to a %s", viewHandlerClass.getName(), ViewHandler.class.getName());

    ViewHandler viewHandler = context.addBean(viewHandlerClass);
    this.viewHandlerMap.put(viewHandler.getType(), viewHandler);

    if (log.isDebugEnabled()) {
      log.debug("register ViewHandler: {} -> {}", viewHandler.getType(), viewHandlerClass.getName());
    }
    String suffix = viewHandler.getSuffix();
    if (suffix != null) {
      suffix = StringUtils.removeStart(suffix, ".");
      if (viewHandlerMap.put(suffix, viewHandler) == null) {
        log.debug("register ViewHandler: {} -> {}", suffix, viewHandlerClass.getName());
      }
    }
  }

  public ViewHandler lookup(String type) {
    return viewHandlerMap.get(type);
  }
}
