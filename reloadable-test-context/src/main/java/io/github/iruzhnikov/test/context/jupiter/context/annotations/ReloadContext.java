package io.github.iruzhnikov.test.context.jupiter.context.annotations;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.*;

/**
 * Mark context for restart
 */
@Inherited
@Tag("RestartContext")
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReloadContext {
    When value();

    enum When {
        /**
         * Restart before current call
         */
        BEFORE,
        /**
         * Restart before next call (don't restart in last test)
         */
        AFTER
    }
}
