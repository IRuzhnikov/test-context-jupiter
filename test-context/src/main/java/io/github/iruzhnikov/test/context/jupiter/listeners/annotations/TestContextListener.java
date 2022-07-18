package io.github.iruzhnikov.test.context.jupiter.listeners.annotations;


import java.lang.annotation.*;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(TestContextListener.List.class)
@Documented
public @interface TestContextListener {
    Class<?> include() default Void.class;

    Class<?> exclude() default Void.class;

    String excludeReference() default "";

    @Target({TYPE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        TestContextListener[] value();
    }
}
