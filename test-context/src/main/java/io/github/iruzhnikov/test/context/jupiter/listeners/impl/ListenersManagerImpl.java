package io.github.iruzhnikov.test.context.jupiter.listeners.impl;

import io.github.iruzhnikov.test.context.jupiter.TestContextManager;
import io.github.iruzhnikov.test.context.jupiter.listeners.ListenerLifecycle;
import io.github.iruzhnikov.test.context.jupiter.listeners.ListenersManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Order;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

@Slf4j
public class ListenersManagerImpl implements ListenersManager {
    private final TestContextManager manager;
    @Getter
    protected Set<Class<?>> registeredListenerTypes = new HashSet<>();
    @Getter
    protected List<Object> contextListeners = new ArrayList<>();
    protected Set<Class<?>> contextListenersTypes = new HashSet<>();
    protected Set<String> contextListenersExclusions = new HashSet<>();
    private final Map<Pair<String, Class<Object>>, List<Object>> listenersByOrder = new HashMap<>();

    public ListenersManagerImpl(TestContextManager manager) {
        this.manager = manager;
        loadListeners(ListenerLifecycle.class);
        loadAnnotationListeners();
    }

    protected void loadAnnotationListeners() {
        manager.getTestAnnotations(io.github.iruzhnikov.test.context.jupiter.listeners.annotations.TestContextListener.class)
                .forEach(definition -> {
                    if (!Void.class.isAssignableFrom(definition.exclude())) {
                        contextListenersExclusions.add(definition.exclude().getCanonicalName());
                    }
                    if (StringUtils.isNotBlank(definition.excludeReference())) {
                        contextListenersExclusions.add(definition.excludeReference());
                    }
                    if (!Void.class.isAssignableFrom(definition.include())) {
                        addContextListener(ReflectionUtils.newInstance(definition.include()));
                    }
                });
        removeContextListeners(getExclusions(contextListeners));
        reorderContextListeners();
    }

    @Override
    public void registerListener(Object listener) {
        if (listener != null) {
            addContextListener(listener);
        }
    }

    @Override
    @NotNull
    public <T> List<T> loadListeners(Class<T> listenersType) {
        if (!listenersType.isInterface()) {
            throw new IllegalArgumentException("listenersType must be interface!");
        }
        registeredListenerTypes.add(listenersType);
        var listeners = ListenersManager.super.loadListeners(listenersType);
        log.info("Loaded " + listenersType.getName() + " instances: "
                + listeners.stream().map(Object::getClass).collect(toList()));

        listeners.removeAll(getExclusions(listeners));
        if (listeners.size() > 0) {
            addContextListeners(listeners.stream()
                    .filter(l -> !contextListenersExclusions.contains(l.getClass().getName())).collect(toList()));
            reorderContextListeners();
            for (var key : SetUtils.unmodifiableSet(listenersByOrder.keySet())) {
                if (listenersType.isAssignableFrom(key.getValue())) {
                    listenersByOrder.remove(key);
                }
            }
        }
        return listeners;
    }

    @Override
    public <T> List<T> getListeners(Class<T> listenerClass, String methodName) {
        @SuppressWarnings("unchecked") var lClass = (Class<Object>) listenerClass;
        return (ArrayList<T>) listenersByOrder.computeIfAbsent(Pair.of(methodName, lClass), s ->
                makeSortedListenersList(s.getKey(), s.getValue(), contextListeners));
    }

    @Override
    public void fireListenersLoaded() {
        removeContextListeners(getExclusions(contextListeners));
        registeredListenerTypes.forEach(this::makeListenersCache);
        //don't make cache for not resolved types
        contextListeners.stream()
                .filter(l -> registeredListenerTypes.stream().noneMatch(c -> c.isAssignableFrom(l.getClass())))
                .forEach(l -> registeredListenerTypes.add(l.getClass()));
        getListeners(ListenerLifecycle.class, "listenerLoaded")
                .forEach(listener -> listener.listenerLoaded(manager.getInjector()));
    }

    protected void makeListenersCache(Class<?> listenerClass) {
        for (Method method : listenerClass.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) {
                getListeners(listenerClass, method.getName());
            }
        }
    }

    private void reorderContextListeners() {
        contextListeners.sort(comparingInt(value ->
                findAnnotation(value.getClass(), Order.class).map(Order::value).orElse(Order.DEFAULT)));
    }

    private <T> List<T> getExclusions(List<T> listeners) {
        return listeners.stream()
                .filter(l -> contextListenersExclusions.contains(l.getClass().getCanonicalName())).collect(toList());
    }

    private void addContextListeners(List<Object> listeners) {
        listeners.forEach(this::addContextListener);
    }

    private void addContextListener(Object listener) {
        if (listener instanceof ServiceLoader.Provider) {
            listener = ((ServiceLoader.Provider<?>) listener).get();
        }
        if (contextListenersTypes.add(listener.getClass())) {
            contextListeners.add(listener);
        }
    }

    private void removeContextListeners(List<Object> listeners) {
        listeners.forEach(this::removeContextListener);
    }

    private void removeContextListener(Object listeners) {
        contextListenersTypes.remove(listeners.getClass());
        contextListeners.remove(listeners);
    }
}
