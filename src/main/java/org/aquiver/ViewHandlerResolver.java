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

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author WangYi
 * @since 2020/8/22
 */
public final class ViewHandlerResolver {
  private final Map<String, ViewHandler> viewHandlerMap = new HashMap<>();

  @PostConstruct
  public void initialize() {
    this.register(CssDataViewHandler.class);
    this.register(GifDataViewHandler.class);
    this.register(PngDataViewHandler.class);
    this.register(XmlDataViewHandler.class);
    this.register(HtmlDataViewHandler.class);
    this.register(JpegDataViewHandler.class);
    this.register(MarkdownDataViewHandler.class);
  }

  public void register(Class<?> viewHandlerClass) {
    if (!viewHandlerClass.isAssignableFrom(ViewHandler.class)) {
      return;
    }
    ApexContext context = ApexContext.of();
  }
}
