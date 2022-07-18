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

package io.github.iruzhnikov.test.context.jupiter.test;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static io.github.iruzhnikov.test.context.jupiter.context.TestContextListener.Methods.*;
import static java.lang.reflect.Modifier.isStatic;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UtilityClass
public class ListenersTestUtils {
    public static void testMethodsConstantsDeclaration(Class<?> testedClass) {
        Class<?> methodsClass = Arrays.stream(testedClass.getDeclaredClasses())
                .filter(c -> "Methods".equals(c.getSimpleName())).findFirst().orElseThrow();
        var methodsConst = getMethodsNameConst(methodsClass);
        var methods = getMethodsName(testedClass);
        methodsConst.forEach(m -> assertTrue(methods.contains(m),
                () -> "Class '" + testedClass.getCanonicalName() + "' not contains method '" + m + "'"));
        methods.forEach(m -> assertTrue(methodsConst.contains(m),
                () -> "Class '" + methodsClass.getCanonicalName() + "' not contains constant for method name '"
                        + testedClass.getName() + "#" + m + "'"));
    }

    @NotNull
    private static List<String> getMethodsName(Class<?> testedClass) {
        return Arrays.stream(testedClass.getDeclaredMethods())
                .map(Method::getName).collect(Collectors.toList());
    }

    @NotNull
    public static List<String> getMethodsNameConst(Class<?> methodsClass) {
        return Arrays.stream(methodsClass.getFields())
                .filter(f -> f.getType() == String.class && isStatic(f.getModifiers()))
                .map(ListenersTestUtils::getValue).collect(Collectors.toList());
    }

    @SneakyThrows
    private static String getValue(Field field) {
        return (String) field.get(null);
    }

    public static void assertTestContextListenerCalled() {
        assertTrue(AbstractListenersCallTest.getInitialized(),
                "Test context must be run before this test");
        AbstractListenersCallTest.getNotCalledMethods().forEach((testedListener, notCalledMethods) ->
                assertTrue(notCalledMethods.isEmpty(), () -> "Not callable listener methods '" +
                        String.join(",", notCalledMethods) + "' in class '" +
                        testedListener.getName() + "'")
        );
    }

    public static abstract class AbstractListenersCallTest implements ServiceLoader.Provider<Object> {
        private final Object runtimeInstance;
        @Getter
        private static final Map<Class<?>, Set<String>> notCalledMethods = new ConcurrentHashMap<>();
        private static final AtomicBoolean initialized = new AtomicBoolean(false);

        public AbstractListenersCallTest(Class<?> testedListener) {
            initialized.set(true);
            notCalledMethods.computeIfAbsent(testedListener, this::getMethods);
            runtimeInstance = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{testedListener},
                    (proxy, method, args) -> {
                        notCalledMethods.get(testedListener).remove(method.getName());
                        return MethodHandles.privateLookupIn(testedListener, MethodHandles.lookup())
                                .in(testedListener)
                                .unreflectSpecial(method, testedListener)
                                .bindTo(proxy)
                                .invokeWithArguments(args);
                    });
        }

        public static boolean getInitialized() {
            return AbstractListenersCallTest.initialized.get();
        }

        //TODO add custom test runner for testing it
        protected Set<String> getMethods(Class<?> c) {
            var methodsName = getMethodsName(c);
            //region not testable, because this is not ordered by jupiter
            methodsName.remove(BEFORE_STOP_CONTEXT);
            methodsName.remove(STOP_CONTEXT);
            methodsName.remove(AFTER_STOP_CONTEXT);
            //endregion
            return new HashSet<>(methodsName);
        }

        @Override
        public Class<?> type() {
            return runtimeInstance.getClass();
        }

        @Override
        public Object get() {
            return runtimeInstance;
        }
    }
}
