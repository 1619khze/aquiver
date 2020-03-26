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

import com.google.inject.AbstractModule;
import org.aquiver.toolkit.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeanManager extends AbstractModule {

  private static final Logger log = LoggerFactory.getLogger(BeanManager.class);

  private final Map<Class<?>, Object> beanPool = new ConcurrentHashMap<>(64);

  private final Discoverer discoverer;
  private final String     scanPath;

  public BeanManager(Discoverer discoverer, String scanPath) {
    this.discoverer = discoverer;
    this.scanPath   = scanPath;
  }

  public void start() throws IllegalAccessException, InstantiationException {
    final Collection<Class<?>> discover = discoverer.discover(scanPath);
    Iterator<Class<?>>         iterator = discover.iterator();
    while (iterator.hasNext()) {
      Class<?> next   = iterator.next();
      boolean  normal = Reflections.isNormal(next);
      if (!normal) {
        iterator.remove();
      }
      if (log.isDebugEnabled()) {
        log.trace("Is this class normal: {}", normal);
      }
    }
  }
}

