package com.game.arkanoid.events;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Simple singleton event bus for publishing game domain events without leaking presentation code
 * into the services layer.
 */
public final class GameEventBus {

    private static final GameEventBus INSTANCE = new GameEventBus();

    private final Map<Class<?>, CopyOnWriteArrayList<Consumer<?>>> subscribers = new ConcurrentHashMap<>();

    private GameEventBus() {
    }

    public static GameEventBus getInstance() {
        return INSTANCE;
    }

    /**
     * Subscribe to events of the given type.
     *
     * @param type    event class to listen for
     * @param handler consumer that will receive events
     * @param <T>     event type
     * @return subscription handle (auto-closeable) to remove the listener
     */
    public <T> Subscription subscribe(Class<T> type, Consumer<T> handler) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(handler, "handler");
        subscribers.computeIfAbsent(type, key -> new CopyOnWriteArrayList<>()).add(handler);
        return new Subscription(type, handler);
    }

    /**
     * Publish an event to all registered subscribers for its type.
     */
    public void publish(Object event) {
        if (event == null) {
            return;
        }
        List<Consumer<?>> handlers = subscribers.get(event.getClass());
        if (handlers == null || handlers.isEmpty()) {
            return;
        }
        for (Consumer<?> rawHandler : handlers) {
            @SuppressWarnings("unchecked")
            Consumer<Object> handler = (Consumer<Object>) rawHandler;
            handler.accept(event);
        }
    }

    public final class Subscription implements AutoCloseable {
        private final Class<?> type;
        private final Consumer<?> handler;
        private volatile boolean closed = false;

        private Subscription(Class<?> type, Consumer<?> handler) {
            this.type = type;
            this.handler = handler;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            List<Consumer<?>> handlers = subscribers.get(type);
            if (handlers != null) {
                handlers.remove(handler);
            }
        }
    }
}
