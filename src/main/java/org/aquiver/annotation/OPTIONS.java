package org.aquiver.annotation;

import java.lang.annotation.*;

/**
 * @author WangYi
 * @since 2020/5/25
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Path(method = PathMethod.OPTIONS)
public @interface OPTIONS {
  String value() default "";
}
