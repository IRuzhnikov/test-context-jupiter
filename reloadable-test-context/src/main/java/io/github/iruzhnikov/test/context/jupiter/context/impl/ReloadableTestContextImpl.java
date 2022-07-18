package io.github.iruzhnikov.test.context.jupiter.context.impl;

import io.github.iruzhnikov.test.context.jupiter.TestContextManager;
import io.github.iruzhnikov.test.context.jupiter.context.ReloadableTestContext;
import io.github.iruzhnikov.test.context.jupiter.context.ReloadableTestContextListener;
import io.github.iruzhnikov.test.context.jupiter.context.annotations.ReloadContext;
import io.github.iruzhnikov.test.context.jupiter.injector.Injector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static io.github.iruzhnikov.test.context.jupiter.context.ReloadableTestContextListener.Methods.*;
import static io.github.iruzhnikov.test.context.jupiter.context.annotations.ReloadContext.When.AFTER;
import static io.github.iruzhnikov.test.context.jupiter.context.annotations.ReloadContext.When.BEFORE;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

@Slf4j
public class ReloadableTestContextImpl extends TestContextImpl implements ReloadableTestContext {
    private final List<ExtensionContext> neededBeforeReloadContext = new CopyOnWriteArrayList<>();
    private final AtomicBoolean hasAfterReload = new AtomicBoolean(false);
    private final AtomicBoolean enteredToReload = new AtomicBoolean(false);
    private final ReentrantLock reloadLock = new ReentrantLock();

    private final ThreadLocal<Integer> logCall = ThreadLocal.withInitial(() -> 0);

    public ReloadableTestContextImpl(TestContextManager manager) {
        super(manager);
        getManager().getListenersManager().loadListeners(ReloadableTestContextListener.class);
    }

    @Override
    protected void internalStop() {
        super.internalStop();
        neededBeforeReloadContext.clear();
        enteredToReload.set(false);
        hasAfterReload.set(false);
        unlock("internalStop", null);
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        lock(BEFORE_ALL, context);
        try {
            reloadBefore(context, BEFORE_ALL);
            fireListeners(BEFORE_ALL, l -> l.beforeAll(injector(), context));
        } finally {
            unlock(BEFORE_ALL, context);
        }
    }

    @Override
    public void interceptBeforeAllMethod(InvocationInterceptor.Invocation<Void> invocation,
                                         ReflectiveInvocationContext<Method> invocationContext,
                                         ExtensionContext extensionContext) throws Throwable {
        try {
            callInterceptor(invocation, extensionContext, BEFORE_ALL_HANDLE, ReloadableTestContextListener::beforeAllHandle);
        } finally {
            unlock(BEFORE_ALL_HANDLE, extensionContext);
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        lock(BEFORE_EACH, context);
        try {
            reloadBefore(context, BEFORE_EACH);
            fireListeners(BEFORE_EACH, l -> l.beforeEach(injector(), context));
        } finally {
            unlock(BEFORE_EACH, context);
        }
    }

    @Override
    public void interceptBeforeEachMethod(InvocationInterceptor.Invocation<Void> invocation,
                                          ReflectiveInvocationContext<Method> invocationContext,
                                          ExtensionContext extensionContext) throws Throwable {
        try {
            callInterceptor(invocation, extensionContext, BEFORE_EACH_HANDLE, ReloadableTestContextListener::beforeEachHandle);
        } finally {
            unlock(BEFORE_EACH_HANDLE, extensionContext);
        }
    }

    @Override
    public void interceptTestMethod(InvocationInterceptor.Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        try {
            callInterceptor(invocation, extensionContext, TEST_METHOD_HANDLE, ReloadableTestContextListener::testMethodHandle);
        } finally {
            unlock(TEST_METHOD_HANDLE, extensionContext);
        }
    }

    @Override
    public void interceptAfterEachMethod(InvocationInterceptor.Invocation<Void> invocation,
                                         ReflectiveInvocationContext<Method> invocationContext,
                                         ExtensionContext extensionContext) throws Throwable {
        lock(AFTER_EACH_HANDLE, extensionContext);
        try {
            callInterceptor(invocation, extensionContext, AFTER_EACH_HANDLE, ReloadableTestContextListener::afterEachHandle);
        } finally {
            unlock(AFTER_EACH_HANDLE, extensionContext);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        try {
            reloadAfter(context, AFTER_EACH);
            fireListeners(AFTER_EACH, l -> l.afterEach(injector(), context));
        } finally {
            unlock(AFTER_EACH_HANDLE, context);
        }
    }

    @Override
    public void interceptAfterAllMethod(InvocationInterceptor.Invocation<Void> invocation,
                                        ReflectiveInvocationContext<Method> invocationContext,
                                        ExtensionContext extensionContext) throws Throwable {
        lock(AFTER_ALL_HANDLE, extensionContext);
        try {
            callInterceptor(invocation, extensionContext, AFTER_ALL_HANDLE, ReloadableTestContextListener::afterAllHandle);
        } finally {
            unlock(AFTER_ALL_HANDLE, extensionContext);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        try {
            reloadAfter(context, AFTER_ALL);
            fireListeners(AFTER_ALL, l -> l.afterAll(injector(), context));
        } finally {
            unlock(AFTER_ALL, context);
        }
    }

    protected void unlock(String method, ExtensionContext context) {
        if (reloadLock.isHeldByCurrentThread()) {
            log("unlock-" + method, false, context);
            reloadLock.unlock();
        }
    }

    protected void lock(String method, ExtensionContext context) {
        log(method, true, context);
        reloadLock.lock();
        log(method, false, context);
    }

    protected void log(String method, boolean beforeLock, ExtensionContext context) {
        if (log.isTraceEnabled()) {
            logCall.set(logCall.get() + 1);
            String stringBuilder = "| " +
                    StringUtils.rightPad(logCall.get().toString(), 5) + " | " +
                    StringUtils.rightPad(Thread.currentThread().getName(), 25) + " | " +
                    StringUtils.rightPad(beforeLock ? "before" : "after", 6) + " | " +
                    StringUtils.rightPad(method, 10) + " | " +
                    StringUtils.rightPad(context == null ? "<none>" : context.getUniqueId(), 210) + " |";
            log.trace(stringBuilder);
        }
    }

    protected void reloadBefore(ExtensionContext context, @ReloadableTestContextListener.MagicConstant String methodName) {
        var annotation = findAnnotation(context.getElement(), ReloadContext.class);
        if (annotation.isPresent() && annotation.get().value().equals(BEFORE) || hasAfterReload.get()) {
            if (!enteredToReload.getAndSet(true)) {
                try {
                    reload(context);
                } finally {
                    neededBeforeReloadContext.clear();
                    enteredToReload.set(false);
                    hasAfterReload.set(false);
                }
            } else {
                logNotFiredExecution(context, methodName);
            }
        }
    }

    protected void reloadAfter(ExtensionContext context, @ReloadableTestContextListener.MagicConstant String methodName) {
        var annotation = findAnnotation(context.getElement(), ReloadContext.class);
        if (annotation.isPresent() && annotation.get().value().equals(AFTER)) {
            if (!hasAfterReload.getAndSet(true) || !enteredToReload.get()) {
                neededBeforeReloadContext.add(context);
            } else {
                logNotFiredExecution(context, methodName);
            }
        }
    }

    protected void logNotFiredExecution(ExtensionContext context, String methodName) {
        var message = "ExtensionContext '" + context.getUniqueId() + "' not fired '" + methodName +
                "', because firing process already started";
        if (log.isDebugEnabled() || log.isTraceEnabled()) {
            log.warn(message);
        } else {
            log.info(message);
        }
    }

    protected void reload(ExtensionContext context) {
        log.info("Restarting context ...");
        fireListeners(BEFORE_RESTART_STOP_CONTEXT,
                l -> l.beforeRestartStopContext(injector(), context, neededBeforeReloadContext));
        fireListeners(RESTART_STOP_CONTEXT,
                l -> l.restartStopContext(injector(), context, neededBeforeReloadContext));
        fireListeners(AFTER_RESTART_STOP_CONTEXT,
                l -> l.afterRestartStopContext(injector(), context, neededBeforeReloadContext));
        fireListeners(RESTART_START_CONTEXT,
                l -> l.restartStartContext(injector(), context, neededBeforeReloadContext));
        fireListeners(AFTER_RESTART_START_CONTEXT,
                l -> l.afterRestartStartContext(injector(), context, neededBeforeReloadContext));
        log.info("Restart context finished");
    }

    private void fireListeners(@ReloadableTestContextListener.MagicConstant String methodName,
                               Consumer<ReloadableTestContextListener> consumer) {
        fireListeners(methodName, ReloadableTestContextListener.class, consumer);
    }

    private void callInterceptor(InvocationInterceptor.Invocation<Void> invocation,
                                 ExtensionContext extensionContext, String methodName,
                                 ListenerOfHandler<ReloadableTestContextListener> listenerConsumer) throws Throwable {
        callInterceptor(invocation, extensionContext, methodName, ReloadableTestContextListener.class, listenerConsumer);
    }

    protected <T> void callInterceptor(InvocationInterceptor.Invocation<Void> invocation,
                                       ExtensionContext extensionContext, String methodName,
                                       @SuppressWarnings("SameParameterValue") Class<T> listenerType,
                                       ListenerOfHandler<T> listenerConsumer) throws Throwable {
        boolean notThrown = true;
        try {
            invocation.proceed();
        } catch (Throwable internal) {
            notThrown = false;
            fireListeners(methodName, listenerType,
                    l -> listenerConsumer.handle(l, injector(), extensionContext, internal));
            throw internal;
        } finally {
            if (notThrown) {
                fireListeners(methodName, listenerType,
                        l -> listenerConsumer.handle(l, injector(), extensionContext, null));
            }
        }
    }

    @FunctionalInterface
    protected interface ListenerOfHandler<T> {
        void handle(T listener, Injector injector, ExtensionContext extensionContext, Throwable tx);
    }
}
