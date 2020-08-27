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
package org.aquiver.result;

import org.aquiver.*;

import java.util.Objects;

import static org.aquiver.Const.SERVER_VIEW_SUFFIX;

/**
 * @author WangYi
 * @since 2020/8/22
 */
public final class StringResultHandler implements ResultHandler<String> {
  private final ViewHandlerResolver viewHandlerResolver;
  private ViewHandler defaultViewHandler;

  public StringResultHandler() {
    this.viewHandlerResolver = new ViewHandlerResolver();
  }

  @Override
  public void handle(RequestContext ctx, String result) throws Exception {
    ViewHandler viewHandler = null;
    String url;

    if (result == null) {
      // 使用默认的 ViewPathName
      url = ctx.request().uri();
    } else {
      int idx = result.indexOf(':');
      if (idx > 0) {
        // 根据 URL 前缀查找 view
        String type = result.substring(0, idx + 1);
        url = result.substring(idx + 1);
        viewHandler = viewHandlerResolver.lookup(type);
        if (viewHandler == null) {
          ctx.writeAndFlush(ResultUtils.emptyResponse());
          return;
        }
      } else {
        url = result;
      }
    }

    if (viewHandler == null) {
      // 根据后缀名查找 view
      String suffix = getFileSuffix(result);
      if (suffix != null) {
        viewHandler = viewHandlerResolver.lookup(suffix);
        if (viewHandler == null) {
          ctx.writeAndFlush(ResultUtils.emptyResponse());
          return;
        }
      }
    }

    if (viewHandler == null) {
      // 使用默认配置 view
      if (defaultViewHandler == null) {
        defaultViewHandler = viewHandlerResolver.lookup(SERVER_VIEW_SUFFIX);
        if (defaultViewHandler == null) {
          ctx.writeAndFlush(ResultUtils.emptyResponse());
          return;
        }
      }
      viewHandler = defaultViewHandler;
    }

    viewHandler.render(ctx, url);
  }

  private String getFileSuffix(String result) {
    if (Objects.isNull(result)) {
      return null;
    }
    int i = result.lastIndexOf(".");
    if (i == -1) {
      return null;
    } else {
      int folderIndex = getFileSeparatorIndex(result);
      return folderIndex > i ? null : result.substring(i + 1);
    }
  }

  private static int getFileSeparatorIndex(String path) {
    for (int i = path.length() - 1; i >= 0; --i) {
      char c = path.charAt(i);
      if (c == '/' || c == '\\') {
        return i;
      }
    }
    return -1;
  }
}
