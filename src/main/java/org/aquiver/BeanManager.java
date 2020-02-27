package org.aquiver;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.*;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BeanManager {

  private final Map<Class<?>, Object> beanMap = new ConcurrentHashMap<>(64);

  private static final Class<? extends Annotation> JAVAX_SINGLETON  = javax.inject.Singleton.class;
  private static final Class<? extends Annotation> JAVAX_INJECT     = javax.inject.Inject.class;
  private static final Class<? extends Annotation> GOOGLE_SINGLETON = com.google.inject.Singleton.class;
  private static final Class<? extends Annotation> GOOGLE_INJECT    = com.google.inject.Inject.class;

  private final Reflections                      reflections;
  private final Set<Class<? extends Annotation>> scanAnnotationSet;

  public BeanManager(String scanPath) {
    if (null == scanPath || scanPath.equals("") || !scanPath.contains(".")) {
      throw new IllegalArgumentException("Please use the correct path");
    }
    this.scanAnnotationSet = this.getScanAnnotationSet();
    this.reflections       = new Reflections(getConfigurationBuilder(scanPath));
  }

  private ConfigurationBuilder getConfigurationBuilder(String scanPath) {
    return new ConfigurationBuilder().forPackages(scanPath).addScanners(getScanners().toArray(new Scanner[8]));
  }

  private Set<Class<? extends Annotation>> getScanAnnotationSet() {
    Set<Class<? extends Annotation>> set = new HashSet<>();
    set.add(JAVAX_SINGLETON);
    set.add(GOOGLE_SINGLETON);
    return set;
  }

  public void start() throws IllegalAccessException, InstantiationException {
    Injector injector = Guice.createInjector(new AutoScanModule(reflections, scanAnnotationSet));
    for (Class<? extends Annotation> annotationCls : scanAnnotationSet) {
      this.collectBeans(annotationCls);
    }
    for (Class<?> typeClass : beanMap.keySet()) {
      Field[] fields = typeClass.getDeclaredFields();
      if (fields.length == 0) {
        continue;
      }
      this.providerInject(injector, typeClass, fields);
    }
  }

  private void collectBeans(Class<? extends Annotation> annotationCls) throws InstantiationException, IllegalAccessException {
    Set<Class<?>> typesAnnotatedWith = this.reflections.getTypesAnnotatedWith(annotationCls, true);
    for (Class<?> typeClass : typesAnnotatedWith) {
      Object type = typeClass.newInstance();
      this.beanMap.put(typeClass, type);
    }
  }

  private void providerInject(Injector injector, Class<?> typeClass, Field[] fields) throws IllegalAccessException {
    for (Field field : fields) {
      if (field.getAnnotation(JAVAX_INJECT) != null || field.getAnnotation(GOOGLE_INJECT) != null) {
        field.setAccessible(true);
        Provider<?> provider = injector.getProvider(field.getType());
        if (provider != null && provider.get() != null) {
          field.set(beanMap.get(typeClass), provider.get());
        }
      }
    }
  }

  private List<Scanner> getScanners() {
    List<Scanner> scannerList = new ArrayList<>();
    scannerList.add(new SubTypesScanner(false));
    scannerList.add(new TypeAnnotationsScanner());
    scannerList.add(new FieldAnnotationsScanner());
    scannerList.add(new MethodAnnotationsScanner());
    scannerList.add(new MethodParameterScanner());
    scannerList.add(new MethodParameterNamesScanner());
    scannerList.add(new SubTypesScanner(false));
    scannerList.add(new MemberUsageScanner());
    return scannerList;
  }
}
