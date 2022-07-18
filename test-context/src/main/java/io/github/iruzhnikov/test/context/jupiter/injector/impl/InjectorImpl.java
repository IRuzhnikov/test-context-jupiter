package io.github.iruzhnikov.test.context.jupiter.injector.impl;

import io.github.iruzhnikov.test.context.jupiter.TestContextManager;
import io.github.iruzhnikov.test.context.jupiter.injector.Injector;
import io.github.iruzhnikov.test.context.jupiter.injector.InjectorResolver;
import io.github.iruzhnikov.test.context.jupiter.listeners.ListenersManager;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
public class InjectorImpl implements Injector {
    private final TestContextManager manager;

    public InjectorImpl(TestContextManager manager) {
        this.manager = manager;
        getListenersManager().loadListeners(InjectorResolver.class);
    }

    private List<InjectorResolver> getResolvers() {
        return getListenersManager().getListeners(InjectorResolver.class, "resolveParameter");
    }

    private ListenersManager getListenersManager() {
        return manager.getListenersManager();
    }

    @Override
    public <T> @Nullable T inject(Type type, Injector injector) {
        if (type instanceof Class) {
            var candidate = this.<T>getListenerCandidate(type);
            if (candidate.isPresent()) {
                return candidate.get();
            }
        }
        return getCandidate(type, getResolvers(), l -> l.supportsParameter(injector, type))
                .map(l -> l.<T>resolveParameter(injector, type))
                .orElse(null);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private <T> Optional<T> getListenerCandidate(Type type) {
        return getCandidate(type, getListenersManager().getContextListeners(),
                l -> ((Class<?>) type).isAssignableFrom(l.getClass())).map(c -> (T) c);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        var type = InjectorResolver.getType(parameterContext);
        return getListenerCandidate(type).isPresent() || getCandidate(type, getResolvers(),
                l -> l.supportsParameter(this, parameterContext, extensionContext)).isPresent();
    }

    @Override
    public <T> T resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        var type = InjectorResolver.getType(parameterContext);
        return this.<T>getListenerCandidate(type).orElseGet(() -> getCandidate(type, getResolvers(),
                l -> l.supportsParameter(this, parameterContext, extensionContext))
                .orElseThrow().resolveParameter(this, parameterContext, extensionContext));
    }

    @NotNull
    private <T> Optional<T> getCandidate(Type type, List<T> candidates, Predicate<T> predicate) {
        //don't try to find all implementations! sometimes we can't recognize
        //which object will be created by our request
        //(integrations with some frameworks without parameters checker supports)
        return candidates.stream().filter(predicate).findFirst();
    }
}
