package org.fxmisc.flowless;


public abstract class VirtualFlowHit<C extends Cell<?, ?>> {

    static <C extends Cell<?, ?>> VirtualFlowHit<C> cellHit(
            int cellIndex, C cell, double cellOffset) {
        return new VirtualFlowHit.CellHit<>(cellIndex, cell, cellOffset);
    }

    static <C extends Cell<?, ?>> VirtualFlowHit<C> hitBeforeCells(double offset) {
        return new VirtualFlowHit.HitBeforeCells<>(offset);
    }

    static <C extends Cell<?, ?>> VirtualFlowHit<C> hitAfterCells(double offset) {
        return new VirtualFlowHit.HitAfterCells<>(offset);
    }

    // private constructor to prevent subclassing
    private VirtualFlowHit() {}

    public abstract boolean isCellHit();
    public abstract boolean isBeforeCells();
    public abstract boolean isAfterCells();

    public abstract int getCellIndex();
    public abstract C getCell();
    public abstract double getCellOffset();

    public abstract double getOffsetBeforeCells();
    public abstract double getOffsetAfterCells();

    private static class CellHit<C extends Cell<?, ?>> extends VirtualFlowHit<C> {
        private final int cellIdx;
        private final C cell;
        private final double cellOffset;

        CellHit(int cellIdx, C cell, double cellOffset) {
            this.cellIdx = cellIdx;
            this.cell = cell;
            this.cellOffset = cellOffset;
        }

        @Override public boolean isCellHit() { return true; }
        @Override public boolean isBeforeCells() { return false; }
        @Override public boolean isAfterCells() { return false; }
        @Override public int getCellIndex() { return cellIdx; }
        @Override public C getCell() { return cell; }
        @Override public double getCellOffset() { return cellOffset; }

        @Override
        public double getOffsetBeforeCells() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getOffsetAfterCells() {
            throw new UnsupportedOperationException();
        }
    }

    private static class HitBeforeCells<C extends Cell<?, ?>> extends VirtualFlowHit<C> {
        private final double offset;

        HitBeforeCells(double offset) {
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

        @Override public double getCellOffset() {
            throw new UnsupportedOperationException();
        }

        @Override public double getOffsetBeforeCells() {
            return offset;
        }

        @Override public double getOffsetAfterCells() {
            throw new UnsupportedOperationException();
        }
    }

    private static class HitAfterCells<C extends Cell<?, ?>> extends VirtualFlowHit<C> {
        private final double offset;

        HitAfterCells(double offset) {
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

        @Override public double getCellOffset() {
            throw new UnsupportedOperationException();
        }

        @Override public double getOffsetBeforeCells() {
            throw new UnsupportedOperationException();
        }

        @Override public double getOffsetAfterCells() {
            return offset;
        }
    }
}