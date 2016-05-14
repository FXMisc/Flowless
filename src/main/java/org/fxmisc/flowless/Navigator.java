package org.fxmisc.flowless;

import java.util.Optional;
import java.util.OptionalInt;

import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Region;

import org.fxmisc.flowless.VirtualFlow.Gravity;
import org.reactfx.Subscription;
import org.reactfx.collection.LiveList;
import org.reactfx.collection.MemoizationList;
import org.reactfx.collection.QuasiListChange;
import org.reactfx.collection.QuasiListModification;

final class Navigator<T, C extends Cell<T, ?>>
extends Region implements TargetPositionVisitor {
    private final CellListManager<T, C> cellListManager;
    private final MemoizationList<C> cells;
    private final CellPositioner<T, C> positioner;
    private final OrientationHelper orientation;
    private final Gravity gravity;
    private final SizeTracker sizeTracker;
    private final Subscription itemsSubscription;

    private TargetPosition currentPosition = TargetPosition.BEGINNING;
    private TargetPosition targetPosition = TargetPosition.BEGINNING;

    public Navigator(
            CellListManager<T, C> cellListManager,
            CellPositioner<T, C> positioner,
            OrientationHelper orientation,
            Gravity gravity,
            SizeTracker sizeTracker) {
        this.cellListManager = cellListManager;
        this.cells = cellListManager.getLazyCellList();
        this.positioner = positioner;
        this.orientation = orientation;
        this.gravity = gravity;
        this.sizeTracker = sizeTracker;

        this.itemsSubscription = LiveList.observeQuasiChanges(cellListManager.getLazyCellList(), this::itemsChanged);
        Bindings.bindContent(getChildren(), cellListManager.getNodes());
    }

    public void dispose() {
        itemsSubscription.unsubscribe();
        Bindings.unbindContent(getChildren(), cellListManager.getNodes());
    }

    @Override
    protected void layoutChildren() {
        // invalidate breadth for each cell that has dirty layout
        int n = cells.getMemoizedCount();
        for(int i = 0; i < n; ++i) {
            int j = cells.indexOfMemoizedItem(i);
            Node node = cells.get(j).getNode();
            if(node instanceof Parent && ((Parent) node).isNeedsLayout()) {
                sizeTracker.forgetSizeOf(j);
            }
        }

        if(!cells.isEmpty()) {
            targetPosition.clamp(cells.size())
                    .accept(this);
        }
        currentPosition = getCurrentPosition();
        targetPosition = currentPosition;
    }

    public void setTargetPosition(TargetPosition targetPosition) {
        this.targetPosition = targetPosition;
        requestLayout();
    }

    public void scrollCurrentPositionBy(double delta) {
        targetPosition = currentPosition.scrollBy(delta);
        requestLayout();
    }

    private TargetPosition getCurrentPosition() {
        OptionalInt firstVisible = positioner.getFirstVisibleIndex();
        if(firstVisible.isPresent()) {
            int idx = firstVisible.getAsInt();
            C cell = positioner.getVisibleCell(idx);
            return new StartOffStart(idx, orientation.minY(cell));
        } else {
            return TargetPosition.BEGINNING;
        }
    }

    private void itemsChanged(QuasiListChange<?> ch) {
        for(QuasiListModification<?> mod: ch) {
            targetPosition = targetPosition.transformByChange(
                    mod.getFrom(), mod.getRemovedSize(), mod.getAddedSize());
        }
        requestLayout(); // TODO: could optimize to only request layout if
                         // target position changed or cells in the viewport
                         // are affected
    }

    void showLengthRegion(int itemIndex, double fromY, double toY) {
        setTargetPosition(new MinDistanceTo(
                itemIndex, Offset.fromStart(fromY), Offset.fromStart(toY)));
    }

    @Override
    public void visit(StartOffStart targetPosition) {
        placeStartAtMayCrop(targetPosition.itemIndex, targetPosition.offsetFromStart);
        fillViewportFrom(targetPosition.itemIndex);
    }

    @Override
    public void visit(EndOffEnd targetPosition) {
        placeEndOffEndMayCrop(targetPosition.itemIndex, targetPosition.offsetFromEnd);
        fillViewportFrom(targetPosition.itemIndex);
    }

    @Override
    public void visit(MinDistanceTo targetPosition) {
        Optional<C> cell = positioner.getCellIfVisible(targetPosition.itemIndex);
        if(cell.isPresent()) {
            placeToViewport(targetPosition.itemIndex, targetPosition.minY, targetPosition.maxY);
        } else {
            OptionalInt prevVisible;
            OptionalInt nextVisible;
            if((prevVisible = positioner.lastVisibleBefore(targetPosition.itemIndex)).isPresent()) {
                // Try keeping prevVisible in place:
                // fill the viewport, see if the target item appeared.
                fillForwardFrom(prevVisible.getAsInt());
                cell = positioner.getCellIfVisible(targetPosition.itemIndex);
                if(cell.isPresent()) {
                    placeToViewport(targetPosition.itemIndex, targetPosition.minY, targetPosition.maxY);
                } else if(targetPosition.maxY.isFromStart()) {
                    placeStartOffEndMayCrop(targetPosition.itemIndex, -targetPosition.maxY.getValue());
                } else {
                    placeEndOffEndMayCrop(targetPosition.itemIndex, -targetPosition.maxY.getValue());
                }
            } else if((nextVisible = positioner.firstVisibleAfter(targetPosition.itemIndex + 1)).isPresent()) {
                // Try keeping nextVisible in place:
                // fill the viewport, see if the target item appeared.
                fillBackwardFrom(nextVisible.getAsInt());
                cell = positioner.getCellIfVisible(targetPosition.itemIndex);
                if(cell.isPresent()) {
                    placeToViewport(targetPosition.itemIndex, targetPosition.minY, targetPosition.maxY);
                } else if(targetPosition.minY.isFromStart()) {
                    placeStartAtMayCrop(targetPosition.itemIndex, -targetPosition.minY.getValue());
                } else {
                    placeEndOffStartMayCrop(targetPosition.itemIndex, -targetPosition.minY.getValue());
                }
            } else {
                if(targetPosition.minY.isFromStart()) {
                    placeStartAtMayCrop(targetPosition.itemIndex, -targetPosition.minY.getValue());
                } else {
                    placeEndOffStartMayCrop(targetPosition.itemIndex, -targetPosition.minY.getValue());
                }
            }
        }
        fillViewportFrom(targetPosition.itemIndex);
    }

    private void placeToViewport(int itemIndex, Offset from, Offset to) {
        C cell = positioner.getVisibleCell(itemIndex);
        double fromY = from.isFromStart()
                ? from.getValue()
                : orientation.length(cell) + to.getValue();
        double toY = to.isFromStart()
                ? to.getValue()
                : orientation.length(cell) + to.getValue();
        placeToViewport(itemIndex, fromY, toY);
    }

    private void placeToViewport(int itemIndex, double fromY, double toY) {
        C cell = positioner.getVisibleCell(itemIndex);
        double d = positioner.shortestDeltaToViewport(cell, fromY, toY);
        positioner.placeStartAt(itemIndex, orientation.minY(cell) + d);
    }

    private void placeStartAtMayCrop(int itemIndex, double startOffStart) {
        cropToNeighborhoodOf(itemIndex, startOffStart);
        positioner.placeStartAt(itemIndex, startOffStart);
    }

    private void placeStartOffEndMayCrop(int itemIndex, double startOffEnd) {
        cropToNeighborhoodOf(itemIndex, startOffEnd);
        positioner.placeStartFromEnd(itemIndex, startOffEnd);
    }

    private void placeEndOffStartMayCrop(int itemIndex, double endOffStart) {
        cropToNeighborhoodOf(itemIndex, endOffStart);
        positioner.placeEndFromStart(itemIndex, endOffStart);
    }

    private void placeEndOffEndMayCrop(int itemIndex, double endOffEnd) {
        cropToNeighborhoodOf(itemIndex, endOffEnd);
        positioner.placeEndFromEnd(itemIndex, endOffEnd);
    }

    private void cropToNeighborhoodOf(int itemIndex, double additionalOffset) {
        double spaceBefore = Math.max(0, sizeTracker.getViewportLength() + additionalOffset);
        double spaceAfter = Math.max(0, sizeTracker.getViewportLength() - additionalOffset);

        Optional<Double> avgLen = sizeTracker.getAverageLengthEstimate();
        int itemsBefore = avgLen.map(l -> spaceBefore/l).orElse(5.0).intValue();
        int itemsAfter = avgLen.map(l -> spaceAfter/l).orElse(5.0).intValue();

        positioner.cropTo(itemIndex - itemsBefore, itemIndex + 1 + itemsAfter);
    }

    private int fillForwardFrom(int itemIndex) {
        return fillForwardFrom(itemIndex, sizeTracker.getViewportLength());
    }

    private int fillForwardFrom0(int itemIndex) {
        return fillForwardFrom0(itemIndex, sizeTracker.getViewportLength());
    }

    private int fillForwardFrom(int itemIndex, double upTo) {
        // resize and/or reposition the starting cell
        // in case the preferred or available size changed
        C cell = positioner.getVisibleCell(itemIndex);
        double length0 = orientation.minY(cell);
        positioner.placeStartAt(itemIndex, length0);

        return fillForwardFrom0(itemIndex, upTo);
    }

    int fillForwardFrom0(int itemIndex, double upTo) {
        double max = orientation.maxY(positioner.getVisibleCell(itemIndex));
        int i = itemIndex;
        while(max < upTo && i < cellListManager.getLazyCellList().size() - 1) {
            ++i;
            C c = positioner.placeStartAt(i, max);
            max = orientation.maxY(c);
        }
        return i;
    }

    private int fillBackwardFrom(int itemIndex) {
        return fillBackwardFrom(itemIndex, 0.0);
    }

    private int fillBackwardFrom0(int itemIndex) {
        return fillBackwardFrom0(itemIndex, 0.0);
    }

    private int fillBackwardFrom(int itemIndex, double upTo) {
        // resize and/or reposition the starting cell
        // in case the preferred or available size changed
        C cell = positioner.getVisibleCell(itemIndex);
        double length0 = orientation.minY(cell);
        positioner.placeStartAt(itemIndex, length0);

        return fillBackwardFrom0(itemIndex, upTo);
    }

    // does not re-place the anchor cell
    int fillBackwardFrom0(int itemIndex, double upTo) {
        double min = orientation.minY(positioner.getVisibleCell(itemIndex));
        int i = itemIndex;
        while(min > upTo && i > 0) {
            --i;
            C c = positioner.placeEndFromStart(i, min);
            min = orientation.minY(c);
        }
        return i;
    }

    private void fillViewportFrom(int itemIndex) {
        // cell for itemIndex is assumed to be placed correctly

        // fill up to the ground
        int ground = fillTowardsGroundFrom0(itemIndex);

        // if ground not reached, shift cells to the ground
        double gapBefore = distanceFromGround(ground);
        if(gapBefore > 0) {
            shiftCellsTowardsGround(ground, itemIndex, gapBefore);
        }

        // fill up to the sky
        int sky = fillTowardsSkyFrom0(itemIndex);

        // if sky not reached, add more cells under the ground and then shift
        double gapAfter = distanceFromSky(sky);
        if(gapAfter > 0) {
            ground = fillTowardsGroundFrom0(ground, -gapAfter);
            double extraBefore = -distanceFromGround(ground);
            double shift = Math.min(gapAfter, extraBefore);
            shiftCellsTowardsGround(ground, sky, -shift);
        }

        // crop to the visible cells
        int first = Math.min(ground, sky);
        int last = Math.max(ground, sky);
        while(first < last &&
                orientation.maxY(positioner.getVisibleCell(first)) <= 0.0) {
            ++first;
        }
        while(last > first &&
                orientation.minY(positioner.getVisibleCell(last)) >= sizeTracker.getViewportLength()) {
            --last;
        }
        positioner.cropTo(first, last + 1);
    }

    private int fillTowardsGroundFrom0(int itemIndex) {
        return gravity == Gravity.FRONT
                ? fillBackwardFrom0(itemIndex)
                : fillForwardFrom0(itemIndex);
    }

    private int fillTowardsGroundFrom0(int itemIndex, double upTo) {
        return gravity == Gravity.FRONT
                ? fillBackwardFrom0(itemIndex, upTo)
                : fillForwardFrom0(itemIndex, sizeTracker.getViewportLength() - upTo);
    }

    private int fillTowardsSkyFrom0(int itemIndex) {
        return gravity == Gravity.FRONT
                ? fillForwardFrom0(itemIndex)
                : fillBackwardFrom0(itemIndex);
    }

    private double distanceFromGround(int itemIndex) {
        C cell = positioner.getVisibleCell(itemIndex);
        return gravity == Gravity.FRONT
                ? orientation.minY(cell)
                : sizeTracker.getViewportLength() - orientation.maxY(cell);
    }

    private double distanceFromSky(int itemIndex) {
        C cell = positioner.getVisibleCell(itemIndex);
        return gravity == Gravity.FRONT
                ? sizeTracker.getViewportLength() - orientation.maxY(cell)
                : orientation.minY(cell);
    }

    private void shiftCellsTowardsGround(
            int groundCellIndex, int lastCellIndex, double amount) {
        if(gravity == Gravity.FRONT) {
            assert groundCellIndex <= lastCellIndex;
            for(int i = groundCellIndex; i <= lastCellIndex; ++i) {
                positioner.shiftCellBy(positioner.getVisibleCell(i), -amount);
            }
        } else {
            assert groundCellIndex >= lastCellIndex;
            for(int i = groundCellIndex; i >= lastCellIndex; --i) {
                positioner.shiftCellBy(positioner.getVisibleCell(i), amount);
            }
        }
    }
}