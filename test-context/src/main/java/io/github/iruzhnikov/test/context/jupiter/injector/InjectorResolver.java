package io.github.iruzhnikov.test.context.jupiter.injector;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

public interface InjectorResolver {

    static Type getType(ParameterContext parameterContext) {
        return parameterContext.getParameter().getParameterizedType();
    }

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    boolean supportsParameter(@Nullable Injector injector, Type type);

    /**
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    <T> T resolveParameter(@Nullable Injector injector, Type type);

    /**
     * Usually you need use simple variant {@link InjectorResolver#supportsParameter(Injector, Type)}
     * <p>
     * Or override both methods
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default boolean supportsParameter(Injector injector, ParameterContext parameterContext,
                                      ExtensionContext extensionContext) throws ParameterResolutionException {
        return supportsParameter(injector, getType(parameterContext));
    }


    /**
     * Usually you need use simple variant {@link InjectorResolver#resolveParameter(Injector, Type)}
     * <p>
     * Or override both methods
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    default <T> T resolveParameter(Injector injector, ParameterContext parameterContext,
                                   ExtensionContext extensionContext) throws ParameterResolutionException {
        return resolveParameter(injector, getType(parameterContext));
    }
}
