package org.fxmisc.flowless;

import static javafx.scene.control.ScrollPane.ScrollBarPolicy.*;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;

import javafx.scene.layout.StackPane;
import org.reactfx.value.Val;
import org.reactfx.value.Var;

public class VirtualizedScrollPane<V extends Node & Virtualized> extends Region implements Virtualized {

    private final ScrollBar hbar;
    private final ScrollBar vbar;
    private final V content;
    private final StackPane corner = new StackPane();

    private Var<Double> hbarValue;
    private Var<Double> vbarValue;

    /** The Policy for the Horizontal ScrollBar */
    private final Var<ScrollPane.ScrollBarPolicy> hbarPolicy;
    public final ScrollPane.ScrollBarPolicy getHbarPolicy() { return hbarPolicy.getValue(); }
    public final void setHbarPolicy(ScrollPane.ScrollBarPolicy value) { hbarPolicy.setValue(value); }
    public final Var<ScrollPane.ScrollBarPolicy> hbarPolicyProperty() { return hbarPolicy; }

    /** The Policy for the Vertical ScrollBar */
    private final Var<ScrollPane.ScrollBarPolicy> vbarPolicy;
    public final ScrollPane.ScrollBarPolicy getVbarPolicy() { return vbarPolicy.getValue(); }
    public final void setVbarPolicy(ScrollPane.ScrollBarPolicy value) { vbarPolicy.setValue(value); }
    public final Var<ScrollPane.ScrollBarPolicy> vbarPolicyProperty() { return vbarPolicy; }

    private final Val<Boolean> shouldDisplayHorizontal;
    private final Val<Boolean> shouldDisplayVertical;
    private final Val<Boolean> shouldDisplayBoth;

    public VirtualizedScrollPane(V content, ScrollPane.ScrollBarPolicy hPolicy, ScrollPane.ScrollBarPolicy vPolicy) {
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
        hbarValue = Var.doubleVar(hbar.valueProperty());
        vbarValue = Var.doubleVar(vbar.valueProperty());
        Bindings.bindBidirectional(
                hbarValue,
                content.estimatedScrollXProperty());
        Bindings.bindBidirectional(
                vbarValue,
                content.estimatedScrollYProperty());

        // scrollbar visibility
        hbarPolicy = Var.newSimpleVar(hPolicy);
        vbarPolicy = Var.newSimpleVar(vPolicy);

        Val<Double> layoutWidth = Val.map(layoutBoundsProperty(), Bounds::getWidth);
        Val<Double> layoutHeight = Val.map(layoutBoundsProperty(), Bounds::getHeight);
        Val<Boolean> needsHBar0 = Val.combine(
                content.totalWidthEstimateProperty(),
                layoutWidth,
                (cw, lw) -> cw > lw);
        Val<Boolean> needsVBar0 = Val.combine(
                content.totalHeightEstimateProperty(),
                layoutHeight,
                (ch, lh) -> ch > lh);
        Val<Boolean> needsHBar = Val.combine(
                needsHBar0,
                needsVBar0,
                content.totalWidthEstimateProperty(),
                vbar.widthProperty(),
                layoutWidth,
                (needsH, needsV, cw, vbw, lw) -> needsH || needsV && cw + vbw.doubleValue() > lw);
        Val<Boolean> needsVBar = Val.combine(
                needsVBar0,
                needsHBar0,
                content.totalHeightEstimateProperty(),
                hbar.heightProperty(),
                layoutHeight,
                (needsV, needsH, ch, hbh, lh) -> needsV || needsH && ch + hbh.doubleValue() > lh);

        shouldDisplayHorizontal = Val.flatMap(hbarPolicy, policy -> {
            switch (policy) {
                case NEVER:
                    return Val.constant(false);
                case ALWAYS:
                    return Val.constant(true);
                default: // AS_NEEDED
                    return needsHBar;
            }
        });
        shouldDisplayVertical = Val.flatMap(vbarPolicy, policy -> {
            switch (policy) {
                case NEVER:
                    return Val.constant(false);
                case ALWAYS:
                    return Val.constant(true);
                default: // AS_NEEDED
                    return needsVBar;
            }
        });

        shouldDisplayBoth = Val.combine(shouldDisplayHorizontal, shouldDisplayVertical, (displayH, displayV) -> displayH && displayV);
        shouldDisplayBoth.addListener(obs -> requestLayout());

        hbar.visibleProperty().bind(shouldDisplayHorizontal);
        vbar.visibleProperty().bind(shouldDisplayVertical);

        getChildren().addAll(content, hbar, vbar, corner);
        getChildren().addListener((Observable obs) -> dispose());
    }

    public VirtualizedScrollPane(V content) {
        this(content, AS_NEEDED, AS_NEEDED);
    }

    /**
     * Does not unbind scrolling from Content before returning Content.
     * @return - the content
     */
    public V getContent() {
        return content;
    }

    /**
     * Unbinds scrolling from Content before returning Content.
     * @return - the content
     */
    public V removeContent() {
        getChildren().clear();
        return content;
    }

    private void dispose() {
        hbarValue.unbindBidirectional(content.estimatedScrollXProperty());
        vbarValue.unbindBidirectional(content.estimatedScrollYProperty());
        unbindScrollBar(hbar);
        unbindScrollBar(vbar);
    }

    private void unbindScrollBar(ScrollBar bar) {
        bar.maxProperty().unbind();
        bar.unitIncrementProperty().unbind();
        bar.blockIncrementProperty().unbind();
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

        if (shouldDisplayBoth.getValue()) {
            double vbarWidth = vbar.prefWidth(-1);
            double hbarHeight = hbar.prefHeight(-1);

            double contentWidth = layoutWidth - vbarWidth;
            double contentHeight = layoutHeight - hbarHeight;

            content.resize(contentWidth, contentHeight);

            hbar.setVisibleAmount(contentWidth);
            hbar.resizeRelocate(0, contentHeight, contentWidth, hbarHeight);

            vbar.setVisibleAmount(contentHeight);
            vbar.resizeRelocate(contentWidth, 0, vbarWidth, contentHeight);

            corner.resizeRelocate(contentWidth, contentHeight, vbarWidth, hbarHeight);
        } else {
            if (shouldDisplayVertical.getValue()) {
                double vbarWidth = vbar.prefWidth(-1);

                double contentWidth = layoutWidth - vbarWidth;

                content.resize(contentWidth, layoutHeight);

                vbar.setVisibleAmount(layoutHeight);
                vbar.resizeRelocate(contentWidth, 0, vbarWidth, layoutHeight);

                hbar.setVisibleAmount(0);
            } else if (shouldDisplayHorizontal.getValue()) {
                double hbarHeight = hbar.prefHeight(-1);

                double contentHeight = layoutHeight - hbarHeight;

                content.resize(layoutWidth, contentHeight);

                hbar.setVisibleAmount(layoutWidth);
                hbar.resizeRelocate(0, contentHeight, layoutWidth, hbarHeight);

                vbar.setVisibleAmount(0);
            } else {
                content.resize(layoutWidth, layoutHeight);

                hbar.setVisibleAmount(0);
                vbar.setVisibleAmount(0);
            }
            corner.resize(0, 0);
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