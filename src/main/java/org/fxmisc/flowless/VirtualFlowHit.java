package org.fxmisc.flowless;

import javafx.geometry.Point2D;

/**
 * Stores the result of a {@link VirtualFlow#hit(double, double)}. Before calling
 * any of the getters, one should determine what kind of hit this object is via {@link #isCellHit()},
 * {@link #isBeforeCells()}, and {@link #isAfterCells()}. Otherwise, calling the wrong getter will throw
 * an {@link UnsupportedOperationException}.
 *
 * <p>Types of VirtualFlowHit:</p>
 *     <ul>
 *         <li>
 *             <em>Cell Hit:</em> a hit occurs on a displayed cell's node. One can call {@link #getCell()},
 *             {@link #getCellIndex()}, and {@link #getCellOffset()}.
 *         </li>
 *         <li>
 *             <em>Hit Before Cells:</em> a hit occurred before the displayed cells. One can call
 *             {@link #getOffsetBeforeCells()}.
 *         </li>
 *         <li>
 *             <em>Hit After Cells:</em> a hit occurred after the displayed cells. One can call
 *             {@link #getOffsetAfterCells()}.
 *         </li>
 *     </ul>
 *
 */
public abstract class VirtualFlowHit<C extends Cell<?, ?>> {

    static <C extends Cell<?, ?>> VirtualFlowHit<C> cellHit(
            int cellIndex, C cell, double x, double y) {
        return new VirtualFlowHit.CellHit<>(cellIndex, cell, new Point2D(x, y));
    }

    static <C extends Cell<?, ?>> VirtualFlowHit<C> hitBeforeCells(double x, double y) {
        return new VirtualFlowHit.HitBeforeCells<>(new Point2D(x, y));
    }

    static <C extends Cell<?, ?>> VirtualFlowHit<C> hitAfterCells(double x, double y) {
        return new VirtualFlowHit.HitAfterCells<>(new Point2D(x, y));
    }

    // private constructor to prevent subclassing
    private VirtualFlowHit() {}

    public abstract boolean isCellHit();
    public abstract boolean isBeforeCells();
    public abstract boolean isAfterCells();

    public abstract int getCellIndex();
    public abstract C getCell();
    public abstract Point2D getCellOffset();

    public abstract Point2D getOffsetBeforeCells();
    public abstract Point2D getOffsetAfterCells();

    private static class CellHit<C extends Cell<?, ?>> extends VirtualFlowHit<C> {
        private final int cellIdx;
        private final C cell;
        private final Point2D cellOffset;

        CellHit(int cellIdx, C cell, Point2D cellOffset) {
            this.cellIdx = cellIdx;
            this.cell = cell;
            this.cellOffset = cellOffset;
        }

        @Override public boolean isCellHit() { return true; }
        @Override public boolean isBeforeCells() { return false; }
        @Override public boolean isAfterCells() { return false; }
        @Override public int getCellIndex() { return cellIdx; }
        @Override public C getCell() { return cell; }
        @Override public Point2D getCellOffset() { return cellOffset; }

        @Override
        public Point2D getOffsetBeforeCells() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Point2D getOffsetAfterCells() {
            throw new UnsupportedOperationException();
        }
    }

    private static class HitBeforeCells<C extends Cell<?, ?>> extends VirtualFlowHit<C> {
        private final Point2D offset;

        HitBeforeCells(Point2D offset) {
            this.offset = offset;
        }

        @Override public boolean isCellHit() { return false; }
        @Override public boolean isBeforeCells() { return true; }
        @Override public boolean isAfterCells() { return false; }

        @Override public int getCellIndex() {
            throw new UnsupportedOperationException();
        }

        @Override public C getCell() {
            throw new UnsupportedOperationException();
        }

        @Override public Point2D getCellOffset() {
            throw new UnsupportedOperationException();
        }

        @Override public Point2D getOffsetBeforeCells() {
            return offset;
        }

        @Override public Point2D getOffsetAfterCells() {
            throw new UnsupportedOperationException();
        }
    }

    private static class HitAfterCells<C extends Cell<?, ?>> extends VirtualFlowHit<C> {
        private final Point2D offset;

        HitAfterCells(Point2D offset) {
            this.offset = offset;
        }

        @Override public boolean isCellHit() { return false; }
        @Override public boolean isBeforeCells() { return false; }
        @Override public boolean isAfterCells() { return true; }

        @Override public int getCellIndex() {
            throw new UnsupportedOperationException();
        }

        @Override public C getCell() {
            throw new UnsupportedOperationException();
        }

        @Override public Point2D getCellOffset() {
            throw new UnsupportedOperationException();
        }

        @Override public Point2D getOffsetBeforeCells() {
            throw new UnsupportedOperationException();
        }

        @Override public Point2D getOffsetAfterCells() {
            return offset;
        }
    }
}