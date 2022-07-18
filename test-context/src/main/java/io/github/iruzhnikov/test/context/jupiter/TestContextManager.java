package io.github.iruzhnikov.test.context.jupiter;

import io.github.iruzhnikov.test.context.jupiter.context.TestContext;
import io.github.iruzhnikov.test.context.jupiter.context.annotations.OverrideTestContextExtension;
import io.github.iruzhnikov.test.context.jupiter.context.impl.TestContextImpl;
import io.github.iruzhnikov.test.context.jupiter.injector.Injector;
import io.github.iruzhnikov.test.context.jupiter.injector.InjectorResolver;
import io.github.iruzhnikov.test.context.jupiter.injector.impl.InjectorImpl;
import io.github.iruzhnikov.test.context.jupiter.listeners.ListenersManager;
import io.github.iruzhnikov.test.context.jupiter.listeners.impl.ListenersManagerImpl;
import io.github.iruzhnikov.test.context.jupiter.managers.ManagerFabric;
import io.github.iruzhnikov.test.context.jupiter.managers.annotations.ManagerId;
import io.github.iruzhnikov.test.context.jupiter.scanner.AnnotationsScanner;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Making same context for all running test classes in application or module
 */
@Slf4j
public class TestContextManager implements InjectorResolver {
    public static final String TEST_CONTEXT_CLASS_PROPERTY = ManagerFabric.BASE_PROPERTY + ".class";
    public static final String LISTENERS_MANAGER_CLASS_PROPERTY = ManagerFabric.BASE_PROPERTY + ".listeners.manager";
    public static final String INJECTOR_CLASS_PROPERTY = ManagerFabric.BASE_PROPERTY + ".listeners.manager";
    public static final String TEST_CONTEXT_CLOSABLE_PROPERTY = ManagerFabric.BASE_PROPERTY + ".closable";

    private final Boolean isClosableContext;
    private final ConcurrentHashMap<String, Boolean> stoppedDescriptors = new ConcurrentHashMap<>();
    private final LauncherDiscoveryRequest launcherDiscoveryRequest;
    @Getter
    private final String id;
    @Getter
    private final AnnotationsScanner annotationScanner;
    private TestContext testContext;
    private Injector injector;
    private ListenersManager listenersManager;

    private final ReentrantLock lock = new ReentrantLock();

    public TestContextManager(String id, AnnotationsScanner annotationScanner) {
        this.id = id;
        this.annotationScanner = annotationScanner;
        launcherDiscoveryRequest = LauncherDiscoveryRequestBuilder.request().build();
        //read configuration from org.junit.platform.engine.ConfigurationParameters.CONFIG_FILE_NAME
        isClosableContext = getConfig().getBoolean(TEST_CONTEXT_CLOSABLE_PROPERTY).orElse(true);
    }

    protected boolean stopExecution(String uniqueId) {
        stoppedDescriptors.computeIfPresent(uniqueId, (s, b) -> true);

        lock.lock();
        try {
            if (!stoppedDescriptors.containsValue(false)) {
                stoppedDescriptors.clear();
                if (isClosableContext) {
                    var stopResult = testContext != null && testContext.stop();
                    testContext = null;
                    injector = null;
                    listenersManager = null;
                    return stopResult;
                }
                return false;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    protected void startExecution(String uniqueId) {
        stoppedDescriptors.putIfAbsent(uniqueId, false);
    }

    public TestContext getContext() {
        lock.lock();
        try {
            if (testContext == null) {
                testContext = newInstance(TEST_CONTEXT_CLASS_PROPERTY, TestContextImpl::new);
            }
            return testContext;
        } finally {
            lock.unlock();
        }
    }

    protected void startContext() {
        if (getContext().isStopped()) {
            getInjector();
            getListenersManager().fireListenersLoaded();
            testContextConflictAssertion();
            getContext().start();
        }
    }

    private void testContextConflictAssertion() {
        var disabled = getTestAnnotations(OverrideTestContextExtension.class);
        var found = getTestAnnotations(ExtendWith.class).stream()
                .flatMap(a -> Arrays.stream(a.value())).filter(TestContextExtension.class::isAssignableFrom)
                .distinct().filter(c -> disabled.stream().noneMatch(r -> r.rewrite().equals(c))).collect(Collectors.toList());

        if (found.size() != 1) {
            throw new IllegalArgumentException("Must enabled only one TestContextExtension! Current enabled: " +
                    found.stream().map(Class::getName).collect(Collectors.joining(", ")));
        }
    }

    public boolean isContextStopped() {
        return testContext == null || testContext.isStopped();
    }

    public Injector getInjector() {
        lock.lock();
        try {
            if (injector == null) {
                injector = newInstance(INJECTOR_CLASS_PROPERTY, InjectorImpl::new);
            }
            return injector;
        } finally {
            lock.unlock();
        }
    }

    public ListenersManager getListenersManager() {
        lock.lock();
        try {
            if (listenersManager == null) {
                listenersManager = newInstance(LISTENERS_MANAGER_CLASS_PROPERTY, ListenersManagerImpl::new);
                listenersManager.registerListener(this);
            }
            return listenersManager;
        } finally {
            lock.unlock();
        }
    }

    /**
     * read parameters from file of junit properties {@link org.junit.platform.engine.ConfigurationParameters#CONFIG_FILE_NAME}
     */
    public ConfigurationParameters getConfig() {
        return launcherDiscoveryRequest.getConfigurationParameters();
    }

    public void registerTestIdentifier(TestIdentifier testIdentifier) {
        getAnnotationScanner().register(testIdentifier);
    }

    public <T extends Annotation> List<T> getTestAnnotations(Class<T> annotation) {
        return getAnnotationScanner().getAnnotations(annotation,
                s -> getAnnotationScanner().getAnnotations(s, ManagerId.class).anyMatch(a -> id.equals(a.value())));
    }

    @Override
    public boolean supportsParameter(@Nullable Injector injector, Type type) {
        return type instanceof Class && (
                TestContext.class.isAssignableFrom((Class<?>) type) ||
                        Injector.class.isAssignableFrom((Class<?>) type) ||
                        ListenersManager.class.isAssignableFrom((Class<?>) type) ||
                        ConfigurationParameters.class.isAssignableFrom((Class<?>) type));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T resolveParameter(@Nullable Injector injector, Type type) {
        if (!(type instanceof Class)) return null;
        if (isExistsResolvingParam(TestContext.class, type, () -> testContext != null)) {
            return (T) getContext();
        }
        if (isExistsResolvingParam(Injector.class, type, () -> injector != null)) {
            return (T) getInjector();
        }
        if (isExistsResolvingParam(ListenersManager.class, type, () -> listenersManager != null)) {
            return (T) getListenersManager();
        }
        if (isExistsResolvingParam(ConfigurationParameters.class, type, () -> launcherDiscoveryRequest != null)) {
            return (T) getConfig();
        }
        throw new IllegalStateException("You cant inject " + type.getTypeName() + "! Test context not created!");
    }

    private boolean isExistsResolvingParam(Class<?> aClass, Type type, Supplier<Boolean> assertion) {
        if (aClass.isAssignableFrom((Class<?>) type)) {
            if (!assertion.get()) {
                throw new IllegalStateException("You can't inject " + type.getTypeName() + "! Test context not created!");
            }
            return true;
        }
        return false;
    }

    @NotNull
    private <T> T newInstance(String configProperty, Function<TestContextManager, T> defaultSupplier) {
        return InstanceUtils.newInstance(configProperty, getConfig(),
                args -> defaultSupplier.apply((TestContextManager) args[0]), this);
    }
}
