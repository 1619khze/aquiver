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

import java.util.Set;

public class ClassgraphOptions {
  private final Set<String> scanPackages;
  private       boolean     verbose;
  private       boolean     enableRealtimeLogging;

  public ClassgraphOptions(Set<String> scanPackages, boolean verbose, boolean enableRealtimeLogging) {
    this.scanPackages          = scanPackages;
    this.verbose               = verbose;
    this.enableRealtimeLogging = enableRealtimeLogging;
  }

  public static FastClassgraphScannerConfigBuilder builder() {
    return new FastClassgraphScannerConfigBuilder();
  }

  public boolean isVerbose() {
    return verbose;
  }

  public boolean isEnableRealtimeLogging() {
    return enableRealtimeLogging;
  }

  public Set<String> getScanPackages() {
    return scanPackages;
  }

  public static class FastClassgraphScannerConfigBuilder {
    private Set<String> scanPackages;
    private boolean     verbose;
    private boolean     enableRealtimeLogging;

    public ClassgraphOptions.FastClassgraphScannerConfigBuilder scanPackages(Set<String> scanPackages) {
      this.scanPackages = scanPackages;
      return this;
    }

    public ClassgraphOptions.FastClassgraphScannerConfigBuilder verbose(boolean verbose) {
      this.verbose = verbose;
      return this;
    }

    public ClassgraphOptions.FastClassgraphScannerConfigBuilder enableRealtimeLogging(boolean enableRealtimeLogging) {
      this.enableRealtimeLogging = enableRealtimeLogging;
      return this;
    }

    public ClassgraphOptions build() {
      return new ClassgraphOptions(this.scanPackages, this.verbose, this.enableRealtimeLogging);
    }
  }
}
