package io.github.iruzhnikov.test.context.jupiter.context.impl;

import io.github.iruzhnikov.test.context.jupiter.TestContextManager;
import io.github.iruzhnikov.test.context.jupiter.context.TestContext;
import io.github.iruzhnikov.test.context.jupiter.context.TestContextListener;
import io.github.iruzhnikov.test.context.jupiter.injector.Injector;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

import static io.github.iruzhnikov.test.context.jupiter.context.TestContextListener.Methods.*;

/**
 * You can disable auto context closing by config `test.context.closable=false` in `{@link org.junit.platform.engine.ConfigurationParameters#CONFIG_FILE_NAME}`
 */
@Slf4j
public class TestContextImpl implements TestContext {
    @Getter
    private boolean isStopped = true;
    @Getter
    private final TestContextManager manager;

    public TestContextImpl(TestContextManager manager) {
        this.manager = manager;
        getManager().getListenersManager().loadListeners(TestContextListener.class);
    }

    @Override
    public void start() {
        if (isStopped) {
            try {
                fireListeners(BEFORE_START_CONTEXT, l -> l.beforeStartContext(injector()));
                isStopped = false;
                internalStart();
                fireListeners(AFTER_START_CONTEXT, l -> l.afterStartContext(injector()));
            } catch (Throwable ex) {
                isStopped = true;
                log.error("Crash starting TestContext");
                throw ex;
            }
        }
    }

    protected void internalStart() {
        fireListeners(START_CONTEXT, l -> l.startContext(injector()));
    }

    @Override
    public boolean stop() {
        if (!isStopped()) {
            try {
                log.info("Stopping TestContext ...");
                fireListeners(BEFORE_STOP_CONTEXT, l -> l.beforeStopContext(injector()));
                internalStop();
                isStopped = true;
                fireListeners(AFTER_STOP_CONTEXT, l -> l.afterStopContext(injector()));
                log.info("Stopped TestContext");
                return true;
            } catch (Throwable ex) {
                isStopped = false;
                log.error("Crash stopping TestContext");
                throw ex;
            }
        }
        return false;
    }

    protected void internalStop() {
        fireListeners(STOP_CONTEXT, l -> l.stopContext(injector()));
    }

    protected <T> void fireListeners(String methodName, Class<T> listenerType, Consumer<T> consumer) {
        getManager().getListenersManager().fireListeners(listenerType, methodName, consumer);
    }

    private void fireListeners(@TestContextListener.MagicConstant String methodName,
                               Consumer<TestContextListener> consumer) {
        fireListeners(methodName, TestContextListener.class, consumer);
    }

    protected Injector injector() {
        return getManager().getInjector();
    }
}
