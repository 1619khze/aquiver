package org.aquiver.annotation.bind;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
    
    String value() default "";
    
    boolean required() default true;
}
