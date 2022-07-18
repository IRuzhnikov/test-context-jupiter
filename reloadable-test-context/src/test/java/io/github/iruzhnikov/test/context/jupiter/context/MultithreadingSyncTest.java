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

import io.github.iruzhnikov.test.context.jupiter.context.annotations.ReloadableTestContext;
import io.github.iruzhnikov.test.context.jupiter.listeners.annotations.TestContextListener;
import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ReloadableTestContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestContextListener(include = MultithreadingSyncTest.TestOrderListener.class)
public class MultithreadingSyncTest {

    @Test
    @Order(1)
    void orderTest_1(TestOrderListener listener) {
        listener.getCallCount().set(10);
    }

    @Test
    @Order(2)
    void orderTest_2(TestOrderListener listener) {
        listener.getCallCount().set(30);
    }

    @SneakyThrows
    @Test
    @Order(3)
    void orderTest_3(TestOrderListener listener) {
        listener.getCallCount().set(20);
    }

    @Test
    @Order(4)
    void orderTest_4(TestOrderListener listener) {
        assertEquals(20, listener.getCallCount().get(), "Method ordering in multithreading broken");
    }

    public static class TestOrderListener {
        @Getter
        private final AtomicInteger callCount = new AtomicInteger(0);
    }
}
