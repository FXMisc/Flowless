package org.fxmisc.flowless;

interface TargetPosition {
    static TargetPosition BEGINNING = new StartOffStart(0, 0.0);

    TargetPosition transformByChange(int pos, int removedSize, int addedSize);
    TargetPosition scrollBy(double delta);
    void accept(TargetPositionVisitor visitor);
    TargetPosition clamp(int size);
}

interface TargetPositionVisitor {
    void visit(StartOffStart targetPosition);
    void visit(EndOffEnd targetPosition);
    void visit(MinDistanceTo targetPosition);
}

final class StartOffStart implements TargetPosition {
    final int itemIndex;
    final double offsetFromStart;

    StartOffStart(int itemIndex, double offsetFromStart) {
        this.itemIndex = itemIndex;
        this.offsetFromStart = offsetFromStart;
    }

    @Override
    public TargetPosition transformByChange(
            int pos, int removedSize, int addedSize) {
        if(itemIndex >= pos + removedSize) {
            // change before the target item, just update item index
            return new StartOffStart(itemIndex - removedSize + addedSize, offsetFromStart);
        } else if(itemIndex >= pos) {
            // target item deleted, show the first inserted at the target offset
            return new StartOffStart(pos, offsetFromStart);
        } else {
            // change after the target item, target position not affected
            return this;
        }
    }

    @Override
    public TargetPosition scrollBy(double delta) {
        return new StartOffStart(itemIndex, offsetFromStart - delta);
    }

    @Override
    public void accept(TargetPositionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public TargetPosition clamp(int size) {
        return new StartOffStart(clamp(itemIndex, size), offsetFromStart);
    }

    static int clamp(int idx, int size) {
        if(size < 0) {
            throw new IllegalArgumentException("size cannot be negative: " + size);
        }
        if(idx <= 0) {
            return 0;
        } else if(idx >= size) {
            return size - 1;
        } else {
            return idx;
        }
    }
}

final class EndOffEnd implements TargetPosition {
    final int itemIndex;
    final double offsetFromEnd;

    EndOffEnd(int itemIndex, double offsetFromEnd) {
        this.itemIndex = itemIndex;
        this.offsetFromEnd = offsetFromEnd;
    }

    @Override
    public TargetPosition transformByChange(
            int pos, int removedSize, int addedSize) {
        if(itemIndex >= pos + removedSize) {
            // change before the target item, just update item index
            return new EndOffEnd(itemIndex - removedSize + addedSize, offsetFromEnd);
        } else if(itemIndex >= pos) {
            // target item deleted, show the last inserted at the target offset
            return new EndOffEnd(pos + addedSize - 1, offsetFromEnd);
        } else {
            // change after the target item, target position not affected
            return this;
        }
    }

    @Override
    public TargetPosition scrollBy(double delta) {
        return new EndOffEnd(itemIndex, offsetFromEnd - delta);
    }

    @Override
    public void accept(TargetPositionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public TargetPosition clamp(int size) {
        return new EndOffEnd(StartOffStart.clamp(itemIndex, size), offsetFromEnd);
    }
}

final class MinDistanceTo implements TargetPosition {
    final int itemIndex;
    final Offset minY;
    final Offset maxY;

    MinDistanceTo(int itemIndex, Offset minY, Offset maxY) {
        this.itemIndex = itemIndex;
        this.minY = minY;
        this.maxY = maxY;
    }

    public MinDistanceTo(int itemIndex) {
        this(itemIndex, Offset.fromStart(0.0), Offset.fromEnd(0.0));
    }

    @Override
    public TargetPosition transformByChange(
            int pos, int removedSize, int addedSize) {
        if(itemIndex >= pos + removedSize) {
            // change before the target item, just update item index
            return new MinDistanceTo(itemIndex - removedSize + addedSize, minY, maxY);
        } else if(itemIndex >= pos) {
            // target item deleted, show the first inserted
            return new MinDistanceTo(pos, Offset.fromStart(0.0), Offset.fromEnd(0.0));
        } else {
            // change after the target item, target position not affected
            return this;
        }
    }

    @Override
    public TargetPosition scrollBy(double delta) {
        return new MinDistanceTo(itemIndex, minY.add(delta), maxY.add(delta));
    }

    @Override
    public void accept(TargetPositionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public TargetPosition clamp(int size) {
        return new MinDistanceTo(StartOffStart.clamp(itemIndex, size), minY, maxY);
    }
}

class Offset {
    public static Offset fromStart(double offset) {
        return new Offset(offset, true);
    }

    public static Offset fromEnd(double offset) {
        return new Offset(offset, false);
    }

    private final double offset;
    private final boolean fromStart;

    private Offset(double offset, boolean fromStart) {
        this.offset = offset;
        this.fromStart = fromStart;
    }

    public double getValue() {
        return offset;
    }

    public boolean isFromStart() {
        return fromStart;
    }

    public boolean isFromEnd() {
        return !fromStart;
    }

    public Offset add(double delta) {
        return new Offset(offset + delta, fromStart);
    }
}