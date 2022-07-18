package io.github.iruzhnikov.test.context.jupiter.context.annotations;

import io.github.iruzhnikov.test.context.jupiter.TestContextExtension;

import java.lang.annotation.*;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Inherited
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(OverrideTestContextExtension.List.class)
public @interface OverrideTestContextExtension {
    @SuppressWarnings("rawtypes") Class<? extends TestContextExtension> rewrite();

    @SuppressWarnings("rawtypes") Class<? extends TestContextExtension> objective();

    Class<?> recommendAnnotation();

    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RUNTIME)
    @Documented
    @Inherited
    @interface List {
        OverrideTestContextExtension[] value();
    }
}
