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

import io.github.iruzhnikov.test.context.jupiter.context.annotations.ReloadContext;
import io.github.iruzhnikov.test.context.jupiter.context.annotations.ReloadableTestContext;
import io.github.iruzhnikov.test.context.jupiter.injector.Injector;
import io.github.iruzhnikov.test.context.jupiter.listeners.ListenersTest;
import io.github.iruzhnikov.test.context.jupiter.listeners.annotations.TestContextListener;
import io.github.iruzhnikov.test.context.jupiter.test.ListenersTestUtils;
import io.github.iruzhnikov.test.context.jupiter.test.MemoryAppender;
import org.junit.jupiter.api.*;

import static ch.qos.logback.classic.Level.INFO;

@ReloadableTestContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestContextListener(include = ReloadableTestContextTest.LogAppender.class)
class ReloadableTestContextTest {

    private static MemoryAppender logAppender;

    @Test
    @Order(1)
    @ReloadContext(ReloadContext.When.BEFORE)
    void checkRestartContextBefore() {
        Assertions.assertTrue(logAppender.contains("Restart context finished", INFO));
        logAppender.reset();
    }

    @Test
    @Order(2)
    @ReloadContext(ReloadContext.When.AFTER)
    void checkRestartContextAfter_0() {
        Assertions.assertFalse(logAppender.contains("Restart context finished", INFO));
        logAppender.reset();
    }

    @Test
    @Order(3)
    void checkRestartContextAfter_1() {
        Assertions.assertTrue(logAppender.contains("Restart context finished", INFO));
        logAppender.reset();
    }

    @Test
    void methodsListTest() {
        ListenersTestUtils.testMethodsConstantsDeclaration(ReloadableTestContextListener.class);
    }

    public static class LogAppender extends ListenersTest.AbstractFromAnnotation {
        @Override
        public void listenerLoaded(Injector injector) {
            logAppender = MemoryAppender.createInstance(ListenersTest.AbstractFromAnnotation.class);
            super.listenerLoaded(injector);
        }
    }
}
