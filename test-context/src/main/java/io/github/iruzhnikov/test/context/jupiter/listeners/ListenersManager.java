package io.github.iruzhnikov.test.context.jupiter.listeners;

import org.apache.commons.collections4.IterableUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Order;
import org.junit.platform.commons.util.ClassLoaderUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

public interface ListenersManager {

    void registerListener(Object listener);

    <T> List<T> getListeners(Class<T> listenerClass, String methodName);

    void fireListenersLoaded();

    Set<Class<?>> getRegisteredListenerTypes();

    List<Object> getContextListeners();

    default <T> void fireListeners(Class<T> listenerClass, String methodName, Consumer<T> consumer) {
        getListeners(listenerClass, methodName).forEach(consumer);
    }

    @NotNull
    default <T> List<T> loadListeners(Class<T> listenersType) {
        List<T> contextListeners;
        Iterable<T> listeners = ServiceLoader.load(listenersType, ClassLoaderUtils.getDefaultClassLoader());
        contextListeners = IterableUtils.toList(listeners);
        contextListeners.sort(comparingInt(value ->
                findAnnotation(value.getClass(), Order.class).map(Order::value).orElse(Order.DEFAULT)));
        return contextListeners;
    }

    @NotNull
    default <T> List<T> makeSortedListenersList(String methodName, Class<T> listenerType, List<?> contextListeners) {
        var result = new ArrayList<>(contextListeners);
        return result.stream()
                .filter(o -> o != null && listenerType.isAssignableFrom(o.getClass()))
                .map(listenerType::cast)
                .sorted(comparingInt(value -> {
                    var m = Arrays.stream(value.getClass().getMethods())
                            .filter(method -> method.getName().equals(methodName))
                            .collect(Collectors.toList());
                    if (m.isEmpty()) {
                        throw new IllegalArgumentException("Not existed method name! Method: '" + methodName +
                                "' in type: '" + listenerType.getCanonicalName() + "'.");
                    }
                    m = m.stream().filter(method -> isAnnotated(method, Order.class))
                            .collect(Collectors.toList());
                    if (m.size() > 1) {
                        throw new IllegalArgumentException("Support only one method annotated by Order! " +
                                "Method: '" + methodName + "', Class: '" + value.getClass() + "'");
                    }
                    var byMethod = !m.isEmpty()
                            ? findAnnotation(m.get(0), Order.class)
                            : Optional.<Order>empty();
                    return byMethod.or(() -> findAnnotation(value.getClass(), Order.class))
                            .map(Order::value).orElse(Order.DEFAULT);
                })).collect(Collectors.toList());
    }
}
