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

import io.github.iruzhnikov.test.context.jupiter.context.annotations.TestContext;
import io.github.iruzhnikov.test.context.jupiter.injector.Injector;
import io.github.iruzhnikov.test.context.jupiter.listeners.annotations.TestContextListener;
import io.github.iruzhnikov.test.context.jupiter.test.ListenersTestUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestContext
@TestContextListener(include = ListenerCallingTestContextRunnerTest.ListenersCallTest.class)
@Execution(ExecutionMode.SAME_THREAD)
class ListenerCallingTestContextRunnerTest {
    @BeforeAll
    static void beforeAll(Injector injector) {
        assertNotNull(injector);
    }

    @AfterAll
    static void afterAll(Injector injector) {
        assertNotNull(injector);
    }

    @BeforeEach
    void beforeEach(Injector injector) {
        assertNotNull(injector);
    }

    @AfterEach
    void afterEach(Injector injector) {
        assertNotNull(injector);
    }

    @Test
    void testContextRunning(Injector injector) {
        assertNotNull(injector);
    }

    public static class ListenersCallTest extends ListenersTestUtils.AbstractListenersCallTest {
        public ListenersCallTest() {
            super(io.github.iruzhnikov.test.context.jupiter.context.TestContextListener.class);
        }
    }
}
