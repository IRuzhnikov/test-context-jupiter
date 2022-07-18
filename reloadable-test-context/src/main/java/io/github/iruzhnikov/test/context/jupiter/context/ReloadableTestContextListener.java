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
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

public interface ReloadableTestContextListener extends TestContextListener {

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void beforeRestartStopContext(Injector injector, ExtensionContext extensionContext,
                                          List<ExtensionContext> afterContext) {
    }

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void restartStopContext(Injector injector, ExtensionContext extensionContext,
                                    List<ExtensionContext> afterContext) {
    }

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void afterRestartStopContext(Injector injector, ExtensionContext extensionContext,
                                         List<ExtensionContext> afterContext) {
    }

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void restartStartContext(Injector injector, ExtensionContext extensionContext,
                                     List<ExtensionContext> afterContext) {
    }

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void afterRestartStartContext(Injector injector, ExtensionContext extensionContext,
                                          List<ExtensionContext> afterContext) {
    }

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void beforeAll(Injector injector, ExtensionContext extensionContext) {
    }

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void afterAll(Injector injector, ExtensionContext extensionContext) {
    }

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void beforeEach(Injector injector, ExtensionContext extensionContext) {
    }

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void afterEach(Injector injector, ExtensionContext extensionContext) {
    }

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void beforeAllHandle(Injector injector, ExtensionContext extensionContext, Throwable th) {
    }

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void beforeEachHandle(Injector injector, ExtensionContext extensionContext, Throwable th) {
    }

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void testMethodHandle(Injector injector, ExtensionContext extensionContext, Throwable th) {
    }

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void afterEachHandle(Injector injector, ExtensionContext extensionContext, Throwable th) {
    }

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default void afterAllHandle(Injector injector, ExtensionContext extensionContext, Throwable th) {
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
        public static final String AFTER_RESTART_START_CONTEXT = "afterRestartStartContext";
        public static final String RESTART_START_CONTEXT = "restartStartContext";
        public static final String AFTER_RESTART_STOP_CONTEXT = "afterRestartStopContext";
        public static final String RESTART_STOP_CONTEXT = "restartStopContext";
        public static final String BEFORE_RESTART_STOP_CONTEXT = "beforeRestartStopContext";
        public static final String AFTER_EACH = "afterEach";
        public static final String BEFORE_EACH = "beforeEach";
        public static final String AFTER_ALL = "afterAll";
        public static final String BEFORE_ALL = "beforeAll";
        public static final String BEFORE_ALL_HANDLE = "beforeAllHandle";
        public static final String BEFORE_EACH_HANDLE = "beforeEachHandle";
        public static final String TEST_METHOD_HANDLE = "testMethodHandle";
        public static final String AFTER_EACH_HANDLE = "afterEachHandle";
        public static final String AFTER_ALL_HANDLE = "afterAllHandle";
    }
}
