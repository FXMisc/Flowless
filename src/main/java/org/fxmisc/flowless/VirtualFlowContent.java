package org.fxmisc.flowless;

import java.util.Optional;
import java.util.function.Function;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

import org.fxmisc.flowless.VirtualFlow.Gravity;
import org.reactfx.collection.MemoizationList;
import org.reactfx.util.Lists;
import org.reactfx.value.Val;
import org.reactfx.value.Var;

class VirtualFlowContent<T, C extends Cell<T, ?>> extends Region {
    private final ObservableList<T> items;
    private final OrientationHelper orientation;
    private final CellListManager<T, C> cellListManager;
    private final SizeTracker sizeTracker;
    private final CellPositioner<T, C> cellPositioner;
    private final Navigator<T, C> navigator;

    // non-negative
    private final ReadOnlyDoubleWrapper breadthOffset = new ReadOnlyDoubleWrapper(0.0);
    public ReadOnlyDoubleProperty breadthOffsetProperty() {
        return breadthOffset.getReadOnlyProperty();
    }

    public Val<Double> totalBreadthEstimateProperty() {
        return sizeTracker.maxCellBreadthProperty();
    }

    private final Val<Double> breadthPositionEstimate;
    public Var<Double> breadthPositionEstimateProperty() {
        return breadthPositionEstimate.asVar(this::setBreadthPosition);
    }

    private final Val<Double> lengthOffsetEstimate;

    private final Val<Double> lengthPositionEstimate;
    public Var<Double> lengthPositionEstimateProperty() {
        return lengthPositionEstimate.asVar(this::setLengthPosition);
    }

    VirtualFlowContent(
            ObservableList<T> items,
            Function<? super T, ? extends C> cellFactory,
            OrientationHelper orientation,
            Gravity gravity) {
        this.getStyleClass().add("virtual-flow-content");
        this.items = items;
        this.orientation = orientation;
        this.cellListManager = new CellListManager<>(items, cellFactory);
        MemoizationList<C> cells = cellListManager.getLazyCellList();
        this.sizeTracker = new SizeTracker(orientation, layoutBoundsProperty(), cells);
        this.cellPositioner = new CellPositioner<>(cellListManager, orientation, sizeTracker);
        this.navigator = new Navigator<>(cellListManager, cellPositioner, orientation, gravity, sizeTracker);

        getChildren().add(navigator);
        clipProperty().bind(Val.map(
                layoutBoundsProperty(),
                b -> new Rectangle(b.getWidth(), b.getHeight())));


        // set up bindings

        breadthPositionEstimate = Val.combine(
                breadthOffset,
                sizeTracker.viewportBreadthProperty(),
                sizeTracker.maxCellBreadthProperty(),
                (off, vpBr, totalBr) -> offsetToScrollbarPosition(off.doubleValue(), vpBr, totalBr));

        lengthOffsetEstimate = sizeTracker.lengthOffsetEstimateProperty();

        lengthPositionEstimate = Val.combine(
                lengthOffsetEstimate,
                sizeTracker.viewportLengthProperty(),
                sizeTracker.totalLengthEstimateProperty(),
                (off, vpLen, totalLen) -> offsetToScrollbarPosition(off, vpLen, totalLen))
                .orElseConst(0.0);
    }

    public void dispose() {
        navigator.dispose();
        sizeTracker.dispose();
        cellListManager.dispose();
    }

    public C getCellFor(int itemIndex) {
        Lists.checkIndex(itemIndex, items.size());
        return cellPositioner.getSizedCell(itemIndex);
    }

    public Optional<C> getCellIfVisible(int itemIndex) {
        return cellPositioner.getCellIfVisible(itemIndex);
    }

    public ObservableList<C> visibleCells() {
        return cellListManager.getLazyCellList().memoizedItems();
    }

    public Val<Double> totalLengthEstimateProperty() {
        return sizeTracker.totalLengthEstimateProperty();
    }

    @Override
    protected void layoutChildren() {

        // navigate to the target position and fill viewport
        while(true) {
            double oldLayoutBreadth = sizeTracker.getCellLayoutBreadth();
            orientation.resize(navigator, oldLayoutBreadth, sizeTracker.getViewportLength());
            navigator.layout();
            if(oldLayoutBreadth == sizeTracker.getCellLayoutBreadth()) {
                break;
            }
        }

        orientation.relocate(navigator, -breadthOffset.get(), 0);
    }

    @Override
    protected final double computePrefWidth(double height) {
        switch(getContentBias()) {
            case HORIZONTAL: // vertical flow
                return computePrefBreadth();
            case VERTICAL: // horizontal flow
                return computePrefLength(height);
            default:
                throw new AssertionError("Unreachable code");
        }
    }

    @Override
    protected final double computePrefHeight(double width) {
        switch(getContentBias()) {
            case HORIZONTAL: // vertical flow
                return computePrefLength(width);
            case VERTICAL: // horizontal flow
                return computePrefBreadth();
            default:
                throw new AssertionError("Unreachable code");
        }
    }

    private double computePrefBreadth() {
        return 100;
    }

    private double computePrefLength(double breadth) {
        return 100;
    }

    @Override
    public final Orientation getContentBias() {
        return orientation.getContentBias();
    }

    void scrollLength(double deltaLength) {
        setLengthOffset(lengthOffsetEstimate.getValue() + deltaLength);
    }

    void scrollBreadth(double deltaBreadth) {
        setBreadthOffset(breadthOffset.get() + deltaBreadth);
    }

    void scrollLengthToPixel(double pixel) {
        setLengthOffset(pixel);
    }

    void scrollBreadthToPixel(double pixel) {
        setBreadthOffset(pixel);
    }

    void scrollX(double deltaX) {
        orientation.scrollHorizontally(this, deltaX);
    }

    void scrollY(double deltaY) {
        orientation.scrollVertically(this, deltaY);
    }

    void scrollXToPixel(double pixel) {
        orientation.scrollHorizontallyToPixel(this, pixel);
    }

    void scrollYToPixel(double pixel) {
        orientation.scrollVerticallyToPixel(this, pixel);
    }

    Val<Double> totalWidthEstimateProperty() {
        return Val.wrap(orientation.widthEstimateProperty(this));
    }

    Val<Double> totalHeightEstimateProperty() {
        return Val.wrap(orientation.heightEstimateProperty(this));
    }

    Val<Double> estimatedWidthPositionProperty() {
        switch (getContentBias()) {
            case HORIZONTAL: // vertical flow
                return breadthPositionEstimateProperty();
            case VERTICAL: // horizontal flow
                return lengthPositionEstimateProperty();
            default:
                throw new AssertionError("Unreachable code");
        }
    }

    Val<Double> estimatedHeightPositionProperty() {
        switch (getContentBias()) {
            case HORIZONTAL: // vertical flow
                return lengthPositionEstimateProperty();
            case VERTICAL: // horizontal flow
                return breadthPositionEstimateProperty();
            default:
                throw new AssertionError("Unreachable code");
        }
    }

    VirtualFlowHit<C> hit(double x, double y) {
        double bOff = orientation.getX(x, y);
        double lOff = orientation.getY(x, y);

        bOff += breadthOffset.get();

        if(items.isEmpty()) {
            return orientation.hitAfterCells(bOff, lOff);
        }

        layout();

        int firstVisible = cellPositioner.getFirstVisibleIndex().getAsInt();
        firstVisible = navigator.fillBackwardFrom0(firstVisible, lOff);
        C firstCell = cellPositioner.getVisibleCell(firstVisible);

        int lastVisible = cellPositioner.getLastVisibleIndex().getAsInt();
        lastVisible = navigator.fillForwardFrom0(lastVisible, lOff);
        C lastCell = cellPositioner.getVisibleCell(lastVisible);

        if(lOff < orientation.minY(firstCell)) {
            return orientation.hitBeforeCells(bOff, lOff - orientation.minY(firstCell));
        } else if(lOff >= orientation.maxY(lastCell)) {
            return orientation.hitAfterCells(bOff, lOff - orientation.maxY(lastCell));
        } else {
            for(int i = firstVisible; i <= lastVisible; ++i) {
                C cell = cellPositioner.getVisibleCell(i);
                if(lOff < orientation.maxY(cell)) {
                    return orientation.cellHit(i, cell, bOff, lOff - orientation.minY(cell));
                }
            }
            throw new AssertionError("unreachable code");
        }
    }

    void show(double viewportOffset) {
        if(viewportOffset < 0) {
            navigator.scrollTargetPositionBy(viewportOffset);
        } else if(viewportOffset > sizeTracker.getViewportLength()) {
            navigator.scrollTargetPositionBy(viewportOffset - sizeTracker.getViewportLength());
        } else {
            // do nothing, offset already in the viewport
        }
    }

    void show(int itemIdx) {
        navigator.setTargetPosition(new MinDistanceTo(itemIdx));
    }

    void showAsFirst(int itemIdx) {
        navigator.setTargetPosition(new StartOffStart(itemIdx, 0.0));
    }

    void showAsLast(int itemIdx) {
        navigator.setTargetPosition(new EndOffEnd(itemIdx, 0.0));
    }

    void showAtOffset(int itemIdx, double offset) {
        navigator.setTargetPosition(new StartOffStart(itemIdx, offset));
    }

    void showRegion(int itemIndex, Bounds region) {
      navigator.showLengthRegion(itemIndex, orientation.minY(region), orientation.maxY(region));
      showBreadthRegion(orientation.minX(region), orientation.maxX(region));
    }

    private void showBreadthRegion(double fromX, double toX) {
        double bOff = breadthOffset.get();
        double spaceBefore = fromX - bOff;
        double spaceAfter = sizeTracker.getViewportBreadth() - toX + bOff;
        if(spaceBefore < 0 && spaceAfter > 0) {
            double shift = Math.min(-spaceBefore, spaceAfter);
            setBreadthOffset(bOff - shift);
        } else if(spaceAfter < 0 && spaceBefore > 0) {
            double shift = Math.max(spaceAfter, -spaceBefore);
            setBreadthOffset(bOff - shift);
        }
    }

    private void setLengthPosition(double pos) {
        setLengthOffset(lengthPositionToPixels(pos));
    }

    private void setBreadthPosition(double pos) {
        setBreadthOffset(breadthPositionToPixels(pos));
    }

    private void setLengthOffset(double pixels) {
        double total = totalLengthEstimateProperty().getOrElse(0.0);
        double length = sizeTracker.getViewportLength();
        double max = Math.max(total - length, 0);
        double current = lengthOffsetEstimate.getValue();

        if(pixels > max) pixels = max;
        if(pixels < 0) pixels = 0;

        double diff = pixels - current;
        if(diff == 0) {
            // do nothing
        } else if(Math.abs(diff) < length) { // distance less than one screen
            navigator.scrollTargetPositionBy(diff);
        } else {
            jumpToAbsolutePosition(pixels);
        }
    }

    private void setBreadthOffset(double pixels) {
        double total = totalBreadthEstimateProperty().getValue();
        double breadth = sizeTracker.getViewportBreadth();
        double max = Math.max(total - breadth, 0);
        double current = breadthOffset.get();

        if(pixels > max) pixels = max;
        if(pixels < 0) pixels = 0;

        if(pixels != current) {
            breadthOffset.set(pixels);
            requestLayout();
            // TODO: could be safely relocated right away?
            // (Does relocation request layout?)
        }
    }

    private void jumpToAbsolutePosition(double pixels) {
        if(items.isEmpty()) {
            return;
        }

        // guess the first visible cell and its offset in the viewport
        double avgLen = sizeTracker.getAverageLengthEstimate().orElse(0.0);
        if(avgLen == 0.0) return;
        int first = (int) Math.floor(pixels / avgLen);
        double firstOffset = -(pixels % avgLen);

        if(first < items.size()) {
            navigator.setTargetPosition(new StartOffStart(first, firstOffset));
        } else {
            navigator.setTargetPosition(new EndOffEnd(items.size() - 1, 0.0));
        }
    }

    private double lengthPositionToPixels(double pos) {
        double total = totalLengthEstimateProperty().getOrElse(0.0);
        double length = sizeTracker.getViewportLength();
        return scrollbarPositionToOffset(pos, length, total);
    }

    private double breadthPositionToPixels(double pos) {
        double total = totalBreadthEstimateProperty().getValue();
        double breadth = sizeTracker.getViewportBreadth();
        return scrollbarPositionToOffset(pos, breadth, total);
    }

    private static double offsetToScrollbarPosition(
            double contentOffset, double viewportSize, double contentSize) {
        return contentSize > viewportSize
                ? contentOffset / (contentSize - viewportSize) * contentSize
                : 0;
    }

    private static double scrollbarPositionToOffset(
            double scrollbarPos, double viewportSize, double contentSize) {
        return contentSize > viewportSize
                ? scrollbarPos / contentSize * (contentSize - viewportSize)
                : 0;
    }
}