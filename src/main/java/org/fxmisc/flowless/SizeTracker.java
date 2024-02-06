package org.fxmisc.flowless;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.NoSuchElementException;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.geometry.Bounds;
import javafx.scene.control.IndexRange;

import org.reactfx.EventStream;
import org.reactfx.EventStreams;
import org.reactfx.Subscription;
import org.reactfx.collection.LiveList;
import org.reactfx.collection.MemoizationList;
import org.reactfx.util.Tuple3;
import org.reactfx.value.Val;
import org.reactfx.value.ValBase;

/**
 * Estimates the size of the entire viewport (if it was actually completely rendered) based on the known sizes of the
 * {@link Cell}s whose nodes are currently displayed in the viewport and an estimated average of
 * {@link Cell}s whose nodes are not displayed in the viewport. The meaning of {@link #breadthForCells} and
 * {@link #totalLengthEstimate} are dependent upon which implementation of {@link OrientationHelper} is used.
 */
final class SizeTracker {
    private final OrientationHelper orientation;
    private final ObservableObjectValue<Bounds> viewportBounds;
    private final MemoizationList<? extends Cell<?, ?>> cells;

    private final MemoizationList<Double> breadths;
    private final Val<Double> maxKnownMinBreadth;

    /** Stores either the greatest minimum cell's node's breadth or the viewport's breadth */
    private final Val<Double> breadthForCells;

    private final MemoizationList<Double> lengths;

    /** Stores either null or the average length of the cells' nodes currently displayed in the viewport */
    private final Val<Double> averageLengthEstimate;

    private final Val<Double> totalLengthEstimate;
    private final Val<Double> lengthOffsetEstimate;

    private final Subscription subscription;

    /**
     * Constructs a SizeTracker
     *
     * @param orientation if vertical, breadth = width and length = height;
     *                    if horizontal, breadth = height and length = width
     */
    public SizeTracker(
            OrientationHelper orientation,
            ObservableObjectValue<Bounds> viewportBounds,
            MemoizationList<? extends Cell<?, ?>> lazyCells) {
        this.orientation = orientation;
        this.viewportBounds = viewportBounds;
        this.cells = lazyCells;
        this.breadths = lazyCells.map(orientation::minBreadth).memoize();
        LiveList<Double> knownBreadths = this.breadths.memoizedItems();

        this.maxKnownMinBreadth = Val.create(
                () -> knownBreadths.stream().mapToDouble( Double::doubleValue ).max().orElse(0.0),
                // skips spurious events resulting from cell replacement (delete then add again)
                knownBreadths.changes().successionEnds( Duration.ofMillis( 15 ) )
        );

        this.breadthForCells = Val.combine(
                maxKnownMinBreadth,
                viewportBounds,
                (a, b) -> Math.max(a, orientation.breadth(b)));

        Val<Function<Cell<?, ?>, Double>> lengthFn;
        lengthFn = (orientation instanceof HorizontalHelper ? breadthForCells : avoidFalseInvalidations(breadthForCells))
                .map(breadth -> cell -> orientation.prefLength(cell, breadth));

        this.lengths = cells.mapDynamic(lengthFn).memoize();
        LiveList<Double> knownLengths = this.lengths.memoizedItems();

        Supplier<Double> averageKnownLengths = () -> {
            // make sure to use pref lengths of all present cells
            for(int i = 0; i < cells.getMemoizedCount(); ++i) try {
                int j = cells.indexOfMemoizedItem(i);
                lengths.force(j, j + 1);
            }
            catch ( IndexOutOfBoundsException IX ) {}
            catch ( NoSuchElementException EX ) {}

            return knownLengths.stream()
                .mapToDouble( Double::doubleValue )
                .sorted().average()
                .orElse( 0.0 );
        };

        final int AVERAGE_LENGTH = 0, TOTAL_LENGTH = 1;
        Val<double[/*average,total*/]> lengthStats = Val.wrap(
                knownLengths.changes().or( cells.sizeProperty().values() )
                .successionEnds( Duration.ofMillis( 15 ) ) // reduce noise
                .map( e -> {
                	if ( e != null && e.isRight() && e.getRight() != null ) return e.getRight();
                	return cells.size();
                } )
                .map( cellCount -> {
                    double averageLength = averageKnownLengths.get();
                    return new double[] { averageLength, cellCount * averageLength };
                } ).toBinding( new double[] { 0.0, 0.0 } )
        );

        EventStream<double[/*average,total*/]> filteredLengthStats;
        // briefly hold back changes that may be from spurious events coming from cell refreshes, these
        // are identified as those where the estimated total length is less than the previous event.
        filteredLengthStats = new PausableSuccessionStream<>( lengthStats.changes(), Duration.ofMillis(1000), chg -> {
                double[/*average,total*/] oldStats = chg.getOldValue();
                double[/*average,total*/] newStats = chg.getNewValue();
                if ( newStats[TOTAL_LENGTH] < oldStats[TOTAL_LENGTH] ) {
                    return false; // don't emit yet, first wait & prefer newer values
                }
                return true;
        } )
        .map( chg -> chg.getNewValue() );

        this.averageLengthEstimate = Val.wrap( filteredLengthStats.map( stats -> stats[AVERAGE_LENGTH] ).toBinding( 0.0 ) );
        this.totalLengthEstimate = Val.wrap( filteredLengthStats.map( stats -> stats[TOTAL_LENGTH] ).toBinding( 0.0 ) );

        Val<Integer> firstVisibleIndex = Val.create(
                () -> cells.getMemoizedCount() == 0 ? null : cells.indexOfMemoizedItem(0),
                cells, cells.memoizedItems()); // need to observe cells.memoizedItems()
                // as well, because they may change without a change in cells.

        Val<Integer> knownLengthCountBeforeFirstVisibleCell = Val.create(() -> {
            return firstVisibleIndex.getOpt()
                    .map(i -> lengths.getMemoizedCountBefore(Math.min(i, lengths.size())))
                    .orElse(0);
        }, lengths, firstVisibleIndex);

        Val<Double> totalKnownLengthBeforeFirstVisibleCell = knownLengths.reduceRange(
                knownLengthCountBeforeFirstVisibleCell.map(n -> new IndexRange(0, n)),
                (a, b) -> a + b).orElseConst(0.0);

        Val<Double> unknownLengthEstimateBeforeFirstVisibleCell = Val.combine(
                firstVisibleIndex.orElseConst(0),
                knownLengthCountBeforeFirstVisibleCell,
                averageLengthEstimate,
                (firstIdx, knownCnt, avgLen) -> (firstIdx - knownCnt) * avgLen);

        Val<Double> firstCellMinY = cells.memoizedItems()
                .collapse(visCells -> visCells.isEmpty() ? null : visCells.get(0))
                .flatMap(orientation::minYProperty);

        EventStream<Tuple3<Double, Double, Double>> lengthOffsetStream = EventStreams.combine(
            totalKnownLengthBeforeFirstVisibleCell.values(),
            unknownLengthEstimateBeforeFirstVisibleCell.values(),
            firstCellMinY.values()
        );

        lengthOffsetEstimate = Val.wrap(
           // skip spurious events resulting from cell replacement (delete then add again), except
           // when immediateUpdate is true: activated via updateNextLengthOffsetEstimateImmediately()
           new PausableSuccessionStream<>( lengthOffsetStream, Duration.ofMillis(15), immediateUpdate )
            .filter( t3 -> t3 != null && t3.test( (a,b,minY) -> a != null && b != null && minY != null ) )
            .map( t3 -> t3.map( (a,b,minY) -> Double.valueOf( Math.round( a + b - minY ) ) ) )
            .toBinding( 0.0 ) );

        // pinning totalLengthEstimate and lengthOffsetEstimate
        // binds it all together and enables memoization
        this.subscription = Subscription.multi(
                totalLengthEstimate.pin(),
                lengthOffsetEstimate.pin());
    }

    private SimpleBooleanProperty immediateUpdate = new SimpleBooleanProperty();
    void updateNextLengthOffsetEstimateImmediately() { immediateUpdate.set( true ); }

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

    public double getViewportBreadth() {
        return orientation.breadth(viewportBounds.get());
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

    public Val<Double> lengthOffsetEstimateProperty() {
        return lengthOffsetEstimate;
    }

    public double breadthFor(int itemIndex) {
        assert cells.isMemoized(itemIndex);
        breadths.force(itemIndex, itemIndex + 1);
        return breadthForCells.getValue();
    }

    public void forgetSizeOf(int itemIndex) {
        breadths.forget(itemIndex, itemIndex + 1);
        lengths.forget(itemIndex, itemIndex + 1);
    }

    public double lengthFor(int itemIndex) {
        return lengths.get(itemIndex);
    }

    public double getCellLayoutBreadth() {
        return breadthForCells.getValue();
    }
}
