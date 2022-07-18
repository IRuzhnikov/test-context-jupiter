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

package io.github.iruzhnikov.test.context.jupiter.scanner.impl;

import io.github.iruzhnikov.test.context.jupiter.TestContextExtension;
import io.github.iruzhnikov.test.context.jupiter.scanner.AnnotationsScanner;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.*;
import org.junit.platform.launcher.TestIdentifier;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class AnnotationsScannerImpl implements AnnotationsScanner {
    private final Set<TestIdentifier> testIdentifiers;

    public AnnotationsScannerImpl() {
        testIdentifiers = new CopyOnWriteArraySet<>();
    }

    @Override
    public void register(TestIdentifier testIdentifier) {
        testIdentifier.getSource().map(this::getAnnotatedElements)
                .filter(s -> s.anyMatch(this::isTestContext)).ifPresent(ignored -> testIdentifiers.add(testIdentifier));
    }

    @Override
    public <T extends Annotation> List<T> getAnnotations(TestIdentifier testIdentifier, Class<T> annotation,
                                                         Filter... customFilters) {
        register(testIdentifier);
        return internalGetAnnotations(testIdentifier, annotation, customFilters).collect(toList());
    }

    @Override
    public <T extends Annotation> List<T> getAnnotations(Class<T> annotation, Filter... customFilters) {
        return testIdentifiers.stream().flatMap(i -> internalGetAnnotations(i, annotation, customFilters)).collect(toList());
    }

    @Override
    public <T extends Annotation> Optional<T> getSingleAnnotation(TestIdentifier testIdentifier, Class<T> annotation,
                                                                  Filter... customFilters) {
        register(testIdentifier);
        return internalGetAnnotations(testIdentifier, annotation, customFilters).reduce((a, b) -> {
            throw new IllegalStateException(testIdentifier.getUniqueId() + " Multiple elements: " + a + ", " + b);
        });
    }

    @Override
    public <T extends Annotation> Optional<T> getSingleAnnotation(Class<T> annotation, Filter... customFilters) {
        return testIdentifiers.stream()
                .flatMap(i -> internalGetAnnotations(i, annotation, customFilters)).reduce((a, b) -> {
                    throw new IllegalStateException("Multiple elements: " + a + ", " + b);
                });
    }

    @Override
    public <T extends Annotation> Optional<T> findAnnotation(AnnotatedElement ae, Class<T> annotation) {
        if (annotation.isAnnotationPresent(Repeatable.class)) {
            return findAnnotations(ae, annotation).stream().reduce((a, b) -> {
                throw new IllegalStateException(ae + " Multiple elements: " + a + ", " + b);
            });
        } else {
            return AnnotationUtils.findAnnotation(ae, annotation);
        }
    }

    @Override
    public <T extends Annotation> List<T> findAnnotations(AnnotatedElement ae, Class<T> annotation) {
        if (annotation.isAnnotationPresent(Repeatable.class)) {
            return AnnotationUtils.findRepeatableAnnotations(ae, annotation);
        } else {
            return AnnotationUtils.findAnnotation(ae, annotation).map(List::of).orElseGet(List::of);
        }
    }

    @NotNull
    @Override
    public <T extends Annotation> Stream<T> getAnnotations(TestSource source, Class<T> annotation,
                                                           Filter... customFilters) {
        return getAnnotations(Optional.ofNullable(source), annotation, customFilters);
    }

    @NotNull
    @Override
    public Stream<AnnotatedElement> getAnnotatedElements(TestIdentifier testIdentifier, Filter... customFilters) {
        return getAnnotatedElements(testIdentifier.getSource(), customFilters);
    }

    @NotNull
    protected <T extends Annotation> Stream<T> internalGetAnnotations(TestIdentifier testIdentifier, Class<T> annotation,
                                                                      Filter... customFilters) {
        return getAnnotations(testIdentifier.getSource(), annotation, customFilters);
    }

    @NotNull
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected <T extends Annotation> Stream<T> getAnnotations(Optional<TestSource> source, Class<T> annotation,
                                                              Filter... customFilters) {
        return getAnnotatedElements(source, customFilters)
                .flatMap(ae -> findAnnotations(ae, annotation).stream());
    }

    @NotNull
    @Override
    public Stream<AnnotatedElement> getAnnotatedElements(Optional<TestSource> source, Filter... customFilters) {
        return source
                .filter(s -> Arrays.stream(customFilters).allMatch(f -> f.apply(s)))
                .map(this::getAnnotatedElements).orElseGet(Stream::empty)
                .filter(this::isTestContext)
                .map(i -> {
                    if (i instanceof Method) {
                        return Pair.of(((Method) i).getDeclaringClass().getName() + "#" + ((Method) i).getName(), i);
                    } else if (i instanceof Class) {
                        return Pair.of(((Class<?>) i).getName() + "#class", i);
                    } else {
                        return Pair.of(i.toString() + "#toString", i);
                    }
                }).collect(toMap(Pair::getKey, Pair::getValue)).values().stream();
    }

    protected boolean isTestContext(AnnotatedElement ae) {
        return findAnnotations(ae, ExtendWith.class).stream()
                .flatMap(a -> Arrays.stream(a.value())).anyMatch(TestContextExtension.class::isAssignableFrom);
    }

    protected Stream<AnnotatedElement> getAnnotatedElements(TestSource source) {
        if (source instanceof ClassSource) {
            var s = (ClassSource) source;
            return Stream.of(s.getJavaClass());
        } else if (source instanceof MethodSource) {
            var s = (MethodSource) source;
            return Stream.of(s.getJavaMethod(), s.getJavaClass());
        } else if (source instanceof ClasspathResourceSource) {
            return Stream.of();
        } else if (source instanceof PackageSource) {
            return Stream.of();
        } else if (source instanceof CompositeTestSource) {
            var s = (CompositeTestSource) source;
            return s.getSources().stream().flatMap(this::getAnnotatedElements);
        } else if (source instanceof UriSource) {
            if (source instanceof FileSystemSource) {
                if (source instanceof DirectorySource) {
                    return Stream.of();
                } else if (source instanceof FileSource) {
                    return Stream.of();
                } else {
                    return Stream.of();
                }
            } else {
                return Stream.of();
            }
        } else {
            return Stream.of();
        }
    }

    public static class Pair<K, V> extends AbstractMap.SimpleImmutableEntry<K, V> {
        public Pair(K key, V value) {
            super(key, value);
        }

        public static <K, V> Pair<K, V> of(K key, V value) {
            return new Pair<>(key, value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof Pair) {
                return Objects.equals(getKey(), ((Pair<?, ?>) o).getKey());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getKey());
        }
    }
}
