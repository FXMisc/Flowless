package org.fxmisc.flowless;

import java.util.Optional;
import java.util.OptionalInt;

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
        return cellManager.getPresentCell(itemIndex);
    }

    public Optional<C> getCellIfVisible(int itemIndex) {
        return cellManager.getCellIfPresent(itemIndex);
    }

    public OptionalInt lastVisibleBefore(int position) {
        return cellManager.lastPresentBefore(position);
    }

    public OptionalInt firstVisibleAfter(int position) {
        return cellManager.firstPresentAfter(position);
    }

    public OptionalInt getLastVisibleIndex() {
        return lastVisibleBefore(cellManager.getLazyCellList().size());
    }

    public OptionalInt getFirstVisibleIndex() {
        return firstVisibleAfter(0);
    }

    public double shortestDeltaToViewport(C cell) {
        double gapBefore = orientation.minY(cell);
        double gapAfter = sizeTracker.getViewportLength() - orientation.maxY(cell);

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
        return cell;
    }

    public C placeEndFromStart(int itemIndex, double endOffStart) {
        C cell = getSizedCell(itemIndex);
        relocate(cell, 0, endOffStart - orientation.length(cell));
        return cell;
    }

    public C placeEndFromEnd(int itemIndex, double endOffEnd) {
        C cell = getSizedCell(itemIndex);
        double y = sizeTracker.getViewportLength() + endOffEnd - orientation.length(cell);
        relocate(cell, 0, y);
        return cell;
    }

    private C getSizedCell(int itemIndex) {
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