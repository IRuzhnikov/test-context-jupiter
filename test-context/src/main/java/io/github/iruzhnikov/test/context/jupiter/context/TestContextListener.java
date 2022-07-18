/*
 * Copyright (c) 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.iruzhnikov.test.context.jupiter.context;

import io.github.iruzhnikov.test.context.jupiter.injector.Injector;
import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Order;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Implement this class for making and configure same specific context for all tests in one test running
 * more in {@link TestContext}
 * <p>
 * Ordered by {@link org.junit.jupiter.api.Order}
 * <p>
 * Default order is {@link Order#DEFAULT}
 */
public interface TestContextListener {
    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void beforeStartContext(Injector injector) {
    }

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void startContext(Injector injector) {
    }

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void afterStartContext(Injector injector) {
    }

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void beforeStopContext(Injector injector) {
    }

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void stopContext(Injector injector) {
    }

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void afterStopContext(Injector injector) {
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target({
            ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE,
            ElementType.ANNOTATION_TYPE,
            ElementType.METHOD
    })
    @org.intellij.lang.annotations.MagicConstant(valuesFromClass = Methods.class)
    @interface MagicConstant {
    }

    @UtilityClass
    class Methods {
        public static final String STOP_CONTEXT = "stopContext";
        public static final String AFTER_STOP_CONTEXT = "afterStopContext";
        public static final String BEFORE_STOP_CONTEXT = "beforeStopContext";
        public static final String START_CONTEXT = "startContext";
        public static final String AFTER_START_CONTEXT = "afterStartContext";
        public static final String BEFORE_START_CONTEXT = "beforeStartContext";
    }
}
