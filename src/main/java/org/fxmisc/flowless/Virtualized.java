package org.fxmisc.flowless;

import org.reactfx.value.Val;
import org.reactfx.value.Var;

public interface Virtualized {
    Val<Double> totalWidthEstimateProperty();
    Val<Double> totalHeightEstimateProperty();
    Var<Double> estimatedScrollXProperty();
    Var<Double> estimatedScrollYProperty();
}
