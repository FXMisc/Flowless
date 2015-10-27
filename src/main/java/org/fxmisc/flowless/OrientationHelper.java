package org.fxmisc.flowless;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;

import org.reactfx.value.Val;
import org.reactfx.value.Var;

interface OrientationHelper {
    Orientation getContentBias();
    double getX(double x, double y);
    double getY(double x, double y);
    double length(Bounds bounds);
    double breadth(Bounds bounds);
    double minX(Bounds bounds);
    double minY(Bounds bounds);
    default double maxX(Bounds bounds) { return minX(bounds) + breadth(bounds); }
    default double maxY(Bounds bounds) { return minY(bounds) + length(bounds); }
    double layoutX(Node node);
    double layoutY(Node node);
    DoubleProperty layoutYProperty(Node node);
    default double length(Node node) { return length(node.getLayoutBounds()); }
    default double breadth(Node node) { return breadth(node.getLayoutBounds()); }
    default Val<Double> minYProperty(Node node) {
        return Val.combine(
                layoutYProperty(node),
                node.layoutBoundsProperty(),
                (layoutY, layoutBounds) -> layoutY.doubleValue() + minY(layoutBounds));
    }
    default double minY(Node node) { return layoutY(node) + minY(node.getLayoutBounds()); }
    default double maxY(Node node) { return minY(node) + length(node); }
    default double minX(Node node) { return layoutX(node) + minX(node.getLayoutBounds()); }
    default double maxX(Node node) { return minX(node) + breadth(node); }
    default double length(Cell<?, ?> cell) { return length(cell.getNode()); }
    default double breadth(Cell<?, ?> cell) { return breadth(cell.getNode()); }
    default Val<Double> minYProperty(Cell<?, ?> cell) { return minYProperty(cell.getNode()); }
    default double minY(Cell<?, ?> cell) { return minY(cell.getNode()); }
    default double maxY(Cell<?, ?> cell) { return maxY(cell.getNode()); }
    default double minX(Cell<?, ?> cell) { return minX(cell.getNode()); }
    default double maxX(Cell<?, ?> cell) { return maxX(cell.getNode()); }
    double minBreadth(Node node);
    default double minBreadth(Cell<?, ?> cell) { return minBreadth(cell.getNode()); }
    double prefBreadth(Node node);
    double prefLength(Node node, double breadth);
    default double prefLength(Cell<?, ?> cell, double breadth) { return prefLength(cell.getNode(), breadth); }
    void resizeRelocate(Node node, double b0, double l0, double breadth, double length);
    void resize(Node node, double breadth, double length);
    void relocate(Node node, double b0, double l0);
    default void resize(Cell<?, ?> cell, double breadth, double length) { resize(cell.getNode(), breadth, length); }
    default void relocate(Cell<?, ?> cell, double b0, double l0) { relocate(cell.getNode(), b0, l0); }

    ObservableValue<Double> widthEstimateProperty(VirtualFlowContent<?, ?> content);
    ObservableValue<Double> heightEstimateProperty(VirtualFlowContent<?, ?> content);
    Var<Double> horizontalPositionProperty(VirtualFlowContent<?, ?> content);
    Var<Double> verticalPositionProperty(VirtualFlowContent<?, ?> content);
    void scrollHorizontally(VirtualFlowContent<?, ?> content, double dx);
    void scrollVertically(VirtualFlowContent<?, ?> content, double dy);
    void scrollHorizontallyToPixel(VirtualFlowContent<?, ?> content, double pixel);
    void scrollVerticallyToPixel(VirtualFlowContent<?, ?> content, double pixel);

    <C extends Cell<?, ?>> VirtualFlowHit<C> hitBeforeCells(double bOff, double lOff);
    <C extends Cell<?, ?>> VirtualFlowHit<C> hitAfterCells(double bOff, double lOff);
    <C extends Cell<?, ?>> VirtualFlowHit<C> cellHit(int itemIndex, C cell, double bOff, double lOff);
}

final class HorizontalHelper implements OrientationHelper {

    @Override
    public Orientation getContentBias() {
        return Orientation.VERTICAL;
    }

    @Override
    public double getX(double x, double y) {
        return y;
    }

    @Override
    public double getY(double x, double y) {
        return x;
    }

    @Override
    public double minBreadth(Node node) {
        return node.minHeight(-1);
    }

    @Override
    public double prefBreadth(Node node) {
        return node.prefHeight(-1);
    }

    @Override
    public double prefLength(Node node, double breadth) {
        return node.prefWidth(breadth);
    }

    @Override
    public double breadth(Bounds bounds) {
        return bounds.getHeight();
    }

    @Override
    public double length(Bounds bounds) {
        return bounds.getWidth();
    }

    @Override
    public double minX(Bounds bounds) {
        return bounds.getMinY();
    }

    @Override
    public double minY(Bounds bounds) {
        return bounds.getMinX();
    }

    @Override
    public double layoutX(Node node) {
        return node.getLayoutY();
    }

    @Override
    public double layoutY(Node node) {
        return node.getLayoutX();
    }

    @Override
    public DoubleProperty layoutYProperty(Node node) {
        return node.layoutXProperty();
    }

    @Override
    public void resizeRelocate(
            Node node, double b0, double l0, double breadth, double length) {
        node.resizeRelocate(l0, b0, length, breadth);
    }

    @Override
    public void resize(Node node, double breadth, double length) {
        node.resize(length, breadth);
    }

    @Override
    public void relocate(Node node, double b0, double l0) {
        node.relocate(l0, b0);
    }

    @Override
    public ObservableValue<Double> widthEstimateProperty(
            VirtualFlowContent<?, ?> content) {
        return content.totalLengthEstimateProperty();
    }

    @Override
    public ObservableValue<Double> heightEstimateProperty(
            VirtualFlowContent<?, ?> content) {
        return content.totalBreadthEstimateProperty();
    }

    @Override
    public Var<Double> horizontalPositionProperty(
            VirtualFlowContent<?, ?> content) {
        return content.lengthPositionEstimateProperty();
    }

    @Override
    public Var<Double> verticalPositionProperty(
            VirtualFlowContent<?, ?> content) {
        return content.breadthPositionEstimateProperty();
    }

    @Override
    public void scrollHorizontally(VirtualFlowContent<?, ?> content, double dx) {
        content.scrollLength(dx);
    }

    @Override
    public void scrollVertically(VirtualFlowContent<?, ?> content, double dy) {
        content.scrollBreadth(dy);
    }

    @Override
    public void scrollHorizontallyToPixel(VirtualFlowContent<?, ?> content, double pixel) {
        content.scrollLengthToPixel(pixel);
    }

    @Override
    public void scrollVerticallyToPixel(VirtualFlowContent<?, ?> content, double pixel) { // breadth
        content.scrollBreadthToPixel(pixel);
    }

    @Override
    public <C extends Cell<?, ?>> VirtualFlowHit<C> hitBeforeCells(
            double bOff, double lOff) {
        return VirtualFlowHit.hitBeforeCells(lOff, bOff);
    }

    @Override
    public <C extends Cell<?, ?>> VirtualFlowHit<C> hitAfterCells(
            double bOff, double lOff) {
        return VirtualFlowHit.hitAfterCells(lOff, bOff);
    }

    @Override
    public <C extends Cell<?, ?>> VirtualFlowHit<C> cellHit(
            int itemIndex, C cell, double bOff, double lOff) {
        return VirtualFlowHit.cellHit(itemIndex, cell, lOff, bOff);
    }
}

final class VerticalHelper implements OrientationHelper {

    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    @Override
    public double getX(double x, double y) {
        return x;
    }

    @Override
    public double getY(double x, double y) {
        return y;
    }

    @Override
    public double minBreadth(Node node) {
        return node.minWidth(-1);
    }

    @Override
    public double prefBreadth(Node node) {
        return node.prefWidth(-1);
    }

    @Override
    public double prefLength(Node node, double breadth) {
        return node.prefHeight(breadth);
    }

    @Override
    public double breadth(Bounds bounds) {
        return bounds.getWidth();
    }

    @Override
    public double length(Bounds bounds) {
        return bounds.getHeight();
    }

    @Override
    public double minX(Bounds bounds) {
        return bounds.getMinX();
    }

    @Override
    public double minY(Bounds bounds) {
        return bounds.getMinY();
    }

    @Override
    public double layoutX(Node node) {
        return node.getLayoutX();
    }

    @Override
    public double layoutY(Node node) {
        return node.getLayoutY();
    }

    @Override
    public DoubleProperty layoutYProperty(Node node) {
        return node.layoutYProperty();
    }

    @Override
    public void resizeRelocate(
            Node node, double b0, double l0, double breadth, double length) {
        node.resizeRelocate(b0, l0, breadth, length);
    }

    @Override
    public void resize(Node node, double breadth, double length) {
        node.resize(breadth, length);
    }

    @Override
    public void relocate(Node node, double b0, double l0) {
        node.relocate(b0, l0);
    }

    @Override
    public ObservableValue<Double> widthEstimateProperty(
            VirtualFlowContent<?, ?> content) {
        return content.totalBreadthEstimateProperty();
    }

    @Override
    public ObservableValue<Double> heightEstimateProperty(
            VirtualFlowContent<?, ?> content) {
        return content.totalLengthEstimateProperty();
    }

    @Override
    public Var<Double> horizontalPositionProperty(
            VirtualFlowContent<?, ?> content) {
        return content.breadthPositionEstimateProperty();
    }

    @Override
    public Var<Double> verticalPositionProperty(
            VirtualFlowContent<?, ?> content) {
        return content.lengthPositionEstimateProperty();
    }

    @Override
    public void scrollHorizontally(VirtualFlowContent<?, ?> content, double dx) {
        content.scrollBreadth(dx);
    }

    @Override
    public void scrollVertically(VirtualFlowContent<?, ?> content, double dy) {
        content.scrollLength(dy);
    }

    @Override
    public void scrollHorizontallyToPixel(VirtualFlowContent<?, ?> content, double pixel) {
        content.scrollBreadthToPixel(pixel);
    }

    @Override
    public void scrollVerticallyToPixel(VirtualFlowContent<?, ?> content, double pixel) { // length
        content.scrollLengthToPixel(pixel);
    }

    @Override
    public <C extends Cell<?, ?>> VirtualFlowHit<C> hitBeforeCells(
            double bOff, double lOff) {
        return VirtualFlowHit.hitBeforeCells(bOff, lOff);
    }

    @Override
    public <C extends Cell<?, ?>> VirtualFlowHit<C> hitAfterCells(
            double bOff, double lOff) {
        return VirtualFlowHit.hitAfterCells(bOff, lOff);
    }

    @Override
    public <C extends Cell<?, ?>> VirtualFlowHit<C> cellHit(
            int itemIndex, C cell, double bOff, double lOff) {
        return VirtualFlowHit.cellHit(itemIndex, cell, bOff, lOff);
    }
}