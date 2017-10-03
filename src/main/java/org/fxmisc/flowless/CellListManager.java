package org.fxmisc.flowless;

import java.util.Optional;
import java.util.function.Function;

import javafx.collections.ObservableList;
import javafx.scene.Node;

import org.reactfx.EventStreams;
import org.reactfx.Subscription;
import org.reactfx.collection.LiveList;
import org.reactfx.collection.MemoizationList;
import org.reactfx.collection.QuasiListModification;

/**
 * Tracks all of the cells that the viewport can display ({@link #cells}) and which cells the viewport is currently
 * displaying ({@link #presentCells}).
 */
final class CellListManager<T, C extends Cell<T, ? extends Node>> {

    private final CellPool<T, C> cellPool;
    private final MemoizationList<C> cells;
    private final LiveList<C> presentCells;
    private final LiveList<Node> cellNodes;

    private final Subscription presentCellsSubscription;

    public CellListManager(
            ObservableList<T> items,
            Function<? super T, ? extends C> cellFactory) {
        this.cellPool = new CellPool<>(cellFactory);
        this.cells = LiveList.map(items, this::cellForItem).memoize();
        this.presentCells = cells.memoizedItems();
        this.cellNodes = presentCells.map(Cell::getNode);
        this.presentCellsSubscription = presentCells.observeQuasiModifications(this::presentCellsChanged);
    }

    public void dispose() {
        // return present cells to pool *before* unsubscribing,
        // because stopping to observe memoized items may clear memoized items
        presentCells.forEach(cellPool::acceptCell);
        presentCellsSubscription.unsubscribe();
        cellPool.dispose();
    }

    /** Gets the list of nodes that the viewport is displaying */
    public ObservableList<Node> getNodes() {
        return cellNodes;
    }

    public MemoizationList<C> getLazyCellList() {
        return cells;
    }

    public boolean isCellPresent(int itemIndex) {
        return cells.isMemoized(itemIndex);
    }

    public C getPresentCell(int itemIndex) {
        // both getIfMemoized() and get() may throw
        return cells.getIfMemoized(itemIndex).get();
    }

    public Optional<C> getCellIfPresent(int itemIndex) {
        return cells.getIfMemoized(itemIndex); // getIfMemoized() may throw
    }

    public C getCell(int itemIndex) {
        return cells.get(itemIndex);
    }

    /**
     * Updates the list of cells to display
     *
     * @param fromItem the index of the first item to display
     * @param toItem the index of the last item to display
     */
    public void cropTo(int fromItem, int toItem) {
        fromItem = Math.max(fromItem, 0);
        toItem = Math.min(toItem, cells.size());
        cells.forget(0, fromItem);
        cells.forget(toItem, cells.size());
    }

    private C cellForItem(T item) {
        C cell = cellPool.getCell(item);

        // apply CSS when the cell is first added to the scene
        Node node = cell.getNode();
        EventStreams.nonNullValuesOf(node.sceneProperty())
                .subscribeForOne(scene -> {
                    node.applyCss();
                });

        // Make cell initially invisible.
        // It will be made visible when it is positioned.
        node.setVisible(false);

        return cell;
    }

    private void presentCellsChanged(QuasiListModification<? extends C> mod) {
        // add removed cells back to the pool
        for(C cell: mod.getRemoved()) {
            cellPool.acceptCell(cell);
        }

        // update indices of added cells and cells after the added cells
        for(int i = mod.getFrom(); i < presentCells.size(); ++i) {
            presentCells.get(i).updateIndex(cells.indexOfMemoizedItem(i));
        }
    }
}