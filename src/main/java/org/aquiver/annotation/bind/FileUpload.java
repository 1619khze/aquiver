package org.aquiver.annotation.bind;

import java.lang.annotation.*;

/**
 * @author WangYi
 * @since 2020/6/14
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FileUpload {
  String value() default "";
}
