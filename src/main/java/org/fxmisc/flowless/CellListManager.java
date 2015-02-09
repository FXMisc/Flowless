package org.fxmisc.flowless;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

import javafx.collections.ObservableList;
import javafx.scene.Node;

import org.reactfx.Subscription;
import org.reactfx.collection.LiveList;
import org.reactfx.collection.MemoizationList;
import org.reactfx.collection.QuasiListModification;

final class CellListManager<T, C extends Cell<T, ?>> {

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

    public OptionalInt lastPresentBefore(int position) {
        int presentBefore = cells.getMemoizedCountBefore(position);
        return presentBefore > 0
                ? OptionalInt.of(cells.indexOfMemoizedItem(presentBefore - 1))
                : OptionalInt.empty();
    }

    public OptionalInt firstPresentAfter(int position) {
        int presentBefore = cells.getMemoizedCountBefore(position);
        int presentAfter = cells.getMemoizedCountAfter(position);

        return presentAfter > 0
                ? OptionalInt.of(cells.indexOfMemoizedItem(presentBefore))
                : OptionalInt.empty();
    }

    public void cropTo(int fromItem, int toItem) {
        fromItem = Math.max(fromItem, 0);
        toItem = Math.min(toItem, cells.size());
        cells.forget(0, fromItem);
        cells.forget(toItem, cells.size());
    }

    private C cellForItem(T item) {
        C cell = cellPool.getCell(item);
//        Node node = cell.getNode();
//
//        // apply CSS when the node is added to the scene
//        EventStreams.nonNullValuesOf(node.sceneProperty())
//                .subscribeForOne(scene -> {
////                    applySkins(node);
//                    node.applyCss();
//                });

        return cell;
    }

//    private static final Method createDefaultSkin;
//    static {
//        try {
//            createDefaultSkin = Control.class.getDeclaredMethod("createDefaultSkin");
//        } catch (NoSuchMethodException | SecurityException e) {
//            throw new RuntimeException("This is too bad", e);
//        }
//        createDefaultSkin.setAccessible(true);
//    }
//    private static void applySkins(Node node) {
//        if(node instanceof Parent) {
//            Parent parent = (Parent) node;
//            if(parent instanceof Control) {
//                Control control = (Control) parent;
//                Skin<?> skin = control.getSkin();
//                System.out.println("skin for " + control + " is " + skin);
//                if(skin == null) {
//                    try {
//                        skin = (Skin<?>) createDefaultSkin.invoke(control);
//                    } catch (IllegalAccessException | IllegalArgumentException
//                            | InvocationTargetException e) {
//                        throw new RuntimeException("Oops!", e);
//                    }
//                    control.setSkin(skin);
//                }
//            }
//            for(Node child: parent.getChildrenUnmodifiable()) {
//                applySkins(child);
//            }
//        }
//    }

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