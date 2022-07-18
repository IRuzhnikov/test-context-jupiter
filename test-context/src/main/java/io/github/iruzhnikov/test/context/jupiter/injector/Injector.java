package io.github.iruzhnikov.test.context.jupiter.injector;

import io.github.iruzhnikov.test.context.jupiter.context.TestContextListener;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;

import java.lang.reflect.Type;

public interface Injector {
    @Nullable <T> T inject(Type aClass, Injector injector);

    boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext);

    <T> T resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext);

    default <T> T inject(Type aClass) {
        return inject(aClass, this);
    }

    /**
     * Use it for getting instance of {@link TestContextListener} and them context objects
     */
    default <T> T inject(Class<T> aClass) {
        return inject((Type) aClass);
    }
}
