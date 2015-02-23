package org.fxmisc.flowless;

import java.util.Optional;
import java.util.OptionalInt;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Region;

import org.fxmisc.easybind.EasyBind;
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
    private final SizeTracker sizeTracker;
    private final Subscription itemsSubscription;
    private final org.fxmisc.easybind.Subscription childrenBinding;

    private TargetPosition targetPosition = TargetPosition.BEGINNING;

    public Navigator(
            CellListManager<T, C> cellListManager,
            CellPositioner<T, C> positioner,
            OrientationHelper orientation,
            SizeTracker sizeTracker) {
        this.cellListManager = cellListManager;
        this.cells = cellListManager.getLazyCellList();
        this.positioner = positioner;
        this.orientation = orientation;
        this.sizeTracker = sizeTracker;

        this.itemsSubscription = LiveList.observeQuasiChanges(cellListManager.getLazyCellList(), this::itemsChanged);
        this.childrenBinding = EasyBind.listBind(getChildren(), cellListManager.getNodes());
    }

    public void dispose() {
        itemsSubscription.unsubscribe();
        childrenBinding.unsubscribe();
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
        targetPosition = getCurrentPosition();
    }

    public void setTargetPosition(TargetPosition targetPosition) {
        this.targetPosition = targetPosition;
        requestLayout();
    }

    public void scrollTargetPositionBy(double delta) {
        targetPosition = targetPosition.scrollBy(delta);
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

  void showLengthRegion(C cell, double fromY, double toY) {
      double minY = orientation.minY(cell);
      double spaceBefore = minY + fromY;
      double spaceAfter = sizeTracker.getViewportLength() - (minY + toY);
      if(spaceBefore < 0 && spaceAfter > 0) {
          double shift = Math.min(-spaceBefore, spaceAfter);
          scrollTargetPositionBy(-shift);
      } else if(spaceAfter < 0 && spaceBefore > 0) {
          double shift = Math.max(spaceAfter, -spaceBefore);
          scrollTargetPositionBy(-shift);
      }
  }

    @Override
    public void visit(StartOffStart targetPosition) {
        placeStartAtMayCrop(targetPosition.itemIndex, targetPosition.offsetFromStart);
        fillViewportFrom(targetPosition.itemIndex);
    }

    @Override
    public void visit(EndOffEnd targetPosition) {
        placeEndAtMayCrop(targetPosition.itemIndex, targetPosition.offsetFromEnd);
        fillViewportFrom(targetPosition.itemIndex);
    }

    @Override
    public void visit(MinDistanceTo targetPosition) {
        Optional<C> cell = positioner.getCellIfVisible(targetPosition.itemIndex);
        if(cell.isPresent()) {
            placeToViewportAndAdjust(targetPosition.itemIndex, targetPosition.additionalOffset);
        } else {
            OptionalInt prevVisible = positioner.lastVisibleBefore(targetPosition.itemIndex);
            OptionalInt nextVisible;
            if((prevVisible = positioner.lastVisibleBefore(targetPosition.itemIndex)).isPresent()) {
                // Try keeping prevVisible in place:
                // fill the viewport, see if the target item appeared.
                fillForwardFrom(prevVisible.getAsInt());
                cell = positioner.getCellIfVisible(targetPosition.itemIndex);
                if(cell.isPresent()) {
                    placeToViewportAndAdjust(targetPosition.itemIndex, targetPosition.additionalOffset);
                } else {
                    placeEndAtMayCrop(targetPosition.itemIndex, targetPosition.additionalOffset);
                }
            } else if((nextVisible = positioner.firstVisibleAfter(targetPosition.itemIndex + 1)).isPresent()) {
                // Try keeping nextVisible in place:
                // fill the viewport, see if the target item appeared.
                fillBackwardFrom(nextVisible.getAsInt());
                cell = positioner.getCellIfVisible(targetPosition.itemIndex);
                if(cell.isPresent()) {
                    placeToViewportAndAdjust(targetPosition.itemIndex, targetPosition.additionalOffset);
                } else {
                    placeStartAtMayCrop(targetPosition.itemIndex, targetPosition.additionalOffset);
                }
            } else {
                placeStartAtMayCrop(targetPosition.itemIndex, targetPosition.additionalOffset);
            }
        }
        fillViewportFrom(targetPosition.itemIndex);
    }

    private void placeToViewportAndAdjust(int itemIndex, double adjustment) {
        C cell = positioner.getVisibleCell(itemIndex);
        double d = positioner.shortestDeltaToViewport(cell);
        positioner.placeStartAt(itemIndex, orientation.minY(cell) + d + adjustment);
    }

    private void placeStartAtMayCrop(int itemIndex, double startOffStart) {
        cropToNeighborhoodOf(itemIndex, startOffStart);
        positioner.placeStartAt(itemIndex, startOffStart);
    }

    private void placeEndAtMayCrop(int itemIndex, double endOffEnd) {
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

        // fill up to viewport start
        int first = fillBackwardFrom0(itemIndex);

        // if viewport start not reached, shift cells to the start
        C firstCell = positioner.getVisibleCell(first);
        double gapBefore = orientation.minY(firstCell);
        if(gapBefore > 0) {
            for(int i = first; i <= itemIndex; ++i) {
                positioner.shiftCellBy(positioner.getVisibleCell(i), -gapBefore);
            }
        }

        // fill up to viewport end
        int last = fillForwardFrom0(itemIndex);

        // if viewport end not reached, add more cells to the front and then shift
        C lastCell = positioner.getVisibleCell(last);
        double gapAfter = sizeTracker.getViewportLength() - orientation.maxY(lastCell);
        if(gapAfter > 0) {
            first = fillBackwardFrom0(first, -gapAfter);
            firstCell = positioner.getVisibleCell(first);
            double extraBefore = -orientation.minY(firstCell);
            double shift = Math.min(gapAfter, extraBefore);
            for(int i = first; i <= last; ++i) {
                positioner.shiftCellBy(positioner.getVisibleCell(i), shift);
            }
        }

        // crop to the visible cells
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
}