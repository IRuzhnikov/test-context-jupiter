package io.github.iruzhnikov.test.context.jupiter.listeners;

import io.github.iruzhnikov.test.context.jupiter.injector.Injector;

public interface ListenerLifecycle {

    /**
     * Called it after create instances for all listeners by SPI
     * <p>
     * Ordered by {@link org.junit.jupiter.api.Order}
     */
    void listenerLoaded(Injector injector);
}
