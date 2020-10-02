package org.aquiver.cors;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author WangYi
 * @since 2020/10/2
 */
public class CorsBuilder {
  private final List<String> origins = new ArrayList<>();

  public List<String> getOrigins() {
    return origins;
  }

  public void addOrigin(String origin) {
    Validate.notNull(origin, "Origin can't be null");
    this.origins.add(origin);
  }

  public void addOrigins(List<String> origins) {
    Validate.notEmpty(origins, "Origin list can't be empty");
    this.origins.addAll(origins);
  }
}
