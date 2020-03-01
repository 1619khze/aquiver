package org.aquiver.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = RequestMethod.HEAD)
public @interface HeadMapping {
  String value() default "";
}
