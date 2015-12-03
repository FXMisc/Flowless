package org.fxmisc.flowless;

import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;
import org.reactfx.value.Val;
import org.reactfx.value.Var;

/**
 * Acts as an intermediate class between {@link VirtualizedScrollPane} and
 * its {@link Virtualized} content in that it scales the content without
 * also scaling the ScrollPane's scroll bars.
 * <pre>
 *     {@code
 *     Virtualized actualContent = // creation code
 *     ScaledVirtualized<Virtualized> wrapper = new ScaledVirtualized(actualContent);
 *     VirtualizedScrollPane<ScaledVirtualized> vsPane = new VirtualizedScrollPane(wrapper);
 *
 *     // To scale actualContent without also scaling vsPane's scrollbars:
 *     wrapper.scaleProperty().setY(3);
 *     wrapper.scaleProperty().setX(2);
 *     }
 * </pre>
 * @param <V> the {@link Virtualized} content to be scaled when inside a {@link VirtualizedScrollPane}
 */
class ScaledVirtualized<V extends Node & Virtualized> extends Region implements Virtualized {
    private final V content;
    private Scale scale = new Scale();

    private Val<Double> estHeight;
    private Val<Double> estWidth;
    private Var<Double> estScrollX;
    private Var<Double> estScrollY;

    ScaledVirtualized(V content) {
        super();
        this.content = content;
        getChildren().add(content);
        getTransforms().add(scale);
        estHeight = Val.combine(
                content.totalHeightEstimateProperty(),
                scale.yProperty(),
                (estHeight, scaleFactor) -> estHeight * scaleFactor.doubleValue()
        );
        estWidth = Val.combine(
                content.totalWidthEstimateProperty(),
                scale.xProperty(),
                (estWidth, scaleFactor) -> estWidth * scaleFactor.doubleValue()
        );
        estScrollX = Var.mapBidirectional(
                content.estimatedScrollXProperty(),
                scrollX -> scrollX * scale.getX(),
                scrollX -> scrollX / scale.getX()
        );
        estScrollY = Var.mapBidirectional(
                content.estimatedScrollYProperty(),
                scrollY -> scrollY * scale.getY(),
                scrollY -> scrollY / scale.getY()
        );
    }

    @Override
    protected void layoutChildren() {
        double width = getLayoutBounds().getWidth();
        double height = getLayoutBounds().getHeight();
        content.resize(width / scale.getX(), height/ scale.getY());
    }

    @Override
    public Var<Double> estimatedScrollXProperty() {
        return estScrollX;
    }

    @Override
    public Var<Double> estimatedScrollYProperty() {
        return estScrollY;
    }

    @Override
    public Val<Double> totalHeightEstimateProperty() {
        return estHeight;
    }

    @Override
    public Val<Double> totalWidthEstimateProperty() {
        return estWidth;
    }

    public Scale scaleProperty() {
        return scale;
    }
}