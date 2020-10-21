package org.aquiver.mvc.http;

import io.netty.handler.codec.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

/**
 * @author WangYi
 * @since 2020/10/20
 */
public class Headers {
  private static final Map<String, Header> headerMap = new HashMap<>();

  public static Map<String, Header> parse(HttpHeaders headers) {
    if (headers.isEmpty()){
      return Headers.headerMap;
    }
    for (Map.Entry<String, String> header : headers) {
      final String key = header.getKey();
      final String value = header.getValue();
      if (key.equals("Cookie")) {
        continue;
      }
      Headers.headerMap.put(key, Header.init(key, value));
    }
    return Headers.headerMap;
  }
}
