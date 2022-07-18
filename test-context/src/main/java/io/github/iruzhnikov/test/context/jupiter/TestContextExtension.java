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

package io.github.iruzhnikov.test.context.jupiter;

import io.github.iruzhnikov.test.context.jupiter.context.TestContext;
import io.github.iruzhnikov.test.context.jupiter.injector.Injector;
import io.github.iruzhnikov.test.context.jupiter.managers.ManagerFabric;
import org.junit.jupiter.api.extension.*;

public interface TestContextExtension<T extends TestContext, I extends Injector> extends ParameterResolver, BeforeAllCallback {
    @Override
    default void beforeAll(ExtensionContext context) {
        //only for situation when TestExecutionListener is not called
        getTestContext(context);
    }

    @Override
    default boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return getInjector(extensionContext).supportsParameter(parameterContext, extensionContext);
    }

    @Override
    default Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return getInjector(extensionContext).resolveParameter(parameterContext, extensionContext);
    }

    default T getTestContext(ExtensionContext context) {
        //noinspection unchecked
        return (T) getTestContextManager(context).getContext();
    }

    default I getInjector(ExtensionContext context) {
        //noinspection unchecked
        return (I) getTestContextManager(context).getInjector();
    }

    default TestContextManager getTestContextManager(ExtensionContext context) {
        var manager = ManagerFabric.getInstance().getManager(context);
        manager.startContext();
        return manager;
    }
}
