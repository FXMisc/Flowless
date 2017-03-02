package org.fxmisc.flowless;

import javafx.geometry.Point2D;
import org.reactfx.value.Val;
import org.reactfx.value.Var;

public interface Virtualized {
    Val<Double> totalWidthEstimateProperty();
    Val<Double> totalHeightEstimateProperty();
    Var<Double> estimatedScrollXProperty();
    Var<Double> estimatedScrollYProperty();

    /**
     * Convenience method: scroll horizontally by {@code deltas.getX()} and vertically by {@code deltas.getY()}
     * @param deltas negative values scroll left/up, positive scroll right/down
     */
    default void scrollBy(Point2D deltas) {
        scrollXBy(deltas.getX());
        scrollYBy(deltas.getY());
    }

    /**
     * Convenience method: scroll horizontally by {@code deltaX} and vertically by {@code deltaY}
     * @param deltaX negative values scroll left, positive scroll right
     * @param deltaY negative values scroll up, positive scroll down
     */
    default void scrollBy(double deltaX, double deltaY) {
        scrollXBy(deltaX);
        scrollYBy(deltaY);
    }

    /**
     * Scroll the content horizontally by the given amount.
     * @param deltaX positive value scrolls right, negative value scrolls left
     */
    void scrollXBy(double deltaX);

    /**
     * Scroll the content vertically by the given amount.
     * @param deltaY positive value scrolls down, negative value scrolls up
     */
    void scrollYBy(double deltaY);

    /**
     * Convenicen method: scroll the content to the pixel
     */
    default void scrollToPixel(Point2D pixel) {
        scrollXToPixel(pixel.getX());
        scrollYToPixel(pixel.getY());
    }

    /**
     * Convenicen method: scroll the content to the pixel
     */
    default void scrollToPixel(double xPixel, double yPixel) {
        scrollXToPixel(xPixel);
        scrollYToPixel(yPixel);
    }

    /**
     * Scroll the content horizontally to the pixel
     * @param pixel - the pixel position to which to scroll
     */
    void scrollXToPixel(double pixel);

    /**
     * Scroll the content vertically to the pixel
     * @param pixel - the pixel position to which to scroll
     */
    void scrollYToPixel(double pixel);
}
