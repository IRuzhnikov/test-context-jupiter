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

package io.github.iruzhnikov.test.context.jupiter.managers;

import io.github.iruzhnikov.test.context.jupiter.InstanceUtils;
import io.github.iruzhnikov.test.context.jupiter.TestContextManager;
import io.github.iruzhnikov.test.context.jupiter.managers.annotations.ManagerId;
import io.github.iruzhnikov.test.context.jupiter.scanner.AnnotationsScanner;
import io.github.iruzhnikov.test.context.jupiter.scanner.impl.AnnotationsScannerImpl;
import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.launcher.TestIdentifier;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.github.iruzhnikov.test.context.jupiter.InstanceUtils.newInstance;

public class ManagerFabric {
    public static final String BASE_PROPERTY = "test.context";
    public static final String FABRIC_PROPERTY = BASE_PROPERTY + ".fabric";

    public static final String MANAGER_CLASS_PROPERTY = BASE_PROPERTY + ".manager.{id}.class";
    public static final String ANNOTATION_SCANNER_CLASS_PROPERTY = BASE_PROPERTY + ".annotation.scanner.class";

    private static final AtomicReference<ManagerFabric> instance;

    static {
        instance = new AtomicReference<>(newInstance(FABRIC_PROPERTY,
                args -> new ManagerFabric((ConfigurationParameters) args[0]), InstanceUtils.getConfig()));
    }

    public static ManagerFabric getInstance() {
        return instance.get();
    }

    private final Map<String, TestContextManager> managers;
    private AnnotationsScanner annotationsScanner;
    @Getter
    private final ConfigurationParameters config;

    public ManagerFabric(ConfigurationParameters config) {
        this.managers = new ConcurrentHashMap<>();
        this.config = config;
    }

    public TestContextManager getManager(String id) {
        return managers.computeIfAbsent(id, s -> newInstance(makeManagerClassProperty(id), getConfig(),
                args -> new TestContextManager((String) args[0], (AnnotationsScanner) args[1]), id, getAnnotationsScanner()));
    }

    public String makeManagerClassProperty(String id) {
        return MANAGER_CLASS_PROPERTY.replace("{id}", id);
    }

    public AnnotationsScanner getAnnotationsScanner() {
        if (annotationsScanner == null) {
            annotationsScanner = newInstance(ANNOTATION_SCANNER_CLASS_PROPERTY, getConfig(),
                    args -> new AnnotationsScannerImpl());
        }
        return annotationsScanner;
    }

    public Optional<TestContextManager> getManager(TestIdentifier testIdentifier) {
        return getManager(getAnnotationsScanner().getAnnotatedElements(testIdentifier));
    }

    public Optional<TestContextManager> getManager(Stream<AnnotatedElement> elementStream) {
        var id = elementStream.flatMap(ae -> getAnnotationsScanner()
                        .findAnnotation(ae, ManagerId.class).stream())
                .map(ManagerId::value).findFirst();
        return id.map(this::getManager);
    }

    public TestContextManager getManager(ExtensionContext context) {
        return getTestSource(context)
                .map(o -> getAnnotationsScanner().getAnnotatedElements(o))
                .map(this::getManager)
                .map(o -> o.orElseGet(() -> this.getManager("default")))
                .orElseGet(() -> this.getManager("default"));
    }

    @SuppressWarnings("Convert2Lambda")
    private Optional<Optional<TestSource>> getTestSource(ExtensionContext context) {
        return ReflectionUtils.findMethod(context.getClass(), "getTestDescriptor")
                .map(new Function<>() {
                    @Override
                    @SneakyThrows
                    public TestDescriptor apply(Method method) {
                        method.setAccessible(true);
                        return (TestDescriptor) method.invoke(context);
                    }
                }).map(o -> ((TestDescriptor) o).getSource());
    }
}
