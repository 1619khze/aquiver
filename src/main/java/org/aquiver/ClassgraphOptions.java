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
