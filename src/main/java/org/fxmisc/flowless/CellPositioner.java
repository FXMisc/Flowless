package org.fxmisc.flowless;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;

import org.reactfx.collection.MemoizationList;

final class CellPositioner<T, C extends Cell<T, ?>> {
    private final CellListManager<T, C> cellManager;
    private final OrientationHelper orientation;
    private final SizeTracker sizeTracker;

    public CellPositioner(
            CellListManager<T, C> cellManager,
            OrientationHelper orientation,
            SizeTracker sizeTracker) {
        this.cellManager = cellManager;
        this.orientation = orientation;
        this.sizeTracker = sizeTracker;
    }

    public void cropTo(int from, int to) {
        cellManager.cropTo(from, to);
    }

    public C getVisibleCell(int itemIndex) {
        C cell = cellManager.getPresentCell(itemIndex);
        if(cell.getNode().isVisible()) {
            return cell;
        } else {
            throw new NoSuchElementException(
                    "Cell " + itemIndex + " is not visible");
        }
    }

    public Optional<C> getCellIfVisible(int itemIndex) {
        return cellManager.getCellIfPresent(itemIndex)
                .filter(c -> c.getNode().isVisible());
    }

    public OptionalInt lastVisibleBefore(int position) {
        MemoizationList<C> cells = cellManager.getLazyCellList();
        int presentBefore = cells.getMemoizedCountBefore(position);
        for(int i = presentBefore - 1; i >= 0; --i) {
            C cell = cells.memoizedItems().get(i);
            if(cell.getNode().isVisible()) {
                return OptionalInt.of(cells.indexOfMemoizedItem(i));
            }
        }
        return OptionalInt.empty();
    }

    public OptionalInt firstVisibleAfter(int position) {
        MemoizationList<C> cells = cellManager.getLazyCellList();
        int presentBefore = cells.getMemoizedCountBefore(position);
        int present = cells.getMemoizedCount();
        for(int i = presentBefore; i < present; ++i) {
            C cell = cells.memoizedItems().get(i);
            if(cell.getNode().isVisible()) {
                return OptionalInt.of(cells.indexOfMemoizedItem(i));
            }
        }
        return OptionalInt.empty();
    }

    public OptionalInt getLastVisibleIndex() {
        return lastVisibleBefore(cellManager.getLazyCellList().size());
    }

    public OptionalInt getFirstVisibleIndex() {
        return firstVisibleAfter(0);
    }

    public double shortestDeltaToViewport(C cell) {
        return shortestDeltaToViewport(cell, 0.0, orientation.length(cell));
    }

    public double shortestDeltaToViewport(C cell, double fromY, double toY) {
        double cellMinY = orientation.minY(cell);
        double gapBefore = cellMinY + fromY;
        double gapAfter = sizeTracker.getViewportLength() - (cellMinY + toY);

        return (gapBefore < 0 && gapAfter > 0) ? Math.min(-gapBefore, gapAfter) :
               (gapBefore > 0 && gapAfter < 0) ? Math.max(-gapBefore, gapAfter) :
               0.0;
    }

    public void shiftCellBy(C cell, double delta) {
        double y = orientation.minY(cell) + delta;
        relocate(cell, 0, y);
    }

    public C placeStartAt(int itemIndex, double startOffStart) {
        C cell = getSizedCell(itemIndex);
        relocate(cell, 0, startOffStart);
        cell.getNode().setVisible(true);
        return cell;
    }

    public C placeEndFromStart(int itemIndex, double endOffStart) {
        C cell = getSizedCell(itemIndex);
        relocate(cell, 0, endOffStart - orientation.length(cell));
        cell.getNode().setVisible(true);
        return cell;
    }

    public C placeEndFromEnd(int itemIndex, double endOffEnd) {
        C cell = getSizedCell(itemIndex);
        double y = sizeTracker.getViewportLength() + endOffEnd - orientation.length(cell);
        relocate(cell, 0, y);
        cell.getNode().setVisible(true);
        return cell;
    }

    public C placeStartFromEnd(int itemIndex, double startOffEnd) {
        C cell = getSizedCell(itemIndex);
        double y = sizeTracker.getViewportLength() + startOffEnd;
        relocate(cell, 0, y);
        cell.getNode().setVisible(true);
        return cell;
    }

    /**
     * Returns properly sized, but not properly positioned cell for the given
     * index.
     */
    C getSizedCell(int itemIndex) {
        C cell = cellManager.getCell(itemIndex);
        double breadth = sizeTracker.breadthFor(itemIndex);
        double length = sizeTracker.lengthFor(itemIndex);
        orientation.resize(cell, breadth, length);
        return cell;
    }

    private void relocate(C cell, double breadth0, double length0) {
        orientation.relocate(cell, breadth0, length0);
    }
}