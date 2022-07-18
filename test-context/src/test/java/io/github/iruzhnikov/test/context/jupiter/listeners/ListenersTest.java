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

package io.github.iruzhnikov.test.context.jupiter.listeners;

import ch.qos.logback.classic.Level;
import io.github.iruzhnikov.test.context.jupiter.context.annotations.TestContext;
import io.github.iruzhnikov.test.context.jupiter.injector.Injector;
import io.github.iruzhnikov.test.context.jupiter.listeners.annotations.TestContextListener;
import io.github.iruzhnikov.test.context.jupiter.test.MemoryAppender;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@TestContext
@TestContextListener(include = ListenersTest.Include_1.class)
@TestContextListener(include = ListenersTest.Include_2.class)
@TestContextListener(include = ListenersTest.Include_3.class)
@TestContextListener(include = ListenersTest.Exclude.class)
@TestContextListener(include = ListenersTest.ExcludeByName.class)
@TestContextListener(exclude = ListenersTest.Exclude.class)
@TestContextListener(excludeReference = "io.github.iruzhnikov.test.context.jupiter.listeners.ListenersTest.ExcludeByName")
public class ListenersTest {

    private static MemoryAppender logAppender;

    @Test
    void injectionTest(ListenersManager manager) {
        assertNotNull(manager);
    }

    @Test
    void injectionInclude_1() {
        assertTrue(logContains(Include_1.class));
    }

    @Test
    void injectionInclude_2() {
        assertTrue(logContains(Include_2.class));
    }

    @Test
    void injectionExclude() {
        assertFalse(logContains(Exclude.class));
    }

    @Test
    void injectionExcludeByName() {
        assertFalse(logContains(ExcludeByName.class));
    }

    @Test
    void orderingTest(Include_1 include1, Include_2 include2) {
        assertTrue(include1.getSelfInitCount() > include2.getSelfInitCount());
    }

    boolean logContains(Class<?> aClass) {
        return logAppender.contains("Fired : " + aClass.getTypeName(), Level.INFO);
    }

    @Order(2)
    public static class Include_1 extends AbstractFromAnnotation {
    }

    public static class Include_2 extends AbstractFromAnnotation {
        @Order(1)
        @Override
        public void listenerLoaded(Injector injector) {
            super.listenerLoaded(injector);
        }
    }

    public static class Include_3 extends AbstractFromAnnotation {
        @Order(0)
        @Override
        public void listenerLoaded(Injector injector) {
            logAppender = MemoryAppender.createInstance(AbstractFromAnnotation.class);
            super.listenerLoaded(injector);
        }
    }

    public static class Exclude extends AbstractFromAnnotation {
    }

    public static class ExcludeByName extends AbstractFromAnnotation {
    }

    @Slf4j
    public static abstract class AbstractFromAnnotation implements ListenerLifecycle {
        static AtomicInteger initCount = new AtomicInteger();
        @Getter
        int selfInitCount;

        @Override
        public void listenerLoaded(Injector injector) {
            selfInitCount = initCount.incrementAndGet();
            log.info("Fired : " + getClass().getTypeName());
        }
    }
}
