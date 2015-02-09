package org.fxmisc.flowless;

import java.util.Optional;
import java.util.function.Function;

import javafx.beans.value.ObservableObjectValue;
import javafx.geometry.Bounds;
import javafx.scene.control.IndexRange;

import org.reactfx.Subscription;
import org.reactfx.collection.LiveList;
import org.reactfx.collection.MemoizationList;
import org.reactfx.value.Val;
import org.reactfx.value.ValBase;

final class SizeTracker {
    private final OrientationHelper orientation;
    private final ObservableObjectValue<Bounds> viewportBounds;
    private final MemoizationList<? extends Cell<?, ?>> cells;
    private final MemoizationList<Double> breadths;
    private final Val<Double> maxKnownMinBreadth;
    private final Val<Double> viewportBreadth;
    private final Val<Double> viewportLength;
    private final Val<Double> breadthForCells;
    private final MemoizationList<Double> lengths;
    private final Val<Double> averageLengthEstimate;
    private final Val<Double> totalLengthEstimate;

    private final Subscription subscription;

    public SizeTracker(
            OrientationHelper orientation,
            ObservableObjectValue<Bounds> viewportBounds,
            MemoizationList<? extends Cell<?, ?>> lazyCells) {
        this.orientation = orientation;
        this.viewportBounds = viewportBounds;
        this.cells = lazyCells;
        this.breadths = lazyCells.map(orientation::minBreadth).memoize();
        this.maxKnownMinBreadth = LiveList.reduce(
                breadths.memoizedItems(), Math::max).orElseConst(0.0);
        this.viewportBreadth = Val.map(viewportBounds, orientation::breadth);
        this.viewportLength = Val.map(viewportBounds, orientation::length);
        this.breadthForCells = Val.combine(
                maxKnownMinBreadth, viewportBreadth, Math::max);

        Val<Function<Cell<?, ?>, Double>> lengthFn = avoidFalseInvalidations(breadthForCells).map(
                breadth -> cell -> orientation.prefLength(cell, breadth));

        this.lengths = cells.mapDynamic(lengthFn).memoize();

        Val<Double> sumOfKnownLengths = this.lengths.memoizedItems().reduce((a, b) -> a + b).orElseConst(0.0);
        Val<Integer> knownLengthCount = this.lengths.memoizedItems().sizeProperty();
        this.averageLengthEstimate = Val.create(
                () -> {
                    // make sure to use pref lengths of all present cells
                    IndexRange cellRange = cells.getMemoizedItemsRange();
                    lengths.force(cellRange.getStart(), cellRange.getEnd());

                    int count = knownLengthCount.getValue();
                    return count == 0
                            ? null
                            : sumOfKnownLengths.getValue() / count;
                },
                sumOfKnownLengths, knownLengthCount);

        this.totalLengthEstimate = Val.combine(
                averageLengthEstimate, cells.sizeProperty(),
                (avg, n) -> n * avg);

        // pinning totalLengthEstimate binds it all together and enables memoization
        this.subscription = totalLengthEstimate.pin();
    }

    private static <T> Val<T> avoidFalseInvalidations(Val<T> src) {
        return new ValBase<T>() {
            @Override
            protected Subscription connect() {
                return src.observeChanges((obs, oldVal, newVal) -> invalidate());
            }

            @Override
            protected T computeValue() {
                return src.getValue();
            }
        };
    }

    public void dispose() {
        subscription.unsubscribe();
    }

    public Val<Double> maxCellBreadthProperty() {
        return maxKnownMinBreadth;
    }

    public Val<Double> viewportBreadthProperty() {
        return viewportBreadth;
    }

    public double getViewportBreadth() {
        return orientation.breadth(viewportBounds.get());
    }

    public Val<Double> viewportLengthProperty() {
        return viewportLength;
    }

    public double getViewportLength() {
        return orientation.length(viewportBounds.get());
    }

    public Val<Double> averageLengthEstimateProperty() {
        return averageLengthEstimate;
    }

    public Optional<Double> getAverageLengthEstimate() {
        return averageLengthEstimate.getOpt();
    }

    public Val<Double> totalLengthEstimateProperty() {
        return totalLengthEstimate;
    }

    public double breadthFor(int itemIndex) {
        assert cells.isMemoized(itemIndex);
        breadths.force(itemIndex, itemIndex + 1);
        return breadthForCells.getValue();
    }

    public void forgetBreadthOf(int itemIndex) {
        breadths.forget(itemIndex, itemIndex + 1);
    }

    public double lengthFor(int itemIndex) {
        return lengths.get(itemIndex);
    }

    public double getCellLayoutBreadth() {
        return breadthForCells.getValue();
    }
}