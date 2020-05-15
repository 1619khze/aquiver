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
package org.aquiver.toolkit;

import org.aquiver.Const;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

import static io.netty.util.internal.PlatformDependent.isWindows;

/**
 * @author WangYi
 * @since 2019/6/10
 */
public abstract class Propertys {

  public static String toProperties(TreeMap<String, Map<String, Object>> config) {
    StringBuilder sb = new StringBuilder();
    for (String key : config.keySet()) {
      sb.append(toString(key, config.get(key)));
    }
    return sb.toString();
  }

  public static Map<String, String> confFieldMap() throws IllegalAccessException {
    final Map<String, String> constArgsMap = new HashMap<>();
    List<Field> fields = Arrays.asList(Const.class.getFields());
    for (Field field : new ArrayList<>(fields)) {
      if (field.getName().contains(Const.PATH_PREFIX_ROOT)) {
        continue;
      }
      constArgsMap.put(field.getName(), String.valueOf(field.get(field.getName())));
    }
    return constArgsMap;
  }

  public static String toString(String key, Map<String, Object> map) {
    StringBuilder sb = new StringBuilder();
    for (String mapKey : map.keySet()) {
      if (map.get(mapKey) instanceof Map) {
        sb.append(toString(String.format("%s.%s", key, mapKey), (Map<String, Object>) map.get(mapKey)));
      } else {
        sb.append(String.format("%s.%s=%s%n", key, mapKey, map.get(mapKey).toString()));
      }
    }
    return sb.toString();
  }

  public static Map<String, String> parseArgs(String[] args) {
    Map<String, String> argsMap = new HashMap<>();
    if (args == null || args.length == 0) {
      return argsMap;
    }

    for (String arg : args) {
      if (arg.startsWith("--") && arg.contains("=")) {
        String[] param = arg.substring(2).split("=");
        argsMap.put(param[0], param[1]);
      }
    }
    return argsMap;
  }

  public static TreeMap<String, Map<String, Object>> yaml(String location) {
    if (!location.startsWith("/")) {
      location = "/" + location;
    }
    InputStream resourceAsStream = Propertys.class.getResourceAsStream(location);
    if (resourceAsStream == null) {
      return null;
    }
    return yaml().loadAs(resourceAsStream, TreeMap.class);
  }

  private static Yaml yaml() {
    return new Yaml();
  }

  public static String getCurrentClassPath() {
    URL url = Propertys.class.getResource("/");
    String path;
    if (null == url) {
      File f = new File(Propertys.class.getProtectionDomain().getCodeSource().getLocation().getPath());
      path = f.getPath();
    } else {
      path = url.getPath();
    }
    try {
      if (isWindows()) {
        return decode(path.replaceFirst("^/(.:/)", "$1"));
      }
      return decode(path);
    } catch (UnsupportedEncodingException e) {
      return "/";
    }
  }

  private static String decode(String path) throws UnsupportedEncodingException {
    return java.net.URLDecoder.decode(path, "utf-8");
  }
}
