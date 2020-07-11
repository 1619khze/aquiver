package org.aquiver.websocket;

import java.lang.annotation.*;

/**
 * @author WangYi
 * @since 2020/7/11
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebSocket {
  String value() default "";
}
