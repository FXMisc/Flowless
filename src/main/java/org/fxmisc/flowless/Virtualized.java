package org.fxmisc.flowless;

import org.reactfx.value.Val;
import org.reactfx.value.Var;

public interface Virtualized {
    Val<Double> totalWidthEstimateProperty();
    Val<Double> totalHeightEstimateProperty();
    Var<Double> estimatedScrollXProperty();
    Var<Double> estimatedScrollYProperty();

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
