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
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;

import org.reactfx.value.Val;
import org.reactfx.value.Var;
import static org.fxmisc.flowless.VirtualFlow.Gravity;

public class VirtualizedScrollPane<V extends Node & Virtualized> extends Region implements Virtualized {

    private final ScrollBar hbar;
    private final ScrollBar vbar;
    private final V content;

    public VirtualizedScrollPane(V content) {
        this.getStyleClass().add("virtualized-scroll-pane");
        this.content = content;

        // create scrollbars
        hbar = new ScrollBar();
        vbar = new ScrollBar();
        hbar.setOrientation(Orientation.HORIZONTAL);
        vbar.setOrientation(Orientation.VERTICAL);

        // scrollbar ranges
        hbar.setMin(0);
        vbar.setMin(0);
        hbar.maxProperty().bind(content.totalWidthEstimateProperty());
        vbar.maxProperty().bind(content.totalHeightEstimateProperty());

        // scrollbar increments
        setupUnitIncrement(hbar);
        setupUnitIncrement(vbar);
        hbar.blockIncrementProperty().bind(hbar.visibleAmountProperty());
        vbar.blockIncrementProperty().bind(vbar.visibleAmountProperty());

        // scrollbar positions
        Bindings.bindBidirectional(
                Var.doubleVar(hbar.valueProperty()),
                content.estimatedScrollXProperty());
        Bindings.bindBidirectional(
                Var.doubleVar(vbar.valueProperty()),
                content.estimatedScrollYProperty());

        // scrollbar visibility
        Val<Double> layoutWidth = Val.map(layoutBoundsProperty(), Bounds::getWidth);
        Val<Double> layoutHeight = Val.map(layoutBoundsProperty(), Bounds::getHeight);
        Val<Boolean> needsHBar0 = Val.combine(
                content.totalHeightEstimateProperty(),
                layoutWidth,
                (cw, lw) -> cw > lw);
        Val<Boolean> needsVBar0 = Val.combine(
                content.totalWidthEstimateProperty(),
                layoutHeight,
                (ch, lh) -> ch > lh);
        Val<Boolean> needsHBar = Val.combine(
                needsHBar0,
                needsVBar0,
                content.totalHeightEstimateProperty(),
                vbar.widthProperty(),
                layoutWidth,
                (needsH, needsV, cw, vbw, lw) -> needsH || needsV && cw + vbw.doubleValue() > lw);
        Val<Boolean> needsVBar = Val.combine(
                needsVBar0,
                needsHBar0,
                content.totalWidthEstimateProperty(),
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
        disposeScrollBar(hbar);
        disposeScrollBar(vbar);
    }

    private void disposeScrollBar(ScrollBar bar) {
        bar.maxProperty().unbind();
        bar.blockIncrementProperty().unbind();
        bar.unitIncrementProperty().unbind();
        bar.visibleProperty().unbind();
    }

    public Val<Double> totalWidthEstimateProperty() {
        return content.totalWidthEstimateProperty();
    }

    public Val<Double> totalHeightEstimateProperty() {
        return content.totalHeightEstimateProperty();
    }

    public Var<Double> estimatedScrollXProperty() {
        return content.estimatedScrollXProperty();
    }

    public Var<Double> estimatedScrollYProperty() {
        return content.estimatedScrollYProperty();
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