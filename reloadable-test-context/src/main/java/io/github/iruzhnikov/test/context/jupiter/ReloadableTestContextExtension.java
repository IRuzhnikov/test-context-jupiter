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

import io.github.iruzhnikov.test.context.jupiter.context.ReloadableTestContext;
import io.github.iruzhnikov.test.context.jupiter.injector.Injector;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Method;

public class ReloadableTestContextExtension<T extends ReloadableTestContext, I extends Injector>
        extends DefaultTestContextExtension<T, I> implements
        InvocationInterceptor,
        BeforeEachCallback, AfterEachCallback,
        BeforeAllCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        getTestContext(context).beforeAll(context);
    }

    @Override
    public void interceptBeforeAllMethod(Invocation<Void> invocation,
                                         ReflectiveInvocationContext<Method> invocationContext,
                                         ExtensionContext extensionContext) throws Throwable {
        getTestContext(extensionContext).interceptBeforeAllMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        getTestContext(context).beforeEach(context);
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation,
                                          ReflectiveInvocationContext<Method> invocationContext,
                                          ExtensionContext extensionContext) throws Throwable {
        getTestContext(extensionContext).interceptBeforeEachMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        getTestContext(extensionContext).interceptTestMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation,
                                         ReflectiveInvocationContext<Method> invocationContext,
                                         ExtensionContext extensionContext) throws Throwable {
        getTestContext(extensionContext).interceptAfterEachMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        getTestContext(context).afterEach(context);
    }

    @Override
    public void interceptAfterAllMethod(Invocation<Void> invocation,
                                        ReflectiveInvocationContext<Method> invocationContext,
                                        ExtensionContext extensionContext) throws Throwable {
        getTestContext(extensionContext).interceptAfterAllMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        getTestContext(context).afterAll(context);
    }
}
