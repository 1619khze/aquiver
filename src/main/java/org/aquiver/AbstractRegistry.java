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

import org.aquiver.mvc.RequestHandler;
import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author WangYi
 * @since 2020/5/23
 */
public abstract class AbstractRegistry implements RegisterStrategy {

  private final Map<String, RequestHandler> requestHandlers = new ConcurrentHashMap<>(64);

  private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  protected ReentrantReadWriteLock.ReadLock readLock() {
    return this.readWriteLock.readLock();
  }

  protected ReentrantReadWriteLock.WriteLock writeLock() {
    return this.readWriteLock.writeLock();
  }

  protected boolean repeat(String url) {
    return this.requestHandlers.containsKey(url);
  }

  protected void register(String url, RequestHandler requestHandler) {
    this.writeLock().lock();
    try {
      this.requestHandlers.put(url, requestHandler);
    } finally {
      this.readWriteLock.writeLock().unlock();
    }
  }

  public void unregister(String url) {
    this.writeLock().lock();
    try {
      this.requestHandlers.remove(url);
    } finally {
      this.writeLock().unlock();
    }
  }

  public Map<String, RequestHandler> getRequestHandlers() {
    readLock().lock();
    try {
      return this.requestHandlers;
    } finally {
      readLock().unlock();
    }
  }

  protected String getMethodUrl(String baseUrl, String methodMappingUrl) {
    StringBuilder url = new StringBuilder(256);
    url.append((baseUrl == null || baseUrl.trim().isEmpty()) ? "" : baseUrl.trim());
    if (methodMappingUrl != null && !methodMappingUrl.trim().isEmpty()) {
      String methodMappingUrlTrim = methodMappingUrl.trim();
      if (!methodMappingUrlTrim.startsWith("/")) {
        methodMappingUrlTrim = "/" + methodMappingUrlTrim;
      }
      if (url.toString().endsWith("/")) {
        url.setLength(url.length() - 1);
      }
      url.append(methodMappingUrlTrim);
    }
    return url.toString();
  }

  protected String[] getMethodParameterNamesByAsm(Class<?> clazz, final Method method) throws IOException {
    final Class<?>[] parameterTypes = method.getParameterTypes();
    if (parameterTypes.length == 0) {
      return null;
    }
    final Type[] types = new Type[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      types[i] = Type.getType(parameterTypes[i]);
    }
    final String[] parameterNames = new String[parameterTypes.length];

    String className = clazz.getName();
    int lastDotIndex = className.lastIndexOf(".");
    className = className.substring(lastDotIndex + 1) + ".class";

    InputStream inputStream = clazz.getResourceAsStream(className);
    ClassReader classReader = new ClassReader(inputStream);
    classReader.accept(new ClassVisitor(Opcodes.ASM4) {
      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return getMethodVisitor(name, desc, method, types, parameterNames);
      }
    }, 0);
    return parameterNames;
  }

  private MethodVisitor getMethodVisitor(String name, String desc, Method method, Type[] types, String[] parameterNames) {
    Type[] argumentTypes = Type.getArgumentTypes(desc);
    if (!method.getName().equals(name) || !Arrays.equals(argumentTypes, types)) {
      return null;
    }
    return new MethodVisitor(Opcodes.ASM4) {
      @Override
      public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        if (Modifier.isStatic(method.getModifiers())) {
          parameterNames[index] = name;
        } else if (index > 0 && index <= parameterNames.length) {
          parameterNames[index - 1] = name;
        }
      }
    };
  }
}
