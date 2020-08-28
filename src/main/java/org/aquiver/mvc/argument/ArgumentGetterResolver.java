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
package org.aquiver.mvc.argument;

import org.apex.ApexContext;
import org.aquiver.Request;
import org.aquiver.RequestContext;
import org.aquiver.Response;
import org.aquiver.mvc.router.multipart.MultipartFile;
import org.aquiver.mvc.router.session.Session;
import org.aquiver.websocket.WebSocketContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/8/26
 */
public final class ArgumentGetterResolver implements GetterResolver<ArgumentGetter<?>> {
  private static final Logger log = LoggerFactory.getLogger(ArgumentGetterResolver.class);
  private final Map<Class<?>, ArgumentGetter<?>> mapping = new HashMap<>();
  private final ApexContext context = ApexContext.of();

  public ArgumentGetterResolver() {
    this.init();
  }

  @Override
  public void init() {
    // Register all Getters that inject parameters by type
    this.registerArgumentGetter(Request.class, RequestArgumentGetter.class);
    this.registerArgumentGetter(Session.class, SessionArgumentGetter.class);
    this.registerArgumentGetter(Response.class, ResponseArgumentGetter.class);
    this.registerArgumentGetter(Throwable.class, ThrowableArgumentGetter.class);
    this.registerArgumentGetter(MultipartFile.class, MultipartFileArgumentGetter.class);
    this.registerArgumentGetter(RequestContext.class, RequestContextArgumentGetter.class);
    this.registerArgumentGetter(WebSocketContext.class, WebSocketContextArgumentGetter.class);
  }

  @Override
  public void registerArgumentGetter(Class<?> bindClass, Class<?> argumentClass) {
    Objects.requireNonNull(bindClass, "bindClass can't be null");
    Objects.requireNonNull(argumentClass, "argumentClass can't be null");

    if (!ArgumentGetter.class.isAssignableFrom(argumentClass)) {
      throw new IllegalArgumentException(argumentClass.getName() + "can't assignable from ArgumentGetter");
    }

    if (log.isDebugEnabled()) {
      log.debug("register ArgumentGetter: {} -> {}", bindClass.getName(), argumentClass.getName());
    }

    final ArgumentGetter<?> argumentGetter = (ArgumentGetter<?>) this.context.addBean(argumentClass);
    this.mapping.put(bindClass, argumentGetter);
  }

  @Override
  public ArgumentGetter<?> lookup(Class<?> bindClass) {
    return mapping.getOrDefault(bindClass, null);
  }
}
