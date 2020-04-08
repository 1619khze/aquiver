package org.aquiver.annotation.bind;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestCookies {
  String value() default "";
}
