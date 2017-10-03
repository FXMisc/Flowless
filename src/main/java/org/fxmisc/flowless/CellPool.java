package org.fxmisc.flowless;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Function;

/**
 * Helper class that stores a pool of reusable cells that can be updated via {@link Cell#updateItem(Object)} or
 * creates new ones via its {@link #cellFactory} if the pool is empty.
 */
final class CellPool<T, C extends Cell<T, ?>> {
    private final Function<? super T, ? extends C> cellFactory;
    private final Queue<C> pool = new LinkedList<>();

    public CellPool(Function<? super T, ? extends C> cellFactory) {
        this.cellFactory = cellFactory;
    }

    /**
     * Returns a reusable cell that has been updated with the current item if the pool has one, or returns a
     * newly-created one via its {@link #cellFactory}.
     */
    public C getCell(T item) {
        C cell = pool.poll();
        if(cell != null) {
            cell.updateItem(item);
        } else {
            cell = cellFactory.apply(item);
        }
        return cell;
    }

    /**
     * Adds the cell to the pool of reusable cells if {@link Cell#isReusable()} is true, or
     * {@link Cell#dispose() disposes} the cell if it's not.
     */
    public void acceptCell(C cell) {
        cell.reset();
        if(cell.isReusable()) {
            pool.add(cell);
        } else {
            cell.dispose();
        }
    }

    /**
     * Disposes the cell pool and prevents any memory leaks.
     */
    public void dispose() {
        for(C cell: pool) {
            cell.dispose();
        }

        pool.clear();
    }
}