package org.aquiver;

import io.github.classgraph.ClassGraph;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ClassgraphDiscoverer implements Discoverer<List<Class<?>>> {

  private final ClassgraphOptions classgraphOptions;
  private final ClassGraph        classGraph;

  public ClassgraphDiscoverer(ClassgraphOptions classgraphOptions) {
    this.classgraphOptions = classgraphOptions;
    this.classGraph        = new ClassGraph();
  }

  @Override
  public List<Class<?>> discover(String scanPackageName) {
    this.classGraph.enableAllInfo();
    if (classgraphOptions != null) {
      this.scanPackageName(classgraphOptions.getScanPackages(), scanPackageName)
              .verbose(classgraphOptions.isVerbose()).enableRealtimeLogging(classgraphOptions.isEnableRealtimeLogging());
    }
    return this.classGraph.scan().getAllClasses().loadClasses();
  }

  /**
   * Set whiteList
   *
   * @param scanPackages
   * @param scanPackageName
   * @return
   */
  private ClassgraphDiscoverer scanPackageName(Set<String> scanPackages, String scanPackageName) {
    if (scanPackageName == null || !scanPackageName.contains(".")) {
      throw new IllegalArgumentException("scanPackageName cannot be empty and needs to conform to the specification");
    }
    if (scanPackages == null) {
      scanPackages = new LinkedHashSet<>();
    }
    scanPackages.add(scanPackageName);
    this.classGraph.whitelistPackages(scanPackages.toArray(new String[0]));
    return this;
  }

  /**
   * Set blacklist
   *
   * @param blackList
   * @return
   */
  private ClassgraphDiscoverer blackList(List<String> blackList) {
    if (blackList != null && blackList.size() > 0) {
      this.classGraph.blacklistPackages(blackList.toArray(new String[0]));
    }
    return this;
  }

  /**
   * Turn on verbose logging
   *
   * @param verbose
   * @return
   */
  private ClassgraphDiscoverer verbose(boolean verbose) {
    if (verbose) {
      this.classGraph.verbose();
    }
    return this;
  }

  /**
   * Enable real-time logging
   *
   * @param enableRealtimeLogging
   */
  private void enableRealtimeLogging(boolean enableRealtimeLogging) {
    if (enableRealtimeLogging) {
      this.classGraph.enableRealtimeLogging();
    }
  }
}
