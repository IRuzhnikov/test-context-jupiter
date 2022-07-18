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
import io.github.iruzhnikov.test.context.jupiter.listeners.annotations.TestContextListener;
import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ReloadableTestContext
@TestContextListener(include = ReloadTestInMultithreadingTest.LongStartedListener.class)
public class ReloadTestInMultithreadingTest {
    @SneakyThrows
    @Test
    void orderTest_3() {
        Thread.sleep(200);
    }

    @SneakyThrows
    @Test
    @ReloadContext(ReloadContext.When.BEFORE)
    void longTest(ReloadTestInMultithreadingTest.LongStartedListener listener) {
        assertNotEquals(156, listener.getLock(), "Context in restarting process!");
        Thread.sleep(50);
        assertNotEquals(156, listener.getLock(), "Context in restarting process!");
    }

    public static class LongStartedListener implements ReloadableTestContextListener {
        @Getter
        private int lock = 0;

        @SneakyThrows
        @Override
        public void afterRestartStopContext(Injector injector, ExtensionContext extensionContext, List<ExtensionContext> afterContext) {
            lock = 156;
            Thread.sleep(200);
            lock = 654;
        }
    }
}
