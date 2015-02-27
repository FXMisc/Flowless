package org.fxmisc.flowless;

import java.util.Optional;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;

import org.reactfx.value.Val;
import org.reactfx.value.Var;

public class VirtualFlow<T, C extends Cell<T, ?>> extends Region {

    public static <T, C extends Cell<T, ?>> VirtualFlow<T, C> createHorizontal(
            ObservableList<T> items,
            Function<? super T, ? extends C> cellFactory) {
        return new VirtualFlow<>(items, cellFactory, new HorizontalHelper());
    }

    public static <T, C extends Cell<T, ?>> VirtualFlow<T, C> createVertical(
            ObservableList<T> items,
            Function<? super T, ? extends C> cellFactory) {
        return new VirtualFlow<>(items, cellFactory, new VerticalHelper());
    }

    private final ScrollBar hbar;
    private final ScrollBar vbar;
    private final VirtualFlowContent<T, C> content;


    private VirtualFlow(
            ObservableList<T> items,
            Function<? super T, ? extends C> cellFactory,
            OrientationHelper orientation) {
        this.getStyleClass().add("virtual-flow");
        this.content = new VirtualFlowContent<>(items, cellFactory, orientation);

        // create scrollbars
        hbar = new ScrollBar();
        vbar = new ScrollBar();
        hbar.setOrientation(Orientation.HORIZONTAL);
        vbar.setOrientation(Orientation.VERTICAL);

        // scrollbar ranges
        hbar.setMin(0);
        vbar.setMin(0);
        hbar.maxProperty().bind(orientation.widthEstimateProperty(content));
        vbar.maxProperty().bind(orientation.heightEstimateProperty(content));

        // scrollbar increments
        setupUnitIncrement(hbar);
        setupUnitIncrement(vbar);
        hbar.blockIncrementProperty().bind(hbar.visibleAmountProperty());
        vbar.blockIncrementProperty().bind(vbar.visibleAmountProperty());

        // scrollbar positions
        Bindings.bindBidirectional(
                Var.doubleVar(hbar.valueProperty()),
                orientation.horizontalPositionProperty(content));
        Bindings.bindBidirectional(
                Var.doubleVar(vbar.valueProperty()),
                orientation.verticalPositionProperty(content));

        // scroll content by mouse scroll
        this.addEventHandler(ScrollEvent.SCROLL, se -> {
            scrollX(se.getDeltaX());
            scrollY(se.getDeltaY());
            se.consume();
        });

        // scrollbar visibility
        Val<Double> layoutWidth = Val.map(layoutBoundsProperty(), Bounds::getWidth);
        Val<Double> layoutHeight = Val.map(layoutBoundsProperty(), Bounds::getHeight);
        Val<Boolean> needsHBar0 = Val.combine(
                orientation.widthEstimateProperty(content),
                layoutWidth,
                (cw, lw) -> cw > lw);
        Val<Boolean> needsVBar0 = Val.combine(
                orientation.heightEstimateProperty(content),
                layoutHeight,
                (ch, lh) -> ch > lh);
        Val<Boolean> needsHBar = Val.combine(
                needsHBar0,
                needsVBar0,
                orientation.widthEstimateProperty(content),
                vbar.widthProperty(),
                layoutWidth,
                (needsH, needsV, cw, vbw, lw) -> needsH || needsV && cw + vbw.doubleValue() > lw);
        Val<Boolean> needsVBar = Val.combine(
                needsVBar0,
                needsHBar0,
                orientation.heightEstimateProperty(content),
                hbar.heightProperty(),
                layoutHeight,
                (needsV, needsH, ch, hbh, lh) -> needsV || needsH && ch + hbh.doubleValue() > lh);
        hbar.visibleProperty().bind(needsHBar);
        vbar.visibleProperty().bind(needsVBar);

        // request layout later, because if currently in layout, the request is ignored
        hbar.visibleProperty().addListener(obs -> Platform.runLater(() -> requestLayout()));
        vbar.visibleProperty().addListener(obs -> Platform.runLater(() -> requestLayout()));

        getChildren().addAll(content, hbar, vbar);
    }

    public void dispose() {
        content.dispose();
    }

    @Override
    public Orientation getContentBias() {
        return content.getContentBias();
    }

    public double getViewportWidth() {
        return content.getWidth();
    }

    public double getViewportHeight() {
        return content.getHeight();
    }

    public ReadOnlyDoubleProperty breadthOffsetProperty() {
        return content.breadthOffsetProperty();
    }

    public Bounds cellToViewport(C cell, Bounds bounds) {
        return cell.getNode().localToParent(bounds);
    }

    public Point2D cellToViewport(C cell, Point2D point) {
        return cell.getNode().localToParent(point);
    }

    public Point2D cellToViewport(C cell, double x, double y) {
        return cell.getNode().localToParent(x, y);
    }

    public void show(int index) {
        content.show(index);
    }

    public void show(double primaryAxisOffset) {
        content.show(primaryAxisOffset);
    }

    public void showAsFirst(int itemIndex) {
        content.showAsFirst(itemIndex);
    }

    public void showAsLast(int itemIndex) {
        content.showAsLast(itemIndex);
    }

    public void showAtOffset(int itemIndex, double offset) {
        content.showAtOffset(itemIndex, offset);
    }

    public void show(int itemIndex, Bounds region) {
        content.showRegion(itemIndex, region);
    }

    /**
     * Scroll the content horizontally by the given amount.
     * @param deltaX positive value scrolls right, negative value scrolls left
     */
    public void scrollX(double deltaX) {
        content.scrollX(deltaX);
    }

    /**
     * Scroll the content vertically by the given amount.
     * @param deltaY positive value scrolls down, negative value scrolls up
     */
    public void scrollY(double deltaY) {
        content.scrollY(deltaY);
    }

    /**
     * If the item is out of view, instantiates a new cell for the item.
     * The returned cell will be properly sized, but not properly positioned
     * relative to the cells in the viewport, unless it is itself in the
     * viewport.
     *
     * @return Cell for the given item. The cell will be valid only until the
     * next layout pass. It should therefore not be stored. It is intended to
     * be used for measurement purposes only.
     */
    public C getCell(int itemIndex) {
        return content.getCellFor(itemIndex);
    }

    public Optional<C> getCellIfVisible(int itemIndex) {
        return content.getCellIfVisible(itemIndex);
    }

    public ObservableList<C> visibleCells() {
        return content.visibleCells();
    }

    /**
     * Hits this virtual flow at the given coordinates.
     * @param x x offset from the left edge of the viewport
     * @param y y offset from the top edge of the viewport
     * @return hit info containing the cell that was hit and coordinates
     * relative to the cell. If the hit was before the cells (i.e. above a
     * vertical flow content or left of a horizontal flow content), returns
     * a <em>hit before cells</em> containing offset from the top left corner
     * of the content. If the hit was after the cells (i.e. below a vertical
     * flow content or right of a horizontal flow content), returns a
     * <em>hit after cells</em> containing offset from the top right corner of
     * the content of a horizontal flow or bottom left corner of the content of
     * a vertical flow.
     */
    public VirtualFlowHit<C> hit(double x, double y) {
        return content.hit(x, y);
    }

    @Override
    protected double computePrefWidth(double height) {
        return content.prefWidth(height);
    }

    @Override
    protected double computePrefHeight(double width) {
        return content.prefHeight(width);
    }

    @Override
    protected double computeMinWidth(double height) {
        return vbar.minWidth(-1);
    }

    @Override
    protected double computeMinHeight(double width) {
        return hbar.minHeight(-1);
    }

    @Override
    protected double computeMaxWidth(double height) {
        return content.maxWidth(height);
    }

    @Override
    protected double computeMaxHeight(double width) {
        return content.maxHeight(width);
    }

    @Override
    protected void layoutChildren() {
        double layoutWidth = getLayoutBounds().getWidth();
        double layoutHeight = getLayoutBounds().getHeight();
        boolean vbarVisible = vbar.isVisible();
        boolean hbarVisible = hbar.isVisible();
        double vbarWidth = vbarVisible ? vbar.prefWidth(-1) : 0;
        double hbarHeight = hbarVisible ? hbar.prefHeight(-1) : 0;

        double w = layoutWidth - vbarWidth;
        double h = layoutHeight - hbarHeight;

        content.resize(w, h);

        hbar.setVisibleAmount(w);
        vbar.setVisibleAmount(h);

        if(vbarVisible) {
            vbar.resizeRelocate(layoutWidth - vbarWidth, 0, vbarWidth, h);
        }

        if(hbarVisible) {
            hbar.resizeRelocate(0, layoutHeight - hbarHeight, w, hbarHeight);
        }
    }

    private static void setupUnitIncrement(ScrollBar bar) {
        bar.unitIncrementProperty().bind(new DoubleBinding() {
            { bind(bar.maxProperty(), bar.visibleAmountProperty()); }

            @Override
            protected double computeValue() {
                double max = bar.getMax();
                double visible = bar.getVisibleAmount();
                return max > visible
                        ? 16 / (max - visible) * max
                        : 0;
            }
        });
    }
}