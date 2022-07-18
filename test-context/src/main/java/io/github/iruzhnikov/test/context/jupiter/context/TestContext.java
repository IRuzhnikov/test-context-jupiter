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

import io.github.iruzhnikov.test.context.jupiter.TestContextManager;

/**
 * You can make custom implementation add full class path to jupiter config {@link org.junit.platform.engine.ConfigurationParameters#CONFIG_FILE_NAME}
 * with property key {@link TestContextManager#TEST_CONTEXT_CLASS_PROPERTY}
 */
public interface TestContext {
    void start();

    boolean stop();

    boolean isStopped();
}
