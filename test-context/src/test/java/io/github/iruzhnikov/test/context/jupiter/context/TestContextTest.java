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
import io.github.iruzhnikov.test.context.jupiter.listeners.annotations.TestContextListener;
import io.github.iruzhnikov.test.context.jupiter.test.ListenersTestUtils;
import lombok.Getter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Order(Integer.MIN_VALUE)
@io.github.iruzhnikov.test.context.jupiter.context.annotations.TestContext
@TestContextListener(include = TestContextTest.TestContextStarted.class)
public class TestContextTest {

    @BeforeAll
    static void testCorrectContextStarting() {
        assertEquals(TestContextStarted.getLocal(), 1000);
    }

    @Test
    void testContextInjection(TestContext context) {
        assertNotNull(context);
    }

    @Test
    void methodsListTest() {
        ListenersTestUtils.testMethodsConstantsDeclaration(io.github.iruzhnikov.test.context.jupiter.context.TestContextListener.class);
    }

    public static class TestContextStarted implements io.github.iruzhnikov.test.context.jupiter.context.TestContextListener {
        @Getter
        private static volatile Integer local = 1126;

        @Override
        public void beforeStartContext(Injector injector) {
            local = 1000;
        }
    }
}
