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

import org.aquiver.loader.RouteAdviceLoader;
import org.aquiver.loader.RestfulRouterLoader;
import org.aquiver.loader.WebLoader;
import org.aquiver.loader.WebSocketLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author WangYi
 * @since 2020/8/23
 */
public class WebInitializer {
  private final Map<String, WebLoader> webLoaderMap = new HashMap<>();

  public WebInitializer() {
    this.registerWebLoader(new RestfulRouterLoader());
    this.registerWebLoader(new WebSocketLoader());
    this.registerWebLoader(new RouteAdviceLoader());
  }

  public void registerWebLoader(WebLoader webLoader) {
    this.webLoaderMap.put(webLoader.getClass().getName(), webLoader);
  }

  public void lookupWebLoader(String className) {
    this.webLoaderMap.getOrDefault(className, null);
  }

  public void initialize(Map<String, Object> instances, Aquiver aquiver) throws Exception {
    if (instances.isEmpty()) {
      return;
    }
    for (Map.Entry<String, WebLoader> entry : webLoaderMap.entrySet()) {
      entry.getValue().load(instances, aquiver);
    }
  }
}
