package org.fxmisc.flowless;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

import javafx.scene.Node;

/**
 * Factory class for wrapping a {@link Cell} and running additional code before/after specific methods
 */
abstract class CellWrapper<T, N extends Node, C extends Cell<T, N>>
implements Cell<T, N> {

    public static <T, N extends Node,C extends Cell<T, N>>
    CellWrapper<T, N, C> beforeDispose(C cell, Runnable action) {
        return new CellWrapper<T, N, C>(cell) {
            @Override
            public void dispose() {
                action.run();
                super.dispose();
            }
        };
    }

    public static <T, N extends Node,C extends Cell<T, N>>
    CellWrapper<T, N, C> afterDispose(C cell, Runnable action) {
        return new CellWrapper<T, N, C>(cell) {
            @Override
            public void dispose() {
                super.dispose();
                action.run();
            }
        };
    }

    public static <T, N extends Node,C extends Cell<T, N>>
    CellWrapper<T, N, C> beforeReset(C cell, Runnable action) {
        return new CellWrapper<T, N, C>(cell) {
            @Override
            public void reset() {
                action.run();
                super.reset();
            }
        };
    }

    public static <T, N extends Node,C extends Cell<T, N>>
    CellWrapper<T, N, C> afterReset(C cell, Runnable action) {
        return new CellWrapper<T, N, C>(cell) {
            @Override
            public void reset() {
                super.reset();
                action.run();
            }
        };
    }

    public static <T, N extends Node,C extends Cell<T, N>>
    CellWrapper<T, N, C> beforeUpdateItem(C cell, Consumer<? super T> action) {
        return new CellWrapper<T, N, C>(cell) {
            @Override
            public void updateItem(T item) {
                action.accept(item);
                super.updateItem(item);
            }
        };
    }

    public static <T, N extends Node,C extends Cell<T, N>>
    CellWrapper<T, N, C> afterUpdateItem(C cell, Consumer<? super T> action) {
        return new CellWrapper<T, N, C>(cell) {
            @Override
            public void updateItem(T item) {
                super.updateItem(item);
                action.accept(item);
            }
        };
    }

    public static <T, N extends Node,C extends Cell<T, N>>
    CellWrapper<T, N, C> beforeUpdateIndex(C cell, IntConsumer action) {
        return new CellWrapper<T, N, C>(cell) {
            @Override
            public void updateIndex(int index) {
                action.accept(index);
                super.updateIndex(index);
            }
        };
    }

    public static <T, N extends Node,C extends Cell<T, N>>
    CellWrapper<T, N, C> afterUpdateIndex(C cell, IntConsumer action) {
        return new CellWrapper<T, N, C>(cell) {
            @Override
            public void updateIndex(int index) {
                super.updateIndex(index);
                action.accept(index);
            }
        };
    }

    private final C delegate;

    public CellWrapper(C delegate) {
        this.delegate = delegate;
    }

    public C getDelegate() {
        return delegate;
    }

    @Override
    public N getNode() {
        return delegate.getNode();
    }

    @Override
    public boolean isReusable() {
        return delegate.isReusable();
    }

    @Override
    public void updateItem(T item) {
        delegate.updateItem(item);
    }

    @Override
    public void updateIndex(int index) {
        delegate.updateIndex(index);
    }

    @Override
    public void reset() {
        delegate.reset();
    }

    @Override
    public void dispose() {
        delegate.dispose();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
