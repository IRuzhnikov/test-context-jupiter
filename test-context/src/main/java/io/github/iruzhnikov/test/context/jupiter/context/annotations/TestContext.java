package io.github.iruzhnikov.test.context.jupiter.context.annotations;

import io.github.iruzhnikov.test.context.jupiter.DefaultTestContextExtension;
import io.github.iruzhnikov.test.context.jupiter.context.TestContextListener;
import io.github.iruzhnikov.test.context.jupiter.managers.annotations.ManagerId;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

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
@ManagerId
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(DefaultTestContextExtension.class)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestContext {
}
