package io.github.iruzhnikov.test.context.jupiter.managers.annotations;

import java.lang.annotation.*;

@Inherited
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ManagerId {
    String value() default "default";
}
