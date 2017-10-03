package org.fxmisc.flowless;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

import javafx.scene.Node;

/**
 * Provides efficient memory usage by wrapping a {@link Node} within this object and reusing it when
 * {@link #isReusable()} is true.
 */
@FunctionalInterface
public interface Cell<T, N extends Node> {
    static <T, N extends Node> Cell<T, N> wrapNode(N node) {
        return new Cell<T, N>() {

            @Override
            public N getNode() { return node; }

            @Override
            public String toString() { return node.toString(); }
        };
    }

    N getNode();

    /**
     * Indicates whether this cell can be reused to display different items.
     *
     * <p>Default implementation returns {@code false}.
     */
    default boolean isReusable() {
        return false;
    }

    /**
     * If this cell is reusable (as indicated by {@link #isReusable()}),
     * this method is called to display a different item. {@link #reset()}
     * will have been called before a call to this method.
     *
     * <p>The default implementation throws
     * {@link UnsupportedOperationException}.
     *
     * @param item the new item to display
     */
    default void updateItem(T item) {
        throw new UnsupportedOperationException();
    }

    /**
     * Called to update index of a visible cell.
     *
     * <p>Default implementation does nothing.
     */
    default void updateIndex(int index) {
        // do nothing by default
    }

    /**
     * Called when this cell is no longer used to display its item.
     * If this cell is reusable, it may later be asked to display a different
     * item by a call to {@link #updateItem(Object)}.
     *
     * <p>Default implementation does nothing.
     */
    default void reset() {
        // do nothing by default
    }

    /**
     * Called when this cell is no longer going to be used at all.
     * {@link #reset()} will have been called before this method is invoked.
     *
     * <p>Default implementation does nothing.
     */
    default void dispose() {
        // do nothing by default
    }

    default Cell<T, N> beforeDispose(Runnable action) {
        return CellWrapper.beforeDispose(this, action);
    }

    default Cell<T, N> afterDispose(Runnable action) {
        return CellWrapper.afterDispose(this, action);
    }

    default Cell<T, N> beforeReset(Runnable action) {
        return CellWrapper.beforeReset(this, action);
    }

    default Cell<T, N> afterReset(Runnable action) {
        return CellWrapper.afterReset(this, action);
    }

    default Cell<T, N> beforeUpdateItem(Consumer<? super T> action) {
        return CellWrapper.beforeUpdateItem(this, action);
    }

    default Cell<T, N> afterUpdateItem(Consumer<? super T> action) {
        return CellWrapper.afterUpdateItem(this, action);
    }

    default Cell<T, N> beforeUpdateIndex(IntConsumer action) {
        return CellWrapper.beforeUpdateIndex(this, action);
    }

    default Cell<T, N> afterUpdateIndex(IntConsumer action) {
        return CellWrapper.afterUpdateIndex(this, action);
    }
}