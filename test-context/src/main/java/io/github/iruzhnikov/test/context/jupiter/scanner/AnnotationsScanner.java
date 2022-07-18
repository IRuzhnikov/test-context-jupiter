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

package io.github.iruzhnikov.test.context.jupiter.scanner;

import org.jetbrains.annotations.NotNull;
import org.junit.platform.engine.TestSource;
import org.junit.platform.launcher.TestIdentifier;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public interface AnnotationsScanner {
    void register(TestIdentifier testIdentifier);

    <T extends Annotation> List<T> getAnnotations(TestIdentifier testIdentifier, Class<T> annotation, Filter... customFilters);

    <T extends Annotation> List<T> getAnnotations(Class<T> annotation, Filter... customFilters);

    <T extends Annotation> Optional<T> getSingleAnnotation(TestIdentifier testIdentifier, Class<T> annotation,
                                                           Filter... customFilters);

    <T extends Annotation> Optional<T> getSingleAnnotation(Class<T> annotation, Filter... customFilters);

    <T extends Annotation> Optional<T> findAnnotation(AnnotatedElement ae, Class<T> annotation);

    <T extends Annotation> List<T> findAnnotations(AnnotatedElement ae, Class<T> annotation);

    @NotNull <T extends Annotation> Stream<T> getAnnotations(TestSource source, Class<T> annotation, Filter... customFilters);

    @NotNull Stream<AnnotatedElement> getAnnotatedElements(TestIdentifier testIdentifier, Filter... customFilters);

    @NotNull
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    Stream<AnnotatedElement> getAnnotatedElements(Optional<TestSource> source, Filter... customFilters);

    @FunctionalInterface
    interface Filter extends Function<TestSource, Boolean> {

    }
}
