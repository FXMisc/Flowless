package org.fxmisc.flowless;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Function;

final class CellPool<T, C extends Cell<T, ?>> {
    private final Function<? super T, ? extends C> cellFactory;
    private final Queue<C> pool = new LinkedList<>();

    public CellPool(Function<? super T, ? extends C> cellFactory) {
        this.cellFactory = cellFactory;
    }

    public C getCell(T item) {
        C cell = pool.poll();
        if(cell != null) {
            cell.updateItem(item);
        } else {
            cell = cellFactory.apply(item);
        }
        return cell;
    }

    public void acceptCell(C cell) {
        cell.reset();
        if(cell.isReusable()) {
            pool.add(cell);
        } else {
            cell.dispose();
        }
    }

    public void dispose() {
        for(C cell: pool) {
            cell.dispose();
        }

        pool.clear();
    }
}