package io.github.iruzhnikov.test.context.jupiter.context.annotations;

import io.github.iruzhnikov.test.context.jupiter.DefaultTestContextExtension;
import io.github.iruzhnikov.test.context.jupiter.ReloadableTestContextExtension;
import io.github.iruzhnikov.test.context.jupiter.context.TestContextListener;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Make same test context for all annotated class and function
 * <p>
 * You can include to project lib's with implements of {@link TestContextListener} or implement it by manual
 * <p>
 * For scanning listener instances please use Java SPI
 * (append lines with full class names of your implementations to file META-INF/services/base.TestContextListener)
 * <p>
 * You can inject to test methods all {@link }
 */
@Inherited
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(ReloadableTestContextExtension.class)
@OverrideTestContextExtension(
        rewrite = DefaultTestContextExtension.class,
        objective = ReloadableTestContextExtension.class,
        recommendAnnotation = ReloadableTestContext.class)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ReloadableTestContext.List.class)
public @interface ReloadableTestContext {
    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @Retention(RUNTIME)
    @Documented
    @Inherited
    @interface List {
        ReloadableTestContext[] value();
    }
}
