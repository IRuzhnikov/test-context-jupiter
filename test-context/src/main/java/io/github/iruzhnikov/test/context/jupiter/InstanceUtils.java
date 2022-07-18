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

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.function.Function;

@UtilityClass
public class InstanceUtils {
    @NotNull
    public static <T> T newInstance(String configProperty, Function<Object[], T> defaultSupplier, Object... args) {
        return newInstance(configProperty, getConfig(), defaultSupplier, args);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String configProperty, ConfigurationParameters config,
                                    Function<Object[], T> defaultSupplier, Object... args) {
        return config.get(configProperty, cn -> ReflectionUtils.tryToLoadClass(cn).toOptional()
                        .map(c -> (Class<T>) c)
                        .map(aClass -> newInstance(aClass, args))
                        .orElse(null))
                .orElseGet(() -> defaultSupplier.apply(args));
    }

    @SneakyThrows
    public static <T> T newInstance(Class<T> aClass, Object... args) {
        return Arrays.stream(aClass.getConstructors())
                .filter(c -> c.getParameterTypes().length == args.length && isCompletableArgsTypes(c, args))
                .map(c -> constructorCast(aClass, c))
                .map(constructor -> ReflectionUtils.newInstance(constructor, args))
                .findFirst().orElseGet(() -> ReflectionUtils.newInstance(aClass));
    }

    public static boolean isCompletableArgsTypes(Constructor<?> c, Object[] args) {
        var parameterTypes = c.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            var arg = args[i];
            if (arg != null && !parameterType.isAssignableFrom(arg.getClass())) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings({"unchecked", "unused"})
    public static <T> Constructor<T> constructorCast(Class<T> aClass, Constructor<?> c) {
        return (Constructor<T>) c;
    }

    public static ConfigurationParameters getConfig() {
        return LauncherDiscoveryRequestBuilder.request().build().getConfigurationParameters();
    }
}
